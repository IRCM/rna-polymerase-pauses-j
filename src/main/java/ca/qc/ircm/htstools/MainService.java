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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.inject.Inject;

/**
 * Main service.
 */
@Component
public class MainService implements CommandLineRunner {
  private static final String RUNNER_ENABLED = "spring.runner.enabled";
  private static Logger logger = LoggerFactory.getLogger(MainService.class);
  @Inject
  private TrimBedEnd trimBedEnd;
  @Inject
  private Environment env;

  protected MainService() {
  }

  /**
   * Launch sub-program.
   *
   * @param args
   *          command line arguments
   */
  @Override
  public void run(String... args) {
    if (env.containsProperty(RUNNER_ENABLED)) {
      if (!Boolean.valueOf(env.getProperty(RUNNER_ENABLED))) {
        return;
      }
    }

    trimBedEnd(args);
  }

  private void trimBedEnd(String... args) {
    TrimBedEndParameters parameters = new TrimBedEndParameters();
    parameters.sizeFromStart = Integer.parseInt(args[1]);
    try {
      trimBedEnd.trimBedEnd(System.in, System.out, parameters);
    } catch (IOException e) {
      System.err.println("Could not trim BED end");
    }
  }
}
