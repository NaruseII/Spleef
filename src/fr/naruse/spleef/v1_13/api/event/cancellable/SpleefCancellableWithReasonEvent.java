package fr.naruse.spleef.v1_13.api.event.cancellable;

import fr.naruse.spleef.manager.SpleefPluginV1_13;

public class SpleefCancellableWithReasonEvent extends SpleefCancellableEvent {
    private String reason = null;
    public SpleefCancellableWithReasonEvent(SpleefPluginV1_13 pl, String eventName) {
        super(pl, eventName);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
