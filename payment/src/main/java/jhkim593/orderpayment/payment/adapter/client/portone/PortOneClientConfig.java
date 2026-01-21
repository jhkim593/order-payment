package jhkim593.orderpayment.payment.adapter.client.portone;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

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
        RestClient restClient = restClientBuilder
                .baseUrl(url)
                .requestFactory(clientHttpRequestFactory())
                .defaultHeader("Authorization", "PortOne " + secret)
                .defaultStatusHandler(HttpStatusCode::isError, new PortOneClientErrorHandler(objectMapper).handler())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(PortOneApi.class);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }
}
