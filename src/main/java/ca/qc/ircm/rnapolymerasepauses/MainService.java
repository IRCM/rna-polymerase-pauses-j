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

import static ca.qc.ircm.rnapolymerasepauses.BedToTrackCommand.BED_TO_TRACK_COMMAND;
import static ca.qc.ircm.rnapolymerasepauses.PausesToTabsCommand.PAUSES_TO_TABS_COMMAND;
import static ca.qc.ircm.rnapolymerasepauses.SgdGeneToTssCommand.SGD_GENE_TO_TSS_COMMAND;
import static ca.qc.ircm.rnapolymerasepauses.WigToTrackCommand.WIG_TO_TRACK_COMMAND;

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
  @Value("${spring.runner.enabled}")
  private boolean runnerEnabled;

  protected MainService() {
  }

  protected MainService(BedConverter bedConverter, WigConverter wigConverter,
      PausesConverter pausesConverter, SgdGeneConverter sgdGeneConverter, boolean runnerEnabled) {
    this.bedConverter = bedConverter;
    this.wigConverter = wigConverter;
    this.pausesConverter = pausesConverter;
    this.sgdGeneConverter = sgdGeneConverter;
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
    PausesToTabsCommand pausesToTabsCommand = new PausesToTabsCommand();
    SgdGeneToTssCommand sgdGeneToTssCommand = new SgdGeneToTssCommand();
    JCommander command = JCommander.newBuilder().addObject(mainCommand)
        .addCommand(bedToTrackCommand).addCommand(wigToTrackCommand).addCommand(pausesToTabsCommand)
        .addCommand(sgdGeneToTssCommand).build();
    command.setCaseSensitiveOptions(false);
    try {
      command.parse(args);
      if (command.getParsedCommand() == null || mainCommand.help) {
        command.usage();
      } else if (command.getParsedCommand().equals(BED_TO_TRACK_COMMAND)) {
        if (bedToTrackCommand.help) {
          command.usage(BED_TO_TRACK_COMMAND);
        } else {
          bedToTrack(bedToTrackCommand);
        }
      } else if (command.getParsedCommand().equals(WIG_TO_TRACK_COMMAND)) {
        if (wigToTrackCommand.help) {
          command.usage(WIG_TO_TRACK_COMMAND);
        } else {
          wigToTrack(wigToTrackCommand);
        }
      } else if (command.getParsedCommand().equals(PAUSES_TO_TABS_COMMAND)) {
        if (pausesToTabsCommand.help) {
          command.usage(PAUSES_TO_TABS_COMMAND);
        } else {
          pausesToTabs(pausesToTabsCommand);
        }
      } else if (command.getParsedCommand().equals(SGD_GENE_TO_TSS_COMMAND)) {
        if (sgdGeneToTssCommand.help) {
          command.usage(SGD_GENE_TO_TSS_COMMAND);
        } else {
          sgdGeneToTss(sgdGeneToTssCommand);
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
      wigConverter.wigToTrack(System.in, System.out, command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse WIG file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }

  private void pausesToTabs(PausesToTabsCommand command) {
    logger.debug("Converts pauses to tab delimited");
    try {
      pausesConverter.pausesToTabs(System.in, System.out, command);
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
      sgdGeneConverter.sgdGeneToTss(System.in, System.out, command);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse SGD gene file");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
      e.printStackTrace();
    }
  }
}
