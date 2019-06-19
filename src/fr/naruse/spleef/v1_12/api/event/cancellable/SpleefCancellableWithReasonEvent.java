package fr.naruse.spleef.v1_12.api.event.cancellable;

import fr.naruse.spleef.manager.SpleefPluginV1_12;

public class SpleefCancellableWithReasonEvent extends SpleefCancellableEvent {
    private String reason = null;
    public SpleefCancellableWithReasonEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
