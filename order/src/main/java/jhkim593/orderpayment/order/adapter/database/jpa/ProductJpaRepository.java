package jhkim593.orderpayment.order.adapter.database.jpa;

import jhkim593.orderpayment.order.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    List<Product> findByIdIn(List<Long> ids);
}