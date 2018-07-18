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
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * SGD gene converter.
 */
@Component
public class SgdGeneConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String NEGATIVE_STRAND = "-";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  protected SgdGeneConverter() {
  }

  /**
   * Converts SGD gene to TSS file.
   *
   * @param input
   *          SGD gene file
   * @param output
   *          output
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read SGD gene file or write to output
   */
  public void sgdGeneToTss(InputStream input, OutputStream output, SgdGeneToTssCommand parameters)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, CHARSET));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, CHARSET))) {
      writer.write("SEQ_NAME");
      writer.write(COLUMN_SEPARATOR);
      writer.write("START");
      writer.write(COLUMN_SEPARATOR);
      writer.write("END");
      writer.write(COLUMN_SEPARATOR);
      writer.write("STRAND");
      writer.write(COLUMN_SEPARATOR);
      writer.write("ANNO_TAG");
      writer.write(LINE_SEPARATOR);
      StringWriter negativeStrandWriter = new StringWriter();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        Writer strandWriter = columns[3].equals(NEGATIVE_STRAND) ? negativeStrandWriter : writer;
        strandWriter.write(columns[2]);
        strandWriter.write(COLUMN_SEPARATOR);
        strandWriter.write(columns[4]);
        strandWriter.write(COLUMN_SEPARATOR);
        strandWriter.write(columns[5]);
        strandWriter.write(COLUMN_SEPARATOR);
        strandWriter.write(columns[3]);
        strandWriter.write(COLUMN_SEPARATOR);
        strandWriter.write(columns[1]);
        strandWriter.write(LINE_SEPARATOR);
      }
      writer.write(negativeStrandWriter.toString());
    }
  }
}
