package fr.naruse.spleef.v1_12.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;

public class SpleefEnablingEvent extends SpleefCancellableEvent {
    public SpleefEnablingEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefEnablingEvent{
        public Pre(SpleefPluginV1_12 pl) {
            super(pl, "SpleefEnablingEvent.Pre");
        }
    }

    public static class Post extends SpleefEnablingEvent{
        public Post(SpleefPluginV1_12 pl) {
            super(pl, "SpleefEnablingEvent.Post");
        }
    }
}
