package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import psuteparuk.insightdata.anomalydetection.event.EventEntry;
import psuteparuk.insightdata.anomalydetection.event.EventType;
import psuteparuk.insightdata.anomalydetection.network.PurchaseData;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;

import java.util.Comparator;

/**
 * Process the batch log.
 * The caveat here is that the events in the batch log may not be in the right order.
 * The good thing is that in the real world, batch computation is not latency sensitive.
 * We can afford to first sort the list and then treat it as a stream log.
 *
 * The purchase event and befriend/unfriend events can be processed separately
 * since we keep no inter-node information apart from their "friend" relationship.
 * In fact, we can run both transformation on different threads.
 */
public class BatchLogProcessor extends LogProcessor {
    final private UserNetwork userNetwork;

    public BatchLogProcessor(
        Observable<String> batchLogSource,
        Scheduler scheduler,
        UserNetwork userNetwork
    ) {
        super(batchLogSource, scheduler);
        this.userNetwork = userNetwork;
    }

    @Override
    public void run() {
        setNetworkParameters();
        processPurchaseEvents();
        processRelationshipEvents();
    }

    /**
     * Update the User Network parameters states
     */
    private void setNetworkParameters() {
        getNetworkParametersSource()
            .subscribe((networkParameters) -> {
                this.userNetwork.setDepthDegree(networkParameters.getDepthDegree());
                this.userNetwork.setTrackedNumber(networkParameters.getTrackedNumber());
            });
    }

    /**
     * Purchases in batch log may not come in a timestamp-order manner.
     * To address this problem, we will group the purchase events by user ids,
     * and sort the events by timestamp. We can then add each purchase to
     * the network and keeping only the latest ones.
     */
    private void processPurchaseEvents() {
        getEntrySource()
            .filter((entry) -> entry.getEventType() == EventType.PURCHASE)
            .groupBy(EventEntry::getBuyerId)
            .flatMap((userPurchases$) -> userPurchases$
                .toSortedList(Comparator.comparing(EventEntry::getTimestamp))
                .toObservable()
            )
            .subscribe(
                (sortedUserPurchases) -> {
                    String buyerId = sortedUserPurchases.get(0).getBuyerId();
                    sortedUserPurchases.forEach((purchaseEntry) -> {
                        PurchaseData purchaseData = PurchaseData.create(
                            purchaseEntry.getAmount(),
                            purchaseEntry.getTimestamp()
                        );
                        this.userNetwork.addPurchase(buyerId, purchaseData);
                    });
                },
                Throwable::printStackTrace,
                () -> System.out.println("Finish purchase batch process.")
            );
    }

    /**
     * Relationship events can be processed independently from the purchase events.
     * We keep only local node information in the user network from the batch computation.
     * Here also, we sort the events by timestamp first and then update the relationship
     * in the network event by event.
     */
    private void processRelationshipEvents() {
        getEntrySource()
            .filter((entry) -> entry.getEventType() == EventType.BEFRIEND || entry.getEventType() == EventType.UNFRIEND)
            .toSortedList(Comparator.comparing(EventEntry::getTimestamp))
            .toObservable()
            .subscribe(
                (sortedUserRelationshipEvents) -> sortedUserRelationshipEvents.forEach((relationshipEvent) -> {
                    String user1Id = relationshipEvent.getUser1Id();
                    String user2Id = relationshipEvent.getUser2Id();

                    EventType eventType = relationshipEvent.getEventType();
                    if (eventType == EventType.BEFRIEND) {
                        this.userNetwork.befriend(user1Id, user2Id);
                    } else if (eventType == EventType.UNFRIEND) {
                        this.userNetwork.unfriend(user1Id, user2Id);
                    }
                }),
                Throwable::printStackTrace,
                () -> System.out.println("Finish relationship batch process.")
            );
    }
}
