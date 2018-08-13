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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class FakeGeneTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final String SEPARATOR = "\t";
  private static final int CHROMOSOME_COUNT = 21;
  private static final int CHROMOSOME_MIN_LENGTH = 150;
  private static final int CHROMOSOME_MAX_LENGTH = 1000;
  private FakeGene fakeGene;
  @Mock
  private FakeGeneCommand parameters;
  private Map<String, Long> sizes;
  private String content;
  private Random random;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    fakeGene = new FakeGene();
    random = new Random();
    sizes = IntStream.range(0, CHROMOSOME_COUNT).collect(() -> new HashMap<>(),
        (map, i) -> map.put("chr" + (i + 1),
            (long) CHROMOSOME_MIN_LENGTH
                + random.nextInt(CHROMOSOME_MAX_LENGTH - CHROMOSOME_MIN_LENGTH)),
        (map1, map2) -> map1.putAll(map2));
    writeSizes();
  }

  private void writeSizes() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Long> entry : sizes.entrySet()) {
      builder.append(entry.getKey());
      builder.append(SEPARATOR);
      builder.append(entry.getValue());
      builder.append(LINE_SEPARATOR);
    }
    content = builder.toString();
  }

  private void assertGeneContent(String trackContent) {
    String[] lines = trackContent.split(LINE_SEPARATOR);
    int lineNumber = 0;
    for (Map.Entry<String, Long> entry : sizes.entrySet()) {
      final String chromosome = entry.getKey();
      final Long size = entry.getValue();
      String plusGene = lines[lineNumber++];
      String[] columns = plusGene.split("\t", -1);
      final String indexValue = columns[0];
      assertEquals(chromosome + "-P", columns[1]);
      assertEquals(chromosome, columns[2]);
      assertEquals("+", columns[3]);
      assertEquals("2", columns[4]);
      assertEquals(String.valueOf(size - 2), columns[5]);
      assertEquals("2", columns[6]);
      assertEquals(String.valueOf(size - 2), columns[7]);
      assertEquals("2,", columns[8]);
      assertEquals((size - 2) + ",", columns[9]);
      String minusGene = lines[lineNumber++];
      columns = minusGene.split("\t", -1);
      assertEquals(indexValue, columns[0]);
      assertEquals(chromosome + "-M", columns[1]);
      assertEquals(chromosome, columns[2]);
      assertEquals("-", columns[3]);
      assertEquals("2", columns[4]);
      assertEquals(String.valueOf(size - 2), columns[5]);
      assertEquals("2", columns[6]);
      assertEquals(String.valueOf(size - 2), columns[7]);
      assertEquals("2,", columns[8]);
      assertEquals((size - 2) + ",", columns[9]);
    }
  }

  @Test
  public void fakeGene() throws Throwable {
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));

    fakeGene.fakeGene(parameters);

    assertGeneContent(writer.toString());
  }
}
