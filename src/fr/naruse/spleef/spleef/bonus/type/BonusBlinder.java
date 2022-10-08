package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;

public class BonusBlinder extends BonusColored {

    public BonusBlinder(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§7§lBlinding Sheep", 12, 5);
    }

    @Override
    protected void onAction() {
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 5, 3, 5, 2);
        List<? extends Player> stream = getNearbyPlayers(sheep.getLocation(), 10, 5, 10).collect(Collectors.toList());;
        runSync(() -> stream.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*8, 2))));
    }
}
