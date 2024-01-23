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

  private void assertGeneContent(String trackContent, int padding) {
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
      assertEquals(String.valueOf(padding), columns[4]);
      assertEquals(String.valueOf(size - padding), columns[5]);
      assertEquals(String.valueOf(padding), columns[6]);
      assertEquals(String.valueOf(size - padding), columns[7]);
      assertEquals(padding + ",", columns[8]);
      assertEquals((size - padding) + ",", columns[9]);
      String minusGene = lines[lineNumber++];
      columns = minusGene.split("\t", -1);
      assertEquals(indexValue, columns[0]);
      assertEquals(chromosome + "-M", columns[1]);
      assertEquals(chromosome, columns[2]);
      assertEquals("-", columns[3]);
      assertEquals(String.valueOf(padding), columns[4]);
      assertEquals(String.valueOf(size - padding), columns[5]);
      assertEquals(String.valueOf(padding), columns[6]);
      assertEquals(String.valueOf(size - padding), columns[7]);
      assertEquals(padding + ",", columns[8]);
      assertEquals((size - padding) + ",", columns[9]);
    }
  }

  @Test
  public void fakeGene() throws Throwable {
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));
    parameters.padding = 2;

    fakeGene.fakeGene(parameters);

    assertGeneContent(writer.toString(), 2);
  }

  @Test
  public void fakeGene_Padding() throws Throwable {
    when(parameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(parameters.writer()).thenReturn(new BufferedWriter(writer));
    parameters.padding = 10;

    fakeGene.fakeGene(parameters);

    assertGeneContent(writer.toString(), 10);
  }
}
