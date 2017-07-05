package psuteparuk.insightdata.anomalydetection.common;

import java.util.*;

public class SocialNetwork<T> {
    private Map<String, Node<T>> nodeIdMap;

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
        this.nodeIdMap
            .put(nodeId, new Node<>(nodeId, nodeData));
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
        if (this.contains(nodeId1)) {
            this.getNode(nodeId1).removeNeighbor(nodeId2);
        }
        if (this.contains(nodeId2)) {
            this.getNode(nodeId2).removeNeighbor(nodeId1);
        }
    }

    public T initializeData(String nodeId) {
        return null;
    }

    private Node<T> getNode(String nodeId) throws NoSuchElementException {
        if (this.contains(nodeId)) {
            return this.nodeIdMap.get(nodeId);
        } else {
            throw new NoSuchElementException("No such node found");
        }
    }

    private static class Node<T> {
        final private String id;
        private T data;
        private Set<String> neighborIds;

        public Node(String id, T data) {
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

        public void setData(T newData) {
            this.data = newData;
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
}
