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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Main service.
 */
@Component
public class MainService implements CommandLineRunner {
  private static Logger logger = LoggerFactory.getLogger(MainService.class);
  @Inject
  private BedConverter bedConverter;
  @Inject
  private WigConverter wigConverter;
  @Inject
  private PausesConverter pausesConverter;
  @Inject
  private SgdGeneConverter sgdGeneConverter;
  @Inject
  private FakeGene fakeGene;
  @Value("${spring.runner.enabled}")
  private boolean runnerEnabled;

  protected MainService() {
  }

  protected MainService(BedConverter bedConverter, WigConverter wigConverter,
      PausesConverter pausesConverter, SgdGeneConverter sgdGeneConverter, FakeGene fakeGene,
      boolean runnerEnabled) {
    this.bedConverter = bedConverter;
    this.wigConverter = wigConverter;
    this.pausesConverter = pausesConverter;
    this.sgdGeneConverter = sgdGeneConverter;
    this.fakeGene = fakeGene;
    this.runnerEnabled = runnerEnabled;
  }

  /**
   * Launch sub-program.
   *
   * @param args
   *          command line arguments
   */
  @Override
  public void run(String... args) {
    if (!runnerEnabled) {
      return;
    }

    MainCommand mainCommand = new MainCommand();
    BedToTrackCommand bedToTrackCommand = new BedToTrackCommand();
    WigToTrackCommand wigToTrackCommand = new WigToTrackCommand();
    PausesToBedCommand pausesToBedCommand = new PausesToBedCommand();
    PausesToTabsCommand pausesToTabsCommand = new PausesToTabsCommand();
    SgdGeneToTssCommand sgdGeneToTssCommand = new SgdGeneToTssCommand();
    FakeGeneCommand fakeGeneCommand = new FakeGeneCommand();
    JCommander command = JCommander.newBuilder().addObject(mainCommand)
        .addCommand(bedToTrackCommand).addCommand(wigToTrackCommand).addCommand(pausesToBedCommand)
        .addCommand(pausesToTabsCommand).addCommand(sgdGeneToTssCommand).addCommand(fakeGeneCommand)
        .build();
    command.setCaseSensitiveOptions(false);
    try {
      command.parse(args);
      if (command.getParsedCommand() == null || mainCommand.help) {
        command.usage();
      } else if (command.getParsedCommand().equals(BedToTrackCommand.COMMAND)) {
        if (bedToTrackCommand.help) {
          command.usage(BedToTrackCommand.COMMAND);
        } else {
          bedToTrack(bedToTrackCommand);
        }
      } else if (command.getParsedCommand().equals(WigToTrackCommand.COMMAND)) {
        if (wigToTrackCommand.help) {
          command.usage(WigToTrackCommand.COMMAND);
        } else {
          wigToTrack(wigToTrackCommand);
        }
      } else if (command.getParsedCommand().equals(PausesToBedCommand.COMMAND)) {
        if (pausesToBedCommand.help) {
          command.usage(PausesToBedCommand.COMMAND);
        } else {
          pausesToBed(pausesToBedCommand);
        }
      } else if (command.getParsedCommand().equals(PausesToTabsCommand.COMMAND)) {
        if (pausesToTabsCommand.help) {
          command.usage(PausesToTabsCommand.COMMAND);
        } else {
          pausesToTabs(pausesToTabsCommand);
        }
      } else if (command.getParsedCommand().equals(SgdGeneToTssCommand.COMMAND)) {
        if (sgdGeneToTssCommand.help) {
          command.usage(SgdGeneToTssCommand.COMMAND);
        } else {
          sgdGeneToTss(sgdGeneToTssCommand);
        }
      } else if (command.getParsedCommand().equals(FakeGeneCommand.COMMAND)) {
        if (fakeGeneCommand.help) {
          command.usage(FakeGeneCommand.COMMAND);
        } else {
          fakeGene(fakeGeneCommand);
        }
      }
    } catch (ParameterException e) {
      System.err.println(e.getMessage() + "\n");
      command.usage();
    }
  }

  private void bedToTrack(BedToTrackCommand command) {
    logger.debug("Converts BED to track");
    try {
      bedConverter.bedToTrack(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse BED file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void wigToTrack(WigToTrackCommand command) {
    logger.debug("Converts WIG to track");
    try {
      wigConverter.wigToTrack(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse WIG file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void pausesToBed(PausesToBedCommand command) {
    logger.debug("Converts pauses to BED");
    try {
      pausesConverter.pausesToBed(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse pauses file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void pausesToTabs(PausesToTabsCommand command) {
    logger.debug("Converts pauses to tab delimited");
    try {
      pausesConverter.pausesToTabs(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse pauses file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void sgdGeneToTss(SgdGeneToTssCommand command) {
    logger.debug("Converts SGD gene to TSS");
    try {
      sgdGeneConverter.sgdGeneToTss(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse SGD gene file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void fakeGene(FakeGeneCommand command) {
    logger.debug("Generates a fake gene file covering all chromosomes");
    try {
      fakeGene.fakeGene(command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse chromosome sizes file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }
}
