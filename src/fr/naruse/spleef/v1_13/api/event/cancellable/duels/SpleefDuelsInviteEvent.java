package fr.naruse.spleef.v1_13.api.event.cancellable.duels;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableWithReasonEvent;
import org.bukkit.entity.Player;

@SpleefCancellable
public class SpleefDuelsInviteEvent extends SpleefCancellableWithReasonEvent {
    private Player player, target;
    public SpleefDuelsInviteEvent(SpleefPluginV1_13 pl, Player player, Player target) {
        super(pl, "SpleefDuelsInviteEvent");
        this.player = player;
        this.target = target;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getTarget() {
        return target;
    }
}
