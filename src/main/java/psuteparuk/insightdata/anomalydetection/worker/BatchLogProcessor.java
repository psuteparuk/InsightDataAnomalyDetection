package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import psuteparuk.insightdata.anomalydetection.common.*;

import java.util.Comparator;

public class BatchLogProcessor extends LogProcessor {
    final UserNetwork userNetwork;

    public BatchLogProcessor(
        Observable<String> batchLogSource,
        UserNetwork userNetwork
    ) {
        super(batchLogSource);
        this.userNetwork = userNetwork;
    }

    @Override
    public void run() {
        getEntrySource()
            .filter((entry) -> entry.getEventType() == EventType.PURCHASE)
            .groupBy((entry) -> entry.getBuyerId())
            .flatMap((userPurchases$) -> userPurchases$
                .toSortedList(Comparator.comparing(EventEntry::getTimestamp))
                .toObservable()
            )
            .subscribe(
                (sortedUserPurchases) -> {
                    String buyerId = sortedUserPurchases.get(0).getBuyerId();
                    UserData buyerData = (this.userNetwork.contains(buyerId))
                        ? this.userNetwork.getData(buyerId)
                        : this.userNetwork.initializeData(buyerId);

                    sortedUserPurchases.forEach((purchaseEntry) -> {
                        PurchaseData purchaseData = PurchaseData.create(
                            purchaseEntry.getAmount(),
                            purchaseEntry.getTimestamp()
                        );
                        buyerData.addPurchase(purchaseData);
                        this.userNetwork.putNode(buyerId, buyerData);
                    });
                },
                Throwable::printStackTrace,
                () -> System.out.println("Finish purchase batch process.")
            );

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
