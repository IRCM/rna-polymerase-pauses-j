package ca.qc.ircm.rnapolymerasepauses;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.rnapolymerasepauses.io.ChromosomeSizesParser;
import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class WigConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int CHROMOSOME_COUNT = 21;
  private static final int CHROMOSOME_MAX_LENGTH = 10000;
  private static final double NO_SCORE_RATIO = 0.75;
  private static final String SEPARATOR = "\t";
  private static final double DELTA = 0.000000001;
  private WigConverter wigConverter;
  @Mock
  private ChromosomeSizesParser chromosomeSizesParser;
  @Mock
  private WigToTrackCommand parameters;
  private Map<String, Long> sizes;
  private Map<String, Map<Long, Double>> scores;
  private String content;
  private Path chromosomeSizes = Paths.get("chromosomeSizes.txt");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    wigConverter = new WigConverter(chromosomeSizesParser);
    Random random = new Random();
    sizes = IntStream.range(0, CHROMOSOME_COUNT).collect(() -> new HashMap<>(),
        (map, i) -> map.put("chr" + (i + 1), (long) random.nextInt(CHROMOSOME_MAX_LENGTH)),
        (map1, map2) -> map1.putAll(map2));
    when(chromosomeSizesParser.chromosomeSizes(any())).thenReturn(sizes);
    scores = new HashMap<>();
    sizes.forEach((chromosome, size) -> {
      scores.put(chromosome, new HashMap<>());
      LongStream.range(0, size).filter(position -> random.nextDouble() > NO_SCORE_RATIO)
          .forEach(position -> {
            scores.get(chromosome).put(position, random.nextDouble());
          });
    });
    generateWig();
    parameters.chromosomeSizes = chromosomeSizes;
    when(chromosomeSizesParser.chromosomeSizes(any())).thenReturn(sizes);
  }

  private void generateWig() throws IOException {
    List<String> lines = new ArrayList<>();
    sizes.forEach((chromosome, size) -> {
      lines.add("variableStep chrom=" + chromosome);
      LongStream.range(0, size).filter(position -> scores.get(chromosome).containsKey(position))
          .forEach(position -> {
            lines.add(position + SEPARATOR + scores.get(chromosome).get(position));
          });
    });
    content = lines.stream().collect(Collectors.joining(LINE_SEPARATOR));
  }

  private void assertTrackContent(String trackContent) {
    String[] lines = trackContent.split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 0; position < sizes.get(chromosome); position++) {
        double score =
            scores.get(chromosome).containsKey(position) ? scores.get(chromosome).get(position)
                : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }

  @Test
  public void wigToTrack() throws Throwable {
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    wigConverter.wigToTrack(parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(writer.toString());
  }

  @Test
  public void wigToTrack_Comments() throws Throwable {
    content = "#comment 1\n" + content.split("\n")[0] + "\n#comment 2\n"
        + Arrays.asList(content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    wigConverter.wigToTrack(parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(writer.toString());
  }

  @Test
  public void wigToTrack_Track() throws Throwable {
    content = "track name=\"my track\"\n" + content;
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    wigConverter.wigToTrack(parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(writer.toString());
  }

  @Test
  public void wigToTrack_BrowserAndTrack() throws Throwable {
    content = "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + content;
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    wigConverter.wigToTrack(parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(writer.toString());
  }

  @Test
  public void wigToTrack_BrowserAndTrackAndComment() throws Throwable {
    content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n" + content;
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    wigConverter.wigToTrack(parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(writer.toString());
  }
}
