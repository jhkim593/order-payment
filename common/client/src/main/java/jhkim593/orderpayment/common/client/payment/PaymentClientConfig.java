package jhkim593.orderpayment.common.client.payment;

import feign.Feign;
import feign.RedirectionInterceptor;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jhkim593.orderpayment.common.client.FeignErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {

    @Value("${endpoints.payment-service:http://localhost:8080}")
    private String paymentServiceUrl;

    private final Feign.Builder feignBuilder;

    public PaymentClientConfig() {
        this.feignBuilder = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new FeignErrorDecoder())
                // 1초 간격으로 시작해 최대 3초 간격으로 점점 증가하며 최대 3번 재시도
                .retryer(new Retryer.Default(1000L, 3000L, 3))
                .responseInterceptor(new RedirectionInterceptor());
    }

    @Bean
    public PaymentClient paymentClient() {
        return feignBuilder.target(PaymentClient.class, paymentServiceUrl);
    }
}