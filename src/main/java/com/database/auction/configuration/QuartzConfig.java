package com.database.auction.configuration;


import com.database.auction.configuration.AutowiringSpringBeanJobFactory;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Slf4j
@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public AutowiringSpringBeanJobFactory jobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            AutowiringSpringBeanJobFactory jobFactory
            /* you can list triggers here if you have any static ones */) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        // if you have dataSource or other properties, set them here
        return factory;
    }

    // If you ever need to inject the Scheduler directly:
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) {
        return factory.getScheduler();
    }

    @Bean
    public ApplicationRunner verifyQuartzUp(Scheduler scheduler) {
        return args -> {
            log.info("✅ Quartz scheduler started? {}", scheduler.isStarted());
        };
    }

}
