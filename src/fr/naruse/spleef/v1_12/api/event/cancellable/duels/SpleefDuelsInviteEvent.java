package fr.naruse.spleef.v1_12.api.event.cancellable.duels;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableWithReasonEvent;
import org.bukkit.entity.Player;

@SpleefCancellable
public class SpleefDuelsInviteEvent extends SpleefCancellableWithReasonEvent {
    private Player player, target;
    public SpleefDuelsInviteEvent(SpleefPluginV1_12 pl, Player player, Player target) {
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
