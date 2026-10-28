package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.provided.ProductQuantity;
import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.StockChange;
import jhkim593.orderpayment.order.domain.StockHistory;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductUpdateService implements ProductUpdater {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void decreaseStock(List<ProductQuantity> items, StockChange change) {
        List<ProductQuantity> sorted = sorted(items);
        productRepository.saveHistories(sorted.stream()
                .map(item -> StockHistory.decreased(item.productId(), item.quantity(), change))
                .toList());
        sorted.forEach(item -> {
            if (!productRepository.decreaseStock(item.productId(), item.quantity())) {
                productRepository.find(item.productId());
                throw new OrderException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }
        });
    }

    @Override
    @Transactional
    public void increaseStock(List<ProductQuantity> items, StockChange change) {
        List<ProductQuantity> sorted = sorted(items);
        productRepository.saveHistories(sorted.stream()
                .map(item -> StockHistory.increased(item.productId(), item.quantity(), change))
                .toList());
        sorted.forEach(item -> productRepository.increaseStock(item.productId(), item.quantity()));
    }

    @Override
    @Transactional
    public void restock(Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new OrderException(ErrorCode.INVALID_RESTOCK_QUANTITY);
        }
        productRepository.find(productId);
        increaseStock(List.of(new ProductQuantity(productId, quantity)), StockChange.restocked());
    }

    private List<ProductQuantity> sorted(List<ProductQuantity> items) {
        return items.stream().sorted(Comparator.comparing(ProductQuantity::productId)).toList();
    }
}