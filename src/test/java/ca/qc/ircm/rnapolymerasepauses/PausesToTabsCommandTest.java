package ca.qc.ircm.rnapolymerasepauses;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class PausesToTabsCommandTest {
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private final InputStream systemIn = System.in;
  private final PrintStream systemOut = System.out;
  private PausesToTabsCommand command = new PausesToTabsCommand();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @After
  public void restoreSystemInputOutput() {
    System.setIn(systemIn);
    System.setOut(systemOut);
  }

  @Test
  public void reader() throws Throwable {
    Path file = temporaryFolder.newFile("file.txt").toPath();
    Files.write(file,
        IntStream.range(0, 2).mapToObj(i -> "test line " + i).collect(Collectors.toList()));
    command.input = file;

    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = command.reader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }

    assertEquals(2, lines.size());
    assertEquals("test line 0", lines.get(0));
    assertEquals("test line 1", lines.get(1));
  }

  @Test
  public void reader_System() throws Throwable {
    ByteArrayInputStream input =
        new ByteArrayInputStream("test line 0\ntest line 1".getBytes(CHARSET));
    System.setIn(input);

    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = command.reader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }

    assertEquals(2, lines.size());
    assertEquals("test line 0", lines.get(0));
    assertEquals("test line 1", lines.get(1));
  }

  @Test
  public void writer() throws Throwable {
    Path file = temporaryFolder.newFile("file.txt").toPath();
    command.output = file;

    try (BufferedWriter writer = command.writer()) {
      writer.write("test line 0");
      writer.write("\n");
      writer.write("test line 1");
    }

    List<String> lines = Files.readAllLines(file);
    assertEquals(2, lines.size());
    assertEquals("test line 0", lines.get(0));
    assertEquals("test line 1", lines.get(1));
  }

  @Test
  public void writer_System() throws Throwable {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));

    try (BufferedWriter writer = command.writer()) {
      writer.write("test line 0");
      writer.write("\n");
      writer.write("test line 1");
    }

    List<String> lines = Arrays.asList(output.toString(CHARSET.name()).split("\n"));
    assertEquals(2, lines.size());
    assertEquals("test line 0", lines.get(0));
    assertEquals("test line 1", lines.get(1));
  }
}
