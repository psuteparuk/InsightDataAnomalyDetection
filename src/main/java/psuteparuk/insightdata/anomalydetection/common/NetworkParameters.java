package psuteparuk.insightdata.anomalydetection.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NetworkParameters {
    private Integer depthDegree;
    private Integer trackedNumber;

    @JsonCreator
    public NetworkParameters(
        @JsonProperty("D") String depthDegreeRaw,
        @JsonProperty("T") String trackedNumberRaw
    ) {
        try {
            this.depthDegree = Integer.parseInt(depthDegreeRaw);
            this.trackedNumber = Integer.parseInt(trackedNumberRaw);
        } catch (NumberFormatException e) {
            this.depthDegree = null;
            this.trackedNumber = null;
        }
    }

    public Integer getDepthDegree() {
        return depthDegree;
    }

    public Integer getTrackedNumber() {
        return trackedNumber;
    }
}
