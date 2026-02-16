
package net.platform.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ContextConfiguration(classes = SpringAiApplication.class)
public abstract class SpringAiApplicationTests {


}
