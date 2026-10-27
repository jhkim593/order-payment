package jhkim593.orderpayment.common.observability;

import io.micrometer.common.KeyValues;
import jhkim593.orderpayment.common.core.event.EventType;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.micrometer.KafkaListenerObservation.DefaultKafkaListenerObservationConvention;
import org.springframework.kafka.support.micrometer.KafkaRecordReceiverContext;

import java.nio.charset.StandardCharsets;

public class EventTypeObservationConvention extends DefaultKafkaListenerObservationConvention {
    static final String KEY = "event.type";

    @Override
    public KeyValues getLowCardinalityKeyValues(KafkaRecordReceiverContext context) {
        return super.getLowCardinalityKeyValues(context).and(KEY, eventType(context.getRecord().headers()));
    }

    static String eventType(Headers headers) {
        Header header = headers.lastHeader(EventType.HEADER);
        return header == null ? "none" : new String(header.value(), StandardCharsets.UTF_8);
    }
}
