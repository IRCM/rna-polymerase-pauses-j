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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * SGD gene converter.
 */
@Component
public class SgdGeneConverter {
  private static final String LINE_SEPARATOR = "\n";
  private static final String COLUMN_SEPARATOR = "\t";
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  protected SgdGeneConverter() {
  }

  /**
   * Converts SGD gene to TSS file.
   *
   * @param input
   *          SGD gene file
   * @param output
   *          output
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read SGD gene file or write to output
   */
  public void sgdGeneToTss(InputStream input, OutputStream output, SgdGeneToTssCommand parameters)
      throws IOException {
    List<Gene> genes = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, CHARSET))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        Gene gene = new Gene();
        gene.name = columns[1];
        gene.chromosome = columns[2];
        gene.strand = columns[3];
        gene.start = Long.parseLong(columns[4]);
        gene.end = Long.parseLong(columns[5]);
        genes.add(gene);
      }
    }
    Collections.sort(genes, (gene1, gene2) -> {
      int compare = gene1.strand.compareTo(gene2.strand);
      compare = compare == 0 ? gene1.chromosome.compareTo(gene2.chromosome) : compare;
      compare = compare == 0 ? Long.compare(gene1.start, gene2.start) : compare;
      compare = compare == 0 ? Long.compare(gene1.end, gene2.end) : compare;
      return compare;
    });
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, CHARSET))) {
      writer.write("SEQ_NAME");
      writer.write(COLUMN_SEPARATOR);
      writer.write("START");
      writer.write(COLUMN_SEPARATOR);
      writer.write("END");
      writer.write(COLUMN_SEPARATOR);
      writer.write("STRAND");
      writer.write(COLUMN_SEPARATOR);
      writer.write("ANNO_TAG");
      writer.write(LINE_SEPARATOR);
      for (Gene gene : genes) {
        writer.write(gene.chromosome);
        writer.write(COLUMN_SEPARATOR);
        writer.write(String.valueOf(gene.start));
        writer.write(COLUMN_SEPARATOR);
        writer.write(String.valueOf(gene.end));
        writer.write(COLUMN_SEPARATOR);
        writer.write(gene.strand);
        writer.write(COLUMN_SEPARATOR);
        writer.write(gene.name);
        writer.write(LINE_SEPARATOR);
      }
    }
  }

  private static class Gene {
    String name;
    String chromosome;
    long start;
    long end;
    String strand;
  }
}
