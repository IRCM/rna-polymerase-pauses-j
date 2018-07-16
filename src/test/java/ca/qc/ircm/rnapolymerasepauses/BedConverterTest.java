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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class BedConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int CHROMOSOME_COUNT = 21;
  private static final int CHROMOSOME_MIN_LENGTH = 150;
  private static final int CHROMOSOME_MAX_LENGTH = 1000;
  private static final int BED_DATA_NAME_LENGTH = 20;
  private static final int BED_DATA_MAX_LENGTH = 100;
  private static final int MAX_SPACE_BETWEEN_DATA = 100;
  private static final String SEPARATOR = "\t";
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final double DELTA = 0.000000001;
  private BedConverter bedConverter;
  @Mock
  private ChromosomeSizesParser chromosomeSizesParser;
  @Mock
  private BedToTrackCommand parameters;
  private Map<String, Long> sizes;
  private Map<String, List<BedData>> datas;
  private String content;
  private Path chromosomeSizes = Paths.get("chromosomeSizes.txt");
  private Random random;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    bedConverter = new BedConverter(chromosomeSizesParser);
    parameters.chromosomeSizes = chromosomeSizes;
    random = new Random();
    sizes = IntStream.range(0, CHROMOSOME_COUNT).collect(() -> new HashMap<>(),
        (map, i) -> map.put("chr" + (i + 1),
            (long) CHROMOSOME_MIN_LENGTH
                + random.nextInt(CHROMOSOME_MAX_LENGTH - CHROMOSOME_MIN_LENGTH)),
        (map1, map2) -> map1.putAll(map2));
    when(chromosomeSizesParser.chromosomeSizes(any())).thenReturn(sizes);
    generateBedDatas();
    writeBedDatas(true);
  }

  private void generateBedDatas() {
    datas = sizes.keySet().stream()
        .collect(Collectors.toMap(chromosome -> chromosome, chromosome -> new ArrayList<>()));
    sizes.forEach((chromosome, size) -> {
      long position = random.nextInt(MAX_SPACE_BETWEEN_DATA);
      while (position <= size) {
        long end =
            Math.min(Math.max(position + random.nextInt(BED_DATA_MAX_LENGTH), position + 1), size);
        BedData data = new BedData();
        data.chromosome = chromosome;
        data.start = position;
        data.end = end;
        data.name = RandomStringUtils.randomAlphanumeric(BED_DATA_NAME_LENGTH);
        data.score = random.nextDouble();
        datas.get(chromosome).add(data);
        position = end + random.nextInt(MAX_SPACE_BETWEEN_DATA);
      }
    });
  }

  private void writeBedDatas(boolean includeName) {
    StringBuilder builder = new StringBuilder();
    for (String chromosome : sizes.keySet()) {
      List<BedData> datas = this.datas.get(chromosome);
      for (BedData data : datas) {
        builder.append(data.chromosome);
        builder.append(SEPARATOR);
        builder.append(String.valueOf(data.start));
        builder.append(SEPARATOR);
        builder.append(String.valueOf(data.end));
        builder.append(SEPARATOR);
        if (includeName) {
          builder.append(data.name);
          builder.append(SEPARATOR);
        }
        builder.append(String.valueOf(data.score));
        builder.append(LINE_SEPARATOR);
      }
    }
    content = builder.toString();
  }

  private void assertTrackContent(String trackContent) {
    String[] lines = trackContent.split(LINE_SEPARATOR);
    int lineNumber = 0;
    int dataIndex;
    for (String chromosome : sizes.keySet()) {
      dataIndex = 0;
      assertEquals("chrom=" + chromosome, lines[lineNumber++]);
      List<BedData> datas = this.datas.get(chromosome);
      BedData data = datas.get(dataIndex++);
      for (long position = 1; position <= sizes.get(chromosome); position++) {
        while (data != null && position >= data.end) {
          data = dataIndex < datas.size() ? datas.get(dataIndex++) : null;
        }
        if (data == null || position < data.start) {
          assertEquals(chromosome + ":" + position, 0.0, Double.parseDouble(lines[lineNumber++]),
              DELTA);
        } else {
          assertEquals(chromosome + ":" + position + ",data=" + data.start + "-" + data.end,
              data.score, Double.parseDouble(lines[lineNumber++]), DELTA);
        }
      }
    }
  }

  @Test
  public void bedToTrack() throws Throwable {
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  @Test
  public void bedToTrack_NoName() throws Throwable {
    writeBedDatas(false);
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  @Test
  public void bedToTrack_Comments() throws Throwable {
    content = "#comment 1\n" + content.split("\n")[0] + "\n#comment 2\n"
        + Arrays.asList(content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  @Test
  public void bedToTrack_Track() throws Throwable {
    content = "track name=\"my track\"\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  @Test
  public void bedToTrack_BrowserAndTrack() throws Throwable {
    content = "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  @Test
  public void bedToTrack_BrowserAndTrackAndComment() throws Throwable {
    content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n" + content;
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    bedConverter.bedToTrack(input, output, parameters);

    verify(chromosomeSizesParser).chromosomeSizes(chromosomeSizes);
    assertTrackContent(output.toString(CHARSET.name()));
  }

  private static class BedData {
    String chromosome;
    long start;
    long end;
    String name;
    double score;
  }
}
