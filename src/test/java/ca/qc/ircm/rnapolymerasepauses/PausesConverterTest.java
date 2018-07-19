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
import static org.mockito.Mockito.when;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class PausesConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int PAUSES_COUNT = 100000;
  private static final int PAUSES_PER_CHROMOSOME = PAUSES_COUNT / 18;
  private static final String PAUSE_NAME_MARKER = ">";
  private static final int PAUSE_NAME_LENGHT = 20;
  private static final int PAUSE_MAX_POSITION = 500;
  private static final double PAUSE_MAX_NORMALIZED_READS = 5;
  private static final double PAUSE_MAX_FOLDS_ABOVE_AVERAGE = 20;
  private static final double PAUSE_MAX_BEGINNING_READS = 4;
  private static final int PAUSE_SEQUENCE_LENGHT = 40;
  private static final String PAUSE_SEPARATOR = "_";
  private static final String OUTPUT_SEPARATOR = "\t";
  private static final double DELTA = 0.000000001;
  private PausesConverter pausesConverter;
  @Mock
  private PausesToTabsCommand parameters;
  private List<Pause> pauses;
  private Random random;
  private String content;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    pausesConverter = new PausesConverter();
    random = new Random();
    pauses = IntStream.range(0, PAUSES_COUNT)
        .mapToObj(i -> generatePause(i / PAUSES_PER_CHROMOSOME + 1)).collect(Collectors.toList());
    StringWriter output = new StringWriter();
    writePauses(pauses, output);
    content = output.toString();
  }

  private Pause generatePause(int chromosome) {
    Pause pause = new Pause();
    pause.name = RandomStringUtils.randomAlphanumeric(PAUSE_NAME_LENGHT);
    pause.chromosome = "chr" + chromosome;
    pause.position = random.nextInt(PAUSE_MAX_POSITION);
    pause.normalizedReads = random.nextDouble() * PAUSE_MAX_NORMALIZED_READS;
    pause.foldsAboveAverage = random.nextDouble() * PAUSE_MAX_FOLDS_ABOVE_AVERAGE;
    pause.beginningReads = random.nextDouble() * PAUSE_MAX_BEGINNING_READS;
    pause.sequence = RandomStringUtils.randomAlphabetic(PAUSE_SEQUENCE_LENGHT);
    return pause;
  }

  private void writePauses(Iterable<Pause> pauses, Writer output) throws IOException {
    for (Pause pause : pauses) {
      output.write(PAUSE_NAME_MARKER);
      output.write(pause.name);
      output.write(PAUSE_SEPARATOR);
      output.write(pause.chromosome);
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.position));
      if (pause.normalizedReads != null) {
        output.write(PAUSE_SEPARATOR);
        output.write(String.valueOf(pause.normalizedReads));
      }
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.foldsAboveAverage));
      if (pause.beginningReads != null) {
        output.write(PAUSE_SEPARATOR);
        output.write(String.valueOf(pause.beginningReads));
      }
      output.write(LINE_SEPARATOR);
      if (pause.sequence != null) {
        output.write(pause.sequence);
        output.write(LINE_SEPARATOR);
      }
    }
  }

  @Test
  public void pausesToTabs() throws Throwable {
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    pausesConverter.pausesToTabs(parameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      String[] columns = lines[i].split(OUTPUT_SEPARATOR);
      assertEquals(7, columns.length);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
      assertEquals(pause.sequence, columns[6]);
    }
  }

  @Test
  public void pausesToTabs_NoSequence() throws Throwable {
    pauses.forEach(pause -> pause.sequence = null);
    StringWriter contentAsWriter = new StringWriter();
    writePauses(pauses, contentAsWriter);
    content = contentAsWriter.toString();
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    pausesConverter.pausesToTabs(parameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      String[] columns = lines[i].split(OUTPUT_SEPARATOR, -1);
      assertEquals(6, columns.length);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
    }
  }

  @Test
  public void pausesToTabs_NoReads() throws Throwable {
    pauses.forEach(pause -> {
      pause.normalizedReads = null;
      pause.beginningReads = null;
    });
    StringWriter contentAsWriter = new StringWriter();
    writePauses(pauses, contentAsWriter);
    content = contentAsWriter.toString();
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    pausesConverter.pausesToTabs(parameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      String[] columns = lines[i].split(OUTPUT_SEPARATOR, -1);
      assertEquals(5, columns.length);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.sequence, columns[4]);
    }
  }

  private static class Pause {
    String name;
    String chromosome;
    int position;
    Double normalizedReads;
    double foldsAboveAverage;
    Double beginningReads;
    String sequence;
  }
}
