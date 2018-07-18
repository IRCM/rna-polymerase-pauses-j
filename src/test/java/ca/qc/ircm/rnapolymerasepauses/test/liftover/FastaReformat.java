package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FastaReformat {
  private static final int LINE_LENGHT = 50;

  /**
   * Reformat sc_sgd_gff_20091011.fna to 50 bases per lines.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    Path input = home.resolve("sc_sgd_gff_20091011.fna");
    Path output = home.resolve("sc_sgd_gff_20091011-50.fna");
    try (BufferedReader reader = Files.newBufferedReader(input);
        BufferedWriter writer = Files.newBufferedWriter(output)) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(">")) {
          while (buffer.length() > LINE_LENGHT) {
            writer.write(buffer.substring(0, LINE_LENGHT));
            writer.write("\n");
            buffer.delete(0, LINE_LENGHT);
          }
          if (buffer.length() > 0) {
            writer.write(buffer.toString());
            writer.write("\n");
            buffer.setLength(0);
          }
          writer.write(line);
          writer.write("\n");
        } else {
          buffer.append(line);
          if (buffer.length() > LINE_LENGHT) {
            writer.write(buffer.substring(0, LINE_LENGHT));
            writer.write("\n");
            buffer.delete(0, LINE_LENGHT);
          }
        }
      }
    }
  }
}
