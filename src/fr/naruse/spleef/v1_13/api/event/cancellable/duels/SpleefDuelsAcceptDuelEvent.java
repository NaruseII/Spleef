package fr.naruse.spleef.v1_13.api.event.cancellable.duels;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableWithReasonEvent;
import org.bukkit.entity.Player;

@SpleefCancellable
public class SpleefDuelsAcceptDuelEvent extends SpleefCancellableWithReasonEvent {
    private Player player;
    public SpleefDuelsAcceptDuelEvent(SpleefPluginV1_13 pl, Player player) {
        super(pl, "SpleefDuelsAcceptDuelEvent");
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
