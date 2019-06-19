package fr.naruse.spleef.v1_12.api.event;
import fr.naruse.spleef.manager.AbstractSpleefPlugin;

public class SpleefEvent {
    private AbstractSpleefPlugin pl;
    private String eventName;
    public SpleefEvent(AbstractSpleefPlugin pl, String eventName) {
        this.pl = pl;
        this.eventName = eventName;
    }

    public AbstractSpleefPlugin getSpleefPlugin() {
        return pl;
    }

    public String getEventName() {
        return eventName;
    }
}
