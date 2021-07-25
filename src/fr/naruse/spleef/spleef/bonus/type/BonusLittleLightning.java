package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import java.util.List;
import java.util.Set;

public class BonusLittleLightning extends BonusColored {
    public BonusLittleLightning(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§e§lTiny Zeus Sheep", 4, 3);
    }

    @Override
    public void onSheepSpawned(Sheep sheep) {
        super.onSheepSpawned(sheep);
        sheep.setBaby();
    }

    @Override
    protected void onAction() {
        List<Location> locations = Lists.newArrayList();
        for (int i = 0; i < 15; i++) {
            for (Block block : Utils.getCircle(sheep.getLocation(), i)) {
                locations.add(block.getLocation());
            }
        }
        Set<Location> strikes = Sets.newHashSet();
        BlockBuffer blockBuffer = new BlockBuffer();

        for (int i = 0; i < 10; i++) {
            Location loc = locations.get(random.nextInt(locations.size()));
            locations.remove(loc);
            loc.setY(0);

            strikes.add(loc);
            for (int j = 0; j < 256; j++) {
                Block b = loc.getWorld().getBlockAt(loc.getBlockX(), j, loc.getBlockZ());
                if(b.getType() == Material.SNOW_BLOCK || b.getType() == Material.TNT){
                    blockBuffer.add(b);
                }
            }
        }
        runSync(() -> strikes.forEach(location -> location.getWorld().strikeLightningEffect(location)));
        if(!blockBuffer.isEmpty()){
            spleef.destroyBlock(p, blockBuffer);
        }
    }
}
