package jhkim593.orderpayment.order.application.provided;

import java.util.List;

public interface ProductUpdater {
    void decreaseStock(List<ProductQuantity> items);
    void increaseStock(List<ProductQuantity> items);
}