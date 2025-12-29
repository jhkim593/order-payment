package jhkim593.orderpayment.order.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.order.adapter.database.jpa.ProductJpaRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductDBRepository implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Product find(Long id) {
        QProduct product = QProduct.product;

        Product result = jpaQueryFactory
                .selectFrom(product)
                .where(product.id.eq(id))
                .fetchOne();

        if (result == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }

        return result;
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        return productJpaRepository.findByIdIn(ids);
    }
}