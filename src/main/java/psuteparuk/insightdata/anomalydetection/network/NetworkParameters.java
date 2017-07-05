package psuteparuk.insightdata.anomalydetection.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object representation of network parameters read from a json input.
 * For the purpose of this challenge, two parameters are stored.
 *  - {@depthDegree} represents the depth of "close" friends in a graph, i.e.
 *      the number of hops from a certain user that are still considered within his network.
 *  - {@trackedNumber} represents the number of latest tracked purchases for a group of
 *      "close" friends.
 */
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
