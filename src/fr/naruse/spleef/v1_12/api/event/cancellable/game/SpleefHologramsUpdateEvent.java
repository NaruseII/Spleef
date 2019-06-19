package fr.naruse.spleef.v1_12.api.event.cancellable.game;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableEvent;
import fr.naruse.spleef.v1_12.util.board.Holograms;

@SpleefCancellable
public class SpleefHologramsUpdateEvent extends SpleefCancellableEvent {
    private String line;
    private Holograms holograms;
    public SpleefHologramsUpdateEvent(SpleefPluginV1_12 pl, String line, Holograms holograms) {
        super(pl, "SpleefHologramsUpdateEvent");
        this.line = line;
        this.holograms = holograms;
    }

    public Holograms getHolograms() {
        return holograms;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
