# 결제·취소 흐름 개편 TODO

설계 근거는 `payment/docs/adr/`, `order/docs/adr/`. 정책은 각 모듈 `spec.md`.

## UNKNOWN 수동 확정 경로

- [ ] `UNKNOWN`에서 나가는 전이 (`confirm*` 접두사)
- [ ] 확정 시 기존 성공/실패 이벤트 발행 — 새 이벤트 타입 만들지 않음
- [ ] 관리자 입구 — `UNKNOWN` 목록 조회 + 확정. **커밋 `260867b`에서 결제 취소 REST 엔드포인트를 제거한 이력이 있어, 다시 여는 게 맞는지 먼저 확인**
- [ ] 운영 알림 — 지금 `log.warn`뿐. 기존 알림 인프라 확인 후 연결

**완료 조건** — `UNKNOWN`을 수동 확정하면 주문이 평소 흐름으로 복귀

## 부분 취소 실측 확인

- [ ] 테스트 결제 한 번으로 `amount.cancelled`가 테스트 채널에서도 채워지는지 확인 (payment ADR 0007 전제)

## 범위 밖 (별도 판단)

- `OrderRecoverService`, `common:outbox`의 `EventPublisher`에도 같은 중복 폴링 문제가 있다
- 묶음 취소 UI가 실제로 필요한지 — 필요 없으면 `order_cancellation_product`를 떼어낼 수 있다
- 주문 취소 결과 조회 API — 취소 API는 접수만 응답하므로 클라이언트가 결과를 볼 곳이 없다
- `user` 모듈 `UserApplicationTests`가 테스트 의존성 없이 컴파일 실패 — 전체 `gradlew build`가 깨짐
- PortOne 취소 웹훅(`Transaction.CancelPending`/`Cancelled`/`PartialCancelled`) — 비동기 취소(`REQUESTED`)가 4회 확인보다 오래 걸리면 `UNKNOWN`으로 넘어감. 실제 PG사에서 `REQUESTED`가 오는지 먼저 확인
