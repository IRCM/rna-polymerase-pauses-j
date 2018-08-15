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

import static ca.qc.ircm.rnapolymerasepauses.BedToTrackCommand.COMMAND;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MainServiceTest {
  private MainService mainService;
  @Mock
  private BedConverter bedConverter;
  @Mock
  private WigConverter wigConverter;
  @Mock
  private PausesConverter pausesConverter;
  @Mock
  private SgdGeneConverter sgdGeneConverter;
  @Mock
  private FakeGene fakeGene;
  @Captor
  private ArgumentCaptor<BedToTrackCommand> bedToTrackCommandCaptor;
  @Captor
  private ArgumentCaptor<WigToTrackCommand> wigToTrackCommandCaptor;
  @Captor
  private ArgumentCaptor<PausesToTabsCommand> pausesToTabsCommandCaptor;
  @Captor
  private ArgumentCaptor<SgdGeneToTssCommand> sgdGeneToTssCommandCaptor;
  @Captor
  private ArgumentCaptor<FakeGeneCommand> fakeGeneCommandCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    mainService = new MainService(bedConverter, wigConverter, pausesConverter, sgdGeneConverter,
        fakeGene, true);
  }

  @Test
  public void run_RunnerDisabled() {
    mainService = new MainService(bedConverter, wigConverter, pausesConverter, sgdGeneConverter,
        fakeGene, false);
    mainService.run(new String[] { COMMAND, "-s", "1" });
    verifyZeroInteractions(bedConverter);
    verifyZeroInteractions(wigConverter);
    verifyZeroInteractions(pausesConverter);
    verifyZeroInteractions(sgdGeneConverter);
    verifyZeroInteractions(fakeGene);
  }

  @Test
  public void run_Help() {
    mainService.run("-h");
    verifyZeroInteractions(bedConverter);
    verifyZeroInteractions(wigConverter);
    verifyZeroInteractions(pausesConverter);
    verifyZeroInteractions(sgdGeneConverter);
    verifyZeroInteractions(fakeGene);
  }

  @Test
  public void run_BedToTrack() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_ChromosomeSizesLongName() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(
        new String[] { BedToTrackCommand.COMMAND, "--chromoseSizes", chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_ChromosomeSizesNotExists() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(bedConverter, never()).bedToTrack(any());
  }

  @Test
  public void run_BedToTrack_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-i", input.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(input, bedToTrackCommandCaptor.getValue().input);
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "--input", input.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(input, bedToTrackCommandCaptor.getValue().input);
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-i", input.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter, never()).bedToTrack(any());
  }

  @Test
  public void run_BedToTrack_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-o", output.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(output, bedToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "--output", output.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(output, bedToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-o", output.toString(), "-s",
        chromosomeSizes.toString() });
    verify(bedConverter).bedToTrack(bedToTrackCommandCaptor.capture());
    assertEquals(output, bedToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, bedToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_BedToTrack_Help() throws Throwable {
    mainService.run(new String[] { BedToTrackCommand.COMMAND, "-h" });
    verify(bedConverter, never()).bedToTrack(any());
  }

  @Test
  public void run_WigToTrack() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(
        new String[] { WigToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_ChromosomeSizesLongName() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "--chromoseSizes",
        chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_ChromosomeSizesNotExists() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    mainService.run(
        new String[] { WigToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_WigToTrack_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-i", input.toString(),
        "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(input, wigToTrackCommandCaptor.getValue().input);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "--input",
        input.toString(), "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(input, wigToTrackCommandCaptor.getValue().input);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-i", input.toString(),
        "-s", chromosomeSizes.toString() });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_WigToTrack_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-o", output.toString(),
        "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(output, wigToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "--output",
        output.toString(), "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(output, wigToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-o", output.toString(),
        "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(output, wigToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_Help() throws Throwable {
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-h" });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_PausesToTabs() throws Throwable {
    mainService.run(new String[] { PausesToTabsCommand.COMMAND });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
  }

  @Test
  public void run_PausesToTabs_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "-i", input.toString() });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
    assertEquals(input, pausesToTabsCommandCaptor.getValue().input);
  }

  @Test
  public void run_PausesToTabs_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "--input", input.toString() });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
    assertEquals(input, pausesToTabsCommandCaptor.getValue().input);
  }

  @Test
  public void run_PausesToTabs_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "-i", input.toString() });
    verify(pausesConverter, never()).pausesToTabs(any());
  }

  @Test
  public void run_PausesToTabs_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "-o", output.toString() });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
    assertEquals(output, pausesToTabsCommandCaptor.getValue().output);
  }

  @Test
  public void run_PausesToTabs_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "--output", output.toString() });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
    assertEquals(output, pausesToTabsCommandCaptor.getValue().output);
  }

  @Test
  public void run_PausesToTabs_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "-o", output.toString() });
    verify(pausesConverter).pausesToTabs(pausesToTabsCommandCaptor.capture());
    assertEquals(output, pausesToTabsCommandCaptor.getValue().output);
  }

  @Test
  public void run_PausesToTabs_Help() throws Throwable {
    mainService.run(new String[] { PausesToTabsCommand.COMMAND, "-h" });
    verify(pausesConverter, never()).pausesToTabs(any());
  }

  @Test
  public void run_SgdGeneToTss() throws Throwable {
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
  }

  @Test
  public void run_SgdGeneToTss_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "-i", input.toString() });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
    assertEquals(input, sgdGeneToTssCommandCaptor.getValue().input);
  }

  @Test
  public void run_SgdGeneToTss_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "--input", input.toString() });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
    assertEquals(input, sgdGeneToTssCommandCaptor.getValue().input);
  }

  @Test
  public void run_SgdGeneToTss_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "-i", input.toString() });
    verify(sgdGeneConverter, never()).sgdGeneToTss(any());
  }

  @Test
  public void run_SgdGeneToTss_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "-o", output.toString() });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
    assertEquals(output, sgdGeneToTssCommandCaptor.getValue().output);
  }

  @Test
  public void run_SgdGeneToTss_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "--output", output.toString() });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
    assertEquals(output, sgdGeneToTssCommandCaptor.getValue().output);
  }

  @Test
  public void run_SgdGeneToTss_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "-o", output.toString() });
    verify(sgdGeneConverter).sgdGeneToTss(sgdGeneToTssCommandCaptor.capture());
    assertEquals(output, sgdGeneToTssCommandCaptor.getValue().output);
  }

  @Test
  public void run_SgdGeneToTss_Help() throws Throwable {
    mainService.run(new String[] { SgdGeneToTssCommand.COMMAND, "-h" });
    verify(sgdGeneConverter, never()).sgdGeneToTss(any());
  }

  @Test
  public void run_FakeGene() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
  }

  @Test
  public void run_FakeGene_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-i", input.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(input, fakeGeneCommandCaptor.getValue().input);
  }

  @Test
  public void run_FakeGene_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "--input", input.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(input, fakeGeneCommandCaptor.getValue().input);
  }

  @Test
  public void run_FakeGene_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-i", input.toString() });
    verify(sgdGeneConverter, never()).sgdGeneToTss(any());
  }

  @Test
  public void run_FakeGene_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-o", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
  }

  @Test
  public void run_FakeGene_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "--output", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
  }

  @Test
  public void run_FakeGene_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-o", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
  }

  @Test
  public void run_FakeGene_Help() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-h" });
    verify(fakeGene, never()).fakeGene(any());
  }

  @Test
  public void run_Other() throws Throwable {
    mainService.run(new String[] { "other" });
    verifyZeroInteractions(bedConverter);
    verifyZeroInteractions(wigConverter);
    verifyZeroInteractions(pausesConverter);
    verifyZeroInteractions(sgdGeneConverter);
    verifyZeroInteractions(fakeGene);
  }
}
