
package net.platform.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles({"h2"})
@ContextConfiguration(classes = SpringAiApplication.class)
class SpringAiApplicationTests {

    @Test
    void contextLoads() {
    }

}
