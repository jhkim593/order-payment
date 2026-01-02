package jhkim593.orderpayment.payment.domain.util;

import jhkim593.orderpayment.payment.domain.BillingCycle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 구독 결제일 계산 유틸리티
 *
 * 월간/연간 구독의 다음 결제일을 계산합니다.
 * - 최초 결제일의 일(day)을 기억하여 매번 동일한 일자에 결제 시도
 * - 해당 월에 동일한 일자가 없는 경우 말일에 결제
 *
 * 예시:
 * - 1월 31일 최초 구독 -> 2월 28/29일, 3월 31일, 4월 30일, 5월 31일 등
 * - 2월 29일(윤년) 최초 구독 -> 다음 해 2월 28일
 */
public class BillingCycleCalculator {

    /**
     * 다음 결제일 계산 (최초 구독일 기준)
     *
     * @param originalSubscriptionDateTime 최초 구독 일시
     * @param currentPaymentDateTime 현재 결제 일시
     * @param billingCycle 구독 주기 (MONTHLY, YEARLY)
     * @return 다음 결제 일시
     */
    public static LocalDateTime calculateNextPaymentDateTime(
            LocalDateTime originalSubscriptionDateTime,
            LocalDateTime currentPaymentDateTime,
            BillingCycle billingCycle) {

        int originalDayOfMonth = originalSubscriptionDateTime.getDayOfMonth();
        LocalTime paymentTime = originalSubscriptionDateTime.toLocalTime();

        LocalDate nextMonthOrYear = switch (billingCycle) {
            case MONTHLY -> currentPaymentDateTime.toLocalDate().plusMonths(1);
            case YEARLY -> currentPaymentDateTime.toLocalDate().plusYears(1);
        };

        int lastDayOfNextMonth = nextMonthOrYear.lengthOfMonth();
        int targetDay = Math.min(originalDayOfMonth, lastDayOfNextMonth);

        return LocalDateTime.of(nextMonthOrYear.withDayOfMonth(targetDay), paymentTime);
    }

    /**
     * 구독 만료일 계산 (다음 결제일 23:59:59)
     *
     * @param originalSubscriptionDateTime 최초 구독 일시
     * @param currentPaymentDateTime 현재 결제 일시
     * @param billingCycle 구독 주기
     * @return 구독 만료 일시 (다음 결제일 23:59:59)
     */
    public static LocalDateTime calculateSubscriptionExpiryDateTime(
            LocalDateTime originalSubscriptionDateTime,
            LocalDateTime currentPaymentDateTime,
            BillingCycle billingCycle) {

        LocalDateTime nextPaymentDateTime = calculateNextPaymentDateTime(
                originalSubscriptionDateTime,
                currentPaymentDateTime,
                billingCycle
        );

        return nextPaymentDateTime.withHour(23).withMinute(59).withSecond(59).withNano(0);
    }
}