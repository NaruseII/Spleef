package fr.naruse.spleef.v1_13.api.event;
import fr.naruse.spleef.manager.SpleefPluginV1_13;

public class SpleefEvent {
    private SpleefPluginV1_13 pl;
    private String eventName;
    public SpleefEvent(SpleefPluginV1_13 pl, String eventName) {
        this.pl = pl;
        this.eventName = eventName;
    }

    public SpleefPluginV1_13 getSpleefPlugin() {
        return pl;
    }

    public String getEventName() {
        return eventName;
    }
}
