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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pauses converter.
 */
@Component
public class PausesConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String SEQUENCE_NAME_SEPARATOR = "_";
  private static final String SEQUENCE_NAME_MARKER = ">";
  private static final String SEPARATOR = "\t";
  private static final Logger logger = LoggerFactory.getLogger(PausesConverter.class);

  protected PausesConverter() {
  }

  /**
   * Converts pauses file to BED file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read pauses file or write to output
   */
  public void pausesToBed(PausesToBedCommand parameters) throws IOException {
    Map<String, Gene> genes = parseTss(parameters.tss).stream()
        .collect(Collectors.toMap(gene -> gene.name, gene -> gene));
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(SEQUENCE_NAME_MARKER)) {
          String[] columns =
              line.substring(SEQUENCE_NAME_MARKER.length()).split(SEQUENCE_NAME_SEPARATOR, -1);
          if (columns.length > 0) {
            String name = columns[0];
            Gene gene = genes.get(name);
            if (gene == null) {
              logger.warn("Gene {} could not be found in TSS file", name);
            } else {
              writer.write(columns[1]);
              writer.write(SEPARATOR);
              long distance = Long.parseLong(columns[2]);
              long start = gene.start + distance;
              writer.write(String.valueOf(start));
              writer.write(SEPARATOR);
              writer.write(String.valueOf(start + 1));
              writer.write(SEPARATOR);
              writer.write(name);
              writer.write(SEPARATOR);
              writer.write(columns[4]);
              writer.write(LINE_SEPARATOR);
            }
          }
        }
      }
    }
  }

  private List<Gene> parseTss(Path tss) throws IOException {
    List<Gene> genes = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(tss)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(SEPARATOR, -1);
        Gene gene = new Gene();
        gene.start = Long.parseLong(columns[1]);
        gene.name = columns[4];
        genes.add(gene);
      }
    }
    return genes;
  }

  /**
   * Converts pauses file to tab delimited file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read pauses file or write to output
   */
  public void pausesToTabs(PausesToTabsCommand parameters) throws IOException {
    try (BufferedReader reader = parameters.reader(); BufferedWriter writer = parameters.writer()) {
      boolean first = true;
      String line;
      StringBuilder sequence = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(SEQUENCE_NAME_MARKER)) {
          if (!first && sequence.length() > 0) {
            writer.write(SEPARATOR);
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
              writer.write(SEPARATOR);
              writer.write(columns[i]);
            }
          }
        } else {
          sequence.append(line);
        }
      }
      if (!first && sequence.length() > 0) {
        writer.write(SEPARATOR);
        writer.write(sequence.toString());
      }
    }
  }

  private static class Gene {
    String name;
    long start;
  }
}
