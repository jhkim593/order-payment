package jhkim593.orderpayment.order.application.required;

import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.StockHistory;

import java.util.List;

public interface ProductRepository {
    Product find(Long id);
    List<Product> findAll(List<Long> ids);
    boolean decreaseStock(Long id, int quantity);
    void increaseStock(Long id, int quantity);
    void saveHistories(List<StockHistory> histories);
}
