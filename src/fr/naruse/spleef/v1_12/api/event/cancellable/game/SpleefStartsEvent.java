package fr.naruse.spleef.v1_12.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_12.game.spleef.Spleef;

public class SpleefStartsEvent extends SpleefCancellableEvent {
    public SpleefStartsEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefStartsEvent{
        private Spleef spleef;
        public Pre(SpleefPluginV1_12 pl, Spleef spleef) {
            super(pl, "SpleefStartsEvent.Pre");
            this.spleef = spleef;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }

    public static class Post extends SpleefStartsEvent{
        private Spleef spleef;
        public Post(SpleefPluginV1_12 pl, Spleef spleef) {
            super(pl, "SpleefStartsEvent.Post");
            this.spleef = spleef;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }
}
