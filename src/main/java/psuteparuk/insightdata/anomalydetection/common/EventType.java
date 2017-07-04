package psuteparuk.insightdata.anomalydetection.common;

public enum EventType {
    PURCHASE("purchase"),
    BEFRIEND("befriend"),
    UNFRIEND("unfriend"),
    INVALID("invalid");

    private final String name;

    EventType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
