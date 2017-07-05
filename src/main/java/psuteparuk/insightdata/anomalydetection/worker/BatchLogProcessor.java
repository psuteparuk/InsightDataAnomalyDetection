package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import psuteparuk.insightdata.anomalydetection.event.EventEntry;
import psuteparuk.insightdata.anomalydetection.event.EventType;
import psuteparuk.insightdata.anomalydetection.network.PurchaseData;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;

import java.util.Comparator;

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

    private void setNetworkParameters() {
        getNetworkParametersSource()
            .subscribe((networkParameters) -> {
                this.userNetwork.setDepthDegree(networkParameters.getDepthDegree());
                this.userNetwork.setTrackedNumber(networkParameters.getTrackedNumber());
            });
    }

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
