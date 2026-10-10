# 결제·취소 흐름 개편 TODO

설계 근거는 `payment/docs/adr.md`, `order/docs/adr.md`. 정책은 각 모듈 `spec.md`.

단계마다 세션을 나눠 진행한다. 1~4는 각각 독립적으로 배포 가능하고, 5는 세 모듈이 함께 나가야 한다.

## 다음 할 일

1. **2번** 예외 분류 단순화 + 취소 멱등키
2. **5번** 부분 취소 — 착수 전 5-0 실측 필수
3. **3번** UNKNOWN 수동 확정 경로 — 부분 취소 적용 후 진행. 관리자 입구를 다시 열지 먼저 결정

---

## 1. check 어휘 전환 + 조회 조건 통합

**상태:** 완료

- [x] `V2` 마이그레이션 — `attempt_count` → `check_count` 리네임, `checked_at` 추가, 인덱스 `(status, checked_at)`
- [x] `Payment` — `CHECK_LIMIT = 4`, `FIRST_CHECK_DELAY_SECONDS = 80`, `CHECK_INTERVAL_SECONDS = 10`, `checkCount` 기본값 `0`, `checkedAt`, `isCheckLimit()`
- [x] `create()` — `checkedAt`에 결제 요청 시각
- [x] `canceling()` — `checkCount = 0`, `checkedAt = now`로 리셋
- [x] `PaymentRepository` / `PaymentDBRepository` — `updateCheck` 하나로 통합 (`UPDATE ... RETURNING` + `SKIP LOCKED`, ADR 0001). 정책 값은 파라미터로 받음
- [x] `PaymentTransactionManager.claimPaymentsToCheck` — 첫 확인(0회·80초)과 재확인(1~3회·10초)을 한 트랜잭션에서 선점
- [x] `PaymentRecoverService` — 선점한 결제만 확인
- [x] 테스트 갱신 (`PaymentTest`, `PaymentRecoverServiceTest`, `PaymentTransactionManagerTest`) — Fake 기반, `Clock` 주입
- [x] 앱 실행으로 네이티브 쿼리 엔티티 매핑 수동 확인

**선점 조건** — 애플리케이션이 두 번 호출
```
첫 확인: check_count BETWEEN 0 AND 0               AND checked_at < now - 80s
재확인:  check_count BETWEEN 1 AND CHECK_LIMIT - 1 AND checked_at < now - 10s
```

**남은 검증** — H2는 `RETURNING`/`SKIP LOCKED` 미지원이라 Postgres 필요
- [x] 선행: `spring-boot-starter-flyway` 추가. 기존 개발 DB는 V1 파일과 스키마가 달라 볼륨을 새로 만들고 V1 → V1.1 → V2 적용 확인
- [x] 리포지토리 테스트(Testcontainers, `PaymentDBRepositoryTest`) — 횟수 범위·시간 조건, 10초 안 재선점 안 됨, `limit`, 다른 서버가 잡은 결제 건너뜀(SKIP LOCKED)
- [x] 통합 테스트(`PaymentTransactionManagerIntegrationTest`) — 재확인 선점이 실패하면 첫 확인 선점도 롤백되어 횟수가 소진되지 않음

**열린 문제** — 고칠지 판단 필요
- [x] 4번째 확인 뒤 `UNKNOWN` 전환이 예외로 실패하면 `PENDING`·4회로 남아 다시는 선점되지 않음 → 별도 스케줄러 `updateCheckLimitedPayments`가 전환 (ADR 0006)

**완료 조건** — 인스턴스 2개에서 같은 결제에 대한 결제사 호출이 사이클당 1회 → `PaymentDBRepositoryTest`의 SKIP LOCKED·10초 재선점 테스트로 충족

---

## 2. 예외 분류 단순화 + 취소 멱등키

**상태:** 완료

- [x] `PortOneApiManager` — 4xx는 `PG_PAYMENT_FAILED`, 그 외 전부 `PG_PAYMENT_UNKNOWN` (ADR 0002)
- [x] `PaymentProcessService` — `PG_PAYMENT_FAILED`일 때만 실패 확정, catch 하나로
- [x] `PortOneApi.cancelPayment` — `Idempotency-Key: cancel-{paymentId}`
- [x] 폴링 중 NOT_FOUND 분기 — 두지 않기로 함. 한도 소진 후 `UNKNOWN`
- [x] HTTP 클라이언트 `HttpURLConnection` → JDK `HttpClient` (POST 재전송 방지)
- [x] 결제 에러 응답이 코드 값(`P006`) 대신 enum 이름을 보내 주문의 `isPgPaymentUnknown()`/`isNotFound()`가 항상 false이던 버그 수정

**완료 조건** — 결제사가 5xx를 주는 상황에서 `FAILED`가 아니라 미확정 상태로 남음 → `PaymentProcessServiceTest`로 충족

**남은 것** — 상태 조회 타임아웃이 60초로 ADR 0006 전제(10초 미만)와 어긋남. 조회 클라이언트만 짧게 분리할지 결정 필요

---

## 3. UNKNOWN 수동 확정 경로

**선행:** 5번(부분 취소) 적용 후 진행. 5-2에서 취소 상태가 `PaymentStatus`의 `CANCEL_*`에서 `CancellationStatus`로 옮겨가므로, 먼저 만들면 취소 쪽 확정 경로를 다시 짜야 한다.

