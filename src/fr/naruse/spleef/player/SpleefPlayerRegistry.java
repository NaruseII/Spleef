package fr.naruse.spleef.player;

import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class SpleefPlayerRegistry {

    private HashMap<OfflinePlayer, SpleefPlayer> spleefPlayerHashMap = Maps.newHashMap();

    public SpleefPlayerRegistry(SpleefPlugin pl) {
        long millis = System.currentTimeMillis();
        pl.getLogger().log(Level.INFO, "Loading the data of all the players... (This may take a few seconds)");
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            registerPlayer(offlinePlayer);
        }
        pl.getLogger().log(Level.INFO, "Data loaded in "+(System.currentTimeMillis()-millis)+" ms");
    }

    public SpleefPlayer registerPlayer(OfflinePlayer p){
        if(spleefPlayerHashMap.containsKey(p)){
            return spleefPlayerHashMap.get(p);
        }
        SpleefPlayer spleefPlayer = new SpleefPlayer(SpleefPlugin.getPlugin(SpleefPlugin.class), p.getUniqueId().toString());
        spleefPlayerHashMap.put(p, spleefPlayer);
        return spleefPlayer;
    }

    public SpleefPlayer getSpleefPlayer(OfflinePlayer p){
        if(spleefPlayerHashMap.containsKey(p)){
            return spleefPlayerHashMap.get(p);
        }
        return registerPlayer(p);
    }

    public Set<Map.Entry<OfflinePlayer, SpleefPlayer>> getSpleefPlayerHashMap() {
        return spleefPlayerHashMap.entrySet();
    }
}
