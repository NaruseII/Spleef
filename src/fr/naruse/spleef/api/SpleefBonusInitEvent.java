package fr.naruse.spleef.api;

import fr.naruse.spleef.spleef.bonus.Bonus;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class SpleefBonusInitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final List<Class<? extends Bonus>> bonuses;

    public SpleefBonusInitEvent(List<Class<? extends Bonus>> bonuses) {
        this.bonuses = bonuses;
    }

    public List<Class<? extends Bonus>> getBonuses() {
        return bonuses;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
