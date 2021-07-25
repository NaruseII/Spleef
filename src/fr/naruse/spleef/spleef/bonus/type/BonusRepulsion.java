package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class BonusRepulsion extends BonusColored {
    public BonusRepulsion(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§6§lRepellent Sheep", 1, 5);
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), "EXPLOSION_HUGE", 1, 1, 1, 2);
        List<Entity> stream = getNearbySheepsAndPlayers(sheep.getLocation(), 4, 5, 4).collect(Collectors.toList());
        runSync(() -> stream.forEach(entity -> entity.setVelocity(Utils.genVector(sheep.getLocation(), entity.getLocation()).setY(0).add(new Vector(0, 0.7, 0)).multiply(2.5))));
    }
}
