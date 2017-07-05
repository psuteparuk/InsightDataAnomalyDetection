package psuteparuk.insightdata.anomalydetection.common;

/**
 * A Node representation for a singly-linked list.
 * @param <T>
 */
public class SinglyLinkedNode<T> {
    private T data;
    private SinglyLinkedNode<T> next;

    public SinglyLinkedNode(T data) {
        this.data = data;
        this.next = null;
    }

    public T getData() {
        return this.data;
    }

    public SinglyLinkedNode<T> getNext() {
        return this.next;
    }

    public void setNext(SinglyLinkedNode<T> next) {
        this.next = next;
    }

    public boolean hasNext() {
        return (this.next != null);
    }
}
