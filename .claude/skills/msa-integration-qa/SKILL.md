---
name: msa-integration-qa
description: "order-payment 모듈 간 경계면 정합성을 교차 검증하는 QA 스킬. 이벤트 등록 누락, 엔티티-DDL 스키마 드리프트, 포트-어댑터 미구현, 모듈 의존 역전, 상태 전이 도달 불가, DTO 계약 불일치를 잡아낸다. 코드 변경 후 검증, '리뷰해줘', '검증해줘', '문제 없는지 봐줘', '정합성 확인', '스키마 맞는지', '이벤트 연결됐는지', 기능 구현 완료 직후 점검, 장애 원인 추적 시 반드시 이 스킬을 사용할 것. 컴파일과 단위 테스트가 통과해도 런타임에 깨지는 결함을 찾는 것이 목적이다."
---

# MSA Integration QA — 경계면 교차 검증

이 프로젝트에서 실제로 터지는 버그는 대부분 **한쪽만 보면 정상인데 두 쪽을 맞대면 어긋나는** 종류다. `ddl-auto: none`이라 스키마 불일치가 기동 시점에 안 잡히고, 이벤트는 JSON 문자열로 오가서 타입 검사가 없으며, 모듈이 분리돼 있어 계약 위반이 컴파일러를 통과한다. 그래서 QA의 핵심은 존재 확인이 아니라 **양쪽 동시 읽기**다.

## 자동 대조 먼저 실행

```bash
bash .claude/skills/msa-integration-qa/scripts/check-boundaries.sh
```

6개 경계면(EventType↔핸들러, Topic↔리스너, 포트↔구현, 엔티티↔DDL, @Value↔yml, 모듈 의존)을 기계적으로 대조한다. 출력은 **사실 나열이지 판정이 아니다.** `?` 표시 항목은 정상일 수 있으므로(런타임 프록시, 연관관계 필드, 기본값 있는 프로퍼티) 반드시 해당 코드를 열어 확인한 뒤 결론을 낸다. 스크립트가 못 잡는 것은 아래 수동 검증으로 채운다.

## 경계면 목록 — 왼쪽(생산자)과 오른쪽(소비자)을 같이 연다

| 경계면 | 왼쪽 | 오른쪽 | 어긋나면 |
|--------|------|--------|---------|
| 이벤트 등록 | `EventType` enum 상수 | `EventHandler.getType()` | 발행되지만 아무도 처리 안 함 (죽은 이벤트) |
| 이벤트 payload | `InternalEventPublisher`가 채우는 필드 | 핸들러의 `getPayload().getX()` | 런타임 NPE. 필드명이 다르면 Jackson이 null로 채운다 |
| 토픽 구독 | `Topic` 상수, `EventType`의 topic | `@KafkaListener(topics=...)` | 이벤트가 소비 서비스에 도달하지 않음 |
| 스키마 | JPA 엔티티 필드/`@Column(nullable)` | Flyway `V*.sql` 컬럼/제약 | 해당 쿼리 첫 실행 시 SQL 오류 |
| 포트 | `application/provided`·`required` 인터페이스 | `adapter/`의 구현체 | 빈 주입 실패 또는 죽은 포트 |
| 원격 계약 | `payment-api`의 DTO·엔드포인트 | `payment-service`의 Controller 응답 | 필드 누락 시 null, 상태값 문자열 불일치 시 오판 |
| 상태 문자열 | `OrderStatus`/`PaymentStatus` enum | `"SUCCEEDED".equals(status)` 같은 문자열 비교 | enum 이름 변경 시 조용히 항상 false |
| 에러 코드 | 도메인 `ErrorCode`/`PaymentErrorCode` | `CustomControllerAdvice`, `*ErrorHandler` 매핑 | 매핑 누락 시 500으로 나감 |
| 상태 전이 | 엔티티의 전이 메서드와 가드 | 실제 호출 경로(서비스·핸들러·스케줄러) | 도달 불가 상태 또는 빠져나갈 수 없는 상태 |
| 재시도 정책 | `ErrorCode.isRetryable()` | `KafkaEventListener`의 재던짐 분기 | 일시 장애가 무시되거나 DLT가 중복으로 오염 |
| 설정 키 | `@Value("${...}")` | 각 모듈 `application.yml` | 기동 실패 또는 의도치 않은 기본값 |

