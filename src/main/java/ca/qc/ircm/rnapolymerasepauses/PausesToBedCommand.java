package ca.qc.ircm.rnapolymerasepauses;

import ca.qc.ircm.rnapolymerasepauses.validation.FileExistsValidation;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts pauses file to a BED file.
 */
@Parameters(
    separators = " =",
    commandNames = PausesToBedCommand.COMMAND,
    commandDescription = "Converts pauses file to BED file")
public class PausesToBedCommand {
  public static final String COMMAND = "pauses2bed";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
  @Parameter(
      names = { "-i", "--input" },
      description = "Input file. Defaults to system input for piping",
      converter = PathConverter.class,
      validateWith = FileExistsValidation.class)
  public Path input;
  @Parameter(
      names = { "-t", "--tss" },
      description = "TSS file",
      required = true,
      converter = PathConverter.class,
      validateWith = FileExistsValidation.class)
  public Path tss;
  @Parameter(
      names = { "-o", "--output" },
      description = "Output file. Defaults to system output for piping",
      converter = PathConverter.class)
  public Path output;

  /**
   * Returns input reader, falls back to <code>System.in</code>.
   *
   * @return input reader
   * @throws IOException
   *           could not created a reader for input
   */
  public BufferedReader reader() throws IOException {
    if (input != null) {
      return Files.newBufferedReader(input);
    } else {
      return new BufferedReader(new InputStreamReader(System.in, CHARSET));
    }
  }

  /**
   * Returns output writer, falls back to <code>System.out</code>.
   *
   * @return output writer
   * @throws IOException
   *           could not created a writer for output
   */
  public BufferedWriter writer() throws IOException {
    if (output != null) {
      return Files.newBufferedWriter(output);
    } else {
      return new BufferedWriter(new OutputStreamWriter(System.out, CHARSET));
    }
  }
}
