package psuteparuk.insightdata.anomalydetection;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import psuteparuk.insightdata.anomalydetection.common.Arguments;
import psuteparuk.insightdata.anomalydetection.io.FileEventWriter;
import psuteparuk.insightdata.anomalydetection.io.FileStreamEventSource;
import psuteparuk.insightdata.anomalydetection.network.UserNetwork;
import psuteparuk.insightdata.anomalydetection.worker.BatchLogProcessor;
import psuteparuk.insightdata.anomalydetection.worker.StreamLogProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Insight Data Engineering Coding Challenge (Sep 2017 batch)
 * Full problem description: https://github.com/InsightDataScience/anomaly_detection
 */
public class MainApplication {
    public static void main(String[] args) {
        // Parse CLI arguments
        Arguments arguments = new Arguments(args);

        // Input sources as Observable
        final Observable<String> batchLogSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.batchFilePath))
            .subscribeOn(Schedulers.io()) // Read file on an io thread
            .replay(1)
            .refCount();
        final Observable<String> streamLogSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.streamFilePath))
            .subscribeOn(Schedulers.io())
            .replay(1)
            .refCount();

        FileEventWriter fileEventWriter = new FileEventWriter(arguments.flaggedFilePath);

        // Main graph storing user data and relationships
        UserNetwork userNetwork = new UserNetwork();

        // Run both batch and stream logs on the same thread provided on the Scheduler
        final Executor logProcessorThreadExecutor = Executors.newSingleThreadExecutor();
        Scheduler logProcessorScheduler = Schedulers.from(logProcessorThreadExecutor);

        // Run the batch log processor befoe the stream log processor
        BatchLogProcessor batchLogProcessor = new BatchLogProcessor(
            batchLogSource,
            logProcessorScheduler,
            userNetwork
        );
        batchLogProcessor.run();

        StreamLogProcessor streamLogProcessor = new StreamLogProcessor(
            streamLogSource,
            fileEventWriter,
            logProcessorScheduler,
            userNetwork
        );
        streamLogProcessor.run();

        // Shutdown computation thread and not accept any more actions
        logProcessorScheduler.shutdown();
    }
}
