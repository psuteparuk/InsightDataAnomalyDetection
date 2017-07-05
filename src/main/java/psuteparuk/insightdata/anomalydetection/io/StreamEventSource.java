package psuteparuk.insightdata.anomalydetection.io;

import io.reactivex.ObservableSource;

/**
 * A stream source of events represented as Observable
 * @param <T>
 */
public interface StreamEventSource<T> extends ObservableSource<T> {
}
