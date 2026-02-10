
package net.platform.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import net.platform.center.cfg.http5.EnableCustomHttpClient;

@SpringBootApplication
@EnableCustomHttpClient
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

    @Bean
    @Profile("h2")
    public FlywayMigrationStrategy cleanMigrationStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

}
