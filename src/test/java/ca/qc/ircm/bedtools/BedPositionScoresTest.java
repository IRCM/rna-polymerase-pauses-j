package ca.qc.ircm.bedtools;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class BedPositionScoresTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_CHROMOSOME = 23;
  private static final int MAX_ANNOTATION_LENGTH = 10000;
  private static final int MIN_ANNOTATION_SCORE = 300;
  private static final int MAX_ANNOTATION_SCORE = 900;
  private static final double EPSILON = 0.00000000001;
  private static final String SEPARATOR = "\t";
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private BedPositionScores bedPositionScores;
  private Path input;
  private List<String> lines = new ArrayList<>();
  private Map<String, List<String>> linesPerChromosome = new HashMap<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    input = temporaryFolder.newFile("input.bed").toPath();
    bedPositionScores = new BedPositionScores(input);
    bedContent();
  }

  private void bedContent() throws IOException {
    Random random = new Random();
    int linesPerChromosome = (int) (LINE_COUNT * 1.3 / MAX_CHROMOSOME);
    for (int chromosome = 1; chromosome <= MAX_CHROMOSOME; chromosome++) {
      String chromosomeAsString = String.valueOf(chromosome);
      int start = 0;
      this.linesPerChromosome.put(chromosomeAsString, new ArrayList<>());
      for (int i = 0; i < linesPerChromosome; i++) {
        int end = start + random.nextInt(MAX_ANNOTATION_LENGTH);
        int score =
            MIN_ANNOTATION_SCORE + random.nextInt(MAX_ANNOTATION_SCORE - MIN_ANNOTATION_SCORE);
        String line = chromosome + SEPARATOR + start + SEPARATOR + end + SEPARATOR + score;
        this.linesPerChromosome.get(chromosomeAsString).add(line);
        lines.add(line);
        start = end;
      }
    }
    Files.write(input, lines);
  }

  @Test
  public void score() throws Throwable {
    long maxPosition = maxPosition();
    for (int chromosome = 1; chromosome <= MAX_CHROMOSOME; chromosome++) {
      for (int position = 0; position < maxPosition + 1; position++) {
        String chromosomeAsString = String.valueOf(chromosome);
        assertEquals(chromosomeAsString + "-" + position,
            expectedScore(chromosomeAsString, position),
            bedPositionScores.score(chromosomeAsString, position), EPSILON);
      }
    }
  }

  private long maxPosition() {
    return lines.stream().mapToLong(line -> Long.parseLong(line.split(SEPARATOR, -1)[2])).max()
        .orElse(0);
  }

  private double expectedScore(String chromosome, long position) {
    return linesPerChromosome.get(chromosome).stream().map(line -> line.split(SEPARATOR, -1))
        .filter(cols -> cols[0].equals(chromosome) && Long.parseLong(cols[1]) <= position
            && Long.parseLong(cols[2]) > position)
        .findFirst().map(cols -> Double.parseDouble(cols[3])).orElse(Double.NaN);
  }
}
