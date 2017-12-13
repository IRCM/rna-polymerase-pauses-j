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

package ca.qc.ircm.htstools;

import ca.qc.ircm.htstools.io.ChunkReader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trims the end of a BED file.
 */
@Component
public class TrimBedEnd {
  /**
   * Trims the end of a BED file.
   *
   * @param input
   *          BED to trim
   * @param output
   *          output
   * @param parameters
   *          trim parameters
   * @throws IOException
   *           could not trim BED
   */
  public void trimBedEnd(InputStream input, OutputStream output, TrimBedEndParameters parameters)
      throws IOException {
    final String lineSeparator = System.getProperty("line.separator");
    try (
        ChunkReader reader = new ChunkReader(
            new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)), 1000000);
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      List<String> chunk;
      while (!(chunk = reader.readChunk()).isEmpty()) {
        for (String line : chunk) {
          String[] columns = line.split("\t", -1);
          columns[2] = String.valueOf(Long.parseLong(columns[1]) + parameters.sizeFromStart);
          writer.write(Arrays.asList(columns).stream().collect(Collectors.joining("\t")));
          writer.write(lineSeparator);
        }
      }
    }
  }
}
