---
name: event-driven-flow
description: "order-payment의 Transactional Outbox + Kafka 이벤트 파이프라인과 주문-결제 보상 트랜잭션(SAGA) 규칙. 새 이벤트 타입 추가, EventType/EventHandler 등록, Kafka 리스너·토픽·재시도·DLT 설정, 아웃박스 발행 경로 수정, 이벤트 멱등성·중복 수신·순서 보장 설계, 주문 취소/결제 실패 보상 흐름 구현이나 디버깅 시 반드시 이 스킬을 사용할 것. '이벤트가 안 온다', '중복 처리된다', '아웃박스가 안 나간다', '상태가 PENDING에 멈춰있다' 같은 증상 추적에도 사용."
---

# Event-Driven Flow — Outbox + Kafka + SAGA

서비스 간 상태 전파는 전부 이 파이프라인을 지난다. 규칙을 지키지 않으면 이벤트가 조용히 유실되거나 중복 처리되는데, 둘 다 런타임에만 드러나므로 컴파일과 단위 테스트로는 잡히지 않는다.

## 전체 경로

```
[TX 안] TransactionManager.succeeded()
   └─ InternalEventPublisher → ApplicationEventPublisher.publishEvent(EventData)
        ├─ EventListener @TransactionalEventListener(BEFORE_COMMIT)
        │     └─ eventUpdater.save()  → outbox_event 테이블 INSERT (같은 트랜잭션)
        └─ EventListener @TransactionalEventListener(AFTER_COMMIT) @Async("outboxPublishEventExecutor")
              └─ EventPublisher.publishEvent() → KafkaTemplate.send(topic, aggregateId, json)
                    └─ 성공 시 outbox_event.published = true

[백업 경로] EventPublisher.publishPendingEvent()  @Scheduled(fixedDelay 10s)
   └─ published=false 인 이벤트를 재발행

[소비] KafkaEventListener @KafkaListener(topics = Topic.X)
   └─ EventData.fromJson() → EventHandlerFactory.get(type) → EventHandler.handle()
```

**핵심 성질:** 커밋 전에 아웃박스에 저장하고 커밋 후에 발행하므로 **at-least-once**다. 발행 성공 후 `published` 갱신에 실패하거나, 비동기 발행과 스케줄러가 겹치면 **같은 이벤트가 두 번 이상 도착할 수 있다.** 모든 소비자는 중복 수신을 전제로 작성한다.

**순서 보장:** Kafka 키가 `aggregateId`(주문 ID·결제 ID)이므로 동일 aggregate의 이벤트는 같은 파티션에 들어가 순서가 보장된다. 새 이벤트를 추가할 때 `aggregateId`를 반드시 해당 aggregate의 식별자로 채운다. null이나 임의 값을 넣으면 순서 보장이 깨진다.

## 새 이벤트 추가 절차

순서대로 6단계. 하나라도 빠지면 이벤트가 발행되지만 아무도 처리하지 않거나(죽은 이벤트), 역직렬화에서 NPE가 난다.

**1. Payload 클래스** — `common/core/.../event/{domain}/payload/{X}EventPayload.java`
```java
@Getter @NoArgsConstructor @AllArgsConstructor
public class PaymentSuccessEventPayload implements EventPayload {
    private Long orderId;
    private Long paymentId;
}
```
Jackson 역직렬화를 위해 기본 생성자와 getter가 필요하다.

**2. Event 클래스** — `common/core/.../event/{domain}/{X}Event.java`. 기존 이벤트 클래스의 형태를 그대로 따른다.

**3. EventType enum 등록** — `common/core/.../event/EventType.java`
```java
PAYMENT_SUCCESS(PaymentSuccessEventPayload.class, Topic.PAYMENT),
```
`EventData.fromJson()`이 이 매핑으로 payload 클래스를 찾는다. 등록하지 않으면 `EventType.from()`이 null을 반환하고 NPE로 이어진다.

**4. 토픽 상수** — 새 토픽이면 `common/core/.../event/Topic.java`에 상수 추가. 토픽은 발행 도메인 기준(`order`, `payment`)이지 소비자 기준이 아니다.

**5. 발행측** — 발행 서비스의 `application/event/InternalEventPublisher`에 메서드 추가. 반드시 `IdGenerator.getId()`(Snowflake)로 eventId를 만들고, TransactionManager의 `@Transactional` 메서드 안에서 호출한다.

**6. 소비측 EventHandler** — 소비 서비스의 `adapter/event/handler/{X}EventHandler.java`
```java
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventHandler implements EventHandler<PaymentSuccessEventPayload> {
    private final OrderProcessor orderProcessor;   // provided 포트만 의존

    @Override public void handle(EventData<PaymentSuccessEventPayload> eventData) { ... }
    @Override public EventType getType() { return EventType.PAYMENT_SUCCESS; }
}
```
`@Component`로 등록하면 `EventHandlerFactory`가 생성자에서 `List<EventHandler>`를 받아 자동 매핑한다. 별도 등록 코드는 필요 없지만, `getType()` 반환값이 틀리면 조용히 다른 이벤트를 먹거나 아무 이벤트도 받지 못한다.

