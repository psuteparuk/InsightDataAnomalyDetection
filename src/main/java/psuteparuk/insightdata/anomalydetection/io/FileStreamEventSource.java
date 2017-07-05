package psuteparuk.insightdata.anomalydetection.io;

import io.reactivex.Observer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * An event source from a file stream represented as an Observable.
 * Read lines from {@inputFilePath File} and push them onto an observer via the subscribe method.
 */
public class FileStreamEventSource implements StreamEventSource<String> {
    private final String inputFilePath;

    public FileStreamEventSource(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     * Push lines from file onto {@observer}
     * @param observer
     */
    @Override
    public void subscribe(Observer<? super String> observer) {
        try {
            Stream<String> fileStream = Files.lines(Paths.get(this.inputFilePath));
            fileStream.forEach(observer::onNext);
            fileStream.close();
            observer.onComplete();
        } catch (IOException e) {
            observer.onError(e);
        }
    }
}
