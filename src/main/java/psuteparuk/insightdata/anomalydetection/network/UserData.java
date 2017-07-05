package psuteparuk.insightdata.anomalydetection.network;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Store user's related data.
 * This includes the latest purchases this user made.
 * We don't need random access for the list of these track purchases
 * so we store them in a linked list.
 * In fact, we use the ArrayDeque class, which implements the Deque class.
 * The number of tracked purchases are bounded so the memory used is constant.
 * As we push the new purchase into the tail of the ArrayDeque, we pop the
 * one on the other end, which is the oldest on in the list.
 */
public class UserData {
    final private String id;
    final private int trackedNumberOfPurchases;
    private Deque<PurchaseData> latestPurchases;

    public UserData(String id, int trackedNumberOfPurchases) {
        this.id = id;
        this.trackedNumberOfPurchases = trackedNumberOfPurchases;
        // Store the lastest purchase list as an ArrayDeque (optimized LinkedList)
        this.latestPurchases = new ArrayDeque<>(trackedNumberOfPurchases);
    }

    public String getId() {
        return this.id;
    }

    public Deque<PurchaseData> getLatestPurchases() {
        return this.latestPurchases;
    }

    /**
     * Add the new purchase data onto the list. Pop the oldest one if necessary
     * to keep the number of tracked purchase constant.
     * @param purchaseData
     */
    public void addPurchase(PurchaseData purchaseData) {
        if (this.latestPurchases.size() >= this.trackedNumberOfPurchases) {
            this.latestPurchases.pollFirst();
        }
        this.latestPurchases.addLast(purchaseData);
    }
}
