package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Sets;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.effect.particle.FollowingParticleEffect;
import fr.naruse.api.effect.particle.FollowingParticlePathEffect;
import fr.naruse.api.particle.Particle;
import fr.naruse.api.particle.sender.ParticleSender;
import fr.naruse.api.particle.version.VersionManager;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.bonus.IFriendlyBonus;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BonusIntergalacticShield extends BonusColored implements IFriendlyBonus {

    private final Set<FollowingParticleEffect> particles = Sets.newHashSet();
    private final Set<FollowingParticlePathEffect> loopingParticles = Sets.newHashSet();
    private final Set<FollowingParticleEffect> counterParticles = Sets.newHashSet();
    private boolean cancel = false;
    private int attackCount = 0;
    private long millis = 0;

    public BonusIntergalacticShield(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§8§lIntergalactic Shield Sheep", 15, 15);
        setApplyVelocity(false);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if(sheep != null && !sheep.isDead()){
            Optional<Entity> optional = getNearbyEntities(sheep.getLocation(), 12, 200, 12)
                    .filter(entity -> entity instanceof Pig && ((Pig) entity).hasPotionEffect(PotionEffectType.INVISIBILITY) && entity.isInvulnerable())
                    .findFirst();

            runSync(() -> {
                sheep.setTarget(p);
                VersionManager.getVersion().moveEntityToDestination(sheep, p.getLocation(), 2);
            });

            if(!optional.isPresent()){
                return;
            }

            runSync(() -> sheep.remove());
            cancel();

            this.createShield(optional.get());
        }
    }

    @Override
    protected void onAction() {

    }

    @Override
    public void onRestart() {
        this.particles.forEach(followingParticle -> followingParticle.setDone(true));
        for (FollowingParticlePathEffect loopingParticle : this.loopingParticles) {
            loopingParticle.setCancelled(true);
        }
        for (FollowingParticleEffect particle : this.counterParticles) {
            particle.setDone(true);
        }
        this.cancel = true;
    }

    private void createShield(Entity entity) {
        this.millis = System.currentTimeMillis();

        Location center = sheep.getLocation().add(0, 8, 0);
        center.setX(entity.getLocation().getX());
        center.setZ(entity.getLocation().getZ());

        int r = 3;
        int amount = 20;
        for (int i = 0; i < 4; i++) {
            for (Location loc : MathUtils.getCircle(center, r, amount)) {
                this.particles.add(new FollowingParticleEffect(loc, Particle.getEnumParticle().SPELL_WITCH(), ParticleSender.buildToNearbyFifty(), sheep.getEyeLocation(), 15).setStopOnTouchTarget(false).start());
            }
            r += 2;
            amount += 4;
            center.add(0, 0.5, 0);
        }

        List<Location> loop = MathUtils.getCircle(center, 11, 24);

        int startIndex = 0;
        for (int i = 0; i < 8; i++) {

            Location loc = loop.get(startIndex);

            this.loopingParticles.add(new FollowingParticlePathEffect(loop, new FollowingParticleEffect[] {
                    new FollowingParticleEffect(loc, Particle.getEnumParticle().SPELL_WITCH(), ParticleSender.buildToNearbyFifty(), sheep.getEyeLocation(), 10).setStopOnTouchTarget(false).start()
                    , new FollowingParticleEffect(loc, Particle.getEnumParticle().CLOUD(), ParticleSender.buildToNearbyFifty(), sheep.getEyeLocation(), 10).setStopOnTouchTarget(false).start()
                    , new FollowingParticleEffect(loc, Particle.getEnumParticle().SMOKE_NORMAL(), ParticleSender.buildToNearbyFifty(), sheep.getEyeLocation(), 10).setStopOnTouchTarget(false).start()
            }, startIndex).start());

            startIndex += 3;
        }

        this.checkIntergalacticRunnable(center, entity);
    }

    private void checkIntergalacticRunnable(Location center, Entity entity){
        Runnable runnable = () -> {
            if(entity.isDead() || this.cancel || System.currentTimeMillis()-millis > 30000){
                this.onRestart();
                return;
            }

            double distance = entity.getLocation().distanceSquared(center);
            if(distance <= 200){
                runSync(() -> {
                    if(!entity.isDead()){
                        entity.setFallDistance(0);
                        entity.setVelocity(new Vector(0, 0.3, 0));
                    }
                });
            }

            if(distance <= 225 && this.counterParticles.size() <= 8){
                Location clone = center.clone().add(0, -2, 0);
                clone.setZ(MathUtils.offSet(clone.getZ(), 800));
                clone.setX(MathUtils.offSet(clone.getX(), 800));

                FollowingParticleEffect particle = new FollowingParticleEffect(entity, Particle.getEnumParticle().TOWN_AURA(), ParticleSender.buildToNearbyFifty(), clone, 15){
                    @Override
                    public void onAsyncParticleTouchTarget(Entity target) {
                        counterParticles.remove(this);
                        attackCount++;
                    }
                }.start();
                this.counterParticles.add(particle);
            }

            if(this.attackCount >= 40){
                this.cancel = true;
                this.onRestart();
                if(!entity.isDead()){
                    runSync(() -> {
                        entity.remove();
                        CollectionManager.ASYNC_ENTITY_LIST.remove(entity);
                    });
                }
                return;
            }

            this.checkIntergalacticRunnable(center, entity);
        };
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
    }
}