- [ ] `UNKNOWN`에서 나가는 전이 (`confirm*` 접두사)
- [ ] 확정 시 기존 성공/실패 이벤트 발행 — 새 이벤트 타입 만들지 않음
- [ ] 관리자 입구 — `UNKNOWN` 목록 조회 + 확정. **커밋 `260867b`에서 결제 취소 REST 엔드포인트를 제거한 이력이 있어, 다시 여는 게 맞는지 먼저 확인**
- [ ] 운영 알림 — 지금 `log.warn`뿐. 기존 알림 인프라 확인 후 연결

**완료 조건** — `UNKNOWN`을 수동 확정하면 주문이 평소 흐름으로 복귀

---

## 4. 크레딧 제거 → 일반 상품

**상태:** 완료

- [x] 크레딧 이벤트·페이로드 삭제, `user` 모듈 크레딧 로직·테이블 삭제 (Kafka 컨슈머 인프라는 유지)
- [x] `Product` 단일 엔티티 + 재고(`stock`), `credit_product`·`product_type` 제거 (order `V2`, user `V2`)
- [x] 재고: 주문 생성 시 차감, 결제 실패·취소 성공 시 복원 (ADR 0003), ID 순 잠금 + `refresh` (ADR 0004)
- [x] 같은 상품 중복 주문 거절
- [x] `OrderStockIntegrationTest` (Testcontainers)
- [x] order에 `spring-boot-starter-flyway` 추가 — 이전엔 마이그레이션이 자동 실행되지 않았음

**왜 5보다 먼저** — 부분 취소에서 크레딧을 부분 회수해야 하는데, 일반 상품으로 가면 그 경로 자체가 없어진다. 먼저 정리해야 5의 범위가 확정된다.

---

## 5. 부분 취소

세 모듈이 함께 나가야 한다. 한 브랜치에서 작업하되 세션은 나눠도 된다.

### 5-0. 선행 확인

- [x] **결제사 결제 조회 응답에 누적 취소 금액이 오는지** — PortOne V2 OpenAPI로 확인 (2026-10-07)
  - `amount.cancelled` (총 취소금액, 필수) + `cancellations` 배열. ADR 0004 전제 성립
  - 부분 취소 후 결제 상태는 `PARTIAL_CANCELLED` → 취소 확정은 상태가 아니라 `amount.cancelled` 증가로 판정
  - 취소 응답 `cancellation.status`가 `REQUESTED`일 수 있음 → 성공 확정 금지, 미확정으로 폴링
  - `CancelPaymentBody.currentCancellableAmount` — 직전 잔액이 일치할 때만 취소. 이중 환불 방어용으로 5-2에서 사용 검토
- [ ] 5-2 구현 후 테스트 결제 한 번으로 `amount.cancelled`가 테스트 채널에서도 채워지는지 확인

### 5-1. `common:core` — 이벤트 계약

- [x] `OrderCancelEventPayload` — `cancellationId`, `amount` 추가
- [x] `PaymentCancelSucceededEventPayload` / `PaymentCancelFailedEventPayload` — `cancellationId` 추가

### 5-2. `payment`

- [x] `payment_cancellation` 테이블 (PK = `cancellationId`), 진행 중 취소 1건 부분 유니크 인덱스 (V3)
- [x] `pg_cancellation_id` / `cancelled_at` 제거. 취소 금액은 누적 컬럼 대신 `Payment`가 계산 (ADR 0005)
- [x] `PaymentStatus`에서 `CANCEL_*` 제거, `CancellationStatus` 신설
- [x] 취소 흐름 분리 — `PaymentCancelProcessService` / `PaymentCancelTransactionManager` / `PaymentCancelRecoverService` / `PaymentCancellationRepository`(조회 전용)
- [x] 가드 — 진행 중 취소 1건, 같은 `cancellationId` 재요청, 취소 합계 + 요청 금액 ≤ 결제 금액
- [x] 응답 `SUCCEEDED`/`FAILED`/`REQUESTED` 처리, 폴링은 `amount.cancelled` 일치로 확정 (ADR 0007)
- [x] `currentCancellableAmount`, 멱등키 = `cancellationId`
- [x] `CancelPaymentRequestDto`를 `payment-api`에서 `payment-service`로 이동 (서비스 간 프로토콜 아님)

### 5-3. `order`

- [ ] `order_cancellation`, `order_cancellation_product` 테이블
- [ ] `order_product.cancelled_quantity` 누적 컬럼
- [ ] `orders.status`에서 취소 관련 값 제거
- [ ] 가드 — 미확정 취소 1건 제한, 수량 초과 시 요청 전체 거절
- [ ] 결과 수신 시 해당 취소 건만 확정, 실패면 수량 되돌림 (미확정이면 되돌리지 않음)
- [ ] 취소 성공 시 그 취소 건의 수량만큼 재고 복원 (4번에서 넘어옴)

**완료 조건** — 상품 2개 중 1개만 취소했을 때 금액·수량이 양쪽 모듈에서 맞고, 나머지 1개를 이어서 취소할 수 있음

---

## 범위 밖 (별도 판단)

- `OrderRecoverService`, `common:outbox`의 `EventPublisher`에도 같은 중복 폴링 문제가 있다
- 묶음 취소 UI가 실제로 필요한지 — 필요 없으면 `order_cancellation_product`를 떼어낼 수 있다
- 주문 취소 결과 조회 API — 취소 API는 접수만 응답하므로 클라이언트가 결과를 볼 곳이 없다
- PortOne 취소 웹훅(`Transaction.CancelPending`/`Cancelled`/`PartialCancelled`) — 비동기 취소(`REQUESTED`)가 4회 확인보다 오래 걸리면 `UNKNOWN`으로 넘어감. 실제 PG사에서 `REQUESTED`가 오는지 먼저 확인