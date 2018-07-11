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

package ca.qc.ircm.bedtools;

import ca.qc.ircm.bedtools.io.ChunkReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Asses RNA polymerase pause sites.
 */
@Component
public class AssesPauseSites {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String COMMENT = "#";
  private static final Charset BED_CHARSET = StandardCharsets.UTF_8;

  /**
   * Asses RNA polymerase pause sites.
   *
   * @param output
   *          output
   * @param parameters
   *          asses pause sites parameters
   * @throws IOException
   *           could not read or write BED
   */
  public void assesPauseSites(OutputStream output, AssesPauseSitesCommand parameters)
      throws IOException {
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    try (
        ChunkReader reader =
            new ChunkReader(Files.newBufferedReader(parameters.input, BED_CHARSET), 1000000);
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
            writer.write(
                Arrays.asList(columns).stream().collect(Collectors.joining(COLUMN_SEPARATOR)));
            writer.write(LINE_SEPARATOR);
          }
        }
      }
    }
  }
}
