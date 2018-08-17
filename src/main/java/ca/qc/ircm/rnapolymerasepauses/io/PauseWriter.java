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
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Pause writer.
 */
public class PauseWriter implements Closeable {
  private static final String MARKER = ">";
  private static final String SEPARATOR = "_";
  private static final int SEQUENCE_LENGHT_PER_LINE = 80;
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private NumberFormat normalizedReadsFormat = new DecimalFormat("0.#####");
  private NumberFormat foldsAboveAverageFormat = new DecimalFormat("0.################");
  private NumberFormat beginningReadsFormat = new DecimalFormat("0.##########");
  private final Writer writer;

  /**
   * Create pause writer.
   *
   * @param writer
   *          underling writer
   */
  public PauseWriter(Writer writer) {
    this.writer = writer;
  }

  /**
   * Writes pause in writer.
   *
   * @param pause
   *          pause
   * @throws IOException
   *           could not write to writer
   */
  public void writePause(Pause pause) throws IOException {
    writer.write(MARKER);
    writer.write(pause.name);
    writer.write(SEPARATOR);
    writer.write(pause.chromosome);
    writer.write(SEPARATOR);
    writer.write(String.valueOf(pause.position));
    writer.write(SEPARATOR);
    writer.write(normalizedReadsFormat.format(pause.normalizedReads));
    writer.write(SEPARATOR);
    writer.write(foldsAboveAverageFormat.format(pause.foldsAboveAverage));
    writer.write(SEPARATOR);
    writer.write(beginningReadsFormat.format(pause.beginningReads));
    writer.write(LINE_SEPARATOR);
    if (pause.sequence != null && pause.sequence.length() != 0) {
      String sequence = pause.sequence;
      while (sequence.length() > SEQUENCE_LENGHT_PER_LINE) {
        writer.write(sequence.substring(0, SEQUENCE_LENGHT_PER_LINE));
        writer.write(LINE_SEPARATOR);
        sequence = sequence.substring(SEQUENCE_LENGHT_PER_LINE);
      }
      if (sequence.length() > 0) {
        writer.write(sequence);
        writer.write(LINE_SEPARATOR);
      }
    }
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
