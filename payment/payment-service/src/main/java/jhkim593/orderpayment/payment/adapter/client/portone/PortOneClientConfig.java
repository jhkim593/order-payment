package jhkim593.orderpayment.payment.adapter.client.portone;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.application.required.PortOneQueryApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class PortOneClientConfig {

    @Value("${portone.api.url}")
    private String url;

    @Value("${portone.api.secret}")
    private String secret;

    @Bean
    public PortOneApi portOneClient(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        return createClient(PortOneApi.class, objectMapper, restClientBuilder, Duration.ofSeconds(10), Duration.ofSeconds(60));
    }

    @Bean
    public PortOneQueryApi portOneQueryClient(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        return createClient(PortOneQueryApi.class, objectMapper, restClientBuilder, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private <T> T createClient(Class<T> type, ObjectMapper objectMapper, RestClient.Builder restClientBuilder,
                               Duration connectTimeout, Duration readTimeout) {
        RestClient restClient = restClientBuilder.clone()
                .baseUrl(url)
                .requestFactory(clientHttpRequestFactory(connectTimeout, readTimeout))
                .defaultHeader("Authorization", "PortOne " + secret)
                .defaultStatusHandler(HttpStatusCode::isError, new PortOneClientErrorHandler(objectMapper).handler())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(type);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
