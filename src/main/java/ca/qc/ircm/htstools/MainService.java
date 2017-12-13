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

package ca.qc.ircm.htstools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.inject.Inject;

/**
 * Main service.
 */
@Component
public class MainService implements CommandLineRunner {
  private static Logger logger = LoggerFactory.getLogger(MainService.class);
  @Inject
  private BedTransform bedTransform;
  @Value("${spring.runner.enabled}")
  private boolean runnerEnabled;

  protected MainService() {
  }

  protected MainService(BedTransform bedTransform, boolean runnerEnabled) {
    this.bedTransform = bedTransform;
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
    SetAnnotationSizeCommand setAnnotationSizeCommand = new SetAnnotationSizeCommand();
    JCommander command =
        JCommander.newBuilder().addObject(mainCommand).addCommand(setAnnotationSizeCommand).build();
    command.setCaseSensitiveOptions(false);
    try {
      command.parse(args);
      if (command.getParsedCommand() == null || mainCommand.help) {
        // Show help.
        command.usage();
      } else if (command.getParsedCommand()
          .equals(SetAnnotationSizeCommand.SET_ANNOTATION_SIZE_COMMAND)) {
        setAnnotationSize(setAnnotationSizeCommand);
      }
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      System.err.println();
      command.usage();
    }
  }

  private void setAnnotationSize(SetAnnotationSizeCommand setAnnotationSizeCommand) {
    logger.debug("Set annotation size with parameters {}", setAnnotationSizeCommand.size);
    try {
      bedTransform.setAnnotationSize(System.in, System.out, setAnnotationSizeCommand.size);
    } catch (NumberFormatException e) {
      System.err.println("Could not parse annotation sizes");
    } catch (IOException e) {
      System.err.println("Could not read input or write to output");
    }
  }
}
