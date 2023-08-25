package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.particle.Particle;
import fr.naruse.api.particle.sender.ParticleBuffer;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BonusIntergalactic extends BonusColored {

    public BonusIntergalactic(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§a§lIntergalactic Sheep", 5, 8+random.nextInt(3));
        setMulticolor(true);
    }

    private long startTime;

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        Location location = sheep.getLocation();
        sendParticle(location, Particle.getEnumParticle().FLAME(), 0.3f, 1, 0.3f, 60, 1);
        for (int i = 0; i < 100; i++) {
            sendParticle(location, Particle.getEnumParticle().TOWN_AURA(), 0.25f, 0.5f, 0.25f, 20);
            location.add(0, 1, 0);
        }
        location = sheep.getLocation();
        Location finalLocation = location;
        runSync(() -> {
            startTime = System.currentTimeMillis();
            Pig pig = (Pig) finalLocation.getWorld().spawnEntity(finalLocation.add(0, 200, 0), EntityType.PIG);
            if(pig == null){
                return;
            }

            CollectionManager.ASYNC_ENTITY_LIST.add(pig);
            pig.setInvulnerable(true);
            pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 10));
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> runPigTicker(pig));
        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    private void runPigTicker(Pig pig) {
        if(pig == null || pig.isDead()){
            return;
        }

        if(System.currentTimeMillis()-startTime > 40000 || pig.getLocation().getY() <= -20 || pig.getLocation().getBlock().getType().name().contains("LAVA") || pig.getLocation().getBlock().getType().name().contains("WATER")){
            runSync(() -> {
                pig.remove();
                CollectionManager.ASYNC_ENTITY_LIST.remove(pig);
            });
            return;
        }

        Runnable runnable = () -> {

            BlockBuffer blockBuffer = new BlockBuffer();
            for (Block block : MathUtils.get3DCylinder(pig.getLocation().clone().add(0, -2, 0), 6, 3, true)) {
                if(block.getType() == Material.SNOW_BLOCK || block.getType() == Material.TNT){
                    blockBuffer.add(block);
                }
            }

            if(!blockBuffer.isEmpty()){
                spleef.destroyBlock(p, blockBuffer, true);
            }

            sendParticle(new ParticleBuffer()
                    .buildParticle(pig.getLocation(), Particle.getEnumParticle().FLAME(), 4, 4, 4, 20, 0.3f)
                    .buildParticle(pig.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 2, 2, 5, 0.5f));

            if(this.checkBlocks(pig.getLocation())){
                runSync(() -> pig.teleport(pig.getLocation().clone().add(0, -1, 0)));
            }

            runPigTicker(pig);
        };
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
    }

    private boolean checkBlocks(Location location){
        return location.getBlock().getRelative(0, -1, 0).getType() != Material.AIR && location.getBlock().getRelative(0, -1, 0).getType() != Material.SNOW_BLOCK && location.getBlock().getRelative(0, -1, 0).getType() != Material.TNT;
    }
}
