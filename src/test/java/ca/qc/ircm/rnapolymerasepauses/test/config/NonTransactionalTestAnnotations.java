package ca.qc.ircm.rnapolymerasepauses.test.config;

import ca.qc.ircm.rnapolymerasepauses.Main;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
public @interface NonTransactionalTestAnnotations {

}
