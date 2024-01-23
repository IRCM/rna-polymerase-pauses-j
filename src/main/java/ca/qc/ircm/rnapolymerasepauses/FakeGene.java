package ca.qc.ircm.rnapolymerasepauses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import org.springframework.stereotype.Component;

/**
 * Generates a fake gene file covering all chromosomes.
 */
@Component
public class FakeGene {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String PLUS_PATTERN = "%s-P";
  private static final String MINUS_PATTERN = "%s-M";
  private static final String PLUS_STRAND = "+";
  private static final String MINUS_STRAND = "-";
  private static final String PROTEIN = "n/a";

  protected FakeGene() {
  }

  /**
   * Generates a fake gene file covering all chromosomes.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read chromosome sizes or write to output
   */
  public void fakeGene(FakeGeneCommand parameters) throws IOException {
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      int firstColumn = 585;
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        if (columns.length < 2) {
          throw new IllegalStateException("chromosome sizes is invalid");
        }
        writeFakeGene(writer, firstColumn, columns[0], Long.parseLong(columns[1]),
            parameters.padding, true);
        writeFakeGene(writer, firstColumn++, columns[0], Long.parseLong(columns[1]),
            parameters.padding, false);
      }
    }
  }

  private void writeFakeGene(Writer writer, int index, String chromosome, long size, int padding,
      boolean plusStrand) throws IOException {
    writer.write(String.valueOf(index));
    writer.write(COLUMN_SEPARATOR);
    writer.write(String.format(plusStrand ? PLUS_PATTERN : MINUS_PATTERN, chromosome));
    writer.write(COLUMN_SEPARATOR);
    writer.write(chromosome);
    writer.write(COLUMN_SEPARATOR);
    writer.write(plusStrand ? PLUS_STRAND : MINUS_STRAND);
    writer.write(COLUMN_SEPARATOR);
    writer.write(String.valueOf(padding));
    writer.write(COLUMN_SEPARATOR);
    writer.write(String.valueOf(size - padding));
    writer.write(COLUMN_SEPARATOR);
    writer.write(String.valueOf(padding));
    writer.write(COLUMN_SEPARATOR);
    writer.write(String.valueOf(size - padding));
    writer.write(COLUMN_SEPARATOR);
    writer.write(padding + ",");
    writer.write(COLUMN_SEPARATOR);
    writer.write((size - padding) + ",");
    writer.write(COLUMN_SEPARATOR);
    writer.write(PROTEIN);
    writer.write(LINE_SEPARATOR);
  }
}
