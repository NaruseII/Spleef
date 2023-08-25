package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.effect.RotationData;
import fr.naruse.api.effect.particle.ParticleShapeEffect;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.List;

public class BonusJinton extends BonusColored {

    private Location location;
    private ParticleShapeEffect effect;
    private int time = 20;

    public BonusJinton(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§8§lJinton Sheep", 15, 3);
    }


    public BonusJinton(BonusManager bonusManager, Player p, String name, int woolColorId, int time) {
        super(bonusManager, p, name, woolColorId, 3);
        this.time = time;
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 3, 2, 2);

        this.location = sheep.getLocation();

        this.effect = new ParticleShapeEffect(sheep.getLocation(), 3, 15,
                new RotationData()
                        .addRotationAxis(MathUtils.Axis.X, MathUtils.Axis.Y, MathUtils.Axis.Z)
                        .setTickInterval(random.nextInt(3))
                        .setDegreeIncrement(1.0),
                 Particle.getEnumParticle().SOUL_FIRE_FLAME());
        this.effect.start();

        runLater();
    }

    private int tick = 0;
    private BlockBuffer blockBuffer = new BlockBuffer();

    private void runLater() {
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(new Runnable() {
            @Override
            public void run() {

                if(spleef.getCurrentStatus() == GameStatus.WAIT){
                    effect.kill();
                    return;
                }

                if(tick >= 20*10){

                    if(tick >= 20*10 && tick <= 20*11){
                        effect.setParticle(Particle.getEnumParticle().CLOUD());
                    }

                    effect.setRadius(effect.getRadius()+0.05);
                    effect.calculateShape();

                    if(tick >= 20*time){

                        double radius = effect.getRadius();

                        List<Location> list = MathUtils.get3DRectangleLocation(location.clone(),
                                (int) radius, (int) radius, (int) radius, effect.getRotationData().getCurrentDegree(), true,
                                MathUtils.Axis.X, MathUtils.Axis.Y,  MathUtils.Axis.Z);

                        for (Location loc : list) {
                            if(loc.getBlock().getType() == Material.SNOW_BLOCK && !blockBuffer.contains(loc.getBlock())){
                                blockBuffer.add(loc.getBlock());
                            }
                        }

                        spleef.destroyBlock(p, blockBuffer, true);

                        effect.kill();
                    }else{
                        tick++;
                        runLater();
                    }
                }else{
                    tick++;
                    runLater();
                }
            }
        });
    }

    @Override
    protected void onTick() {

    }
}
