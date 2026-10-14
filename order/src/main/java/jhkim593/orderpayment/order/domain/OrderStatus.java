package jhkim593.orderpayment.order.domain;

import java.util.List;

public enum OrderStatus {
    PENDING,
    PAYMENT_REQUESTED,
    FAILED,
    SUCCEEDED;

    public List<OrderStatus> updatableFrom() {
        return switch (this) {
            case PAYMENT_REQUESTED -> List.of(PENDING);
            case SUCCEEDED, FAILED -> List.of(PENDING, PAYMENT_REQUESTED);
            case PENDING -> List.of();
        };
    }
}
