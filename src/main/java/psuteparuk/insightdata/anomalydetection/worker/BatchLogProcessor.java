package psuteparuk.insightdata.anomalydetection.worker;

import io.reactivex.Observable;
import psuteparuk.insightdata.anomalydetection.common.EventType;

public class BatchLogProcessor extends LogProcessor {
    public BatchLogProcessor(Observable<String> batchLogSource) {
        super(batchLogSource);
    }

    @Override
    public void run() {
        getEntrySource()
            .filter((entry) -> entry.eventType == EventType.PURCHASE)
            .subscribe(
                (entry) -> System.out.println(entry.eventType.toString() + entry.amount.toString()),
                Throwable::printStackTrace,
                () -> System.out.println("finished")
            );
    }
}
