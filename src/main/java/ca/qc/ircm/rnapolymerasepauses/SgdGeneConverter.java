package ca.qc.ircm.rnapolymerasepauses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

  protected SgdGeneConverter() {
  }

  /**
   * Converts SGD gene to TSS file.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read SGD gene file or write to output
   */
  public void sgdGeneToTss(SgdGeneToTssCommand parameters) throws IOException {
    List<Gene> genes = new ArrayList<>();
    try (BufferedReader reader = parameters.reader()) {
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
    try (BufferedWriter writer = parameters.writer()) {
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
