package psuteparuk.insightdata.anomalydetection.network;

import psuteparuk.insightdata.anomalydetection.common.GraphNode;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A generic graph representing a social network.
 * Contain methods such as getFriends, befriend, unfriend.
 * The underlying data structure uses a HashMap to map an ID
 * to the corresponding GraphNode. Each GraphNode then stores
 * a set of its friends' IDs.
 * @param <T>
 */
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

    /**
     * @param nodeId
     * @return the set of friends' IDs
     */
    public Set<String> getFriends(String nodeId) throws NoSuchElementException {
        return this.getNode(nodeId).getNeighborIds();
    }

    /**
     * Update the node ID {@nodeId} with the new data.
     * Create a new node with the specified ID if there's none existed.
     * @param nodeId
     * @param nodeData
     */
    public void putNode(String nodeId, T nodeData) {
        if (this.contains(nodeId)) {
            this.getNode(nodeId).setData(nodeData);
        } else {
            this.nodeIdMap.put(nodeId, new GraphNode<>(nodeId, nodeData));
        }
    }

    /**
     * Connect two nodes
     * @param nodeId1
     * @param nodeId2
     */
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

    /**
     * Disconnect two nodes
     * @param nodeId1
     * @param nodeId2
     */
    public void unfriend(String nodeId1, String nodeId2) {
        if (this.contains(nodeId1) && this.contains(nodeId2)) {
            this.getNode(nodeId1).removeNeighbor(nodeId2);
            this.getNode(nodeId2).removeNeighbor(nodeId1);
        }
    }

    /**
     * Specify the initialization of the node data
     * @param nodeId
     */
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
