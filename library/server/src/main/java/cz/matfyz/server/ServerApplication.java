package cz.matfyz.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author jachym.bartik
 */
@SpringBootApplication
// First load the default.properties file. Then it will be overriden by application.properties.
@PropertySource("classpath:default.properties")
// This is needed to prevent the application from trying to automatically connect to the MongoDB database.
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, Neo4jAutoConfiguration.class })
// This is needed to enable the @Async annotations.
@EnableAsync
// This is needed to enable the @Scheduled annotations.
@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean(name = "jobExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("AsyncJob-");
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(60);
        executor.afterPropertiesSet();

        return executor;
    }

}
