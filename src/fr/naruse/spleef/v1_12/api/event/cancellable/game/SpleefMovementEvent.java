package fr.naruse.spleef.v1_12.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.SpleefMovementType;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_12.game.spleef.Spleef;

public class SpleefMovementEvent extends SpleefCancellableEvent {
    public SpleefMovementEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefMovementEvent{
        private Spleef spleef;
        private SpleefMovementType movementType;
        public Pre(SpleefPluginV1_12 pl, Spleef spleef, SpleefMovementType movementType) {
            super(pl, "SpleefMovementEvent.Pre");
            this.spleef = spleef;
            this.movementType = movementType;
        }

        public SpleefMovementType getMovementType() {
            return movementType;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }

    public static class Post extends SpleefMovementEvent{
        private Spleef spleef;
        private SpleefMovementType movementType;
        public Post(SpleefPluginV1_12 pl, Spleef spleef, SpleefMovementType movementType) {
            super(pl, "SpleefMovementEvent.Post");
            this.spleef = spleef;
            this.movementType = movementType;
        }

        public SpleefMovementType getMovementType() {
            return movementType;
        }

        public Spleef getSpleef() {
            return spleef;
        }
    }
}
