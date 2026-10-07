package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.Product;

import java.util.List;

public interface ProductUpdater {
    List<Product> decreaseStock(List<ProductQuantity> items);
    void increaseStock(List<ProductQuantity> items);
}