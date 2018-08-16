package ca.qc.ircm.rnapolymerasepauses.test.liftover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TabsToPauses {
  public static void main(String[] args) throws IOException {
    Path home = Paths.get(System.getProperty("user.home")).resolve("Downloads");
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:.*/(WT|Y1F).*\\.txt");
    List<Path> files =
        Files.list(home).filter(file -> matcher.matches(file)).collect(Collectors.toList());
    for (Path file : files) {
      String outputFilename = file.getFileName().toString();
      outputFilename = outputFilename.substring(0, outputFilename.indexOf(".txt"));
      outputFilename += ".csv";
      Path output = file.resolveSibling(outputFilename);
      try (BufferedReader reader = Files.newBufferedReader(file);
          BufferedWriter writer = Files.newBufferedWriter(output)) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] columns = line.split("\t");
          writer.write(columns[0]);
          for (int i = 1; i < columns.length - 1; i++) {
            writer.write("_");
            writer.write(columns[i]);
          }
          writer.write("\n");
          writer.write(columns[columns.length - 1]);
          writer.write("\n");
        }
      }
    }
  }
}
