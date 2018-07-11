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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allows to return score per position from a BED file.
 *
 * <p>
 * File must be sorted.
 * </p>
 */
public class BedPositionScores {
  private static final String COLUMN_SEPARATOR = "\t";
  private static final String BROWSER_PATTERN = "^browser( .*)?$";
  private static final String TRACK_PATTERN = "^track( .*)?$";
  private static final String COMMENT = "#";
  /**
   * BED file.
   */
  private Path input;
  /**
   * Current chromosome.
   */
  private String currentChromosome;
  /**
   * Data for current chromosome.
   */
  private List<BedData> datas = new ArrayList<>();

  public BedPositionScores(Path input) {
    this.input = input;
  }

  /**
   * Returns score of data at position from BED file, or {@link Double#NaN} if position cannot be
   * found on chromosome.
   *
   * @param chromosome
   *          chromosome
   * @param position
   *          position
   * @return score of data at position from BED file, or {@link Double#NaN} if position cannot be
   *         found on chromosome
   * @throws IOException
   *           cannot read BED file
   */
  public double score(String chromosome, long position) throws IOException {
    fillData(chromosome);
    return datas.stream().filter(data -> data.containsPosition(position)).findFirst()
        .map(data -> data.score).orElse(Double.NaN);
  }

  private void fillData(String chromosome) throws IOException {
    if (currentChromosome != null && currentChromosome.equals(chromosome)) {
      // Nothing to do.
      return;
    }
    datas.clear();
    Pattern browserPattern = Pattern.compile(BROWSER_PATTERN);
    Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
    datas = Files.lines(input)
        .filter(line -> !browserPattern.matcher(line).matches()
            && !trackPattern.matcher(line).matches() && !line.startsWith(COMMENT))
        .map(line -> data(line.split(COLUMN_SEPARATOR, -1)))
        .filter(data -> data.chromosome.equals(chromosome)).collect(Collectors.toList());
    currentChromosome = chromosome;
  }

  private BedData data(String[] columns) {
    BedData data = new BedData();
    data.chromosome = columns[0];
    data.start = Long.parseLong(columns[1]);
    data.end = Long.parseLong(columns[2]);
    data.score = Double.parseDouble(columns[3]);
    return data;
  }

  private class BedData {
    String chromosome;
    long start;
    long end;
    double score;

    boolean containsPosition(long position) {
      return position >= start && position < end;
    }

    @Override
    public String toString() {
      return "BedData [chromosome=" + chromosome + ", start=" + start + ", end=" + end + ", score="
          + score + "]";
    }
  }
}
