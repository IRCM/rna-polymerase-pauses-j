package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SgdGeneToTss {
  /**
   * Converts SGD Gene from UCSC to TSS.
   *
   * @param args
   *          not used
   * @throws Throwable
   *           cannot read one of the files
   */
  public static void main(String[] args) throws Throwable {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    convert(home.resolve("sgdGene.txt"), home.resolve("TSS-sacCer3.txt"));
  }

  private static void convert(Path input, Path output) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(input);
        BufferedWriter writer = Files.newBufferedWriter(output)) {
      writer.write("SEQ_NAME\tSTART\tEND\tSTRAND\tANNO_TAG\n");
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split("\t", -1);
        writer.write(columns[2]);
        writer.write("\t");
        writer.write(columns[4]);
        writer.write("\t");
        writer.write(columns[5]);
        writer.write("\t");
        writer.write(columns[3]);
        writer.write("\t");
        writer.write(columns[1]);
        writer.write("\n");
      }
    }
  }
}
