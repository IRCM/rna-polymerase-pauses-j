package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PausesToBed {
  /**
   * Converts pauses to BED.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    Path input = home.resolve("100225t_IP_DST1.txt");
    Path output = home.resolve("100225t_IP_DST1.bed");
    try (BufferedReader reader = Files.newBufferedReader(input);
        BufferedWriter writer = Files.newBufferedWriter(output)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        writer.write(columns[1]);
        writer.write("\t");
        writer.write(columns[2]);
        writer.write("\t");
        writer.write(String.valueOf(Integer.parseInt(columns[2]) + 1));
        writer.write("\t");
        writer.write(columns[0]);
        writer.write("__");
        writer.write(columns[3]);
        writer.write("__");
        writer.write(columns[4]);
        writer.write("__");
        writer.write(columns[5]);
        writer.write("__");
        writer.write(columns[6]);
        writer.write("\n");
      }
    }
  }
}
