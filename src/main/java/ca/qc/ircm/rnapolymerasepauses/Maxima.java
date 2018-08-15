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

import ca.qc.ircm.rnapolymerasepauses.io.PauseReader;
import ca.qc.ircm.rnapolymerasepauses.io.PauseWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Keeps only best pause inside a window.
 */
public class Maxima {
  /**
   * Keeps only best pause inside a window.
   *
   * @param parameters
   *          parameters
   * @throws IOException
   *           could not read pauses file or write to output
   */
  public void maxima(MaximaCommand parameters) throws IOException {
    try (PauseReader reader = new PauseReader(parameters.reader());
        PauseWriter writer = new PauseWriter(parameters.writer())) {
      String gene = null;
      List<Pause> pauses = new ArrayList<>();
      Pause pause;
      boolean first = true;
      while ((pause = reader.readPause()) != null) {
        if (gene == null) {
          gene = pause.name;
        }
        if (pause.name.equals(gene)) {
          pauses.add(pause);
        } else {
          if (first) {
            first = false;
            System.out.println(pauses);
            System.out.println(maxima(pauses, parameters.windowSize));
          }
          pauses = maxima(pauses, parameters.windowSize);
          for (Pause pa : pauses) {
            writer.writePause(pa);
          }
          gene = pause.name;
          pauses.clear();
          pauses.add(pause);
        }
      }
      pauses = maxima(pauses, parameters.windowSize);
      for (Pause pa : pauses) {
        writer.writePause(pa);
      }
    }
  }

  private List<Pause> maxima(List<Pause> pauses, int window) {
    List<Pause> maxima = new ArrayList<>();
    for (Pause pause : pauses) {
      if (window(pauses, pause.position, window)
          .filter(pa -> pa.foldsAboveAverage > pause.foldsAboveAverage).count() == 0) {
        maxima.add(pause);
      }
    }
    return maxima;
  }

  private Stream<Pause> window(List<Pause> pauses, int position, int window) {
    int start = position - window;
    int end = position + window;
    return pauses.stream().filter(pause -> pause.position >= start && pause.position <= end);
  }
}
