package jhkim593.orderpayment.order.adapter.database.jpa;

import jhkim593.orderpayment.order.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Query("""
            update Product p set p.stock = p.stock - :quantity, p.updatedAt = current_timestamp
            where p.productId = :id and p.stock >= :quantity
            """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("update Product p set p.stock = p.stock + :quantity, p.updatedAt = current_timestamp where p.productId = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
