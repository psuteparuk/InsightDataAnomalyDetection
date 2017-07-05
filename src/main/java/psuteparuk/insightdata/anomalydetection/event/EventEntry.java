package psuteparuk.insightdata.anomalydetection.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An object representation of events read from a json input.
 * Convert the type to an {EventType} enum.
 * Also store the original string representation {@originalMessage}.
 */
public class EventEntry {
    private EventType eventType;
    private Date timestamp;
    private Double amount;
    private String buyerId;
    private String user1Id;
    private String user2Id;
    private String originalMessage;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @JsonCreator
    public EventEntry(
        @JsonProperty("event_type") String eventTypeRaw,
        @JsonProperty("timestamp") String timestampRaw,
        @JsonProperty("amount") String amountRaw,
        @JsonProperty("id") String idRaw,
        @JsonProperty("id1") String id1Raw,
        @JsonProperty("id2") String id2Raw
    ) {
        this.eventType = parseEventType(eventTypeRaw);
        this.timestamp = parseTimestamp(timestampRaw);
        this.amount = parseAmount(amountRaw);
        this.buyerId = idRaw;
        this.user1Id = id1Raw;
        this.user2Id = id2Raw;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Double getAmount() {
        return amount;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String message) {
        originalMessage = message;
    }

    private EventType parseEventType(String eventTypeRaw) {
        try {
            switch (eventTypeRaw) {
                case "purchase":
                    return EventType.PURCHASE;
                case "befriend":
                    return EventType.BEFRIEND;
                case "unfriend":
                    return EventType.UNFRIEND;
                default:
                    return EventType.INVALID;
            }
        } catch (NullPointerException e) {
            return EventType.INVALID;
        }
    }

    private Date parseTimestamp(String timestampRaw) {
        try {
            return dateFormat.parse(timestampRaw);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

    private Double parseAmount(String amountRaw) {
        try {
            return Double.parseDouble(amountRaw);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
