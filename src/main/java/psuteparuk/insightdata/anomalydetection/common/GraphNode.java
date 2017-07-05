package psuteparuk.insightdata.anomalydetection.common;

import java.util.HashSet;
import java.util.Set;

public class GraphNode<T> {
    final private String id;
    private T data;
    private Set<String> neighborIds;

    public GraphNode(String id, T data) {
        this.id = id;
        this.data = data;
        this.neighborIds = new HashSet<>();
    }

    public String getId() {
        return this.id;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Set<String> getNeighborIds() {
        return this.neighborIds;
    }

    public void addNeighbor(String neighborId) {
        this.neighborIds.add(neighborId);
    }

    public void removeNeighbor(String neighborId) {
        this.neighborIds.remove(neighborId);
    }
}
