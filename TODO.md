# TODO

## 보류 (나중에)

### 웹훅 수신
- 포트원 웹훅 엔드포인트 추가. 폴링은 안전망으로 유지
- 웹훅 본문을 믿지 않고 조회 API로 상태/금액 재확인
- 서명 검증 필요 (결제 확정을 외부 요청으로 트리거하는 엔드포인트)
- 중복 수신은 확정 가드의 멱등성에 위임

### 주문 쪽 무한 폴링
- `OrderRecoverService`는 시도 제한이 없음
- 결제가 PENDING으로 굳으면 주문도 영원히 PENDING이고 30초마다 계속 조회됨
- 결제 쪽 시도 제한을 넣은 뒤 주문 쪽에도 종료 조건 필요

### 재시도 소진 건 알림
- 3회 소진 시 현재는 로그만. 추후 알림 연동

### 구독/정기 결제
- `PaymentType`, `BillingCycle`, `BillingCycleCalculator`가 있으나 미사용
- 이번 범위에서 제외. 살릴지 지울지 미정

## 버그 (별도 트랙)

- `PaymentProcessService`가 포트원 요청 통화를 `"USD"`로 하드코딩. 주문은 `KRW`를 넘김
- `Payment.cancelledAt`이 `updatable=false`라 취소 성공 시각이 UPDATE에 포함되지 않음
- `PaymentHistoryUpdateService`가 빈 클래스. PG 원본 응답이 저장되지 않음
- `payment_history` 테이블의 `type`, `status`가 NOT NULL인데 엔티티에 해당 필드 없음
- `V1__init.sql`의 부분 유니크 인덱스가 존재하지 않는 상태값(`PAYMENT_PENDING`, `PAYMENT_SUCCESS`)을 참조
- `application.yml`에 포트원 시크릿 평문 커밋
