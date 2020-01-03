package fr.naruse.spleef.common;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import org.bukkit.entity.Player;

public class WorldEditHelper {

    public static Region getRegion(SpleefPluginV1_13 pl, Player p) {
        try {
            return pl.worldEditPlugin.getSession(p).getSelection(new BukkitWorld(p.getWorld()));
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
