package psuteparuk.insightdata.anomalydetection.network;

import psuteparuk.insightdata.anomalydetection.network.PurchaseData;

import java.util.ArrayDeque;
import java.util.Deque;

public class UserData {
    final private String id;
    final private int trackedNumberOfPurchases;
    private Deque<PurchaseData> latestPurchases;

    public UserData(String id, int trackedNumberOfPurchases) {
        this.id = id;
        this.trackedNumberOfPurchases = trackedNumberOfPurchases;
        this.latestPurchases = new ArrayDeque<>(trackedNumberOfPurchases);
    }

    public String getId() {
        return this.id;
    }

    public Deque<PurchaseData> getLatestPurchases() {
        return this.latestPurchases;
    }

    public void addPurchase(PurchaseData purchaseData) {
        if (this.latestPurchases.size() >= this.trackedNumberOfPurchases) {
            this.latestPurchases.pollFirst();
        }
        this.latestPurchases.addLast(purchaseData);
    }
}
