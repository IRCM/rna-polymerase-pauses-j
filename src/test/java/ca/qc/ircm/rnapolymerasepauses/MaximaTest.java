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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MaximaTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int PAUSES_GROUP_COUNT = 100;
  private static final int PAUSE_NAME_LENGHT = 10;
  private static final int MAX_CHROMOSOME = 18;
  private static final String MARKER = ">";
  private static final double PAUSE_MAX_NORMALIZED_READS = 5;
  private static final double PAUSE_MAX_FOLDS_ABOVE_AVERAGE = 20;
  private static final double PAUSE_MAX_BEGINNING_READS = 4;
  private static final int PAUSE_SEQUENCE_LENGHT = 40;
  private static final String SEPARATOR = "_";
  private static final double DELTA = 0.00001;
  private Maxima maxima;
  @Mock
  private MaximaCommand parameters;
  private List<Pause> pauses;
  private List<Pause> maximaPauses;
  private Random random;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    maxima = new Maxima();
    random = new Random();
  }

  private void generatePauses(int window) {
    pauses = new ArrayList<>();
    maximaPauses = new ArrayList<>();
    for (int i = 0; i < PAUSES_GROUP_COUNT; i++) {
      String gene = RandomStringUtils.randomAlphanumeric(PAUSE_NAME_LENGHT);
      int chromosome = random.nextInt(MAX_CHROMOSOME) + 1;
      List<Pause> group = generatePausesGroup(gene, chromosome, 0, window);
      pauses.addAll(group);
      double max = group.stream().mapToDouble(pause -> pause.foldsAboveAverage).max().getAsDouble();
      maximaPauses
          .add(group.stream().filter(pause -> pause.foldsAboveAverage == max).findFirst().get());
      group = generatePausesGroup(gene, chromosome, window * 3, window);
      pauses.addAll(group);
      double max2 =
          group.stream().mapToDouble(pause -> pause.foldsAboveAverage).max().getAsDouble();
      maximaPauses
          .add(group.stream().filter(pause -> pause.foldsAboveAverage == max2).findFirst().get());
    }
  }

  private List<Pause> generatePausesGroup(String gene, int chromosome, int start, int window) {
    List<Pause> pauses = new ArrayList<>();
    for (int i = 0; i < window; i++) {
      if (random.nextBoolean()) {
        Pause pause = new Pause();
        pause.name = gene;
        pause.chromosome = "chr" + chromosome;
        pause.position = start + i;
        pause.normalizedReads = random.nextDouble() * PAUSE_MAX_NORMALIZED_READS;
        pause.foldsAboveAverage = random.nextDouble() * PAUSE_MAX_FOLDS_ABOVE_AVERAGE;
        pause.beginningReads = random.nextDouble() * PAUSE_MAX_BEGINNING_READS;
        pause.sequence = RandomStringUtils.randomAlphabetic(PAUSE_SEQUENCE_LENGHT);
        pauses.add(pause);
      }
    }
    return pauses;
  }

  private void writePauses(Iterable<Pause> pauses, Writer output) throws IOException {
    for (Pause pause : pauses) {
      output.write(MARKER);
      output.write(pause.name);
      output.write(SEPARATOR);
      output.write(pause.chromosome);
      output.write(SEPARATOR);
      output.write(String.valueOf(pause.position));
      output.write(SEPARATOR);
      output.write(String.valueOf(pause.normalizedReads));
      output.write(SEPARATOR);
      output.write(String.valueOf(pause.foldsAboveAverage));
      output.write(SEPARATOR);
      output.write(String.valueOf(pause.beginningReads));
      output.write(LINE_SEPARATOR);
      if (pause.sequence != null) {
        output.write(pause.sequence);
        output.write(LINE_SEPARATOR);
      }
    }
  }

  @Test
  public void maxima() throws Throwable {
    int window = 20;
    generatePauses(window);
    StringWriter contentWriter = new StringWriter();
    writePauses(pauses, contentWriter);
    when(parameters.reader())
        .thenReturn(new BufferedReader(new StringReader(contentWriter.toString())));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));
    parameters.windowSize = window;

    maxima.maxima(parameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (Pause pause : maximaPauses) {
      String line = lines[lineNumber++];
      assertTrue(line.startsWith(MARKER));
      String[] columns = line.substring(1).split(SEPARATOR);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
      StringBuilder sequence = new StringBuilder();
      while (lineNumber < lines.length && !lines[lineNumber].startsWith(MARKER)) {
        line = lines[lineNumber++];
        assertTrue(line.length() <= 80);
        sequence.append(line);
      }
      assertEquals(pause.sequence, sequence.toString());
    }
  }
}
