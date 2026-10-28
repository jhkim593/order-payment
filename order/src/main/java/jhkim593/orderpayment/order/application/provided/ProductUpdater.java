package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.StockChange;

import java.util.List;

public interface ProductUpdater {
    void decreaseStock(List<ProductQuantity> items, StockChange change);
    void increaseStock(List<ProductQuantity> items, StockChange change);
    void restock(Long productId, Integer quantity);
}