package ca.qc.ircm.rnapolymerasepauses.io;

import ca.qc.ircm.rnapolymerasepauses.Pause;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Pause reader.
 */
public class PauseReader implements Closeable {
  private static final String MARKER = ">";
  private static final String SEPARATOR = "_";
  private final BufferedReader reader;
  private String line = null;

  /**
   * Create pause reader.
   *
   * @param reader
   *          underling reader
   */
  public PauseReader(BufferedReader reader) {
    this.reader = reader;
  }

  /**
   * Reads next pause.
   *
   * @return returns next pause found in reader
   * @throws IOException
   *           could not read reader
   */
  public Pause readPause() throws IOException {
    if (line == null) {
      line = reader.readLine();
    }
    while (line != null && !line.startsWith(MARKER)) {
      // Skip potential comments.
      line = reader.readLine();
    }
    if (line == null) {
      return null;
    }
    String[] columns = line.substring(MARKER.length(), line.length()).split(SEPARATOR);
    Pause pause = new Pause();
    pause.name = columns[0];
    pause.chromosome = columns[1];
    pause.position = Integer.parseInt(columns[2]);
    pause.normalizedReads = Double.parseDouble(columns[3]);
    pause.foldsAboveAverage = Double.parseDouble(columns[4]);
    pause.beginningReads = Double.parseDouble(columns[5]);
    StringBuilder builder = new StringBuilder();
    line = reader.readLine();
    while (line != null && !line.startsWith(MARKER)) {
      builder.append(line);
      line = reader.readLine();
    }
    pause.sequence = builder.toString();
    return pause;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
