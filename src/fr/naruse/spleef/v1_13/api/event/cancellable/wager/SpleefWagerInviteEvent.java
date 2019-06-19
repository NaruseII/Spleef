package fr.naruse.spleef.v1_13.api.event.cancellable.wager;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_13.game.wager.Wager;
import org.bukkit.entity.Player;

public class SpleefWagerInviteEvent extends SpleefCancellableEvent {
    public SpleefWagerInviteEvent(SpleefPluginV1_13 pl, String eventName) {
        super(pl, eventName);
    }

    @SpleefCancellable
    public static class Pre extends SpleefWagerInviteEvent {
        private Player player, target;
        private Wager wager;
        public Pre(SpleefPluginV1_13 pl, Player p, Player p2, Wager wager) {
            super(pl, "SpleefWagerInviteEvent.Pre");
            this.player = p;
            this.target = p2;
            this.wager = wager;
        }

        public Player getTarget() {
            return target;
        }

        public Player getPlayer() {
            return player;
        }

        public Wager getWager() {
            return wager;
        }
    }

    public static class Post extends SpleefWagerInviteEvent {

        private Player player, target;
        private Wager wager;
        public Post(SpleefPluginV1_13 pl, Player p, Player p2, Wager wager) {
            super(pl, "SpleefWagerInviteEvent.Post");
            this.player = p;
            this.target = p2;
            this.wager = wager;
        }

        public Player getTarget() {
            return target;
        }

        public Player getPlayer() {
            return player;
        }

        public Wager getWager() {
            return wager;
        }
    }
}
