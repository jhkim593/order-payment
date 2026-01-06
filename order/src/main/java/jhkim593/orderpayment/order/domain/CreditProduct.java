package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "credit_product")
@DiscriminatorValue("CREDIT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class CreditProduct extends Product {

    @Column(nullable = false)
    private Integer creditAmount;
}
