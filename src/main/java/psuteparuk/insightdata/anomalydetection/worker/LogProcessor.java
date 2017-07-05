package psuteparuk.insightdata.anomalydetection.worker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import psuteparuk.insightdata.anomalydetection.event.EventEntry;
import psuteparuk.insightdata.anomalydetection.event.EventType;
import psuteparuk.insightdata.anomalydetection.network.NetworkParameters;

import java.util.Objects;

/**
 * Abstract class providing streams that transform each event in the event log
 * to a suitable object representation.
 */
abstract class LogProcessor implements Runnable {
    // Input event log
    private final Observable<String> logSource;
    // Specify the thread the transformation should be run on
    private final Scheduler scheduler;
    // Jackson object mapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    LogProcessor(Observable<String> logSource, Scheduler scheduler) {
        this.logSource = logSource;
        this.scheduler = scheduler;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Transform each json event into an {EventEntry} object.
     * @return a Subject emitting {EventEntry} objects.
     */
    Observable<EventEntry> getEntrySource() {
        // Declare as a ReplaySubject so any subscribers can start reading from the start of the log file
        final Subject<EventEntry> eventEntrySource = ReplaySubject.create();

        logSource
            .observeOn(this.scheduler)
            .map((entry) -> {
                try {
                    EventEntry eventEntry = objectMapper.readValue(entry, EventEntry.class);
                    if (eventEntry != null && eventEntry.getEventType() != EventType.INVALID) {
                        // save original string representation for output use
                        eventEntry.setOriginalMessage(entry);
                    }
                    return eventEntry;
                } catch (JsonMappingException | JsonParseException e) {
                    return new EventEntry(EventType.INVALID.toString(), null, null, null, null, null);
                }
            })
            .filter((entry) -> entry.getEventType() != EventType.INVALID)
            .subscribe(eventEntrySource);

        return eventEntrySource;
    }

    /**
     * Transform the network parameters, e.g. depth of graph, number of tracked purchases
     * into the corresponding Java object representation.
     * @return a Subject emitting {NetworkParameters} objects.
     */
    Observable<NetworkParameters> getNetworkParametersSource() {
        // Declare as a ReplaySubject so any subscribers can start reading from the start of the log file
        final Subject<NetworkParameters> networkParametersSource = ReplaySubject.create();

        logSource
            .observeOn(this.scheduler)
            .map((entry) -> {
                try {
                    return objectMapper.readValue(entry, NetworkParameters.class);
                } catch (JsonMappingException | JsonParseException e) {
                    return new NetworkParameters(null, null);
                }
            })
            .filter((param) -> Objects.nonNull(param.getDepthDegree()) && Objects.nonNull(param.getTrackedNumber()))
            .subscribe(networkParametersSource);

        return networkParametersSource.take(1);
    }
}
