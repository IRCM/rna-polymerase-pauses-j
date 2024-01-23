package ca.qc.ircm.rnapolymerasepauses;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class PausesConverterTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final int PAUSES_COUNT = 100000;
  private static final int PAUSES_PER_CHROMOSOME = PAUSES_COUNT / 18;
  private static final String PAUSE_NAME_MARKER = ">";
  private static final int PAUSE_NAME_LENGHT = 20;
  private static final int PAUSE_MAX_POSITION = 500;
  private static final double PAUSE_MAX_NORMALIZED_READS = 5;
  private static final double PAUSE_MAX_FOLDS_ABOVE_AVERAGE = 20;
  private static final double PAUSE_MAX_BEGINNING_READS = 4;
  private static final int PAUSE_SEQUENCE_LENGHT = 40;
  private static final String PAUSE_SEPARATOR = "_";
  private static final int GENE_MAX_POSITION = 1000000;
  private static final int GENE_MIN_LENGTH = 200;
  private static final int GENE_MAX_LENGTH = 1000;
  private static final String SEPARATOR = "\t";
  private static final double DELTA = 0.000000001;
  private PausesConverter pausesConverter;
  @Mock
  private PausesToBedCommand bedParameters;
  @Mock
  private PausesToTabsCommand tabsParameters;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private List<Pause> pauses;
  private Map<Pause, Gene> genes;
  private Random random;
  private String content;
  private String tssContent;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    pausesConverter = new PausesConverter();
    random = new Random();
    pauses = IntStream.range(0, PAUSES_COUNT)
        .mapToObj(i -> generatePause(i / PAUSES_PER_CHROMOSOME + 1)).collect(Collectors.toList());
    StringWriter output = new StringWriter();
    writePauses(pauses, output);
    content = output.toString();
  }

  private Pause generatePause(int chromosome) {
    Pause pause = new Pause();
    pause.name = RandomStringUtils.randomAlphanumeric(PAUSE_NAME_LENGHT);
    pause.chromosome = "chr" + chromosome;
    pause.position = random.nextInt(PAUSE_MAX_POSITION);
    pause.normalizedReads = random.nextDouble() * PAUSE_MAX_NORMALIZED_READS;
    pause.foldsAboveAverage = random.nextDouble() * PAUSE_MAX_FOLDS_ABOVE_AVERAGE;
    pause.beginningReads = random.nextDouble() * PAUSE_MAX_BEGINNING_READS;
    pause.sequence = RandomStringUtils.randomAlphabetic(PAUSE_SEQUENCE_LENGHT);
    return pause;
  }

  private void writePauses(Iterable<Pause> pauses, Writer output) throws IOException {
    for (Pause pause : pauses) {
      output.write(PAUSE_NAME_MARKER);
      output.write(pause.name);
      output.write(PAUSE_SEPARATOR);
      output.write(pause.chromosome);
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.position));
      if (pause.normalizedReads != null) {
        output.write(PAUSE_SEPARATOR);
        output.write(String.valueOf(pause.normalizedReads));
      }
      output.write(PAUSE_SEPARATOR);
      output.write(String.valueOf(pause.foldsAboveAverage));
      if (pause.beginningReads != null) {
        output.write(PAUSE_SEPARATOR);
        output.write(String.valueOf(pause.beginningReads));
      }
      output.write(LINE_SEPARATOR);
      if (pause.sequence != null) {
        output.write(pause.sequence);
        output.write(LINE_SEPARATOR);
      }
    }
  }

  private void generateGenes() throws IOException {
    genes = pauses.stream().collect(Collectors.toMap(pause -> pause, pause -> generateGene(pause)));
    StringWriter output = new StringWriter();
    writeGenes(genes.values(), output);
    tssContent = output.toString();
  }

  private Gene generateGene(Pause pause) {
    Gene gene = new Gene();
    gene.name = pause.name;
    gene.chromosome = pause.chromosome;
    gene.strand = random.nextBoolean() ? "+" : "-";
    gene.start = random.nextInt(GENE_MAX_POSITION);
    gene.end = gene.start + random.nextInt(GENE_MAX_LENGTH - GENE_MIN_LENGTH) + GENE_MIN_LENGTH;
    return gene;
  }

  private void writeGenes(Iterable<Gene> genes, Writer output) throws IOException {
    output.write("SEQ_NAME");
    output.write(SEPARATOR);
    output.write("START");
    output.write(SEPARATOR);
    output.write("END");
    output.write(SEPARATOR);
    output.write("STRAND");
    output.write(SEPARATOR);
    output.write("ANNO_TAG");
    output.write(LINE_SEPARATOR);
    for (Gene gene : genes) {
      output.write(gene.chromosome);
      output.write(SEPARATOR);
      output.write(String.valueOf(gene.start));
      output.write(SEPARATOR);
      output.write(String.valueOf(gene.end));
      output.write(SEPARATOR);
      output.write(gene.strand);
      output.write(SEPARATOR);
      output.write(gene.name);
      output.write(LINE_SEPARATOR);
    }
  }

  @Test
  public void pausesToBed() throws Throwable {
    generateGenes();
    when(bedParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(bedParameters.writer()).thenReturn(new BufferedWriter(writer));
    Path tss = temporaryFolder.newFile("tss.txt").toPath();
    Files.write(tss, Arrays.asList(tssContent.split(LINE_SEPARATOR)));
    bedParameters.tss = tss;

    pausesConverter.pausesToBed(bedParameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      Gene gene = genes.get(pause);
      String[] columns = lines[i].split(SEPARATOR);
      assertEquals(5, columns.length);
      assertEquals(pause.chromosome, columns[0]);
      assertEquals(gene.start + pause.position, Integer.parseInt(columns[1]));
      assertEquals(gene.start + pause.position + 1, Integer.parseInt(columns[2]));
      assertEquals(pause.name, columns[3]);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
    }
  }

  @Test
  public void pausesToBed_NoSequence() throws Throwable {
    pauses.forEach(pause -> pause.sequence = null);
    StringWriter contentAsWriter = new StringWriter();
    writePauses(pauses, contentAsWriter);
    content = contentAsWriter.toString();
    generateGenes();
    when(bedParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(bedParameters.writer()).thenReturn(new BufferedWriter(writer));
    Path tss = temporaryFolder.newFile("tss.txt").toPath();
    Files.write(tss, Arrays.asList(tssContent.split(LINE_SEPARATOR)));
    bedParameters.tss = tss;

    pausesConverter.pausesToBed(bedParameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      Gene gene = genes.get(pause);
      String[] columns = lines[i].split(SEPARATOR);
      assertEquals(5, columns.length);
      assertEquals(pause.chromosome, columns[0]);
      assertEquals(gene.start + pause.position, Integer.parseInt(columns[1]));
      assertEquals(gene.start + pause.position + 1, Integer.parseInt(columns[2]));
      assertEquals(pause.name, columns[3]);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
    }
  }

  @Test
  public void pausesToTabs() throws Throwable {
    when(tabsParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(tabsParameters.writer()).thenReturn(new BufferedWriter(writer));

    pausesConverter.pausesToTabs(tabsParameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      String[] columns = lines[i].split(SEPARATOR);
      assertEquals(7, columns.length);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
      assertEquals(pause.sequence, columns[6]);
    }
  }

  @Test
  public void pausesToTabs_NoSequence() throws Throwable {
    pauses.forEach(pause -> pause.sequence = null);
    StringWriter contentAsWriter = new StringWriter();
    writePauses(pauses, contentAsWriter);
    content = contentAsWriter.toString();
    when(tabsParameters.reader()).thenReturn(new BufferedReader(new StringReader(content)));
    StringWriter writer = new StringWriter();
    when(tabsParameters.writer()).thenReturn(new BufferedWriter(writer));

    pausesConverter.pausesToTabs(tabsParameters);

    String[] lines = writer.toString().split(LINE_SEPARATOR);
    for (int i = 0; i < pauses.size(); i++) {
      Pause pause = pauses.get(i);
      String[] columns = lines[i].split(SEPARATOR, -1);
      assertEquals(7, columns.length);
      assertEquals(pause.name, columns[0]);
      assertEquals(pause.chromosome, columns[1]);
      assertEquals(pause.position, Integer.parseInt(columns[2]));
      assertEquals(pause.normalizedReads, Double.parseDouble(columns[3]), DELTA);
      assertEquals(pause.foldsAboveAverage, Double.parseDouble(columns[4]), DELTA);
      assertEquals(pause.beginningReads, Double.parseDouble(columns[5]), DELTA);
      assertEquals("", columns[6]);
    }
  }

  private static class Pause {
    String name;
    String chromosome;
    int position;
    Double normalizedReads;
    double foldsAboveAverage;
    Double beginningReads;
    String sequence;
  }

  private static class Gene {
    String name;
    String chromosome;
    long start;
    long end;
    String strand;
  }
}
