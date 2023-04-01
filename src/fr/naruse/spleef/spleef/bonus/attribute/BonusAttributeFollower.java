package fr.naruse.spleef.spleef.bonus.attribute;

import fr.naruse.api.particle.version.VersionManager;
import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.IBonusAttribute;
import org.bukkit.entity.Player;

import java.util.Optional;

public class BonusAttributeFollower implements IBonusAttribute {

    private final Bonus bonus;
    private final double speed;

    public BonusAttributeFollower(Bonus bonus, double speed) {
        this.bonus = bonus;
        this.speed = speed;
    }

    public void nearestPlayerFound(Player target) {
        VersionManager.getVersion().moveEntityToDestination(bonus.getSheep(), target.getLocation(), speed);
    }

    @Override
    public void onTick() {
        if(bonus.getSheep() != null && !bonus.getSheep().isDead()){
            Optional<? extends Player> optional = bonus.getNearbyPlayers(bonus.getSheep().getLocation(), 50, 5, 50).filter(entity -> entity != bonus.getPlayer()).findFirst();
            if(!optional.isPresent()){
                return;
            }
            bonus.runSync(() -> nearestPlayerFound(optional.get()));
        }
    }
}
