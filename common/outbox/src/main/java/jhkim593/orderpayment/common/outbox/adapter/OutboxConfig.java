package jhkim593.orderpayment.common.outbox.adapter;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EnableAsync
@Configuration
@EnableScheduling
@ComponentScan("jhkim593.orderpayment.common.outbox")
@EntityScan("jhkim593.orderpayment.common.outbox")
@EnableJpaRepositories("jhkim593.orderpayment.common.outbox")
public class OutboxConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, String> outboxKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
        //otel observation
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

    @Bean
    public Executor outboxPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("outbox-pub-");
        //otel context
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return executor;
    }

    @Bean
    public TaskScheduler outboxPublishPendingEventExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return scheduler;
    }
}
