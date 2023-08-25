package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.effect.RotationData;
import fr.naruse.api.effect.particle.ParticleShapeEffect;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BonusCubeVacuum extends BonusColored {
    
    private final List<ParticleShapeEffect> cubeList = Lists.newArrayList();
    private final List<FallingBlock> fallingBlockList = Lists.newArrayList();
    private final List<Block> locatedBlocks = Lists.newArrayList();
    private boolean willDestroysItself = false;

    public BonusCubeVacuum(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§8§lCube Vacuum Sheep", 7, 3);

        BonusManager.addRestartRunnable(() -> this.destroy());
    }

    @Override
    protected void onAction() {
        this.sheep.getWorld().playSound(this.sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        this.sendParticle(this.sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 4, 3, 4, 5);

        for (Block block : MathUtils.nearBlocks(this.sheep.getLocation().getBlock())) {
            if(block.getType() == Material.SNOW_BLOCK){

                this.locateBlocks(block.getLocation());

                this.startCubeSpawner(this.sheep.getLocation().clone().add(0, 8, 0), System.currentTimeMillis());
                return;
            }
        }

    }

    private void locateBlocks(Location location) {
        if(this.locatedBlocks.size() >= 40){
            return;
        }
        List<Block> list = Lists.newArrayList(MathUtils.nearBlocks(location.getBlock()));
        Collections.shuffle(list);

        for (Block block : list) {
            if(block.getType() == Material.SNOW_BLOCK && !this.locatedBlocks.contains(block)){
                this.locatedBlocks.add(block);
                this.locateBlocks(block.getLocation());
            }
        }

    }

    private void startCubeSpawner(Location location, long addTime) {
        if(System.currentTimeMillis()-addTime < 500){
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> this.startCubeSpawner(location, addTime));
            return;
        }

       if(this.destroyedIfNeeded()){
           return;
       }

       if(this.cubeList.size() < 1){

           location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
           this.sendParticle(location, Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 1, 2, 2);

           RotationData rotationData =  new RotationData();

           List<MathUtils.Axis> list = Lists.newArrayList(MathUtils.Axis.values());
           if(Utils.RANDOM.nextBoolean()){
               Collections.shuffle(list);
               list.remove(list.get(0));
           }

           rotationData.addRotationAxis(list.toArray(new MathUtils.Axis[0]));
           rotationData.setTickInterval(0);
           rotationData.setDegreeIncrement(0);
           if(Utils.RANDOM.nextBoolean()){
               rotationData.setNegativeRotation();
           }

           ParticleShapeEffect effect = new ParticleShapeEffect(location, 6, 15, rotationData,
                   Particle.getEnumParticle().WITCH());
           effect.start();

           this.cubeList.add(effect);

           CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> this.startCubeSpawner(location.add(0, 8, 0), System.currentTimeMillis()));
       }else{

           for (ParticleShapeEffect effect : this.cubeList) {
               this.moveCubeApart(effect, System.currentTimeMillis(), 3+Utils.RANDOM.nextInt(2));
           }

       }
    }


    private void moveCubeApart(ParticleShapeEffect effect, long addTime, int maxDegree){
        if(System.currentTimeMillis()-addTime < 500){
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> this.moveCubeApart(effect, addTime, maxDegree));
            return;
        }
        if(this.destroyedIfNeeded()){
            return;
        }

        RotationData rotationData = effect.getRotationData();
        if(rotationData.getDegreeIncrement() >= maxDegree){
            this.vacuum(effect, System.currentTimeMillis());
            return;
        }

        effect.getRotationData().setDegreeIncrement(effect.getRotationData().getDegreeIncrement()+0.1);

        this.moveCubeApart(effect, System.currentTimeMillis(), maxDegree);
    }

    private void vacuum(ParticleShapeEffect effect, long addTime) {
        if(System.currentTimeMillis()-addTime < 50+Utils.RANDOM.nextInt(250)){
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> this.vacuum(effect, addTime));
            return;
        }
        if (this.destroyedIfNeeded()) {
            return;
        }

        if(this.locatedBlocks.isEmpty()){
            if(this.willDestroysItself){
                return;
            }
            this.willDestroysItself = true;
            this.runSyncLater(() -> this.destroy(), 20*10);
            return;
        }

        Block newBlock = this.locatedBlocks.get(0);
        this.locatedBlocks.remove(0);
        if(newBlock.getType() != Material.SNOW_BLOCK){
            this.vacuum(effect, addTime);
            return;
        }

        this.vacuum(effect,  System.currentTimeMillis());

        Block finalNewBlock = newBlock;
        this.runSync(() -> {
            FallingBlock fallingBlock = finalNewBlock.getWorld().spawnFallingBlock(finalNewBlock.getLocation(), Material.SNOW_BLOCK, (byte) 0);
            fallingBlock.setGravity(false);
            fallingBlock.setInvulnerable(true);
            this.fallingBlockList.add(fallingBlock);

            this.vacuumBlock(effect, fallingBlock);

            this.spleef.destroyBlock(this.p, finalNewBlock, true);
        });
    }

    private void vacuumBlock(ParticleShapeEffect effect, FallingBlock fallingBlock) {
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> {

            double distanceSquared = MathUtils.distanceSquared(fallingBlock.getLocation(), effect.getLocation());
            if(distanceSquared >= 3.5*3.5){
                fallingBlock.setVelocity(MathUtils.genVector(fallingBlock.getLocation(), effect.getLocation()).normalize().multiply(0.2));
            }else{
                //fallingBlock.setVelocity(fallingBlock.getVelocity().multiply(0.9));
            }
            fallingBlock.setTicksLived(1);

            this.vacuumBlock(effect, fallingBlock);
        });
    }

    private boolean destroyedIfNeeded(){
        if(this.spleef.getCurrentStatus() == GameStatus.WAIT){
            this.destroy();
            return true;
        }
        return false;
    }

    private void destroy() {
        for (ParticleShapeEffect effect : this.cubeList) {
            effect.kill();
        }
        Runnable runnable = () -> {
            for (FallingBlock fallingBlock : this.fallingBlockList) {
                fallingBlock.remove();
            }
        };
        if(Bukkit.isPrimaryThread()){
            runnable.run();
        }else{
            this.runSync(runnable);
        }
    }


}
