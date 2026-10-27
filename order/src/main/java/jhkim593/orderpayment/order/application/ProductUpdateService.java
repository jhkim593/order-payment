package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.provided.ProductQuantity;
import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.application.required.ProductRepository;
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
    public void decreaseStock(List<ProductQuantity> items) {
        sorted(items).forEach(item -> {
            if (!productRepository.decreaseStock(item.productId(), item.quantity())) {
                productRepository.find(item.productId());
                throw new OrderException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }
        });
    }

    @Override
    @Transactional
    public void increaseStock(List<ProductQuantity> items) {
        sorted(items).forEach(item -> productRepository.increaseStock(item.productId(), item.quantity()));
    }

    private List<ProductQuantity> sorted(List<ProductQuantity> items) {
        return items.stream().sorted(Comparator.comparing(ProductQuantity::productId)).toList();
    }
}