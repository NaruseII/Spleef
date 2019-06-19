package fr.naruse.spleef.v1_13.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableEvent;

public class SpleefEnablingEvent extends SpleefCancellableEvent {
    public SpleefEnablingEvent(SpleefPluginV1_13 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefEnablingEvent {
        public Pre(SpleefPluginV1_13 pl) {
            super(pl, "SpleefEnablingEvent.Pre");
        }
    }

    public static class Post extends SpleefEnablingEvent {
        public Post(SpleefPluginV1_13 pl) {
            super(pl, "SpleefEnablingEvent.Post");
        }
    }
}
