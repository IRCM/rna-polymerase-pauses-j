package ca.qc.ircm.htstools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Command line parameters.
 */
@Parameters
public class MainCommand {
  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
}
