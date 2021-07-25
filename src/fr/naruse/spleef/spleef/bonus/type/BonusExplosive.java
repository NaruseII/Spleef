package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BonusExplosive extends BonusColored {
    public BonusExplosive(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§c§lExplosive Sheep", 14, 2+random.nextInt(6));
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), "EXPLOSION_HUGE", 4, 3, 4, 5);
        BlockBuffer blockBuffer = new BlockBuffer();
        for (int i = 0; i < 5; i++) {
            for (Block block : Utils.getCircle(sheep.getLocation().clone().add(0, -1, 0), i)) {
                if(block.getType() == Material.SNOW_BLOCK || block.getType() == Material.TNT){
                    blockBuffer.add(block);
                }
            }
        }
        spleef.destroyBlock(p, blockBuffer);
    }
}
