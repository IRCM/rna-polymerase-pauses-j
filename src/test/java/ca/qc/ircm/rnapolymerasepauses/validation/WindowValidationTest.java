package ca.qc.ircm.rnapolymerasepauses.validation;

import static org.junit.Assert.fail;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import com.beust.jcommander.ParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class WindowValidationTest {
  private WindowValidation windowValidation = new WindowValidation();

  @Test
  public void validate() throws Throwable {
    try {
      windowValidation.validate("my-name", "20");
      // Success
    } catch (ParameterException e) {
      fail("ParameterException not expected");
    }
  }

  @Test
  public void validate_One() throws Throwable {
    try {
      windowValidation.validate("my-name", "1");
      // Success
    } catch (ParameterException e) {
      fail("ParameterException not expected");
    }
  }

  @Test
  public void validate_Zero() {
    try {
      windowValidation.validate("my-name", "0");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }

  @Test
  public void validate_Negative() {
    try {
      windowValidation.validate("my-name", "-1");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }

  @Test
  public void validate_Invalid() {
    try {
      windowValidation.validate("my-name", "a");
      fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
      // Success
    }
  }

  @Test
  public void validate_Double() {
    try {
      windowValidation.validate("my-name", "1.2");
      fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
      // Success
    }
  }
}
