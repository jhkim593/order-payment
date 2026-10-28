package jhkim593.orderpayment.order.domain;

public record StockChange(StockChangeReason reason, Long orderId, Long cancellationId) {

    public static StockChange ordered(Long orderId) {
        return new StockChange(StockChangeReason.ORDERED, orderId, null);
    }

    public static StockChange orderFailed(Long orderId) {
        return new StockChange(StockChangeReason.ORDER_FAILED, orderId, null);
    }

    public static StockChange cancelled(Long orderId, Long cancellationId) {
        return new StockChange(StockChangeReason.CANCELLED, orderId, cancellationId);
    }

    public static StockChange restocked() {
        return new StockChange(StockChangeReason.RESTOCKED, null, null);
    }
}