package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BonusMelt extends BonusColored {
    public BonusMelt(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§b§lMelting Sheep", 3, 5);
    }

    @Override
    protected void onTick() {
        super.onTick();
        BlockBuffer blockBuffer = new BlockBuffer();
        for (int i = 0; i < 4; i++) {
            for (Block block : MathUtils.get2DCircleBlock(sheep.getLocation().add(0, -1, 0), i)) {
                if((block.getType() == Material.SNOW_BLOCK || block.getType() == Material.TNT) && block.getLocation().distanceSquared(p.getLocation()) > 9){
                    blockBuffer.add(block);
                }
            }
        }

        if(!blockBuffer.isEmpty()){
            spleef.destroyBlock(p, blockBuffer, 10, true);
        }
    }

    @Override
    protected void onAction() {

    }
}
