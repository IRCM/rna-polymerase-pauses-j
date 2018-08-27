/*
 * Copyright (c) 2017 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
