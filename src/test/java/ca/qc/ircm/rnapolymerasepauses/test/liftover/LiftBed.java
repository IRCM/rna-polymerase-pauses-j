package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
    List<Path> inputs = Files.list(home).filter(p -> p.toString().endsWith("-ori.bed"))
        .collect(Collectors.toList());
    for (Path input : inputs) {
      String outputFilename = input.getFileName().toString().replace("-ori.bed", "-2lift.bed");
      lift(input, input.resolveSibling(outputFilename));
    }
  }

  private static void lift(Path input, Path output) throws IOException {
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
          } else if (start <= 126895 && end > 126895) {
            end--;
          }
          if (start == end) {
            System.err.println("Skipping unmapped " + columns[0] + ":" + columns[1] + "-"
                + columns[2] + " of input " + input);
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
