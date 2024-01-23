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
  private Maxima maxima;
  @Mock
  private SgdGeneConverter sgdGeneConverter;
  @Mock
  private FakeGene fakeGene;
  @Captor
  private ArgumentCaptor<BedToTrackCommand> bedToTrackCommandCaptor;
  @Captor
  private ArgumentCaptor<WigToTrackCommand> wigToTrackCommandCaptor;
  @Captor
  private ArgumentCaptor<PausesToBedCommand> pausesToBedCommandCaptor;
  @Captor
  private ArgumentCaptor<PausesToTabsCommand> pausesToTabsCommandCaptor;
  @Captor
  private ArgumentCaptor<MaximaCommand> maximaCommandCaptor;
  @Captor
  private ArgumentCaptor<SgdGeneToTssCommand> sgdGeneToTssCommandCaptor;
  @Captor
  private ArgumentCaptor<FakeGeneCommand> fakeGeneCommandCaptor;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void beforeTest() {
    mainService = new MainService(bedConverter, wigConverter, pausesConverter, maxima,
        sgdGeneConverter, fakeGene, true);
  }

  @Test
  public void run_RunnerDisabled() {
    mainService = new MainService(bedConverter, wigConverter, pausesConverter, maxima,
        sgdGeneConverter, fakeGene, false);
    mainService.run(new String[] { COMMAND, "-s", "1" });
    verifyZeroInteractions(bedConverter);
    verifyZeroInteractions(wigConverter);
    verifyZeroInteractions(pausesConverter);
    verifyZeroInteractions(maxima);
    verifyZeroInteractions(sgdGeneConverter);
    verifyZeroInteractions(fakeGene);
  }

  @Test
  public void run_Help() {
    mainService.run("-h");
    verifyZeroInteractions(bedConverter);
    verifyZeroInteractions(wigConverter);
    verifyZeroInteractions(pausesConverter);
    verifyZeroInteractions(maxima);
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
  public void run_BedToTrack_ChromosomeSizesMissing() throws Throwable {
    mainService.run(new String[] { BedToTrackCommand.COMMAND });
    verify(bedConverter, never()).bedToTrack(any());
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
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_ChromosomeSizesMissing() throws Throwable {
    mainService.run(new String[] { WigToTrackCommand.COMMAND });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_WigToTrack_ChromosomeSizesLongName() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(
        new String[] { WigToTrackCommand.COMMAND, "--chromoseSizes", chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_ChromosomeSizesNotExists() throws Throwable {
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-s", chromosomeSizes.toString() });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_WigToTrack_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-i", input.toString(), "-s",
        chromosomeSizes.toString() });
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
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "--input", input.toString(), "-s",
        chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(input, wigToTrackCommandCaptor.getValue().input);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-i", input.toString(), "-s",
        chromosomeSizes.toString() });
    verify(wigConverter, never()).wigToTrack(any());
  }

  @Test
  public void run_WigToTrack_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-o", output.toString(), "-s",
        chromosomeSizes.toString() });
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
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "--output", output.toString(), "-s",
        chromosomeSizes.toString() });
    verify(wigConverter).wigToTrack(wigToTrackCommandCaptor.capture());
    assertEquals(output, wigToTrackCommandCaptor.getValue().output);
    assertEquals(chromosomeSizes, wigToTrackCommandCaptor.getValue().chromosomeSizes);
  }

  @Test
  public void run_WigToTrack_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Path chromosomeSizes = temporaryFolder.getRoot().toPath().resolve("chromosomeSizes.txt");
    Files.createFile(chromosomeSizes);
    mainService.run(new String[] { WigToTrackCommand.COMMAND, "-o", output.toString(), "-s",
        chromosomeSizes.toString() });
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
  public void run_PausesToBed() throws Throwable {
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "-t", tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_TssMissing() throws Throwable {
    mainService.run(new String[] { PausesToBedCommand.COMMAND });
    verify(pausesConverter, never()).pausesToBed(any());
  }

  @Test
  public void run_PausesToBed_TssLongName() throws Throwable {
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "--tss", tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_TssNotExists() throws Throwable {
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "-t", tss.toString() });
    verify(pausesConverter, never()).pausesToBed(any());
  }

  @Test
  public void run_PausesToBed_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(
        new String[] { PausesToBedCommand.COMMAND, "-i", input.toString(), "-t", tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(input, pausesToBedCommandCaptor.getValue().input);
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "--input", input.toString(), "-t",
        tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(input, pausesToBedCommandCaptor.getValue().input);
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(
        new String[] { PausesToBedCommand.COMMAND, "-i", input.toString(), "-t", tss.toString() });
    verify(pausesConverter, never()).pausesToBed(any());
  }

  @Test
  public void run_PausesToBed_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(
        new String[] { PausesToBedCommand.COMMAND, "-o", output.toString(), "-t", tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(output, pausesToBedCommandCaptor.getValue().output);
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "--output", output.toString(), "-t",
        tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(output, pausesToBedCommandCaptor.getValue().output);
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Path tss = temporaryFolder.getRoot().toPath().resolve("tss.txt");
    Files.createFile(tss);
    mainService.run(
        new String[] { PausesToBedCommand.COMMAND, "-o", output.toString(), "-t", tss.toString() });
    verify(pausesConverter).pausesToBed(pausesToBedCommandCaptor.capture());
    assertEquals(output, pausesToBedCommandCaptor.getValue().output);
    assertEquals(tss, pausesToBedCommandCaptor.getValue().tss);
  }

  @Test
  public void run_PausesToBed_Help() throws Throwable {
    mainService.run(new String[] { PausesToBedCommand.COMMAND, "-h" });
    verify(pausesConverter, never()).pausesToBed(any());
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
  public void run_Maxima() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_WindowMissing() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_WindowLongName() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "--window", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_WindowBelowMinimum() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-w", "0" });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_NegativeWindow() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-w", "-1" });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_InvalidWindow() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-w", "a" });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_DoubleWindow() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-w", "1.2" });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { MaximaCommand.COMMAND, "-i", input.toString(), "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(input, maximaCommandCaptor.getValue().input);
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService
        .run(new String[] { MaximaCommand.COMMAND, "--input", input.toString(), "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(input, maximaCommandCaptor.getValue().input);
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { MaximaCommand.COMMAND, "-i", input.toString(), "-w", "20" });
    verify(maxima, never()).maxima(any());
  }

  @Test
  public void run_Maxima_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { MaximaCommand.COMMAND, "-o", output.toString(), "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(output, maximaCommandCaptor.getValue().output);
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService
        .run(new String[] { MaximaCommand.COMMAND, "--output", output.toString(), "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(output, maximaCommandCaptor.getValue().output);
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { MaximaCommand.COMMAND, "-o", output.toString(), "-w", "20" });
    verify(maxima).maxima(maximaCommandCaptor.capture());
    assertEquals(output, maximaCommandCaptor.getValue().output);
    assertEquals(20, maximaCommandCaptor.getValue().windowSize);
  }

  @Test
  public void run_Maxima_Help() throws Throwable {
    mainService.run(new String[] { MaximaCommand.COMMAND, "-h" });
    verify(maxima, never()).maxima(any());
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
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_Padding() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-p", "10" });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(10, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_PaddingLongName() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "--padding", "10" });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(10, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_PaddingZero() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-p", "0" });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(0, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_PaddingNegative() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-p", "-2" });
    verify(fakeGene, never()).fakeGene(any());
  }

  @Test
  public void run_FakeGene_PaddingInvalid() throws Throwable {
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-p", "a" });
    verify(fakeGene, never()).fakeGene(any());
  }

  @Test
  public void run_FakeGene_Input() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-i", input.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(input, fakeGeneCommandCaptor.getValue().input);
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_InputLongName() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    Files.createFile(input);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "--input", input.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(input, fakeGeneCommandCaptor.getValue().input);
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_InputNotExists() throws Throwable {
    Path input = temporaryFolder.getRoot().toPath().resolve("input.txt");
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-i", input.toString() });
    verify(fakeGene, never()).fakeGene(any());
  }

  @Test
  public void run_FakeGene_Output() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-o", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_OutputLongName() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    Files.createFile(output);
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "--output", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
  }

  @Test
  public void run_FakeGene_OutputNotExists() throws Throwable {
    Path output = temporaryFolder.getRoot().toPath().resolve("output.txt");
    mainService.run(new String[] { FakeGeneCommand.COMMAND, "-o", output.toString() });
    verify(fakeGene).fakeGene(fakeGeneCommandCaptor.capture());
    assertEquals(output, fakeGeneCommandCaptor.getValue().output);
    assertEquals(2, fakeGeneCommandCaptor.getValue().padding);
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
    verifyZeroInteractions(maxima);
    verifyZeroInteractions(sgdGeneConverter);
    verifyZeroInteractions(fakeGene);
  }
}
