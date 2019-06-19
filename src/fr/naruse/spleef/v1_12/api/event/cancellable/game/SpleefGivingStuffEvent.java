package fr.naruse.spleef.v1_12.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_12.game.spleef.Spleef;
import org.bukkit.entity.Player;

import java.util.List;

@SpleefCancellable
public class SpleefGivingStuffEvent extends SpleefCancellableEvent {
    private Spleef spleef;
    private List<Player> players;
    public SpleefGivingStuffEvent(SpleefPluginV1_12 pl, Spleef spleef, List<Player> playerList) {
        super(pl, "SpleefGivingStuffEvent");
        this.spleef = spleef;
        this.players = playerList;
    }

    public Spleef getSpleef() {
        return spleef;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
