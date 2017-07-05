package psuteparuk.insightdata.anomalydetection.network;

import psuteparuk.insightdata.anomalydetection.common.GroupStats;
import psuteparuk.insightdata.anomalydetection.common.SinglyLinkedNode;

import java.util.*;

/**
 * The social network of UserData.
 * Extends the functionality of the generic social network.
 *
 * We can calculate the group stats of "close" friend group
 * based on the {@depthDegree} and {@trackedNumber} parameters.
 *
 * To calculate the "close" friend or "depth" group, we utilize
 * the breadth-first-search algorithm.
 * To calculate the {@trackedNumber} latest purchases of the "depth" group,
 * we utilize the PriorityQueue to merge multiple sorted lists.
 */
public class UserNetwork extends SocialNetwork<UserData> {
    private int depthDegree;
    private int trackedNumber;

    /**
     * Parameters Setters
     */

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

    /**
     * A predicate that calculates the amount of the specified purchase
     * against the "depth" group stats and returns whether it is above the
     * anomaly threshold.
     * @param purchaseData
     * @param groupStats
     */
    public boolean isPurchaseAnomaly(PurchaseData purchaseData, GroupStats groupStats) {
        return purchaseData.amount() > groupStats.anomalyThreshold();
    }

    /**
     * Add a user's purchase into the network
     * @param userId
     * @param purchaseData
     */
    public void addPurchase(String userId, PurchaseData purchaseData) {
        UserData userData = (this.contains(userId)) ? this.getData(userId) : this.initializeData(userId);
        userData.addPurchase(purchaseData);
        this.putNode(userId, userData);
    }

    /**
     * Find the {@trackedNumber} latest purchases in the "depth" group of a user
     * and calculate the stats (mean and sd).
     * @param userId
     * @return a stats of the latest purchases
     */
    public GroupStats calculateGroupStats(String userId) {
        List<PurchaseData> groupLatestPurchases = this.getGroupLatestPurchases(userId);
        double mean = PurchaseData.calculateMean(groupLatestPurchases);
        double sd = PurchaseData.calculateStandardDeviation(groupLatestPurchases);
        return GroupStats.create(mean, sd);
    }

    /**
     * Find the {@trackedNumber} latest purchases in the "depth" group of a user.
     * Each node in the "depth" contains a sorted list of its own {@trackedNumber} latest purchases.
     * This function merge these sorted lists into a new sorted list, keeping only
     * {@trackedNumber} latest ones.
     * To achieve this, it utilizes a PriorityQueue. We first push the head of each list into
     * the PriorityQueue. For each dequeue, we take the next node in the list of that dequeued node and
     * push it into the queue again.
     * @param userId
     * @return
     */
    private List<PurchaseData> getGroupLatestPurchases(String userId) {
        // return list. keep track of only {@trackedNumber} of purchases
        List<PurchaseData> groupLatestPurchases = new ArrayList<>(this.trackedNumber);

        // PriorityQueue used for merge operation of multiple sorted lists
        // At each point in time, the queue will never contain more than the number of nodes
        // in the "depth" group
        Set<String> depthGroup = this.getDepthGroup(userId);
        PriorityQueue<SinglyLinkedNode<PurchaseData>> mergeBuffer = new PriorityQueue<>(
            depthGroup.size(),
            Comparator.comparing(
                (SinglyLinkedNode<PurchaseData> purchaseDataNode) -> purchaseDataNode.getData().timestamp()
            ).reversed()
        );

        // We first convert the list of latest purchases of each friend into a singly-linked list.
        // This is so that we know which purchase data we need to push into the queue next
        // when we dequeue data from the queue.
        // Note that we reverse the order of the list so the latest one is at the head.
        List<SinglyLinkedNode<PurchaseData>> depthGroupPurchases = new ArrayList<>(depthGroup.size());
        depthGroup.forEach((nodeId) -> {
            Deque<PurchaseData> latestPurchases = this.getData(nodeId).getLatestPurchases();

            List<SinglyLinkedNode<PurchaseData>> currentNodePurchases = new ArrayList<>(latestPurchases.size());
            latestPurchases.forEach((purchase) -> currentNodePurchases.add(new SinglyLinkedNode<>(purchase)));
            for (int purchaseInd = currentNodePurchases.size()-1; purchaseInd > 0; --purchaseInd) {
                currentNodePurchases.get(purchaseInd).setNext(currentNodePurchases.get(purchaseInd-1));
            }
            depthGroupPurchases.add(currentNodePurchases.get(currentNodePurchases.size()-1));
        });
        depthGroupPurchases.forEach(mergeBuffer::add);

        // Actual merge operation. Only need {@trackedNumber} of latest purchase data.
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

    /**
     * Breadth-first algorithm to find nodes within {@depthDegree} hops
     * from the specified user.
     * @param userId
     * @return a Set of "close" nodes (nodes that are within {@depthDegree} hops)
     */
    private Set<String> getDepthGroup(String userId) {
        Set<String> depthGroup = new HashSet<>();

        // keep tracked of visisted node
        Map<String, Boolean> isVisited = new HashMap<>();
        // queue in neighbors that need to be visited next
        Deque<String> toVisitIds = new ArrayDeque<>();

        // put the current user node as a start node
        isVisited.put(userId, true);
        toVisitIds.push(userId);

        // we consider only nodes that are no more than {@depthDegree} levels deep
        for (int depth = 0; depth < this.depthDegree; ++depth) {
            if (toVisitIds.isEmpty()) {
                break;
            }

            String currentNodeId = toVisitIds.poll();
            final int currentDepth = depth;
            this.getFriends(currentNodeId).forEach((friendId) -> {
                // If an unvisited node is found, push it into the return set
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
