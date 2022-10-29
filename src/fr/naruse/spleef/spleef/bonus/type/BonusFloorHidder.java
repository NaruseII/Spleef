package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.effect.RotationData;
import fr.naruse.api.effect.particle.FollowingParticleEffect;
import fr.naruse.api.effect.particle.ParticleShapeEffect;
import fr.naruse.api.particle.IParticle;
import fr.naruse.api.particle.Particle;
import fr.naruse.api.particle.sender.ParticleSender;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BonusFloorHidder extends BonusColored {

    private int duration = 20*8;

    public BonusFloorHidder(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§2§lFloor Hidder Sheep", 2, 7);
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 3, 2, 2);

        onRun(sheep.getLocation().clone());
    }

    private void onRun(Location location) {
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(new Runnable() {
            @Override
            public void run() {
                if(duration == 0 || spleef.getCurrentStatus() == GameStatus.WAIT){
                    return;
                }

                duration--;

                for (int i = 0; i < 6; i++) {
                    for (Location loc : MathUtils.getCircle(location, i, 10)) {
                        Particle.buildParticle(loc, Particle.getEnumParticle().CLOUD(), 1, 0, 1, 10).toNearbyFifty();
                    }
                }


                onRun(location);
            }
        });
    }

    @Override
    protected void onTick() {

    }

}
