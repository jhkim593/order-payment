package jhkim593.orderpayment.order.adapter.database;

import jakarta.persistence.EntityManager;
import jhkim593.orderpayment.order.adapter.database.jpa.ProductJpaRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductDBRepository implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Product find(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new OrderException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public List<Product> findAllForUpdate(List<Long> ids) {
        List<Product> products = productJpaRepository.findAllForUpdate(ids);
        // 이미 영속성 컨텍스트에 있던 상품은 락 조회로 값이 갱신되지 않아, 락을 잡은 뒤 최신 값으로 다시 읽는다
        products.forEach(entityManager::refresh);
        return products;
    }
}
