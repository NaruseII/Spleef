package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Sets;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import java.util.Set;

public class BonusBallistic extends BonusColored {

    public BonusBallistic(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§4§lBallistic Sheep", 14, 5+random.nextInt(4));
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 5, 2, 5, 3);
        Set<Bonus> set = Sets.newHashSet();
        for (int i = 0; i < 5; i++) {
            Bonus bonus = new BonusExplosive(bonusManager, p){
                @Override
                public void onSheepSpawned(Sheep sheep) {
                    super.onSheepSpawned(sheep);
                    sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 3, 3, 3, 2);
                    sheep.setVelocity(new Vector(random.nextInt(3)*(random.nextBoolean() ? 1 : -1), 0.5F, random.nextInt(3)*(random.nextBoolean() ? 1 : -1)));
                }
            };
            bonus.setSpawnLocation(sheep.getLocation());
            set.add(bonus);
        }
        runSync(() -> {
            for (Bonus bonus : set) {
                bonus.launchSheep();
            }
        });
    }
}
