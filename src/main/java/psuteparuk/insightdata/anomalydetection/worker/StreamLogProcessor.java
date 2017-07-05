package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import psuteparuk.insightdata.anomalydetection.common.GroupStats;
import psuteparuk.insightdata.anomalydetection.event.EventEntry;
import psuteparuk.insightdata.anomalydetection.io.FileEventWriter;
import psuteparuk.insightdata.anomalydetection.network.PurchaseData;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;

import java.text.DecimalFormat;

public class StreamLogProcessor extends LogProcessor {
    final private Subject<String> outputLogSink = BehaviorSubject.create();
    final private FileEventWriter fileEventWriter;
    final private UserNetwork userNetwork;

    final private DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    public StreamLogProcessor(
        Observable<String> streamLogSource,
        FileEventWriter fileEventWriter,
        Scheduler scheduler,
        UserNetwork userNetwork
    ) {
        super(streamLogSource, scheduler);
        this.fileEventWriter = fileEventWriter;
        this.userNetwork = userNetwork;
    }

    @Override
    public void run() {
        subscribeToOutput();
        processEntry();
    }

    private void subscribeToOutput() {
        this.outputLogSink
            .observeOn(Schedulers.io())
            .subscribe(fileEventWriter::write);
    }

    private void processEntry() {
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

        GroupStats groupStats = this.userNetwork.calculateGroupStats(buyerId);
        if (this.userNetwork.isPurchaseAnomaly(purchaseData, groupStats)) {
            this.outputLogSink.onNext(buildOutputMessage(entry, groupStats));
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

    private String buildOutputMessage(EventEntry entry, GroupStats groupStats) {
        StringBuilder outputEvent = new StringBuilder();
        outputEvent.append(entry.getOriginalMessage().trim());
        outputEvent.deleteCharAt(outputEvent.length() - 1);
        outputEvent.append(", \"mean\": \"");
        outputEvent.append(decimalFormat.format(groupStats.mean()));
        outputEvent.append("\", \"sd\": \"");
        outputEvent.append(decimalFormat.format(groupStats.sd()));
        outputEvent.append("\"}");
        return outputEvent.toString();
    }
}
