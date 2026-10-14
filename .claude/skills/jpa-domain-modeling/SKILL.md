---
name: jpa-domain-modeling
description: "order-payment의 JPA 엔티티 작성 규칙, 상태 머신 설계, Flyway 마이그레이션 관리 규약. 새 엔티티/테이블 추가, 컬럼 변경, 상태(OrderStatus/PaymentStatus) 전이 추가, 도메인 예외·ErrorCode 정의, Flyway V*__*.sql 작성, 엔티티와 DDL 불일치 진단 시 반드시 이 스킬을 사용할 것. '테이블 추가', '컬럼 추가', '마이그레이션', '엔티티 만들어줘', '상태 하나 더 필요해' 같은 요청에 모두 해당한다."
---

# JPA Domain Modeling — 엔티티·상태 머신·마이그레이션

`ddl-auto: none`이므로 스키마의 유일한 진실은 Flyway SQL이다. 엔티티는 그 스키마를 반영할 뿐이며, 둘이 어긋나도 애플리케이션은 기동하고 해당 쿼리를 처음 실행하는 런타임에야 깨진다. 그래서 엔티티와 SQL은 항상 같은 변경에서 함께 수정한다.

## 엔티티 작성 규칙

기존 엔티티(`Order`, `Payment`, `OutboxEvent`)가 공유하는 형태를 따른다.

```java
@Entity
@Table(name = "orders")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Order create(Long userId, Integer totalAmount) { ... }
}
```

**지켜야 할 것과 이유:**

| 규칙 | 이유 |
|------|------|
| 생성자를 전부 막고 `static create(...)` 팩토리만 노출 | 불완전한 상태의 엔티티가 만들어질 수 없게 한다. 초기 상태(PENDING)와 `statusUpdatedAt` 같은 필수 값을 팩토리가 책임진다 |
| `@Setter` 금지 | 상태 변경은 반드시 의미 있는 도메인 메서드(`succeeded()`, `canceling()`)를 통한다. setter가 있으면 가드를 우회한 전이가 생긴다 |
| `@Enumerated(EnumType.STRING)` | ORDINAL은 enum 상수 순서가 바뀌는 순간 기존 데이터의 의미가 통째로 바뀐다 |
| ID는 `Long` | 서비스 간 이벤트에서 `aggregateId: Long`으로 전달된다. 타입을 바꾸면 이벤트 계약이 깨진다 |
| 이벤트 ID는 `IdGenerator`(Snowflake) | 아웃박스 이벤트 ID는 DB 시퀀스가 아니라 애플리케이션이 만든다. 커밋 전에 ID가 필요하기 때문 |
| 컬렉션은 `@Builder.Default`로 초기화 | 빌더가 null로 덮어써 NPE가 난다 |

`@OneToMany`를 추가할 때는 `application.yml`의 `default_batch_fetch_size: 100`이 N+1을 완화하지만 없애지는 않는다는 점을 기억한다. 목록 조회 경로라면 fetch join이나 QueryDSL 조회를 `adapter/database`에 명시적으로 만든다.

## 상태 머신 설계

상태 전이는 엔티티 메서드로만 표현하고, 각 메서드는 **가드 → 전이 → 타임스탬프 갱신** 순서를 지킨다.

```java
public void canceling() {
    if (!isSuccess()) throw new OrderException(ErrorCode.ORDER_NOT_SUCCEEDED);
    this.status = OrderStatus.CANCELING;
    this.statusUpdatedAt = LocalDateTime.now();
}
```

새 상태를 추가할 때 반드시 답해야 하는 네 가지:

1. **누가 이 상태로 넣는가** — 어떤 유스케이스/이벤트 핸들러인가
2. **누가 이 상태에서 빼내는가** — 나가는 전이가 없으면 영구 정지 상태다
3. **중간 상태라면 복구 경로가 있는가** — `PENDING`, `CANCELING` 같은 중간 상태는 `{X}RecoverService` 스케줄러의 조회 대상에 포함시킨다. 그렇지 않으면 외부 API 응답이 유실될 때 영원히 그 상태로 남는다
4. **중복 이벤트로 두 번 호출되면 어떻게 되는가** — 가드가 이미 완료된 전이를 막아야 한다 (`isComplete()`, `isCancelComplete()` 패턴). 이 가드가 던지는 ErrorCode는 재시도 불가로 지정한다

`statusUpdatedAt`은 복구 스케줄러가 "N초 이상 이 상태에 머문 레코드"를 찾는 기준이므로 전이마다 반드시 갱신한다.

## 판단은 도메인에 묻는다

서비스가 엔티티의 getter를 꺼내 조건을 조립하고 있다면, 그 조건은 도메인 규칙이므로 엔티티 메서드로 선언한다. 서비스는 상태를 캐물어 스스로 판단하지 말고 엔티티에 메시지를 보내 답을 받는다.

```java
// 서비스가 판단 — 같은 조건이 여러 서비스에 복제되고, 상태가 늘면 전부 찾아 고쳐야 한다
if (PaymentStatus.CANCELING.equals(payment.getStatus()) && payment.isAttemptExhausted()) { ... }

// 도메인이 판단 — 규칙의 정의가 한 곳에만 있다
if (payment.isCancelingLimit()) { ... }
```

