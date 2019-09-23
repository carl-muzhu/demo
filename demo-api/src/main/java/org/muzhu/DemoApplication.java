package org.muzhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

//    @Bean
//    public EmbeddedServletContainerCustomizer containerCustomizer() {
//        return container -> {
//            //单位为S
//            container.setSessionTimeout(1 * 24 * 60 * 60);//1 * 24 * 60 * 60
//        };
//    }

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("scheduled-thread-");
        return scheduler;
    }
}
