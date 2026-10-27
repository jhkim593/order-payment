package jhkim593.orderpayment.common.observability;

import io.micrometer.common.KeyValues;
import org.springframework.kafka.support.micrometer.KafkaRecordSenderContext;
import org.springframework.kafka.support.micrometer.KafkaTemplateObservation.DefaultKafkaTemplateObservationConvention;

public class EventTypeTemplateObservationConvention extends DefaultKafkaTemplateObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(KafkaRecordSenderContext context) {
        return super.getLowCardinalityKeyValues(context)
                .and(EventTypeObservationConvention.KEY, EventTypeObservationConvention.eventType(context.getRecord().headers()));
    }
}
