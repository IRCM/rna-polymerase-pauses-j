package ca.qc.ircm.bedtools.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates that file exists for specified parameter.
 */
public class FileExistsValidation implements IParameterValidator {
  @Override
  public void validate(String name, String value) throws ParameterException {
    Path path = Paths.get(value);
    if (!Files.exists(path)) {
      throw new ParameterException("File " + value + " does not exists for parameter " + name);
    }
  }
}
