package ca.qc.ircm.htstools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * Set annotation size parameters.
 */
@Parameters(
    separators = " =",
    commandNames = SetAnnotationSizeCommand.SET_ANNOTATION_SIZE_COMMAND,
    commandDescription = "Set annotation size")
public class SetAnnotationSizeCommand {
  public static final String SET_ANNOTATION_SIZE_COMMAND = "setannotationsize";

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
  @Parameter(
      names = { "-s", "-size" },
      description = "Annotation size",
      required = true,
      validateWith = PositiveInteger.class)
  public Integer size;
}
