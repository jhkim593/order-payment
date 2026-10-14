---
name: hexagonal-architecture
description: "order-payment 멀티모듈 프로젝트의 헥사고날 계층 규칙(adapter/application/domain, provided·required 포트)과 Gradle 모듈 의존 방향을 정의. 새 클래스를 어느 패키지에 둘지 판단할 때, 새 Controller/Service/Repository/Config를 만들 때, 모듈 간 의존을 추가할 때, 계층 위반·순환 의존을 진단할 때, 트랜잭션 경계(@Transactional)를 어디에 둘지 정할 때 반드시 이 스킬을 사용할 것. order·payment·user·common 어느 모듈이든 코드를 추가·이동·리팩터링하면 먼저 이 규칙을 확인한다."
---

# Hexagonal Architecture — order-payment 계층 규칙

이 프로젝트는 헥사고날(포트&어댑터) 구조를 따른다. 계층을 지키는 이유는 도메인 로직이 Kafka·JPA·PortOne 같은 외부 기술에 오염되지 않게 하여, 인프라를 바꿔도 도메인과 유스케이스가 그대로 남게 하기 위해서다.

## 모듈 구조와 의존 방향

```
common:core      ← 이벤트 정의, Snowflake ID, DataSerializer. 어떤 서비스 모듈도 의존하지 않음
common:outbox    ← Transactional Outbox 발행/저장. common:core 의존
payment:payment-api      ← order가 쓰는 결제 클라이언트 라이브러리 (bootJar 비활성, jar만)
payment:payment-service  ← 결제 서비스 애플리케이션
order                    ← 주문 서비스 애플리케이션
user                     ← 사용자 서비스 애플리케이션
```

**허용되는 의존:**

| 모듈 | 의존 가능 |
|------|----------|
| order | `common:core`, `common:outbox`, `payment:payment-api` |
| payment-service | `common:core`, `common:outbox`, `payment:payment-api` |
| user | `common:core` |
| common:outbox | `common:core` |
| common:core | (없음) |

**금지:**
- `order` → `payment:payment-service` 직접 의존. 결제 호출은 반드시 `payment-api`의 `PaymentClient`를 통한다. 서비스 구현체에 직접 의존하면 배포 단위가 결합되어 MSA의 의미가 사라진다.
- 서비스 모듈 간 직접 의존 (order ↔ user 등). 서비스 간 통신은 Kafka 이벤트 또는 `*-api` 클라이언트 모듈로만.
- `common:core`가 서비스 모듈을 의존하는 역방향.

`payment-api`는 라이브러리 모듈이므로 `bootJar.enabled = false`, `jar.enabled = true`를 유지하고, 빈 등록은 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 한다. `@ComponentScan`으로 소비자 모듈이 강제로 스캔하게 만들지 않는다 — 소비자가 쓰지 않는 클라이언트 빈까지 주입되는 문제를 피하기 위해 이 모듈을 분리했다.

## 패키지 계층

각 서비스 모듈은 `jhkim593.orderpayment.{service}` 하위에 세 계층을 둔다.

```
domain/                     ← JPA 엔티티, 상태 전이 규칙, 값 객체, dto, error
application/
  provided/                 ← 인바운드 포트 (인터페이스)
  required/                 ← 아웃바운드 포트 (인터페이스)
  {X}Service / service/     ← provided 포트 구현. 유스케이스 오케스트레이션
  {X}TransactionManager     ← @Transactional 경계 전담
  event/InternalEventPublisher ← 도메인 이벤트 발행
adapter/
  api/                      ← Controller, ControllerAdvice, 응답 DTO
  database/                 ← required 포트의 JPA 구현. jpa/ 하위에 Spring Data 인터페이스
  client/                   ← 외부 API 어댑터 (PortOne 등)
  event/                    ← EventHandler, EventHandlerFactory
  mq/kafka/                 ← KafkaListener, KafkaConfig
  common/                   ← 모듈 공통 Config
```

**의존 방향은 안쪽으로만 흐른다:** `adapter → application → domain`. `domain`은 어떤 계층도 import하지 않고, `application`은 `adapter`를 import하지 않는다.

### provided 포트 (인바운드)

유스케이스를 외부에 노출하는 인터페이스. `adapter.api`의 Controller와 `adapter.event`의 EventHandler가 이 인터페이스에만 의존한다.

```java
// application/provided/OrderProcessor.java
public interface OrderProcessor {
    OrderProcessResponseDto processOrder(OrderProcessRequestDto request);
    void cancelSucceededOrder(Long orderId);
}
```

Controller가 `OrderProcessService`(구현체)를 직접 주입받으면 안 된다. HTTP 어댑터를 교체하거나 이벤트 어댑터가 같은 유스케이스를 호출할 때 결합이 드러나기 때문이다.

