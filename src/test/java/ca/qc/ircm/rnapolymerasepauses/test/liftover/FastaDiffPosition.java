package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FastaDiffPosition {
  /**
   * Finds differences between sacCer2 genome and sc_sgd_gff_20091011.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    Path input = home.resolve("sacCer2.fa");
    Path input2 = home.resolve("sc_sgd_gff_20091011-50.fna");
    StringBuilder buffer = new StringBuilder();
    try (BufferedReader reader = Files.newBufferedReader(input)) {
      String line;
      boolean read = false;
      while ((line = reader.readLine()) != null) {
        if (line.equals(">chrX")) {
          read = true;
          continue;
        } else if (line.startsWith(">")) {
          read = false;
        }
        if (read) {
          buffer.append(line);
        }
      }
    }
    StringBuilder buffer2 = new StringBuilder();
    try (BufferedReader reader = Files.newBufferedReader(input2)) {
      String line;
      boolean read = false;
      while ((line = reader.readLine()) != null) {
        if (line.equals(">chrX")) {
          read = true;
          continue;
        } else if (line.startsWith(">")) {
          read = false;
        }
        if (read) {
          buffer2.append(line);
        }
      }
    }
    System.out.println(buffer.length());
    System.out.println(buffer2.length());
    int i = 0;
    for (; i < buffer2.length(); i++) {
      if (buffer.charAt(i) != buffer2.charAt(i)) {
        System.out.println("Mismatch at " + i);
        break;
      }
    }
    for (; i < buffer2.length(); i++) {
      if (buffer.charAt(i + 1) != buffer2.charAt(i)) {
        System.out.println("Mismatch at " + i);
        break;
      }
    }
  }
}
