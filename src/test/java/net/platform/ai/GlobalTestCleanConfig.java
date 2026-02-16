package net.platform.ai;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Profile("test")
@Configuration
public class GlobalTestCleanConfig {
    @PreDestroy
    public void tearDown() {
        System.out.println("\n=== [Docker Cleanup]: 正在物理销毁 Docker 容器 ===");
        System.out.flush();
        try {
            String cmd = "docker compose -f compose-test.yaml down -v && echo 'SUCCESS' > container_cleanup.status";
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Docker Cleanup]: " + line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[Docker Cleanup] <<< 销毁成功。");
            } else {
                System.err.println("[Docker Cleanup] <<< 销毁失败，错误码: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("[Docker Cleanup] error!" + e.getMessage());
        }
    }

//    @Bean
//    public FlywayMigrationStrategy cleanMigrationStrategy() {
//        return flyway -> {
//            flyway.clean();
//            flyway.migrate();
//        };
//    }
}
