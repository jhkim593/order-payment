package jhkim593.orderpayment.common.core.snowflake;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {
    @Bean
    public IdGenerator dbIdGenerator() {
        return new Snowflake();
    }
}