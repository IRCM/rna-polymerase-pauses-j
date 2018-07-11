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

package ca.qc.ircm.bedtools;

import ca.qc.ircm.bedtools.command.FileExistsValidation;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import com.beust.jcommander.validators.PositiveInteger;
import java.nio.file.Path;

/**
 * Asses pause sites for RNA polymerase.
 */
@Parameters(
    separators = " =",
    commandNames = AssesPauseSitesCommand.ASSES_PAUSE_SITES_COMMAND,
    commandDescription = "Asses RNA polymerase pause sites, "
        + "BED file must be generated using \"bedtools genomecov -bga\"")
public class AssesPauseSitesCommand {
  public static final String ASSES_PAUSE_SITES_COMMAND = "assespausesites";

  @Parameter(names = { "-h", "-help", "--h", "--help" }, description = "Show help", help = true)
  public boolean help = false;
  @Parameter(
      names = { "-i" },
      description = "Input BED file, must be generated using \"bedtools genomecov -bga\"",
      required = true,
      converter = PathConverter.class,
      validateWith = FileExistsValidation.class)
  public Path input;
  @Parameter(
      names = { "-w", "--window" },
      description = "Size of window around annotation position"
          + " on which to compute mean and standard deviation",
      required = false,
      validateWith = PositiveInteger.class)
  public int window = 200;
  @Parameter(
      names = { "-std" },
      description = "Number of standard deviation above which position is considered significant",
      required = false)
  public double standardDeviationFactor = 3.0;
}
