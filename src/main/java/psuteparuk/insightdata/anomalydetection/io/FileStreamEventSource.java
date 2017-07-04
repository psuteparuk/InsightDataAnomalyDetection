package psuteparuk.insightdata.anomalydetection.io;

import io.reactivex.Observer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStreamEventSource implements StreamEventSource<String> {
    private final String inputFilePath;

    public FileStreamEventSource(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    @Override
    public void subscribe(Observer<? super String> observer) {
        try {
            Files.lines(Paths.get(this.inputFilePath)).forEach(observer::onNext);
            observer.onComplete();
        } catch (IOException e) {
            observer.onError(e);
        }
    }
}
