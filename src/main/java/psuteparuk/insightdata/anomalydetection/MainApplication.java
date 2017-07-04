package psuteparuk.insightdata.anomalydetection;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import psuteparuk.insightdata.anomalydetection.common.Arguments;
import psuteparuk.insightdata.anomalydetection.io.FileStreamEventSource;
import psuteparuk.insightdata.anomalydetection.worker.BatchLogProcessor;

public class MainApplication {
    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);

        final Observable<String> batchLogSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.batchFilePath))
            .subscribeOn(Schedulers.io())
            .replay(1)
            .refCount();
        final Observable<String> streamLogSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.streamFilePath))
            .subscribeOn(Schedulers.io())
            .replay(1)
            .refCount();

        BatchLogProcessor batchLogProcessor = new BatchLogProcessor(batchLogSource);
        batchLogProcessor.run();

        sleep(3000);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