소비 서비스가 그 토픽을 아직 듣고 있지 않다면 `adapter/mq/kafka/KafkaEventListener`에 `@KafkaListener` 메서드를 추가한다.

## 멱등성 설계

`EventHandlerFactory`는 중복을 걸러주지 않는다. 멱등성은 **도메인 상태 가드**로 확보한다.

```java
public void succeeded() {
    if (isComplete()) throw new OrderException(ErrorCode.ORDER_ALREADY_COMPLETED);
    this.status = OrderStatus.SUCCEEDED;
}
```

새 상태 전이 메서드를 만들 때마다 "이미 그 상태이거나 더 진행된 상태면?"에 대한 가드를 넣는다. 그리고 이 가드가 던지는 ErrorCode는 **재시도 불가(`isRetryable() == false`)**로 지정해야 한다. 중복 이벤트가 재시도 토픽을 거쳐 DLT까지 가는 것은 정상 동작이 아니라 노이즈다.

## 실패 처리와 재시도

`KafkaEventListener`의 계약:

```java
@RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000, multiplier = 2.0), ...)
```

- `OrderException`을 잡아 `e.getErrorCode().isRetryable()`이 true일 때만 다시 던진다 → 재시도 토픽으로
- false면 삼킨다 → 이미 처리된 중복이나 논리적으로 재시도해도 소용없는 실패
- 그 외 예외는 그대로 던져 재시도 → 3회 소진 시 `@DltHandler`가 받는다

새 ErrorCode를 추가할 때 `isRetryable()` 값을 의도적으로 정한다. 기본값에 기대지 말 것 — 잘못 지정하면 일시적 DB 장애가 조용히 무시되거나(false로 잘못 지정), 영구적 검증 실패가 3회 재시도 후 DLT를 오염시킨다(true로 잘못 지정).

## 주문-결제 보상 트랜잭션 (SAGA)

**정상 결제:**
```
POST /orders → Order(PENDING) 저장 → PaymentClient.billingKeyPayment() (동기 HTTP)
  ├─ 성공 → Order.succeeded() → CREDIT_ORDER_SUCCEED 이벤트 → user 서비스가 크레딧 적립
  ├─ 실패 → Order.failed()
  └─ 지연(PAYMENT_PROCESSING_DELAYED) → PENDING 유지 → OrderRecoverService가 10초마다 결제 상태 조회해 정정
```

**주문 취소:**
```
POST /orders/{id}/cancel → Order.canceling() → ORDER_CANCEL 이벤트 발행 (아웃박스)
  → payment-service의 OrderCancelEventHandler → PaymentProcessor.cancelPayment() → PortOne 취소 API
      ├─ 성공 → PAYMENT_CANCEL_SUCCEEDED → order의 핸들러 → Order.cancelSucceeded() → CREDIT_ORDER_CANCEL_SUCCEED
      └─ 실패 → PAYMENT_CANCEL_FAILED → order의 핸들러 → Order.cancelFailed()
```

취소는 동기 HTTP가 아니라 이벤트로 흐른다. 따라서 `CANCELING`은 응답 시점에 아직 확정되지 않은 중간 상태이며, 클라이언트에 최종 결과를 즉시 돌려줄 수 없다. 새 흐름을 설계할 때 "이 상태는 누가 언제 벗어나게 하는가"를 명시하고, 아무도 없으면 복구 스케줄러를 추가한다.

## 증상별 진단 경로

| 증상 | 확인 순서 |
|------|----------|
| 이벤트가 발행 안 됨 | ① `outbox_event` 테이블에 행이 있는가 → 없으면 `@Transactional` 밖에서 발행한 것 ② `published=false`로 남아있는가 → Kafka 연결/토픽 문제 |
| 소비되지 않음 | ① `EventType` enum에 등록됐는가 ② 소비 모듈에 그 토픽의 `@KafkaListener`가 있는가 ③ `EventHandler.getType()`이 맞는가 ④ 컨슈머 그룹 ID / `auto-offset-reset` |
| 중복 처리됨 | 정상 동작(at-least-once). 도메인 상태 가드가 없거나 ErrorCode가 retryable로 잘못 지정된 것 |
| 상태가 PENDING에 멈춤 | 복구 스케줄러가 그 상태를 조회 대상에 포함하는가 (`findPendingOrders`) |
| DLT에 쌓임 | 로그의 원 예외 확인 → 중복이면 ErrorCode를 non-retryable로 |

```bash
docker compose up -d kafka          # Kafka UI: http://localhost:8989
./gradlew :order:compileJava :payment:payment-service:compileJava
```
