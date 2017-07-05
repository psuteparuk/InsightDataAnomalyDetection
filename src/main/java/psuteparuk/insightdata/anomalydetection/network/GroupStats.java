package psuteparuk.insightdata.anomalydetection.network;

import com.google.auto.value.AutoValue;

/**
 * An AutoValue immutable representing statistics of a group of purchases.
 * Current statistics required are the mean and sd of the amounts in the
 * group of purchases.
 */
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

    /**
     * @return threshold for an anomaly purchase
     */
    public double anomalyThreshold() {
        return mean() + 3.0 * sd();
    }
}
