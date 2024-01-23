package ca.qc.ircm.rnapolymerasepauses.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.rnapolymerasepauses.Pause;
import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.StringWriter;
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
public class PauseWriterTest {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final int PAUSES_COUNT = 100000;
  private static final int PAUSES_PER_CHROMOSOME = PAUSES_COUNT / 18;
  private static final String MARKER = ">";
  private static final int PAUSE_NAME_LENGHT = 20;
  private static final int PAUSE_MAX_POSITION = 500;
  private static final double PAUSE_MAX_NORMALIZED_READS = 5;
  private static final double PAUSE_MAX_FOLDS_ABOVE_AVERAGE = 20;
  private static final double PAUSE_MAX_BEGINNING_READS = 4;
  private static final int PAUSE_SEQUENCE_MAX_LENGHT = 400;
  private static final String SEPARATOR = "_";
  private static final double DELTA = 0.00001;
  private List<Pause> pauses;
  private Random random;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    random = new Random();
    pauses = IntStream.range(0, PAUSES_COUNT)
        .mapToObj(i -> generatePause(i / PAUSES_PER_CHROMOSOME + 1)).collect(Collectors.toList());
  }

  private Pause generatePause(int chromosome) {
    Pause pause = new Pause();
    pause.name = RandomStringUtils.randomAlphanumeric(PAUSE_NAME_LENGHT);
    pause.chromosome = "chr" + chromosome;
    pause.position = random.nextInt(PAUSE_MAX_POSITION);
    pause.normalizedReads = random.nextDouble() * PAUSE_MAX_NORMALIZED_READS;
    pause.foldsAboveAverage = random.nextDouble() * PAUSE_MAX_FOLDS_ABOVE_AVERAGE;
    pause.beginningReads = random.nextDouble() * PAUSE_MAX_BEGINNING_READS;
    pause.sequence = RandomStringUtils.randomAlphabetic(random.nextInt(PAUSE_SEQUENCE_MAX_LENGHT));
    return pause;
  }

  @Test
  public void writePause() throws Throwable {
    StringWriter actualContentWriter = new StringWriter();
    try (PauseWriter writer = new PauseWriter(actualContentWriter)) {
      for (Pause pause : pauses) {
        writer.writePause(pause);
      }
    }

    String[] lines = actualContentWriter.toString().split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (Pause pause : pauses) {
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

  @Test
  public void writePause_NoSequence() throws Throwable {
    pauses.forEach(pause -> pause.sequence = null);
    StringWriter actualContentWriter = new StringWriter();
    try (PauseWriter writer = new PauseWriter(actualContentWriter)) {
      for (Pause pause : pauses) {
        writer.writePause(pause);
      }
    }

    String[] lines = actualContentWriter.toString().split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (Pause pause : pauses) {
      String line = lines[lineNumber++];
      assertTrue(line.startsWith(MARKER));
      String[] columns = line.substring(1).split(SEPARATOR);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
    }
  }
}
