package psuteparuk.insightdata.anomalydetection.network;

import psuteparuk.insightdata.anomalydetection.common.GroupStats;
import psuteparuk.insightdata.anomalydetection.common.SinglyLinkedNode;

import java.util.*;

public class UserNetwork extends SocialNetwork<UserData> {
    private int depthDegree;
    private int trackedNumber;

    public void setDepthDegree(int depthDegree) {
        this.depthDegree = depthDegree;
    }

    public void setTrackedNumber(int trackedNumber) {
        this.trackedNumber = trackedNumber;
    }

    @Override
    public UserData initializeData(String userId) {
        return new UserData(userId, this.trackedNumber);
    }

    public boolean isPurchaseAnomaly(PurchaseData purchaseData, GroupStats groupStats) {
        return purchaseData.amount() > groupStats.anomalyThreshold();
    }

    public void addPurchase(String userId, PurchaseData purchaseData) {
        UserData userData = (this.contains(userId)) ? this.getData(userId) : this.initializeData(userId);
        userData.addPurchase(purchaseData);
        this.putNode(userId, userData);
    }

    public GroupStats calculateGroupStats(String userId) {
        List<PurchaseData> groupLatestPurchases = this.getGroupLatestPurchases(userId);
        double mean = PurchaseData.calculateMean(groupLatestPurchases);
        double sd = PurchaseData.calculateStandardDeviation(groupLatestPurchases);
        return GroupStats.create(mean, sd);
    }

    private List<PurchaseData> getGroupLatestPurchases(String userId) {
        List<PurchaseData> groupLatestPurchases = new ArrayList<>(this.trackedNumber);

        Set<String> depthGroup = this.getDepthGroup(userId);
        PriorityQueue<SinglyLinkedNode<PurchaseData>> mergeBuffer = new PriorityQueue<>(
            depthGroup.size(),
            Comparator.comparing((purchaseDataNode) -> purchaseDataNode.getData().timestamp())
        );

        List<SinglyLinkedNode<PurchaseData>> depthGroupPurchases = new ArrayList<>(depthGroup.size());
        depthGroup.forEach((nodeId) -> {
            Deque<PurchaseData> latestPurchases = this.getData(nodeId).getLatestPurchases();

            List<SinglyLinkedNode<PurchaseData>> currentNodePurchases = new ArrayList<>(latestPurchases.size());
            latestPurchases.forEach((purchase) -> currentNodePurchases.add(new SinglyLinkedNode<>(purchase)));
            for (int purchaseInd = 0; purchaseInd < currentNodePurchases.size()-1; ++purchaseInd) {
                currentNodePurchases.get(purchaseInd).setNext(currentNodePurchases.get(purchaseInd+1));
            }
            depthGroupPurchases.add(currentNodePurchases.get(0));
        });
        depthGroupPurchases.forEach(mergeBuffer::add);

        int count = this.trackedNumber;
        while (!mergeBuffer.isEmpty() && count-- > 0) {
            SinglyLinkedNode<PurchaseData> purchaseDataNode = mergeBuffer.poll();
            groupLatestPurchases.add(purchaseDataNode.getData());
            if (purchaseDataNode.hasNext()) {
                mergeBuffer.add(purchaseDataNode.getNext());
            }
        }

        return groupLatestPurchases;
    }

    private Set<String> getDepthGroup(String userId) {
        Set<String> depthGroup = new HashSet<>();

        Map<String, Boolean> isVisited = new HashMap<>();
        Deque<String> toVisitIds = new ArrayDeque<>();

        isVisited.put(userId, true);
        toVisitIds.push(userId);

        for (int depth = 0; depth < this.depthDegree; ++depth) {
            if (toVisitIds.isEmpty()) {
                break;
            }

            String currentNodeId = toVisitIds.poll();
            final int currentDepth = depth;
            this.getFriends(currentNodeId).forEach((friendId) -> {
                if (!isVisited.containsKey(friendId) || !isVisited.get(friendId)) {
                    isVisited.put(friendId, true);
                    depthGroup.add(friendId);
                    if (currentDepth != this.depthDegree - 1) {
                        toVisitIds.add(friendId);
                    }
                }
            });
        }

        return depthGroup;
    }
}
