package psuteparuk.insightdata.anomalydetection.network;

import com.google.auto.value.AutoValue;

import java.util.Date;
import java.util.List;

/**
 * An AutoValue immutable representing a purchase data including
 * {@amount} and {@timestamp}.
 */
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

    /**
     * @param purchaseDataList
     * @return the mean of amounts for the list of purchase data
     */
    public static double calculateMean(List<PurchaseData> purchaseDataList) {
        double sum = 0.0;
        for (PurchaseData purchaseData : purchaseDataList) {
            sum += purchaseData.amount();
        }
        return sum / purchaseDataList.size();
    }

    /**
     * @param purchaseDataList
     * @return the sd of amounts for the list of purchase data
     */
    public static double calculateStandardDeviation(List<PurchaseData> purchaseDataList) {
        double sumOfSquares = 0.0;
        for (PurchaseData purchaseData : purchaseDataList) {
            sumOfSquares += Math.pow(purchaseData.amount(), 2);
        }
        double mean = calculateMean(purchaseDataList);
        return Math.sqrt(sumOfSquares / purchaseDataList.size() - Math.pow(mean, 2));
    }
}
