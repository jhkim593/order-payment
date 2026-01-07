package jhkim593.orderpayment.user.application.provided;

public interface CreditUpdater {
    void addCredit(Long userId, Long orderId, Integer amount);
    void cancelCredit(Long userId, Long orderId, Integer amount);
}