package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.api.particle.version.VersionManager;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.bonus.IFriendlyBonus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.Set;
import java.util.stream.Collectors;

public class BonusProjectileCounter extends BonusColored implements IFriendlyBonus {
    public BonusProjectileCounter(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§5§lProjectile Counter Sheep", 10, 8);
        setApplyVelocity(false);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if(sheep != null && !sheep.isDead()){
            sheep.setTarget(p);
            Set<Entity> stream = getNearbyEntities(sheep.getLocation(), 10, 5, 10).filter(entity -> entity instanceof Projectile).collect(Collectors.toSet());
            runSync(() -> {
                VersionManager.getVersion().moveEntityToDestination(sheep, p.getLocation(), 2);
                stream.forEach(entity -> entity.setVelocity(MathUtils.genVector(sheep.getLocation(), entity.getLocation()).multiply(1.5).setY(0.5)));
            });
        }
    }

    @Override
    protected void onAction() {

    }
}
