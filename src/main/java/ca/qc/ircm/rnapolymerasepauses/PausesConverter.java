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

import ca.qc.ircm.rnapolymerasepauses.io.PauseReader;
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
    try (PauseReader reader = new PauseReader(parameters.reader());
        BufferedWriter writer = parameters.writer()) {
      Pause pause;
      while ((pause = reader.readPause()) != null) {
        Gene gene = genes.get(pause.name);
        if (gene == null) {
          logger.warn("Gene {} could not be found in TSS file", pause.name);
        } else {
          writer.write(pause.chromosome);
          writer.write(SEPARATOR);
          long distance = pause.position;
          long start = gene.start + distance;
          writer.write(String.valueOf(start));
          writer.write(SEPARATOR);
          writer.write(String.valueOf(start + 1));
          writer.write(SEPARATOR);
          writer.write(pause.name);
          writer.write(SEPARATOR);
          writer.write(String.valueOf(pause.foldsAboveAverage));
          writer.write(LINE_SEPARATOR);
        }
      }
    }
  }

  private List<Gene> parseTss(Path tss) throws IOException {
    List<Gene> genes = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(tss)) {
      String line;
      reader.readLine(); // Skip header.
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
    try (PauseReader reader = new PauseReader(parameters.reader());
        BufferedWriter writer = parameters.writer()) {
      Pause pause;
      while ((pause = reader.readPause()) != null) {
        writer.write(pause.name);
        writer.write(SEPARATOR);
        writer.write(pause.chromosome);
        writer.write(SEPARATOR);
        writer.write(String.valueOf(pause.position));
        writer.write(SEPARATOR);
        writer.write(String.valueOf(pause.normalizedReads));
        writer.write(SEPARATOR);
        writer.write(String.valueOf(pause.foldsAboveAverage));
        writer.write(SEPARATOR);
        writer.write(String.valueOf(pause.beginningReads));
        writer.write(SEPARATOR);
        writer.write(String.valueOf(pause.sequence));
        writer.write(LINE_SEPARATOR);
      }
    }
  }

  private static class Gene {
    String name;
    long start;
  }
}
