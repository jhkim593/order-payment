#!/usr/bin/env bash
# order-payment 경계면 정합성 자동 대조.
# 프로젝트 루트에서 실행한다: bash .claude/skills/msa-integration-qa/scripts/check-boundaries.sh
# 출력은 사실 나열이다. 각 항목이 실제 결함인지는 호출자가 판단한다.
set -uo pipefail
export LC_ALL=${LC_ALL:-en_US.UTF-8}
export LANG=${LANG:-en_US.UTF-8}

ROOT="$(pwd)"
SRC=(order/src/main/java payment/payment-service/src/main/java payment/payment-api/src/main/java user/src/main/java common/core/src/main/java common/outbox/src/main/java)
EXISTING=()
for d in "${SRC[@]}"; do [ -d "$d" ] && EXISTING+=("$d"); done

hr() { printf '\n=== %s ===\n' "$1"; }

# ---------- 1. EventType ↔ EventHandler 매핑 ----------
hr "1. EventType 등록 vs EventHandler 구현"
ETFILE=$(find common/core -name EventType.java | head -1)
if [ -n "${ETFILE:-}" ]; then
  TYPES=$(grep -oE '^\s+[A-Z_]+\(' "$ETFILE" | tr -d ' (' | sort -u)
  HANDLED=$(grep -rhoE 'EventType\.[A-Z_]+' "${EXISTING[@]}" --include='*EventHandler.java' | sed 's/EventType\.//' | sort -u)
  PUBLISHED=$(grep -rhoE 'new [A-Za-z]+Event\(' "${EXISTING[@]}" --include='InternalEventPublisher.java' | sed 's/new //;s/($//;s/(//' | sort -u)
  echo "[정의된 EventType]"; echo "$TYPES" | sed 's/^/  /'
  echo "[핸들러가 선언한 타입]"; echo "$HANDLED" | sed 's/^/  /'
  echo "[처리 핸들러 없음 = 죽은 이벤트 후보]"
  comm -23 <(echo "$TYPES") <(echo "$HANDLED") | sed 's/^/  ! /'
  echo "[EventType enum에 없는 핸들러 참조 = 컴파일 오류 후보]"
  comm -13 <(echo "$TYPES") <(echo "$HANDLED") | sed 's/^/  ! /'
  echo "[InternalEventPublisher가 발행하는 Event 클래스]"; echo "$PUBLISHED" | sed 's/^/  /'
else
  echo "  EventType.java 없음"
fi

# ---------- 2. Topic 상수 ↔ @KafkaListener ----------
hr "2. Topic 상수 vs @KafkaListener 구독"
TOPICFILE=$(find common/core -name Topic.java | head -1)
if [ -n "${TOPICFILE:-}" ]; then
  grep -oE 'String [A-Z_]+ *= *"[^"]+"' "$TOPICFILE" | sed 's/^/  정의: /'
fi
grep -rn 'topics *=' "${EXISTING[@]}" --include='*.java' | sed 's/^/  구독: /'

# ---------- 3. 포트 인터페이스 사용 실태 ----------
hr "3. 포트 인터페이스 vs 구현체/사용처"
for d in "${EXISTING[@]}"; do
  for kind in provided required; do
    find "$d" -path "*application/$kind/*.java" 2>/dev/null | while read -r f; do
      iface=$(basename "$f" .java)
      impls=$(grep -rlE "(implements|extends)[^{]*\b$iface\b" "${EXISTING[@]}" --include='*.java' 2>/dev/null | grep -vc "/$kind/$iface.java")
      uses=$(grep -rl "\b$iface\b" "${EXISTING[@]}" --include='*.java' 2>/dev/null | grep -vc "/$kind/$iface.java")
      if [ "$impls" = "0" ] && [ "$uses" = "0" ]; then
        echo "  !! 죽은 포트 (구현체도 사용처도 없음): $kind/$iface  ($f)"
      elif [ "$impls" = "0" ]; then
        echo "  ?  구현 클래스 미발견 (${uses}곳에서 주입됨 — 런타임 프록시면 정상): $kind/$iface"
      fi
    done
  done
done

# ---------- 4. 엔티티 필드 ↔ Flyway 컬럼 ----------
hr "4. JPA 엔티티 필드 vs Flyway DDL 컬럼"
camel_to_snake() { sed -E 's/([a-z0-9])([A-Z])/\1_\2/g' | tr 'A-Z' 'a-z'; }
MIGS=$(find . -path '*/db/migration/*.sql' -not -path './build/*' 2>/dev/null)
ALLDDL=$(cat $MIGS 2>/dev/null | tr 'A-Z' 'a-z')
find "${EXISTING[@]}" -name '*.java' 2>/dev/null \
  | xargs grep -lE '^@Entity[[:space:]]*$|^@Entity\(' 2>/dev/null | sort -u | while read -r ent; do
  cls=$(basename "$ent" .java)
  tbl=$(grep -oE '@Table\(name *= *"[^"]+"' "$ent" | sed 's/.*"\(.*\)"/\1/')
  [ -z "$tbl" ] && tbl=$(echo "$cls" | camel_to_snake)
  ddl=$(echo "$ALLDDL" | awk -v t="create table $tbl" 'BEGIN{p=0} index($0,t){p=1;next} p&&/^\)/{p=0} p{print}')
  if [ -z "$ddl" ]; then echo "  ! DDL 없음: $cls -> 테이블 $tbl"; continue; fi
  # 영속 필드만: `private <Type> <name>;` 형태의 한 줄 선언. 메서드/컬렉션 연관은 제외
  fields=$(grep -E '^[[:space:]]+private [A-Za-z0-9_.]+ [a-zA-Z0-9_]+ *;' "$ent" \
    | sed -E 's/.*private [A-Za-z0-9_.]+ +([a-zA-Z0-9_]+) *;.*/\1/' | camel_to_snake | sort -u)
  cols=$(echo "$ddl" | sed -E 's/^[[:space:]]*([a-z_]+).*/\1/' | grep -E '^[a-z_]+$' | sort -u)
  missing=$(comm -23 <(echo "$fields") <(echo "$cols"))
  if [ -n "$missing" ]; then
    echo "  ? $cls ($tbl) — DDL에서 못 찾은 필드 (@Transient/상속/연관이면 정상):"
    echo "$missing" | sed 's/^/      /'
  else
    echo "  OK $cls ($tbl)"
  fi
done

# ---------- 5. @Value 프로퍼티 키 ↔ application.yml ----------
hr "5. @Value 프로퍼티 키 vs application.yml"
grep -rhoE '@Value\("\$\{[^}:]+' "${EXISTING[@]}" --include='*.java' 2>/dev/null \
  | sed 's/@Value("${//' | sort -u | while read -r key; do
  leaf=$(echo "$key" | awk -F. '{print $NF}')
  if ! grep -rq "$leaf:" */src/main/resources/application.yml */*/src/main/resources/application.yml 2>/dev/null; then
    echo "  ? yml에서 못 찾은 키(기본값 있으면 정상): $key"
  fi
done

# ---------- 6. 모듈 의존 방향 ----------
hr "6. 금지된 모듈 의존"
grep -rn 'import jhkim593.orderpayment.payment.\(adapter\|application\|domain\)' order/src 2>/dev/null | sed 's/^/  ! order가 payment-service 내부를 import: /'
grep -rn 'import jhkim593.orderpayment.*\.adapter\.' --include='*.java' */src/main/java/*/*/*/*/domain */*/src/main/java/*/*/*/*/*/domain 2>/dev/null | sed 's/^/  ! domain이 adapter를 import: /'

hr "완료"
