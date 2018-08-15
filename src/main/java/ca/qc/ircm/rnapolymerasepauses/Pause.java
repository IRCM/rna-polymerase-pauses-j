package ca.qc.ircm.rnapolymerasepauses;

/**
 * A pause computed by Churchman program.
 */
public class Pause {
  public String name;
  public String chromosome;
  public int position;
  public double normalizedReads;
  public double foldsAboveAverage;
  public double beginningReads;
  public String sequence;

  @Override
  public String toString() {
    return "Pause [name=" + name + ", chromosome=" + chromosome + ", position=" + position
        + ", foldsAboveAverage=" + foldsAboveAverage + "]";
  }
}
