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

/**
 * Process the stream log.
 * We can assume that the events in the stream log come from some stream API
 * and therefore are in the correct time order.
 *
 * The purchase events and befriend/unfriend events cannot be processed independently.
 * This is because we need to calculate the "close" friends group for each buyer
 * using the state of whole network at that specific moment in time. That means the
 * relationships need to be up-to-date.
 */
public class StreamLogProcessor extends LogProcessor {
    // Emit strings that will be written to the output log
    final private Subject<String> outputLogSink = BehaviorSubject.create();
    final private FileEventWriter fileEventWriter;
    final private UserNetwork userNetwork;

    // Show numbers by only two decimal places
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

    /**
     * Push result to be written to output file
     */
    private void subscribeToOutput() {
        this.outputLogSink
            .observeOn(Schedulers.io())
            .subscribe(fileEventWriter::write);
    }

    /**
     * The purchase and befriend/unfriend events are processed in the same stream.
     *
     */
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
        // Update the tracked purchases of this event's buyer
        String buyerId = entry.getBuyerId();
        PurchaseData purchaseData = PurchaseData.create(entry.getAmount(), entry.getTimestamp());
        this.userNetwork.addPurchase(buyerId, purchaseData);

        // Calculate the "close" friend group stats (mean, sd).
        // If the purchase is anomaly, push the result to the output log.
        GroupStats groupStats = this.userNetwork.calculateGroupStats(buyerId);
        if (this.userNetwork.isPurchaseAnomaly(purchaseData, groupStats)) {
            this.outputLogSink.onNext(buildOutputMessage(entry, groupStats));
        }
    }

    /**
     * Befriend and Unfriend can be updated in constant time.
     */

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

    /**
     * @param entry
     * @param groupStats
     * @return String representation of the output event.
     */
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
