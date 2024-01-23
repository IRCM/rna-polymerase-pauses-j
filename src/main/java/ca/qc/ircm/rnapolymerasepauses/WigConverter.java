package ca.qc.ircm.rnapolymerasepauses;

import ca.qc.ircm.rnapolymerasepauses.io.ChromosomeSizesParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Wig converter.
 */
@Component
public class WigConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "[\\t\\s]";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String VARIABLE_STEP_PATTERN = "^variableStep( .*)?$";
  private static final String CHROMOSOME_PATTERN = "chrom=(chr)?([\\w\\d]+)";
  private static final String COMMENT = "#";
  @Inject
  private ChromosomeSizesParser chromosomeSizesParser;

  protected WigConverter() {
  }

  protected WigConverter(ChromosomeSizesParser chromosomeSizesParser) {
    this.chromosomeSizesParser = chromosomeSizesParser;
  }

  /**
   * Converts WIG file to track file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read WIG or write to output
   */
  public void wigToTrack(WigToTrackCommand parameters) throws IOException {
    final Map<String, Long> sizes =
        chromosomeSizesParser.chromosomeSizes(parameters.chromosomeSizes);
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    Pattern variableStepPattern = Pattern.compile(VARIABLE_STEP_PATTERN);
    Pattern chromosomePattern = Pattern.compile(CHROMOSOME_PATTERN);
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      String chromosome;
      long position = 0;
      long size = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        if (browserPattern.matcher(line).matches() || trackPattern.matcher(line).matches()
            || line.startsWith(COMMENT)) {
          continue;
        } else if (variableStepPattern.matcher(line).matches()) {
          while (position < size) {
            writer.write("0");
            writer.write(LINE_SEPARATOR);
            position++;
          }
          Matcher matcher = chromosomePattern.matcher(line);
          if (matcher.find()) {
            writer.write(matcher.group(0));
            writer.write(LINE_SEPARATOR);
            chromosome = matcher.group(1) + matcher.group(2);
            if (!sizes.containsKey(chromosome)) {
              throw new IllegalStateException(
                  "Sizes file does not contain chromosome " + chromosome);
            }
            size = sizes.get(chromosome);
          }
          position = 0;
        } else {
          String[] columns = line.split(COLUMN_SEPARATOR, -1);
          if (columns.length != 2) {
            throw new IOException("WIG file does not contain 2 columns in line " + line);
          }
          long wigPosition = Long.parseLong(columns[0]);
          while (position < wigPosition) {
            writer.write("0");
            writer.write(LINE_SEPARATOR);
            position++;
          }
          writer.write(columns[1]);
          writer.write(LINE_SEPARATOR);
          position++;
        }
      }
      while (position < size) {
        writer.write("0");
        writer.write(LINE_SEPARATOR);
        position++;
      }
    }
  }
}
