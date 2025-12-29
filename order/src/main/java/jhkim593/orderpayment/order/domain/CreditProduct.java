package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "credit_products")
@DiscriminatorValue("CREDIT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class CreditProduct extends Product {

    @Column(nullable = false)
    private Integer creditAmount;

    @Column(nullable = false)
    private Integer validityDays;

    public static CreditProduct create(String name, String description, Integer price, Integer creditAmount, Integer validityDays) {
        CreditProduct creditProduct = CreditProduct.builder()
                .creditAmount(creditAmount)
                .validityDays(validityDays)
                .build();

        // 부모 클래스 필드는 리플렉션 또는 생성자를 통해 설정해야 합니다
        return creditProduct;
    }
}
