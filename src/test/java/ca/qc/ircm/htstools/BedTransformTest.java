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

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.htstools.test.config.NonTransactionalTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class BedTransformTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_CHROMOSOME = 23;
  private static final int MAX_ANNOTATION_LENGTH = 10000;
  private static final int MAX_ANNOTATION_START = Integer.MAX_VALUE - MAX_ANNOTATION_LENGTH;
  private static final int MIN_ANNOTATION_SCORE = 300;
  private static final int MAX_ANNOTATION_SCORE = 900;
  private BedTransform bedTransform;
  private String content;
  private InputStream input;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    bedTransform = new BedTransform();
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
  public void setAnnotationsSize() throws Throwable {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.setAnnotationsSize(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_Comments() throws Throwable {
    content = "#comment 1\n" + content.split("\n")[0] + "\n#comment 2\n"
        + Arrays.asList(content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.setAnnotationsSize(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    {
      String[] columns = lines[1].split("\t", -1);
      String[] outputColumns = outputLines[1].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
    assertEquals(lines[2], outputLines[2]);
    for (int i = 3; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_Track() throws Throwable {
    content = "track name=\"my track\"\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.setAnnotationsSize(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    for (int i = 1; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_BrowserAndTrack() throws Throwable {
    content = "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.setAnnotationsSize(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    assertEquals(lines[1], outputLines[1]);
    for (int i = 2; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void setAnnotationsSize_BrowserAndTrackAndComment() throws Throwable {
    content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.setAnnotationsSize(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    assertEquals(lines[1], outputLines[1]);
    assertEquals(lines[2], outputLines[2]);
    for (int i = 3; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(columns[1], outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations() throws Throwable {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_NegativeDistance() throws Throwable {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, -3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    for (int i = 0; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + -3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + -3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_Comments() throws Throwable {
    content = "#comment 1\n" + content.split("\n")[0] + "\n#comment 2\n"
        + Arrays.asList(content.split("\n")).stream().skip(1).collect(Collectors.joining("\n"));
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    {
      String[] columns = lines[1].split("\t", -1);
      String[] outputColumns = outputLines[1].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
    assertEquals(lines[2], outputLines[2]);
    for (int i = 3; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_Track() throws Throwable {
    content = "track name=\"my track\"\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    for (int i = 1; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_BrowserAndTrack() throws Throwable {
    content = "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    assertEquals(lines[1], outputLines[1]);
    for (int i = 2; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }

  @Test
  public void moveAnnotations_BrowserAndTrackAndComment() throws Throwable {
    content =
        "browser position chr7:127471196-127495720\ntrack name=\"my track\"\n#comment\n" + content;
    input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bedTransform.moveAnnotations(input, output, 3);
    String[] outputLines = Arrays.asList(output.toString(StandardCharsets.UTF_8.name()).split("\n"))
        .stream().filter(line -> !line.isEmpty()).toArray(count -> new String[count]);
    String[] lines = content.split("\n");
    assertEquals(lines.length, outputLines.length);
    assertEquals(lines[0], outputLines[0]);
    assertEquals(lines[1], outputLines[1]);
    assertEquals(lines[2], outputLines[2]);
    for (int i = 3; i < lines.length; i++) {
      String[] columns = lines[i].split("\t", -1);
      String[] outputColumns = outputLines[i].split("\t", -1);
      assertEquals(columns.length, outputColumns.length);
      assertEquals(columns[0], outputColumns[0]);
      assertEquals(String.valueOf(Long.parseLong(columns[1]) + 3), outputColumns[1]);
      assertEquals(String.valueOf(Long.parseLong(columns[2]) + 3), outputColumns[2]);
      for (int j = 3; j < columns.length; j++) {
        assertEquals(columns[j], outputColumns[j]);
      }
    }
  }
}
