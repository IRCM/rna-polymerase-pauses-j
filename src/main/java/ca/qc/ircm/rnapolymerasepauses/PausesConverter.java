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

package ca.qc.ircm.rnapolymerasepauses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * Pauses converter.
 */
@Component
public class PausesConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String SEQUENCE_NAME_SEPARATOR = "_";
  private static final String SEQUENCE_NAME_MARKER = ">";
  private static final String OUTPUT_SEPARATOR = "\t";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  protected PausesConverter() {
  }

  /**
   * Converts pauses file to tab delimited file.
   *
   * @param input
   *          pauses file
   * @param output
   *          output
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read pauses file or write to output
   */
  public void pausesToTabs(InputStream input, OutputStream output, PausesToTabsCommand parameters)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, CHARSET));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, CHARSET))) {
      boolean first = true;
      String line;
      StringBuilder sequence = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(SEQUENCE_NAME_MARKER)) {
          if (!first && sequence.length() > 0) {
            writer.write(OUTPUT_SEPARATOR);
            writer.write(sequence.toString());
          }
          sequence.setLength(0);
          if (!first) {
            writer.write(LINE_SEPARATOR);
          }
          first = false;
          String[] columns =
              line.substring(SEQUENCE_NAME_MARKER.length()).split(SEQUENCE_NAME_SEPARATOR, -1);
          if (columns.length > 0) {
            writer.write(columns[0]);
            for (int i = 1; i < columns.length; i++) {
              writer.write(OUTPUT_SEPARATOR);
              writer.write(columns[i]);
            }
          }
        } else {
          sequence.append(line);
        }
      }
      if (!first && sequence.length() > 0) {
        writer.write(OUTPUT_SEPARATOR);
        writer.write(sequence.toString());
      }
    }
  }
}
