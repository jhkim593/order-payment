package jhkim593.orderpayment.order.application.required;

import jhkim593.orderpayment.order.domain.Product;

import java.util.List;

public interface ProductRepository {
    Product find(Long id);
    List<Product> findByIds(List<Long> ids);
}