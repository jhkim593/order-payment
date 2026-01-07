package jhkim593.orderpayment.payment.adapter.client.portone;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.*;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class PortOneClientConfig {

    @Value("${portone.api.url}")
    private String url;

    @Value("${portone.api.secret}")
    private String secret;

    private Feign.Builder feignBuilder;

    @Bean
    public PortOneApi portOneClient() {
        return feignBuilder.target(PortOneApi.class, url);
    }

    public PortOneClientConfig(ObjectMapper objectMapper, PortOneClientErrorDecoder errorDecoder) {
        Request.Options options = new Request.Options(
                10, TimeUnit.SECONDS,
                60, TimeUnit.SECONDS
                ,true
        );

        this.feignBuilder = Feign.builder()
                .client(new OkHttpClient())
                .options(options)
                .retryer(Retryer.NEVER_RETRY)
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .requestInterceptor(requestInterceptor())
                .errorDecoder(errorDecoder)
                .responseInterceptor(new RedirectionInterceptor());
    }

    private RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "PortOne "+ secret);
        };
    }
}
