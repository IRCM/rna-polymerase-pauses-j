package ca.qc.ircm.rnapolymerasepauses.validation;

import static org.junit.Assert.fail;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import com.beust.jcommander.ParameterException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class FileExistsValidationTest {
  private FileExistsValidation fileExistsValidation = new FileExistsValidation();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void validate_Exists() throws Throwable {
    Path path = temporaryFolder.newFile("exists.txt").toPath();

    try {
      fileExistsValidation.validate("my-name", path.toString());
      // Success
    } catch (ParameterException e) {
      fail("ParameterException not expected");
    }
  }

  @Test
  public void validate_NotExists() {
    try {
      fileExistsValidation.validate("my-name", "notexists.txt");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }
}
