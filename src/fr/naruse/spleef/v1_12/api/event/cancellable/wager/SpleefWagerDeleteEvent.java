package fr.naruse.spleef.v1_12.api.event.cancellable.wager;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_12.game.wager.Wager;

public class SpleefWagerDeleteEvent extends SpleefCancellableEvent {
    public SpleefWagerDeleteEvent(SpleefPluginV1_12 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefWagerDeleteEvent{
        private Wager wager;
        public Pre(SpleefPluginV1_12 pl, Wager wager) {
            super(pl, "SpleefWagerDeleteEvent.Pre");
            this.wager = wager;
        }

        public Wager getWager() {
            return wager;
        }
    }

    public static class Post extends SpleefWagerDeleteEvent{
        private Wager wager;
        public Post(SpleefPluginV1_12 pl, Wager wager) {
            super(pl, "SpleefWagerDeleteEvent.Post");
            this.wager = wager;
        }

        public Wager getWager() {
            return wager;
        }
    }
}
