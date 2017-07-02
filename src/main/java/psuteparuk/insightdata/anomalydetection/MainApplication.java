package psuteparuk.insightdata.anomalydetection;

import io.reactivex.Observable;
import psuteparuk.insightdata.anomalydetection.common.Arguments;
import psuteparuk.insightdata.anomalydetection.source.FileStreamEventSource;

public class MainApplication {
    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);

        final Observable<String> batchSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.batchFilePath))
            .replay(1)
            .refCount();
        final Observable<String> streamSource = Observable
            .defer(() -> new FileStreamEventSource(arguments.streamFilePath))
            .replay(1)
            .refCount();

        batchSource.subscribe(
            line -> System.out.println("HEY: " + line),
            Throwable::printStackTrace,
            () -> System.out.println("finished")
        );
    }
}
