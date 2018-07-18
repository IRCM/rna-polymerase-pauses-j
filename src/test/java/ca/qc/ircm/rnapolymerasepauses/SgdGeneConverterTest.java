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

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class SgdGeneConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int CHROMOSOME_COUNT = 21;
  private static final int MAX_GENE_PER_CHROMOSOME = 1000;
  private static final int GENE_NAME_SIZE = 20;
  private static final int MAX_GENE_SIZE = 1000;
  private static final int MIN_GENE_SIZE = 100;
  private static final int MAX_GENE_SEPARATOR = 1000;
  private static final int PROTEIN_ID_SIZE = 15;
  private static final String SEPARATOR = "\t";
  private static final String EXON_SEPARATOR = ",";
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private SgdGeneConverter sgdGeneConverter;
  @Mock
  private SgdGeneToTssCommand parameters;
  private Map<String, List<Gene>> genes;
  private String content;
  private Random random;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    sgdGeneConverter = new SgdGeneConverter();
    random = new Random();
    generateSgdGenes();
    generateContent();
  }

  private void generateSgdGenes() {
    genes = new HashMap<>();
    int id = 1;
    for (int chromosomeIndex = 1; chromosomeIndex <= CHROMOSOME_COUNT; chromosomeIndex++) {
      String chromosome = "chr" + chromosomeIndex;
      genes.put(chromosome, new ArrayList<>());
      int count = random.nextInt(MAX_GENE_PER_CHROMOSOME);
      long start = 0 + random.nextInt(MAX_GENE_SEPARATOR);
      for (int i = 0; i < count; i++) {
        final long end = start + random.nextInt(MAX_GENE_SIZE - MIN_GENE_SIZE) + MIN_GENE_SIZE;
        Gene gene = new Gene();
        gene.id = id++;
        gene.name = RandomStringUtils.randomAlphanumeric(GENE_NAME_SIZE);
        gene.chromosome = chromosome;
        gene.strand = random.nextInt(2) == 0 ? "+" : "-";
        gene.txStart = start;
        gene.txEnd = end;
        gene.cdsStart = start + random.nextInt(21);
        gene.cdsEnd = end - random.nextInt(21);
        gene.exonCount = random.nextInt(2) + 1;
        if (gene.exonCount == 1) {
          gene.exonStarts = new long[] { start };
          gene.exonEnds = new long[] { end };
        } else {
          gene.exonStarts = new long[] { start, start + 30 };
          gene.exonEnds = new long[] { end - 30, end };
        }
        gene.proteinId = RandomStringUtils.randomAlphanumeric(PROTEIN_ID_SIZE);
        genes.get(chromosome).add(gene);
        start = end + random.nextInt(MAX_GENE_SEPARATOR);
      }
    }
  }

  private void generateContent() {
    StringBuilder builder = new StringBuilder();
    for (String chromosome : genes.keySet()) {
      for (Gene gene : genes.get(chromosome)) {
        builder.append(gene.id);
        builder.append(SEPARATOR);
        builder.append(gene.name);
        builder.append(SEPARATOR);
        builder.append(gene.chromosome);
        builder.append(SEPARATOR);
        builder.append(gene.strand);
        builder.append(SEPARATOR);
        builder.append(gene.txStart);
        builder.append(SEPARATOR);
        builder.append(gene.txEnd);
        builder.append(SEPARATOR);
        builder.append(gene.cdsStart);
        builder.append(SEPARATOR);
        builder.append(gene.cdsEnd);
        builder.append(SEPARATOR);
        builder.append(gene.exonCount);
        builder.append(SEPARATOR);
        for (int j = 0; j < gene.exonStarts.length; j++) {
          builder.append(gene.exonStarts[j]);
          builder.append(EXON_SEPARATOR);
        }
        builder.append(SEPARATOR);
        for (int j = 0; j < gene.exonEnds.length; j++) {
          builder.append(gene.exonEnds[j]);
          builder.append(EXON_SEPARATOR);
        }
        builder.append(SEPARATOR);
        builder.append(gene.proteinId);
        builder.append(LINE_SEPARATOR);
      }
    }
    content = builder.toString();
  }

  @Test
  public void sgdGeneToTss() throws Throwable {
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(CHARSET));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    sgdGeneConverter.sgdGeneToTss(input, output, parameters);

    String[] lines = output.toString(CHARSET.name()).split(LINE_SEPARATOR);
    String[] headerColumns = lines[0].split(SEPARATOR, -1);
    assertEquals("SEQ_NAME", headerColumns[0]);
    assertEquals("START", headerColumns[1]);
    assertEquals("END", headerColumns[2]);
    assertEquals("STRAND", headerColumns[3]);
    assertEquals("ANNO_TAG", headerColumns[4]);
    int lineNumber = 1;
    for (String chromosome : genes.keySet()) {
      for (Gene gene : genes.get(chromosome)) {
        if (gene.strand.equals("-")) {
          continue;
        }
        String[] columns = lines[lineNumber++].split(SEPARATOR, -1);
        assertEquals(chromosome + ":" + gene.txStart, chromosome, columns[0]);
        assertEquals(chromosome + ":" + gene.txStart, gene.txStart, Long.parseLong(columns[1]));
        assertEquals(chromosome + ":" + gene.txStart, gene.txEnd, Long.parseLong(columns[2]));
        assertEquals(chromosome + ":" + gene.txStart, gene.strand, columns[3]);
        assertEquals(chromosome + ":" + gene.txStart, gene.name, columns[4]);
      }
    }
    for (String chromosome : genes.keySet()) {
      for (Gene gene : genes.get(chromosome)) {
        if (gene.strand.equals("+")) {
          continue;
        }
        String[] columns = lines[lineNumber++].split(SEPARATOR, -1);
        assertEquals(chromosome + ":" + gene.txStart, chromosome, columns[0]);
        assertEquals(chromosome + ":" + gene.txStart, gene.txStart, Long.parseLong(columns[1]));
        assertEquals(chromosome + ":" + gene.txStart, gene.txEnd, Long.parseLong(columns[2]));
        assertEquals(chromosome + ":" + gene.txStart, gene.strand, columns[3]);
        assertEquals(chromosome + ":" + gene.txStart, gene.name, columns[4]);
      }
    }
  }

  private static class Gene {
    int id;
    String name;
    String chromosome;
    String strand;
    long txStart;
    long txEnd;
    long cdsStart;
    long cdsEnd;
    int exonCount;
    long[] exonStarts;
    long[] exonEnds;
    String proteinId;
  }
}
