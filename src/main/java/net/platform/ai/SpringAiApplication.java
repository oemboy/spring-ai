
package net.platform.ai;

import net.platform.center.cfg.http5.EnableCustomHttpClient;
import net.platform.center.cfg.redis.EnableCustomRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCustomRedis
@EnableCustomHttpClient
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

}