**엔티티로 옮겨야 하는 신호:**

| 신호 | 예 |
|------|-----|
| getter 두 개 이상을 조합한 조건문 | `getStatus()` + `getAttemptCount()` |
| `equals`/비교로 상태 enum을 직접 판별 | `PaymentStatus.PENDING.equals(payment.getStatus())` |
| 같은 조건식이 두 군데 이상에 등장 | 결제/취소 복구 스케줄러 양쪽 |
| 상수와 필드를 비교 | `payment.getAttemptCount() >= Payment.ATTEMPT_LIMIT` |

판단을 옮기면 그 판단에만 쓰이던 getter와 상수는 `private`으로 내린다. 노출된 채로 두면 다음 사람이 다시 조립한다 — `isAttemptLimit()`이 private이고 `isPendingLimit()`/`isCancelingLimit()`만 public인 이유다.

**서비스에 남기는 것:** 외부 시스템의 응답을 해석하는 조건(`"PAID".equals(response.getStatus())`), 여러 애그리거트를 엮는 판단, 스케줄·설정값 기반 분기. 엔티티 자신의 필드만으로 답할 수 있는 질문이 아니면 도메인 메서드가 아니다.

메서드 이름은 판단 결과를 말하고(`isPendingLimit`), 구현 방식을 말하지 않는다(`checkStatusAndCount`). 어휘는 도메인 전체에서 하나로 통일한다 — `limit`과 `exhausted`를 섞어 쓰지 않는다.

## 도메인 예외

```java
// domain/error/ErrorCode.java
ORDER_ALREADY_COMPLETED(...),   // 재시도 불가
ORDER_PROCESSING_DELAYED(...),  // 재시도 가능
```

`ErrorCode`에는 `isRetryable()`이 있고 `KafkaEventListener`가 이 값으로 재시도 여부를 결정한다. 새 코드를 추가할 때 이 값을 의도적으로 정한다 — 자세한 판단 기준은 `event-driven-flow` 스킬 참조.

HTTP 응답 매핑은 `adapter/api/config/CustomControllerAdvice`가 담당한다. 새 예외를 도메인에 추가하면 여기에 매핑을 추가했는지 확인한다. 빠뜨리면 500으로 나간다.

## Flyway 마이그레이션

**모듈별 위치:**

| 모듈 | 경로 | 현재 |
|------|------|------|
| order | `order/src/main/resources/db/migration/` | `V1__init.sql` |
| payment-service | `payment/payment-service/src/main/resources/db/migration/` | `V1__init.sql` |
| user | `user/src/main/resources/db/migration/` | `V1__init.sql` |
| common:outbox | `common/outbox/src/main/resources/db/migration/` | `V1.1__init.sql` |

**`common:outbox`의 마이그레이션은 이 모듈을 의존하는 모든 서비스의 DB에 클래스패스 병합으로 함께 적용된다.** 그래서 버전이 `V1.1`로 각 서비스의 `V1`과 겹치지 않게 잡혀 있다. outbox 마이그레이션을 추가할 때는 `V1.2`, `V1.3`처럼 소수점 계열을 유지해 서비스 모듈의 `V2`, `V3`와 충돌하지 않게 한다.

**규칙:**

- **이미 적용된 마이그레이션 파일은 절대 수정하지 않는다.** Flyway가 체크섬을 검증하므로 기동이 실패한다. 변경은 항상 새 버전 파일로.
- 파일명은 `V{버전}__{스네이크_설명}.sql` — 언더스코어 **두 개**.
- 컬럼명은 snake_case, 엔티티 필드는 camelCase. Spring Boot 기본 `CamelCaseToUnderscoresNamingStrategy`가 변환하므로 `@Column(name=...)`은 이 규칙에서 벗어날 때만 쓴다.
- `NOT NULL` 컬럼을 기존 테이블에 추가할 때는 DEFAULT를 주거나 백필 후 제약을 거는 2단계로 나눈다. 운영 데이터가 있으면 한 번에 걸 수 없다.
- 테스트 데이터 INSERT는 `V1__init.sql`처럼 초기 마이그레이션에만 둔다.

## 엔티티 ↔ DDL 정합성 체크

변경 후 반드시 대조한다. 자동 대조는 `msa-integration-qa` 스킬의 `scripts/check-boundaries.sh`를 쓴다.

수동으로 볼 항목:
- 엔티티의 모든 영속 필드에 대응하는 컬럼이 SQL에 있는가 (camelCase → snake_case 변환 후 비교)
- `@Column(nullable = false)`와 SQL의 `NOT NULL`이 일치하는가
- 타입 대응: `Long`↔`BIGINT`, `Integer`↔`INTEGER`, `String`↔`VARCHAR/TEXT`, `LocalDateTime`↔`TIMESTAMP`, `boolean`↔`BOOLEAN`, enum+`EnumType.STRING`↔`VARCHAR`
- `@Table(name=...)`이 실제 테이블명과 같은가 (`Order` 엔티티는 예약어 회피로 `orders`)

```bash
./gradlew :order:compileJava
docker compose up -d order-db payment-db user-db
./gradlew :order:bootRun    # Flyway가 마이그레이션을 실제로 적용하는지 확인
```
