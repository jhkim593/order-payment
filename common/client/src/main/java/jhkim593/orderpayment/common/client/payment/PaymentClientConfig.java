package jhkim593.orderpayment.common.client.payment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.RedirectionInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jhkim593.orderpayment.common.client.FeignErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {

    @Value("${endpoints.payment-service:http://localhost:8082}")
    private String paymentServiceUrl;

    private final Feign.Builder feignBuilder;

    public PaymentClientConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.feignBuilder = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new FeignErrorDecoder(objectMapper))
                .responseInterceptor(new RedirectionInterceptor());
    }

    @Bean
    public PaymentClient paymentClient() {
        return feignBuilder.target(PaymentClient.class, paymentServiceUrl);
    }
}