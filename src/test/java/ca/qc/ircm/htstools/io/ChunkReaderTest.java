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

package ca.qc.ircm.htstools.io;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkReaderTest {
  private static final int LINE_COUNT = 1000;
  private static final int MAX_LINE_LENGHT = 100;
  private static final int RANDOM_CHAR_MAX_VALUE = 26 * 2 + 10 + 1;
  private StringReader contentReader;
  private String content;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    Random random = new Random();
    content = IntStream.range(0, LINE_COUNT).mapToObj(lineNumber -> {
      int lineLenght = random.nextInt(MAX_LINE_LENGHT);
      String line = IntStream.range(0, lineLenght).map(i -> random.nextInt(RANDOM_CHAR_MAX_VALUE))
          .collect(() -> new StringBuilder(), (builder, letter) -> {
            builder.append(charFromRandom(letter));
          }, (builder1, builder2) -> {
            builder1.append(builder2);
          }).toString();
      return line;
    }).collect(Collectors.joining("\n"));
    contentReader = new StringReader(content);
  }

  private char charFromRandom(int value) {
    if (value < 26) {
      return (char) ('a' + value);
    } else if (value < 26 * 2) {
      return (char) ('A' + value - 26);
    } else {
      return ' ';
    }
  }

  @Test
  public void readChunk() throws Throwable {
    int chunkSize = LINE_COUNT / 3;
    try (ChunkReader reader = new ChunkReader(new BufferedReader(contentReader), chunkSize)) {
      List<String> lines = reader.readChunk();
      assertEquals(chunkSize, lines.size());
      assertEquals(Arrays.asList(content.split("\n", -1)).subList(0, chunkSize), lines);
      lines = reader.readChunk();
      assertEquals(chunkSize, lines.size());
      assertEquals(Arrays.asList(content.split("\n", -1)).subList(chunkSize, chunkSize * 2), lines);
      lines = reader.readChunk();
      assertEquals(chunkSize, lines.size());
      assertEquals(Arrays.asList(content.split("\n", -1)).subList(chunkSize * 2, chunkSize * 3),
          lines);
      lines = reader.readChunk();
      assertEquals(LINE_COUNT - chunkSize * 3, lines.size());
      assertEquals(Arrays.asList(content.split("\n", -1)).subList(chunkSize * 3, LINE_COUNT),
          lines);
    }
  }
}
