package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LiftBed {
  /**
   * Lifts positions for chromosome X between sc_sgd_gff_20091011 and sacCer2.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    Path input = home.resolve("091113t_IP_WT-lift.bed");
    Path output = home.resolve("091113t_IP_WT-lift2.bed");
    try (BufferedReader reader = Files.newBufferedReader(input);
        BufferedWriter writer = Files.newBufferedWriter(output)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        if (columns[0].equals("chrX")) {
          long start = Long.parseLong(columns[1]);
          long end = Long.parseLong(columns[2]);
          if (start > 126895) {
            start--;
            end--;
            System.err.println("Increase " + columns[0] + ":" + columns[1] + "-" + columns[2]);
          } else if (start <= 126895 && end > 126895) {
            end--;
            System.err.println("Increase end " + columns[0] + ":" + columns[1] + "-" + columns[2]);
          }
          if (start == end) {
            System.err
                .println("Skipping unmapped " + columns[0] + ":" + columns[1] + "-" + columns[2]);
            continue;
          } else {
            writer.write(columns[0]);
            writer.write("\t");
            writer.write(String.valueOf(start));
            writer.write("\t");
            writer.write(String.valueOf(end));
            for (int i = 3; i < columns.length; i++) {
              writer.write("\t");
              writer.write(columns[i]);
            }
            writer.write("\n");
          }
        } else {
          writer.write(line);
          writer.write("\n");
        }
      }
    }
  }
}
