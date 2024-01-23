package ca.qc.ircm.rnapolymerasepauses.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Chromosome sizes parser.
 */
@Component
public class ChromosomeSizesParser {
  private static final String SEPARATOR = "\t";

  /**
   * Parsers chromosome sizes from file.
   *
   * @param file
   *          file
   * @return chromosome sizes
   * @throws IOException
   *           could not read file
   */
  public Map<String, Long> chromosomeSizes(Path file) throws IOException {
    Map<String, Long> sizes = new HashMap<>();
    try (BufferedReader reader = Files.newBufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(SEPARATOR, -1);
        if (columns.length < 2) {
          throw new IOException("Line " + line + " does not contain 2 columns");
        }
        String chromosome = columns[0];
        Long size = Long.valueOf(columns[1]);
        sizes.put(chromosome, size);
      }
    }
    return sizes;
  }
}
