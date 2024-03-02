package org.transitclock.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.transitclock.ApplicationProperties;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.core.avl.AvlReportProcessor.AvlReportProcessingTask;
import org.transitclock.core.avl.AvlReportProcessorQueue;
import org.transitclock.utils.ExceptionHandlingAsyncTaskExecutor;
import org.transitclock.utils.threading.NamedThreadFactory;

import java.util.concurrent.*;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
@Profile("!testdev & !testprod")
public class AsyncConfiguration implements AsyncConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final TaskExecutionProperties taskExecutionProperties;

    public AsyncConfiguration(TaskExecutionProperties taskExecutionProperties) {
        this.taskExecutionProperties = taskExecutionProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Bean(name = "avlExecutingThreadPool")
    public Executor avlExecutingThreadPool(ApplicationProperties properties) {
        ApplicationProperties.Avl avlProperties = properties.getAvl();
        final int numberThreads = avlProperties.getNumThreads();
        final int maxAVLQueueSize = avlProperties.getQueueSize();

        AvlReportProcessorQueue workQueue = new AvlReportProcessorQueue(maxAVLQueueSize);
        NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory("avlClient");

        // Called when queue fills up
        RejectedExecutionHandler rejectedHandler = (arg0, arg1) -> {
            logger.error("Rejected AVL report {} in AvlExecutor for agencyId={}. The work queue with capacity {}  must be full.",
                ((AvlReportProcessingTask) arg0).getAvlReport(),
                AgencyConfig.getAgencyId(),
                maxAVLQueueSize);
        };

        logger.info("Starting AvlExecutor for directly handling AVL reports via a queue instead of JMS. maxAVLQueueSize={} and numberThreads={}", maxAVLQueueSize, numberThreads);

        return new ThreadPoolExecutor(1,
            numberThreads,
            1,
            TimeUnit.HOURS,
            (BlockingQueue) workQueue,
            avlClientThreadFactory,
            rejectedHandler);

    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
