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

package ca.qc.ircm.htstools;

import ca.qc.ircm.htstools.test.config.NonTransactionalTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class TrimBedEndTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_CHROMOSOME = 23;
  private static final int MAX_ANNOTATION_LENGTH = 10000;
  private static final int MAX_ANNOTATION_START = Integer.MAX_VALUE - MAX_ANNOTATION_LENGTH;
  private static final int MIN_ANNOTATION_SCORE = 300;
  private static final int MAX_ANNOTATION_SCORE = 900;
  private TrimBedEnd trimBedEnd;
  private String content;
  private InputStream input;

  @Before
  public void beforeTest() {
    trimBedEnd = new TrimBedEnd();
    Random random = new Random();
    content = IntStream.range(0, LINE_COUNT).mapToObj(lineNumber -> {
      int chromosome = random.nextInt(MAX_CHROMOSOME);
      int start = random.nextInt(MAX_ANNOTATION_START);
      int end = start + random.nextInt(MAX_ANNOTATION_LENGTH);
      int score =
          MIN_ANNOTATION_SCORE + random.nextInt(MAX_ANNOTATION_SCORE - MIN_ANNOTATION_SCORE);
      return chromosome + "\t" + start + "\t" + end + "\t" + score;
    }).collect(Collectors.joining("\n"));
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void trimBedEnd() throws Throwable {
    TrimBedEndParameters parameters = new TrimBedEndParameters();
    parameters.sizeFromStart = 3;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    trimBedEnd.trimBedEnd(input, output, parameters);
    String outputContent = output.toString(StandardCharsets.UTF_8.name());
    System.out.println(outputContent);
  }
}
