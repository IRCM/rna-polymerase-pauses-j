package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FastaToSizes {
  /**
   * Convers Fasta file to sizes.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    Path input = home.resolve("sacCer3.fsa");
    Path output = home.resolve("sacCer3-sizes.txt");
    List<String> outputLines = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(input)) {
      String line;
      String chromosome = null;
      long size = 0;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(">")) {
          if (chromosome != null) {
            outputLines.add(chromosome + "\t" + size);
          }
          chromosome = line.split("\\s")[0].substring(1);
          size = 0;
        } else {
          size += line.length();
        }
      }
      if (chromosome != null) {
        outputLines.add(chromosome + "\t" + size);
      }
    }
    Files.write(output, outputLines);
  }
}
