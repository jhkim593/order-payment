# 0001. 복구 스케줄러 중복 실행은 분산 락 대신 조건부 UPDATE로 막는다

## 2026-10-04 (적용 중)

### 배경

- 복구 스케줄러가 미확정 결제를 폴링하며 결제사에 상태를 되묻는다
- 인스턴스가 여러 대면 같은 행을 중복으로 묻고 확인 횟수를 인스턴스 수만큼 빨리 소진한다

### 결정

- 분산 락(Redis·ShedLock) 없이 `UPDATE ... RETURNING` 한 번으로 확인 대상을 점유하고, 점유한 행만 처리한다
- 조회와 점유가 한 쿼리라 그사이 상태가 바뀌는 틈이 없고, `SKIP LOCKED`로 인스턴스끼리 행을 나눠 가진다
- 점유한 인스턴스가 죽어도 대기 시간이 지나면 다른 인스턴스가 회수한다
- 첫 확인(`check_count` 0, 80초)과 재확인(1 ~ `CHECK_LIMIT - 1`, 10초)은 애플리케이션이 조건을 바꿔 두 번 호출한다. 정책 값은 파라미터로 넘긴다
- 폴링은 결제사에 상태를 묻는 것이므로 `check`(확인)로 센다. `checked_at`은 생성 시 결제 요청 시각으로 채운다
- 기준 시각은 DB `now()`가 아니라 넘겨받은 `checkedAt`. `checked_at`과 같은 시계로 비교해야 서버 간 시계 차이에 흔들리지 않는다

```sql
UPDATE payment
   SET check_count = check_count + 1, checked_at = :checkedAt
 WHERE payment_id IN (
       SELECT payment_id FROM payment
        WHERE status = :status
          AND check_count BETWEEN :minCheckCount AND :maxCheckCount
          AND checked_at < :checkedAt - :intervalSeconds
        ORDER BY payment_id
        LIMIT :limit
        FOR UPDATE SKIP LOCKED)
RETURNING *
```

### 트레이드오프

- 얻는 것: 락 서버·락 테이블 없이 행 하나로 끝난다
- 감수하는 것: 이 쿼리만 네이티브 SQL이고 Postgres 문법(`RETURNING`, `SKIP LOCKED`)에 묶인다
