package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BonusAttraction extends BonusColored {

    public BonusAttraction(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§2§lAttraction Sheep", 13, 5);
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 5, 3, 5, 2);
        List<Entity> list = getNearbySheepsAndPlayers(sheep.getLocation(), 12, 5, 12).filter(entity -> entity != p).collect(Collectors.toList());;
        runSync(() -> list.forEach(entity -> entity.setVelocity(MathUtils.genVector(entity.getLocation(), sheep.getLocation()).multiply(2).setY(0.6))));
    }
}
