package jhkim593.orderpayment.payment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class PaymentClientConfig {

    @Value("${endpoints.payment-service:http://localhost:8082}")
    private String paymentServiceUrl;

    @Bean
    public PaymentClient paymentClient(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {

        RestClient restClient = restClientBuilder
                .baseUrl(paymentServiceUrl)
                .defaultStatusHandler(HttpStatusCode::isError, new PaymentClientErrorHandler(objectMapper).handler())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(PaymentClient.class);
    }
}