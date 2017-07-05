package psuteparuk.insightdata.anomalydetection.common;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GroupStats {
    public static GroupStats create(
        double mean,
        double sd
    ) {
        return new AutoValue_GroupStats(
            mean,
            sd
        );
    }

    /**
     * AutoValue implementations
     */

    public abstract double mean();
    public abstract double sd();

    public double anomalyThreshold() {
        return mean() + 3.0 * sd();
    }
}
