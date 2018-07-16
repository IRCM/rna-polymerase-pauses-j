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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.rnapolymerasepauses.io.ChromosomeSizesParser;
import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
  private static final Charset WIG_CHARSET = StandardCharsets.UTF_8;
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
      LongStream.rangeClosed(1, size).filter(position -> random.nextDouble() > NO_SCORE_RATIO)
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
      LongStream.rangeClosed(1, size)
          .filter(position -> scores.get(chromosome).containsKey(position)).forEach(position -> {
            lines.add(position + SEPARATOR + scores.get(chromosome).get(position));
          });
    });
    content = lines.stream().collect(Collectors.joining(LINE_SEPARATOR));
  }

  @Test
  public void wigToTrack() throws Throwable {
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(WIG_CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    wigConverter.wigToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    String[] lines = output.toString(WIG_CHARSET.name()).split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 1; position <= sizes.get(chromosome); position++) {
        double score =
            scores.get(chromosome).containsKey(position) ? scores.get(chromosome).get(position)
                : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }

  @Test
  public void wigToTrack_Comments() throws Throwable {
    content = "#comment 1\n" + content.split("\n")[0] + "\n#comment 2\n"
        + Arrays.asList(content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(WIG_CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    wigConverter.wigToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    String[] lines = output.toString(WIG_CHARSET.name()).split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 0; position < sizes.get(chromosome); position++) {
        double score = scores.get(chromosome).containsKey(position + 1)
            ? scores.get(chromosome).get(position + 1)
            : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }

  @Test
  public void wigToTrack_Track() throws Throwable {
    content = "track name=\"my track\"\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(WIG_CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    wigConverter.wigToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    String[] lines = output.toString(WIG_CHARSET.name()).split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 0; position < sizes.get(chromosome); position++) {
        double score = scores.get(chromosome).containsKey(position + 1)
            ? scores.get(chromosome).get(position + 1)
            : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }

  @Test
  public void wigToTrack_BrowserAndTrack() throws Throwable {
    content = "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(WIG_CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    wigConverter.wigToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    String[] lines = output.toString(WIG_CHARSET.name()).split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 0; position < sizes.get(chromosome); position++) {
        double score = scores.get(chromosome).containsKey(position + 1)
            ? scores.get(chromosome).get(position + 1)
            : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }

  @Test
  public void wigToTrack_BrowserAndTrackAndComment() throws Throwable {
    content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(WIG_CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    wigConverter.wigToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    String[] lines = output.toString(WIG_CHARSET.name()).split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (String chromosome : sizes.keySet()) {
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      for (long position = 0; position < sizes.get(chromosome); position++) {
        double score = scores.get(chromosome).containsKey(position + 1)
            ? scores.get(chromosome).get(position + 1)
            : 0.0;
        assertEquals(chromosome + ":" + position, score, Double.parseDouble(lines[lineNumber++]),
            DELTA);
      }
    }
  }
}
