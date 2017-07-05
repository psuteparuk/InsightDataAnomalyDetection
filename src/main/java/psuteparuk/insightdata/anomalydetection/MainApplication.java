package psuteparuk.insightdata.anomalydetection;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import psuteparuk.insightdata.anomalydetection.common.Arguments;
import psuteparuk.insightdata.anomalydetection.io.FileStreamEventSource;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;
import psuteparuk.insightdata.anomalydetection.worker.BatchLogProcessor;
import psuteparuk.insightdata.anomalydetection.worker.StreamLogProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

        UserNetwork userNetwork = new UserNetwork();

        final Executor logProcessorThreadExecutor = Executors.newSingleThreadExecutor();
        Scheduler logProcessorScheduler = Schedulers.from(logProcessorThreadExecutor);

        BatchLogProcessor batchLogProcessor = new BatchLogProcessor(batchLogSource, logProcessorScheduler, userNetwork);
        batchLogProcessor.run();

        StreamLogProcessor streamLogProcessor = new StreamLogProcessor(streamLogSource, logProcessorScheduler, userNetwork);
        streamLogProcessor.run();

        logProcessorScheduler.shutdown();
    }
}
