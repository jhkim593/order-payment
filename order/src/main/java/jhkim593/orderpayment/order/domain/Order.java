package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Order create(Long userId, Integer totalAmount) {
        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
    }

    public void succeeded() {
        if(isComplete()){
            throw new OrderException(ErrorCode.ORDER_ALREADY_COMPLETED);
        }
        this.status = OrderStatus.SUCCEEDED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void failed() {
        if(isComplete()){
            throw new OrderException(ErrorCode.ORDER_ALREADY_COMPLETED);
        }
        this.status = OrderStatus.FAILED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void canceling() {
        if(!isSuccess()){
            throw new OrderException(ErrorCode.ORDER_NOT_SUCCEEDED);
        }
        this.status = OrderStatus.CANCELING;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void cancelSucceeded() {
        if(isCancelComplete()){
            throw new OrderException(ErrorCode.ORDER_ALREADY_CANCEL_COMPLETED);
        }
        this.status = OrderStatus.CANCEL_SUCCEEDED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void cancelFailed() {
        if(isCancelComplete()){
            throw  new OrderException(ErrorCode.ORDER_ALREADY_CANCEL_COMPLETED);
        }
        this.status = OrderStatus.CANCEL_FAILED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    private boolean isCancelComplete() {
        return OrderStatus.CANCEL_SUCCEEDED.equals(status)
                || OrderStatus.CANCEL_FAILED.equals(status);
    }

    private boolean isComplete() {
        return OrderStatus.SUCCEEDED.equals(status)
                || OrderStatus.FAILED.equals(status)
                || OrderStatus.CANCELING.equals(status)
                || OrderStatus.CANCEL_SUCCEEDED.equals(status);
    }

    private boolean isSuccess(){
        return OrderStatus.SUCCEEDED.equals(status);
    }
}