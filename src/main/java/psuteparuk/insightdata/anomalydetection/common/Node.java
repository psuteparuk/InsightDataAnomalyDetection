package psuteparuk.insightdata.anomalydetection.common;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class Node<T> {
    public static <T> Node<T> create(
        T data,
        Node<T>[] neighbors
    ) {
        return new AutoValue_Node(
            data,
            ImmutableList.copyOf(neighbors)
        );
    }

    /**
     * AutoValue implementations
     */

    public abstract T data();
    public abstract ImmutableList<Node<T>> neighbors(); // Adjacency List
}
