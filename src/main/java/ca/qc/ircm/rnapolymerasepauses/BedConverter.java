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

import ca.qc.ircm.rnapolymerasepauses.io.ChromosomeSizesParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * BED converter.
 */
@Component
public class BedConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "[\\t\\s]";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String COMMENT = "#";
  private static final String CHROMOSOME_PATTERN = "chrom=%s";
  @Inject
  private ChromosomeSizesParser chromosomeSizesParser;

  protected BedConverter() {
  }

  protected BedConverter(ChromosomeSizesParser chromosomeSizesParser) {
    this.chromosomeSizesParser = chromosomeSizesParser;
  }

  /**
   * Converts BED file to track file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read BED or write to output
   */
  public void bedToTrack(BedToTrackCommand parameters) throws IOException {
    final Map<String, Long> sizes =
        chromosomeSizesParser.chromosomeSizes(parameters.chromosomeSizes);
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      String chromosome = "not a valid chromosome";
      long position = 0;
      long size = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        if (browserPattern.matcher(line).matches() || trackPattern.matcher(line).matches()
            || line.startsWith(COMMENT)) {
          continue;
        }
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        if (columns.length < 4) {
          throw new IllegalStateException("BED file does not contain score for all lines");
        }
        if (!chromosome.equals(columns[0])) {
          while (position < size) {
            writer.write("0");
            writer.write(LINE_SEPARATOR);
            position++;
          }
          chromosome = columns[0];
          position = 0;
          writer.write(String.format(CHROMOSOME_PATTERN, chromosome));
          writer.write(LINE_SEPARATOR);
          if (!sizes.containsKey(chromosome)) {
            throw new IllegalStateException("Sizes file does not contain chromosome " + chromosome);
          }
          size = sizes.get(chromosome);
        }

        long start = Math.min(Long.parseLong(columns[1]), size);
        long end = Math.min(Long.parseLong(columns[2]), size + 1);
        double score;
        if (columns.length == 4) {
          score = Double.parseDouble(columns[3]);
        } else {
          score = Double.parseDouble(columns[4]);
        }
        while (position < start) {
          writer.write("0");
          writer.write(LINE_SEPARATOR);
          position++;
        }
        while (position < end) {
          writer.write(String.valueOf(score));
          writer.write(LINE_SEPARATOR);
          position++;
        }
      }
      while (position < size) {
        writer.write("0");
        writer.write(LINE_SEPARATOR);
        position++;
      }
    }
  }
}
