
package net.platform.ai.base;

import lombok.extern.slf4j.Slf4j;
import net.platform.ai.SpringAiApplicationTests;
import net.platform.center.bean.Dictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bing zi
 * @since 2021/8/26 16:45
 */

@Slf4j
@SpringBootTest
class SimpleRedisTests extends SpringAiApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Dictionary> redisTemplate;

    @Test
    void redis() {
        stringRedisTemplate.opsForValue().set("CD:date", "20210828");
        assertEquals("20210828", stringRedisTemplate.opsForValue().get("CD:date"));
    }

    @Test
    void redisCustomRedis() {

        Dictionary dictionary = new Dictionary();
        dictionary.setDictId("killer");
        dictionary.setDictName("zhangsan");
        dictionary.setGroupId("org");
        dictionary.setSortOrder(1);
        dictionary.setState(1);
        dictionary.setStateDate("2021/08/26");
        dictionary.setMemo("test");
        redisTemplate.opsForValue().set("CD:dict", dictionary);
        assertEquals(dictionary, redisTemplate.opsForValue().get("CD:dict"));
    }

    @Test
    void checkDocker() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "compose", "version");

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Docker Check: {}", line);
            }
        }

        int exitCode = process.waitFor();
        log.info("Exited with code: {}", exitCode);
    }

}
