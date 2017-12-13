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

package ca.qc.ircm.htstools.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChunkReader implements Closeable {
  private final BufferedReader reader;
  private final int chunkSize;

  public ChunkReader(BufferedReader reader, int chunkSize) {
    this.reader = reader;
    this.chunkSize = chunkSize;
  }

  /**
   * Returns up to chunk size lines.
   *
   * @return lines
   * @throws IOException
   *           could not read lines from reader
   */
  public List<String> readChunk() throws IOException {
    List<String> lines = new ArrayList<>();
    String line;
    while (lines.size() < chunkSize && (line = reader.readLine()) != null) {
      lines.add(line);
    }
    return lines;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
