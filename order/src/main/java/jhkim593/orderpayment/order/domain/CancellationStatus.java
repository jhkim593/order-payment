package jhkim593.orderpayment.order.domain;

import java.util.List;

public enum CancellationStatus {
    PENDING,
    SUCCEEDED,
    FAILED;

    public List<CancellationStatus> updatableFrom() {
        return switch (this) {
            case SUCCEEDED, FAILED -> List.of(PENDING);
            case PENDING -> List.of();
        };
    }
}
