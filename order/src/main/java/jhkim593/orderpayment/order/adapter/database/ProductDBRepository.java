package jhkim593.orderpayment.order.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.order.adapter.database.jpa.ProductJpaRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.QProduct;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductDBRepository implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product find(Long id) {
        return productJpaRepository.findById(id).orElseThrow(() -> new OrderException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        return productJpaRepository.findByProductIdIn(ids);
    }
}