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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Chromosome sizes parser.
 */
@Component
public class ChromosomeSizesParser {
  private static final String SEPARATOR = "\t";

  /**
   * Parsers chromosome sizes from file.
   *
   * @param file
   *          file
   * @return chromosome sizes
   * @throws IOException
   *           could not read file
   */
  public Map<String, Long> chromosomeSizes(Path file) throws IOException {
    Map<String, Long> sizes = new HashMap<>();
    try (BufferedReader reader = Files.newBufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(SEPARATOR, -1);
        if (columns.length < 2) {
          throw new IOException("Line " + line + " does not contain 2 columns");
        }
        String chromosome = columns[0];
        Long size = Long.valueOf(columns[1]);
        sizes.put(chromosome, size);
      }
    }
    return sizes;
  }
}
