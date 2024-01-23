package ca.qc.ircm.rnapolymerasepauses.validation;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 * Validates that window is <code>&gt;= 1</code>.
 */
public class WindowValidation implements IParameterValidator {
  @Override
  public void validate(String name, String value) throws ParameterException {
    int valueAsInt = Integer.parseInt(value);
    if (valueAsInt < 1) {
      throw new ParameterException("Parameter " + name + " should be >= 1 (found " + value + ")");
    }
  }
}
