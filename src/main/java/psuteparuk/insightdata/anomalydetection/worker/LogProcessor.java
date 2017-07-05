package psuteparuk.insightdata.anomalydetection.worker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import psuteparuk.insightdata.anomalydetection.common.EventEntry;
import psuteparuk.insightdata.anomalydetection.common.NetworkParameters;

import java.util.Objects;

abstract class LogProcessor implements Action {
    private final Observable<String> logSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    LogProcessor(Observable<String> logSource) {
        this.logSource = logSource;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    Observable<String> getLogSource() {
        return this.logSource;
    }

    Observable<EventEntry> getEntrySource() {
        final Subject<EventEntry> eventEntrySource = ReplaySubject.create();

        logSource
            .observeOn(Schedulers.computation())
            .map((entry) -> {
                try {
                    return objectMapper.readValue(entry, EventEntry.class);
                } catch (JsonMappingException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .subscribe(eventEntrySource);

        return eventEntrySource;
    }

    Observable<NetworkParameters> getNetworkParametersSource() {
        final Subject<NetworkParameters> networkParametersSource = ReplaySubject.create();

        logSource
            .observeOn(Schedulers.computation())
            .map((entry) -> {
                try {
                    return objectMapper.readValue(entry, NetworkParameters.class);
                } catch (JsonMappingException e) {
                    return null;
                }
            })
            .filter((param) -> Objects.nonNull(param.getDepthDegree()) && Objects.nonNull(param.getTrackedNumber()))
            .subscribe(networkParametersSource);

        return networkParametersSource.take(1);
    }
}
