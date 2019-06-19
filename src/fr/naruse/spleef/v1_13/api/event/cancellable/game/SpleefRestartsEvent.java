package fr.naruse.spleef.v1_13.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_13.game.spleef.Spleef;

public class SpleefRestartsEvent extends SpleefCancellableEvent {
    public SpleefRestartsEvent(SpleefPluginV1_13 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefStartsEvent {
        private Spleef spleef;
        public Pre(SpleefPluginV1_13 pl, Spleef spleef) {
            super(pl, "SpleefRestartsEvent.Pre");
            this.spleef = spleef;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }

    @SpleefCancellable
    public static class Post extends SpleefStartsEvent {
        private Spleef spleef;
        public Post(SpleefPluginV1_13 pl, Spleef spleef) {
            super(pl, "SpleefRestartsEvent.Post");
            this.spleef = spleef;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }
}
