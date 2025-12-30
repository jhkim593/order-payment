package jhkim593.orderpayment.order.adapter.mq.kafka;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.Topic;
import jhkim593.orderpayment.common.core.event.payload.EventPayload;
import jhkim593.orderpayment.common.core.event.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.common.core.event.payload.PaymentSuccessEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandlerFactory;
import jhkim593.orderpayment.order.application.OrderUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final EventHandlerFactory eventHandlerFactory;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @KafkaListener(
            topics = Topic.ORDER,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentEvent(String message) {
        try {
            log.info("Received payment event: {}", message);

            EventData<EventPayload> eventData = EventData.fromJson(message);
            eventHandlerFactory.get(eventData.getType()).handle(eventData);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", message, e);
            // 재시도를 위해 acknowledge 하지 않음 (또는 DLQ로 보내기)
            throw e;
        }
    }

    @DltHandler
    public void handleDlt(String message,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // DLQ로 들어온 메시지에 대한 최종 처리 로직
        log.error("DLT Received :: Topic: {}, Event: {}", topic, event);
        // e.g., 데이터베이스에 실패 로그 기록, 관리자에게 알림 발송 등
        failedEventRepository.save(new FailedEvent(topic, event));
    }
}
