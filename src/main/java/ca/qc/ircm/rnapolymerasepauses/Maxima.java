package ca.qc.ircm.rnapolymerasepauses;

import ca.qc.ircm.rnapolymerasepauses.io.PauseReader;
import ca.qc.ircm.rnapolymerasepauses.io.PauseWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Keeps only best pause inside a window.
 */
@Component
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
      while ((pause = reader.readPause()) != null) {
        if (gene == null) {
          gene = pause.name;
        }
        if (pause.name.equals(gene)) {
          pauses.add(pause);
        } else {
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
