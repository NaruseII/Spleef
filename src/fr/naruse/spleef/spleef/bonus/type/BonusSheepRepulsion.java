package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.api.effect.RotationData;
import fr.naruse.api.effect.particle.ParticleRotatingCircleEffect;
import fr.naruse.api.effect.particle.ParticleRotatingCirclesEffect;
import fr.naruse.api.particle.Particle;
import fr.naruse.api.particle.version.VersionManager;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.bonus.IFriendlyBonus;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import java.util.List;
import java.util.stream.Collectors;

public class BonusSheepRepulsion extends BonusColored implements IFriendlyBonus {

    private ParticleRotatingCirclesEffect effect;

    public BonusSheepRepulsion(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§9§lDeviant Sheep", 11, 8);
        setApplyVelocity(false);
    }

    @Override
    public void onSheepSpawned(Sheep sheep) {
        super.onSheepSpawned(sheep);

        Location location = sheep.getLocation();
        this.effect = new ParticleRotatingCirclesEffect() {
            @Override
            public void initCircleEffect() {
                newCircleEffect(location, 2, 20, MathUtils.Axis.Y,
                        new RotationData()
                                .addRotationAxis(MathUtils.Axis.X, MathUtils.Axis.Y, MathUtils.Axis.Z)
                                .setTickInterval(random.nextInt(3))
                                .setDegreeIncrement(1),
                        Particle.getEnumParticle().SOUL_FIRE_FLAME());
                newCircleEffect(location, 2, 20, MathUtils.Axis.X,
                        new RotationData()
                                .addRotationAxis(MathUtils.Axis.X, MathUtils.Axis.Y, MathUtils.Axis.Z)
                                .setTickInterval(random.nextInt(3))
                                .setDegreeIncrement(1),
                        Particle.getEnumParticle().FIRE());
                newCircleEffect(location, 2, 20, MathUtils.Axis.Z,
                        new RotationData()
                                .addRotationAxis(MathUtils.Axis.X, MathUtils.Axis.Y, MathUtils.Axis.Z)
                                .setTickInterval(random.nextInt(3))
                                .setDegreeIncrement(1),
                        Particle.getEnumParticle().SOUL());
            }
        };
        this.effect.start();
    }

    @Override
    protected void onTick() {
        super.onTick();
        if(sheep != null && !sheep.isDead()){
            sheep.setTarget(p);
            List<Entity> stream = getNearbySheeps(sheep.getLocation(), 10, 5, 10, true, p).filter(entity -> entity != sheep).collect(Collectors.toList());;
            runSync(() -> {
                VersionManager.getVersion().moveEntityToDestination(sheep, p.getLocation(), 2);
                stream.forEach(entity -> entity.setVelocity(MathUtils.genVector(sheep.getLocation(), entity.getLocation()).multiply(1.5).setY(0.5)));
            });

            if(this.effect != null){
                for (ParticleRotatingCircleEffect effect : this.effect.getEffectList()) {
                    effect.setLocation(sheep.getLocation().clone());
                    effect.calculateShape();
                }
            }

        }
    }

    @Override
    protected void onAction() {
        effect.kill();
    }
}
