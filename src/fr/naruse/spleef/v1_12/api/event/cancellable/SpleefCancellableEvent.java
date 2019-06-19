package fr.naruse.spleef.v1_12.api.event.cancellable;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefEvent;

public class SpleefCancellableEvent extends SpleefEvent {
    private boolean isCancelled = false;
    public SpleefCancellableEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
