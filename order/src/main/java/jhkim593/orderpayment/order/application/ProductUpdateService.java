package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.provided.ProductQuantity;
import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductUpdateService implements ProductUpdater {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public List<Product> decreaseStock(List<ProductQuantity> items) {
        Map<Long, Product> products = lock(items);
        items.forEach(item -> products.get(item.productId()).decreaseStock(item.quantity()));
        return List.copyOf(products.values());
    }

    @Override
    @Transactional
    public void increaseStock(List<ProductQuantity> items) {
        Map<Long, Product> products = lock(items);
        items.forEach(item -> products.get(item.productId()).increaseStock(item.quantity()));
    }

    private Map<Long, Product> lock(List<ProductQuantity> items) {
        List<Long> productIds = items.stream().map(ProductQuantity::productId).distinct().toList();
        List<Product> products = productRepository.findAllForUpdate(productIds);
        if (products.size() != productIds.size()) {
            throw new OrderException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
    }
}