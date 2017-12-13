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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * BED file transformations.
 */
@Component
public class BedTransform {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String COMMENT = "#";
  private static final Charset BED_CHARSET = StandardCharsets.UTF_8;

  /**
   * Sets the size of annotations in BED file.
   *
   * @param input
   *          BED to trim
   * @param output
   *          output
   * @param size
   *          new size for annotations
   * @throws IOException
   *           could not trim BED
   */
  public void setAnnotationSize(InputStream input, OutputStream output, int size)
      throws IOException {
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    try (
        ChunkReader reader =
            new ChunkReader(new BufferedReader(new InputStreamReader(input, BED_CHARSET)), 1000000);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, BED_CHARSET))) {
      List<String> chunk;
      while (!(chunk = reader.readChunk()).isEmpty()) {
        for (String line : chunk) {
          String[] columns = line.split(COLUMN_SEPARATOR, -1);
          if (browserPattern.matcher(columns[0]).matches()
              || trackPattern.matcher(columns[0]).matches() || columns[0].startsWith(COMMENT)) {
            writer.write(line);
            writer.write(LINE_SEPARATOR);
          } else {
            columns[2] = String.valueOf(Long.parseLong(columns[1]) + size);
            writer.write(
                Arrays.asList(columns).stream().collect(Collectors.joining(COLUMN_SEPARATOR)));
            writer.write(LINE_SEPARATOR);
          }
        }
      }
    }
  }
}
