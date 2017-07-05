package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import psuteparuk.insightdata.anomalydetection.event.EventEntry;
import psuteparuk.insightdata.anomalydetection.network.PurchaseData;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;

public class StreamLogProcessor extends LogProcessor {
    final private UserNetwork userNetwork;

    public StreamLogProcessor(
        Observable<String> streamLogSource,
        Scheduler scheduler,
        UserNetwork userNetwork
    ) {
        super(streamLogSource, scheduler);
        this.userNetwork = userNetwork;
    }

    @Override
    public void run() {
        getEntrySource()
            .subscribe(
                (entry) -> {
                    switch (entry.getEventType()) {
                        case PURCHASE:
                            processPurchaseEntry(entry);
                            break;
                        case BEFRIEND:
                            processBefriendEntry(entry);
                            break;
                        case UNFRIEND:
                            processUnfriendEntry(entry);
                            break;
                        default:
                    }
                },
                Throwable::printStackTrace,
                () -> System.out.println("Finish stream process.")
            );
    }

    private void processPurchaseEntry(EventEntry entry) {
        String buyerId = entry.getBuyerId();
        PurchaseData purchaseData = PurchaseData.create(entry.getAmount(), entry.getTimestamp());
        this.userNetwork.addPurchase(buyerId, purchaseData);

        if (this.userNetwork.isPurchaseAnomaly(buyerId, purchaseData)) {
            System.out.println("YAY: flagged buyerId: " + buyerId + ", amount: " + entry.getAmount());
        }
    }

    private void processBefriendEntry(EventEntry entry) {
        String user1Id = entry.getUser1Id();
        String user2Id = entry.getUser2Id();
        this.userNetwork.befriend(user1Id, user2Id);
    }

    private void processUnfriendEntry(EventEntry entry) {
        String user1Id = entry.getUser1Id();
        String user2Id = entry.getUser2Id();
        this.userNetwork.unfriend(user1Id, user2Id);
    }
}
