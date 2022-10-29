package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import fr.naruse.spleef.utils.BlockBuffer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BonusFloorFixer extends BonusColored {

    private Location location;
    private ParticleShapeEffect effect;
    private int remainingBlockToFix = 50;
    private IParticle particle = Particle.getEnumParticle().FIRE();

    private List<FollowingParticleEffect> fixingBlocks = Lists.newArrayList();
    private Map<FollowingParticleEffect, Block> blockMap = Maps.newHashMap();

    public BonusFloorFixer(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§d§lFloor Fixer Sheep", 6, 3);
    }

    public BonusFloorFixer(BonusManager bonusManager, Player p, String name, int remainingBlockToFix) {
        super(bonusManager, p, name, 6, 3);
        this.remainingBlockToFix = remainingBlockToFix;
        this.particle = Particle.getEnumParticle().SOUL();
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 3, 2, 2);

        this.location = sheep.getLocation().clone().add(0, 3, 0);

        this.effect = new ParticleShapeEffect(sheep.getLocation().clone().add(0, 3, 0), 2.3, 7,
                new RotationData()
                        .addRotationAxis(MathUtils.Axis.X, MathUtils.Axis.Y, MathUtils.Axis.Z)
                        .setTickInterval(1)
                        .setNegativeRotation()
                        .setDegreeIncrement(1),
                this.particle){

            @Override
            protected void run() {
                super.run();

                Particle.buildParticle(getLocation(), Particle.getEnumParticle().HEART(), 0.3f, 0.3f, 0.3f, remainingBlockToFix).toNearbyFifty();

            }
        };
        this.effect.start();

        onRun();
    }

    private void onRun() {
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(new Runnable() {
            @Override
            public void run() {
                if(effect != null) {

                    if(spleef.getCurrentStatus() == GameStatus.WAIT){
                        effect.kill();
                        return;
                    }

                    if (remainingBlockToFix <= 0) {
                        if (fixingBlocks.size() == 0) {
                            effect.kill();
                            return;
                        }
                    } else {

                        if (spleef.getDestroyedBlocks().size() != 0) {
                            List<Block> list = Lists.newArrayList(spleef.getDestroyedBlocks());
                            Collections.shuffle(list);

                            for (Block block : list) {
                                if (blockMap.values().contains(block)) {
                                    continue;
                                }

                                fixBlock(block);
                                remainingBlockToFix--;

                                break;
                            }
                        }
                    }
                }

                onRun();
            }
        });
    }

    @Override
    protected void onTick() {

    }

    private void fixBlock(Block block) {
        FollowingParticleEffect followingEffect = new FollowingParticleEffect(block.getLocation(),
                Particle.getEnumParticle().HEART(), ParticleSender.buildToAll(), this.effect.getLocation().clone(), 12){
            @Override
            public void onAsyncParticleTouchTarget(Entity target) {
                super.onAsyncParticleTouchTarget(target);

                FollowingParticleEffect effect1 = this;
                fixingBlocks.remove(this);
                runSync(new Runnable() {
                    @Override
                    public void run() {
                        spleef.getDestroyedBlocks().remove(block);
                        blockMap.get(effect1).setType(Material.SNOW_BLOCK);
                        blockMap.remove(effect1);
                    }
                });
            }
        }.setStopOnTouchTarget(true);

        followingEffect.start();

        this.fixingBlocks.add(followingEffect);
        this.blockMap.put(followingEffect, block);
    }

}