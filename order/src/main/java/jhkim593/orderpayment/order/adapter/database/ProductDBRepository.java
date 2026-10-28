package jhkim593.orderpayment.order.adapter.database;

import jhkim593.orderpayment.order.adapter.database.jpa.ProductJpaRepository;
import jhkim593.orderpayment.order.adapter.database.jpa.StockHistoryJpaRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.StockHistory;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductDBRepository implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final StockHistoryJpaRepository stockHistoryJpaRepository;

    @Override
    public Product find(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new OrderException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public List<Product> findAll(List<Long> ids) {
        List<Long> distinctIds = ids.stream().distinct().toList();
        List<Product> products = productJpaRepository.findAllById(distinctIds);
        if (products.size() != distinctIds.size()) {
            throw new OrderException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return products;
    }

    @Override
    public boolean decreaseStock(Long id, int quantity) {
        return productJpaRepository.decreaseStock(id, quantity) == 1;
    }

    @Override
    public void increaseStock(Long id, int quantity) {
        productJpaRepository.increaseStock(id, quantity);
    }

    @Override
    public void saveHistories(List<StockHistory> histories) {
        stockHistoryJpaRepository.saveAll(histories);
    }
}
