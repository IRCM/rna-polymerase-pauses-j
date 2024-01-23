package ca.qc.ircm.rnapolymerasepauses.io;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.rnapolymerasepauses.Pause;
import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class PauseReaderTest {
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
  private static final double DELTA = 0.000000001;
  private List<Pause> pauses;
  private Random random;
  private String content;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
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
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.normalizedReads));
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.foldsAboveAverage));
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.beginningReads));
      output.write(LINE_SEPARATOR);
      if (pause.sequence != null) {
        output.write(pause.sequence);
        output.write(LINE_SEPARATOR);
      }
    }
  }

  @Test
  public void readPause() throws Throwable {
    List<Pause> pauses = new ArrayList<>();
    try (PauseReader reader = new PauseReader(new BufferedReader(new StringReader(content)))) {
      Pause pause;
      while ((pause = reader.readPause()) != null) {
        pauses.add(pause);
      }
    }

    assertEquals(this.pauses.size(), pauses.size());
    for (int i = 0; i < pauses.size(); i++) {
      Pause expected = this.pauses.get(i);
      Pause actual = pauses.get(i);
      assertEquals(expected.name, actual.name);
      assertEquals(expected.chromosome, actual.chromosome);
      assertEquals(expected.position, actual.position);
      assertEquals(expected.normalizedReads, actual.normalizedReads, DELTA);
      assertEquals(expected.foldsAboveAverage, actual.foldsAboveAverage, DELTA);
      assertEquals(expected.beginningReads, actual.beginningReads, DELTA);
      assertEquals(expected.sequence, actual.sequence);
    }
  }

  @Test
  public void readPause_NoSequence() throws Throwable {
    pauses.forEach(pause -> pause.sequence = null);
    StringWriter contentAsWriter = new StringWriter();
    writePauses(pauses, contentAsWriter);
    content = contentAsWriter.toString();
    List<Pause> pauses = new ArrayList<>();
    try (PauseReader reader = new PauseReader(new BufferedReader(new StringReader(content)))) {
      Pause pause;
      while ((pause = reader.readPause()) != null) {
        pauses.add(pause);
      }
    }

    assertEquals(this.pauses.size(), pauses.size());
    for (int i = 0; i < pauses.size(); i++) {
      Pause expected = this.pauses.get(i);
      Pause actual = pauses.get(i);
      assertEquals(expected.name, actual.name);
      assertEquals(expected.chromosome, actual.chromosome);
      assertEquals(expected.position, actual.position);
      assertEquals(expected.normalizedReads, actual.normalizedReads, DELTA);
      assertEquals(expected.foldsAboveAverage, actual.foldsAboveAverage, DELTA);
      assertEquals(expected.beginningReads, actual.beginningReads, DELTA);
      assertEquals("", actual.sequence);
    }
  }
}
