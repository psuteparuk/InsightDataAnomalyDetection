package psuteparuk.insightdata.anomalydetection.network;

import psuteparuk.insightdata.anomalydetection.common.GraphNode;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class SocialNetwork<T> {
    private Map<String, GraphNode<T>> nodeIdMap;

    public SocialNetwork() {
        this.nodeIdMap = new HashMap<>();
    }

    public boolean contains(String nodeId) {
        return this.nodeIdMap.containsKey(nodeId);
    }

    public T getData(String nodeId) throws NoSuchElementException {
        return this.getNode(nodeId).getData();
    }

    public Set<String> getFriends(String nodeId) throws NoSuchElementException {
        return this.getNode(nodeId).getNeighborIds();
    }

    public void putNode(String nodeId, T nodeData) {
        if (this.contains(nodeId)) {
            this.getNode(nodeId).setData(nodeData);
        } else {
            this.nodeIdMap.put(nodeId, new GraphNode<>(nodeId, nodeData));
        }
    }

    public void befriend(String nodeId1, String nodeId2) {
        if (!this.contains(nodeId1)) {
            this.putNode(nodeId1, this.initializeData(nodeId1));
        }
        if (!this.contains(nodeId2)) {
            this.putNode(nodeId2, this.initializeData(nodeId2));
        }
        this.getNode(nodeId1).addNeighbor(nodeId2);
        this.getNode(nodeId2).addNeighbor(nodeId1);
    }

    public void unfriend(String nodeId1, String nodeId2) {
        if (this.contains(nodeId1) && this.contains(nodeId2)) {
            this.getNode(nodeId1).removeNeighbor(nodeId2);
            this.getNode(nodeId2).removeNeighbor(nodeId1);
        }
    }

    public T initializeData(String nodeId) {
        return null;
    }

    private GraphNode<T> getNode(String nodeId) throws NoSuchElementException {
        if (this.contains(nodeId)) {
            return this.nodeIdMap.get(nodeId);
        } else {
            throw new NoSuchElementException("No such node found");
        }
    }
}
