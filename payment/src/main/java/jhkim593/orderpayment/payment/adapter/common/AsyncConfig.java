package jhkim593.orderpayment.payment.adapter.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //동시 동작 쓰레드수
        executor.setCorePoolSize(10);
        //core 사이즈 초과시 쓰레드 생성 요청이 해당 사이즈로 queue에 생성
        executor.setQueueCapacity(50);
        //queue에 초과시 해당 사이즈만큼 쓰레드를 만들어 처리
        executor.setMaxPoolSize(30);
        executor.setThreadNamePrefix("asyncExecutor");
        executor.setKeepAliveSeconds(3600);
        executor.initialize();
        return executor;
    }
}
