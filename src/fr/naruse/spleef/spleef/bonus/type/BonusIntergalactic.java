package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import fr.naruse.spleef.utils.CollectionManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        Location location = sheep.getLocation();
        sendParticle(location, "FLAME", 0.3f, 1, 0.3f, 60, 1);
        for (int i = 0; i < 100; i++) {
            sendParticle(location, "TOWN_AURA", 0.25f, 0.5f, 0.25f, 20);
            location.add(0, 1, 0);
        }
        location = sheep.getLocation();
        Location finalLocation = location;
        runSync(() -> {
            Pig pig = (Pig) finalLocation.getWorld().spawnEntity(finalLocation.add(0, 100, 0), EntityType.PIG);
            if(pig == null){
                return;
            }
            pig.setInvulnerable(true);
            pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 10));
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> runPigTicker(pig));
        });
    }

    private void runPigTicker(Pig pig) {
        if(pig == null || pig.isDead()){
            return;
        }
        if(pig.getLocation().getY() <= -20 || pig.getLocation().getBlock().getType().name().contains("LAVA") || pig.getLocation().getBlock().getType().name().contains("WATER")){
            runSync(() -> pig.remove());
            return;
        }
        Runnable runnable = () -> {
            BlockBuffer blockBuffer = new BlockBuffer();
            for (Location loc : Utils.getSphere(pig.getLocation().add(0, -1, 0), 6, 2, false, true, 0)) {
                if(loc.getBlock().getType() == Material.SNOW_BLOCK || loc.getBlock().getType() == Material.TNT){
                    blockBuffer.add(loc.getBlock());
                }
            }

            if(!blockBuffer.isEmpty()){
                spleef.destroyBlock(p, blockBuffer);
            }

            sendParticle(new ParticleBuffer()
                    .add(pig.getLocation(), "FLAME", 4, 4, 4, 8, 1f)
                    .add(pig.getLocation(), "EXPLOSION_HUGE", 2, 2, 2, 2, 1)
                    .add(pig.getLocation(), "TOWN_AURA", 4, 4, 4, 4, 1)
                    .add(pig.getLocation(), "EXPLOSION_LARGE", 2, 2, 2, 2, 1));
            if(pig.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR){
                runSync(() -> pig.teleport(pig.getLocation().clone().add(0, -1, 0)));
            }
            runPigTicker(pig);
        };
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
    }
}