## 수동 검증 절차

자동 스크립트 결과를 확인한 뒤, 변경 범위에 해당하는 항목만 수행한다.

**1. 이벤트 payload 필드 대조 (스크립트가 못 잡는 부분)**
발행측 `InternalEventPublisher`의 payload 생성자 인자와 소비측 핸들러가 호출하는 getter를 나란히 놓고 비교한다. payload 클래스에 기본 생성자와 getter가 있는지, `EventData.fromJson()`이 `EventType.getPayloadClass()`로 찾는 클래스가 맞는지 확인한다.

**2. 상태 전이 도달성 분석**
enum의 모든 상수에 대해 표를 만든다.

| 상태 | 진입 경로 | 이탈 경로 | 복구 스케줄러 대상 |
|------|----------|----------|------------------|

이탈 경로가 없는 비종단 상태, 진입 경로가 없는 상태(죽은 상태), 중간 상태인데 복구 대상이 아닌 것을 결함으로 보고한다. `PENDING`, `CANCELING` 같은 중간 상태가 특히 위험하다 — 외부 API 응답이 유실되면 영원히 그 상태로 남는다.

**3. 멱등성 확인**
각 `EventHandler`가 같은 이벤트를 두 번 받았을 때의 결과를 추적한다. 아웃박스는 at-least-once이므로 중복은 정상 동작이다. 도메인 가드(`isComplete()` 등)가 막아주는지, 그 가드가 던지는 ErrorCode가 non-retryable인지 확인한다.

**4. 트랜잭션 경계 확인**
- `@Transactional`이 `{X}Service`에 붙어 있으면 결함 — 외부 API 호출이 트랜잭션 안에 들어간다
- 이벤트 발행(`InternalEventPublisher` 호출)이 `@Transactional` 메서드 **밖**에 있으면 결함 — 아웃박스가 `BEFORE_COMMIT`에 저장하므로 이벤트가 유실된다
- `@Transactional` 메서드 안에서 외부 HTTP 호출을 하면 결함

**5. 빌드 검증**
```bash
./gradlew compileJava compileTestJava
./gradlew test
```

## 실행 시점 — 전체 완성 후가 아니라 모듈 완성 직후

경계면 결함은 뒤로 갈수록 전파되며 수정 비용이 커진다. 한 모듈(예: 결제 취소 API + 이벤트 핸들러)이 완성되면 그 즉시 관련 경계면만 검증한다. 전체 작업 종료 시 한 번 더 전체 스크립트를 돌린다.

## 보고 형식

```markdown
## QA 검증 결과

### 확인된 결함
| # | 심각도 | 경계면 | 위치 | 증상 | 수정 방향 |
|---|-------|--------|------|------|----------|
| 1 | High | EventType↔핸들러 | EventType.java:21 | PAYMENT_FAIL 처리 핸들러 없음 | order에 핸들러 추가 또는 의도적이면 주석 명시 |

### 판단 보류 (추가 확인 필요)
- 항목과 확인이 필요한 이유

### 통과 항목
- 검증했고 문제없는 경계면 목록

### 미검증
- 검증하지 못한 영역과 그 이유 (환경 미기동, 범위 밖 등)
```

심각도는 **런타임에 무엇이 깨지는가**로 정한다. High = 데이터 정합성 훼손 또는 요청 실패, Medium = 특정 경로에서만 실패, Low = 죽은 코드·규약 이탈.

**추측을 결함으로 보고하지 않는다.** 확인하지 못한 것은 "판단 보류"나 "미검증"에 넣는다. 잘못된 지적은 팀 전체의 재작업을 유발하므로, 결함으로 올리기 전에 반드시 양쪽 파일을 실제로 열어 근거(파일:라인)를 확보한다.
