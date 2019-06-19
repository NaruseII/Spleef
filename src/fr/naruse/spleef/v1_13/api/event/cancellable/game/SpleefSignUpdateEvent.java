package fr.naruse.spleef.v1_13.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_13.game.spleef.Spleef;
import org.bukkit.block.Sign;

import java.util.List;

@SpleefCancellable
public class SpleefSignUpdateEvent extends SpleefCancellableEvent {
    private Spleef spleef;
    private Sign sign;
    private List<Sign> signs;
    public SpleefSignUpdateEvent(SpleefPluginV1_13 pl, Spleef spleef, Sign sign, List<Sign> signs) {
        super(pl, "SpleefSignUpdateEvent");
        this.spleef = spleef;
        this.sign = sign;
        this.signs = signs;
    }

    public Spleef getSpleef() {
        return spleef;
    }

    public List<Sign> getSigns() {
        return signs;
    }

    public Sign getSign() {
        return sign;
    }
}
