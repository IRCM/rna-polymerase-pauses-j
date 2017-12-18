package ca.qc.ircm.bedtools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Move annotation parameters.
 */
@Parameters(
    separators = " =",
    commandNames = MoveAnnotationsCommand.MOVE_ANNOTATIONS_COMMAND,
    commandDescription = "Move annotations")
public class MoveAnnotationsCommand {
  public static final String MOVE_ANNOTATIONS_COMMAND = "moveannotations";

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
  @Parameter(names = { "-d", "-distance" }, description = "Distance", required = true)
  public Integer distance;
}
