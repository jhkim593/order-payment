package jhkim593.orderpayment.order.adapter.database.jpa;

import jakarta.persistence.LockModeType;
import jhkim593.orderpayment.order.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.productId in :ids order by p.productId")
    List<Product> findAllForUpdate(@Param("ids") List<Long> ids);
}