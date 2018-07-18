package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BedToPauses {
  /**
   * Converts BED to pauses.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    convert(home.resolve("091113t_IP_WT-lift.bed"), home.resolve("091113t_IP_WT-lift.txt"));
    convert(home.resolve("100225t_IP_DST1-lift.bed"), home.resolve("100225t_IP_DST1-lift.txt"));
  }

  private static void convert(Path input, Path output) throws Throwable {
    try (BufferedReader reader = Files.newBufferedReader(input);
        BufferedWriter writer = Files.newBufferedWriter(output)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        String[] nameColumns = columns[3].split("__", -1);
        writer.write(nameColumns[0]);
        writer.write("\t");
        writer.write(columns[0]);
        writer.write("\t");
        writer.write(columns[1]);
        writer.write("\t");
        writer.write(nameColumns[1]);
        writer.write("\t");
        writer.write(nameColumns[2]);
        writer.write("\t");
        writer.write(nameColumns[3]);
        writer.write("\t");
        writer.write(nameColumns[4]);
        writer.write("\n");
      }
    }
  }
}
