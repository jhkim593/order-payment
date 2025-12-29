package jhkim593.orderpayment.order.domain;

public enum OrderStatus {
    PENDING,        // 주문 생성됨, 결제 대기중
    PAYMENT_FAILED, // 결제 실패
    COMPLETED,      // 결제 성공, 주문 완료
    CANCELLED       // 사용자가 취소
}