### required 포트 (아웃바운드)

application이 필요로 하는 능력을 선언하는 인터페이스. `adapter.database`, `adapter.client`가 구현한다.

```java
// application/required/OrderRepository.java  ← 인터페이스
// adapter/database/OrderDBRepository.java     ← 구현체, 내부에서 OrderJpaRepository 사용
```

application 코드에 `JpaRepository`, `EntityManager`, `QueryDSL` 타입이 등장하면 계층 위반이다. 이들은 `adapter/database` 안에 갇혀 있어야 한다.

## 트랜잭션 경계 규칙 — 가장 중요

**`{X}Service`에는 `@Transactional`을 붙이지 않는다. `{X}TransactionManager`만 트랜잭션을 소유한다.**

이유: 주문·결제 유스케이스는 중간에 외부 PG API(PortOne) 또는 원격 결제 서비스(`PaymentClient`)를 호출한다. 이 호출을 트랜잭션 안에 넣으면 네트워크 지연 동안 DB 커넥션과 락이 잡혀 있어 커넥션 풀이 고갈된다. 그래서 흐름을 이렇게 쪼갠다:

```
OrderProcessService.processOrder()          ← 트랜잭션 없음
  ├─ transactionManager.createOrder()       ← TX 1: 주문 PENDING 저장, 커밋
  ├─ paymentClient.billingKeyPayment()      ← 트랜잭션 밖에서 외부 호출
  └─ transactionManager.succeeded()         ← TX 2: 상태 전이 + 이벤트 발행, 커밋
```

따라서 새 유스케이스를 만들 때:
- 외부 호출이 있으면 → Service(오케스트레이션) + TransactionManager(짧은 TX) 로 분리한다
- 상태 전이 후 이벤트를 발행해야 하면 → 반드시 TransactionManager의 `@Transactional` 메서드 안에서 `InternalEventPublisher`를 호출한다. 아웃박스가 `BEFORE_COMMIT`에 이벤트를 저장하므로, 트랜잭션 밖에서 발행하면 이벤트가 유실된다
- 상태 전이 실패가 전체 유스케이스를 깨뜨리면 안 되는 지점(예: 결제 성공 후 주문 상태 갱신 실패)은 Service에서 try-catch로 감싸 로그만 남긴다. 복구는 `{X}RecoverService`의 스케줄러가 담당한다

## 복구 스케줄러 규칙

`OrderRecoverService`, `PaymentRecoverService`는 PENDING 상태로 남은 레코드를 주기적으로 조회해 실제 상태를 조회·정정한다. 새 상태 머신을 추가하면 "중간 상태에 머물 수 있는가?"를 자문하고, 머물 수 있다면 복구 경로를 함께 설계한다. 복구 경로 없는 중간 상태는 영구 정지 상태가 된다.

## 새 코드 배치 판단표

| 만들려는 것 | 위치 |
|------------|------|
| HTTP 엔드포인트 | `adapter/api/{X}Controller.java` |
| 유스케이스 인터페이스 | `application/provided/{X}or.java` |
| 유스케이스 구현 | `application/{X}Service.java` 또는 `application/service/{X}Service.java` (기존 모듈 관례를 따른다 — order는 평면, payment는 `service/` 하위) |
| DB 접근 인터페이스 | `application/required/{X}Repository.java` |
| DB 접근 구현 | `adapter/database/{X}DBRepository.java` + `adapter/database/jpa/{X}JpaRepository.java` |
| 외부 API 호출 | `adapter/client/{provider}/` + `application/required/` 포트 |
| Kafka 소비 | `adapter/mq/kafka/` (리스너), `adapter/event/handler/` (핸들러) |
| 엔티티·상태 전이 | `domain/{X}.java` |
| 도메인 예외 | `domain/error/{X}Exception.java` + `ErrorCode` enum |
| 서비스 간 공유 이벤트 | `common/core/event/{domain}/` (common:core 모듈) |

## 검증

계층 규칙 위반을 확인할 때 쓰는 명령:

```bash
# application 계층에 JPA/Kafka 타입이 새어 들어왔는지
grep -rn "org.springframework.data.jpa\|jakarta.persistence.EntityManager\|org.apache.kafka" \
  --include="*.java" order/src/main/java/**/application payment/payment-service/src/main/java/**/application

# domain이 adapter를 import하는지
grep -rn "import jhkim593.orderpayment.*\.adapter\." --include="*.java" */src/main/java/**/domain

# Controller가 구현체를 직접 주입받는지 (provided 포트를 써야 함)
grep -rn "private final .*Service " --include="*.java" */src/main/java/**/adapter/api

# 컴파일 검증
./gradlew :order:compileJava :payment:payment-service:compileJava :user:compileJava
```
