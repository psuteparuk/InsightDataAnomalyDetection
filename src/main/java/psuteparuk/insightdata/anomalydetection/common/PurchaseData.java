package psuteparuk.insightdata.anomalydetection.common;

import com.google.auto.value.AutoValue;

import java.util.Date;

@AutoValue
public abstract class PurchaseData {
    public static PurchaseData create(
        double amount,
        Date timestamp
    ) {
        return new AutoValue_PurchaseData(
            amount,
            timestamp
        );
    }

    /**
     * AutoValue implementations
     */

    public abstract double amount();
    public abstract Date timestamp();
}
