package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BonusSpeed extends BonusColored {
    public BonusSpeed(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§d§lFlash Sheep", 2, 5);
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 1, 1, 1, 2);
        List<? extends Player> stream = getNearbyPlayers(sheep.getLocation(), 10, 5, 10).collect(Collectors.toList());;
        runSync(() -> stream.forEach((Consumer<Player>) player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*15, 2))));
    }
}