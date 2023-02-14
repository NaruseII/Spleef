package fr.naruse.spleef.spleef;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.type.BowSpleef;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.spleef.type.Splegg;
import fr.naruse.spleef.spleef.type.TeamSpleef;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Spleefs {

    private SpleefPlugin pl;
    private List<Spleef> spleefs = Lists.newArrayList();
    private final Map<Integer, String> misconfiguredReasons = Maps.newHashMap();

    public Spleefs(SpleefPlugin pl) {
        this.pl = pl;
        reload();
    }

    public void reload() {
        for (Spleef spleef : spleefs) {
            spleef.disable();
        }
        misconfiguredReasons.clear();
        spleefs.clear();
        for (int i = 0; i < 9999; i++) {
            if(pl.getConfig().contains("spleef."+i+".name")){
                String name = pl.getConfig().getString("spleef."+i+".name");
                boolean isOpened = pl.getConfig().getBoolean("spleef."+i+".isOpened");
                if(!pl.getConfig().contains("spleef."+i+".gameType")){
                    pl.getConfig().set("spleef."+i+".gameType", GameType.SPLEEF.name());
                    pl.saveConfig();
                }
                GameType gameType;
                try{
                    String type = pl.getConfig().getString("spleef."+i+".gameType");
                    if(type.equals("SPPLEGG")){
                        gameType = GameType.SPLEGG;
                    }else{
                        gameType = GameType.valueOf(type);
                    }
                }catch (Exception e){
                    String reason = "Can't recognize GameType for Spleef '"+name+"'";
                    pl.getLogger().warning(reason);
                    this.misconfiguredReasons.put(i, reason);
                    continue;
                }

                Location arena = Utils.getLocation(pl, "spleef."+i+".location.arena");
                if(arena == null){
                    String reason ="Location ARENA not found for Spleef '"+name+"'";
                    pl.getLogger().warning(reason);
                    this.misconfiguredReasons.put(i, reason);
                    continue;
                }
                Location spawn = Utils.getLocation(pl, "spleef."+i+".location.spawn");
                if(spawn == null){
                    String reason = "Location SPAWN not found for Spleef '"+name+"'";
                    pl.getLogger().warning(reason);
                    this.misconfiguredReasons.put(i, reason);
                    continue;
                }
                Location lobby = Utils.getLocation(pl, "spleef."+i+".location.lobby");

                if(!pl.getConfig().contains("spleef."+i+".min")){
                    String reason = "Minimum not found for Spleef '"+name+"'";
                    pl.getLogger().warning(reason);
                    this.misconfiguredReasons.put(i, reason);
                    continue;
                }
                int min = pl.getConfig().getInt("spleef."+i+".min");

                if(!pl.getConfig().contains("spleef."+i+".max")){
                    String reason = "Maximum not found for Spleef '"+name+"'";
                    pl.getLogger().warning(reason);
                    this.misconfiguredReasons.put(i, reason);
                    continue;
                }
                int max = pl.getConfig().getInt("spleef."+i+".max");

                boolean sheepBonusEnabled = false;
                if(pl.getConfig().contains("spleef."+i+".sheepBonusEnabled")){
                    sheepBonusEnabled = pl.getConfig().getBoolean("spleef."+i+".sheepBonusEnabled");
                }else{
                    sheepBonusEnabled = pl.getConfig().contains("sheepBonuses") ? pl.getConfig().getBoolean("sheepBonuses") : true;
                    pl.getConfig().set("spleef."+i+".sheepBonusEnabled", sheepBonusEnabled);
                    pl.saveConfig();
                }

                Spleef spleef;
                switch (gameType){
                    case SPLEGG:
                        spleef = new Splegg(pl, i, name, isOpened, max, min, arena, spawn, lobby, sheepBonusEnabled);
                        break;
                    case BOW:
                        spleef = new BowSpleef(pl, i, name, isOpened, max, min, arena, spawn, lobby, sheepBonusEnabled);
                        break;
                    case TEAM_TWO:
                        spleef = new TeamSpleef(pl, i, name, isOpened, max, min, arena, spawn, lobby, 2, sheepBonusEnabled);
                        break;
                    case TEAM_THREE:
                        spleef = new TeamSpleef(pl, i, name, isOpened, max, min, arena, spawn, lobby, 3, sheepBonusEnabled);
                        break;
                    case TEAM_FOUR:
                        spleef = new TeamSpleef(pl, i, name, isOpened, max, min, arena, spawn, lobby, 4, sheepBonusEnabled);
                        break;
                    default:
                        spleef = new Spleef(pl, i, name, isOpened, max, min, arena, spawn, lobby, sheepBonusEnabled);
                }

                pl.getServer().getPluginManager().registerEvents(spleef, pl);
                spleefs.add(spleef);
            }
        }
        pl.getLogger().log(Level.INFO, spleefs.size()+" spleefs found");
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> {
            for (int i = 0; i < spleefs.size(); i++) {
                Spleef spleef = spleefs.get(i);
                for(World world : Bukkit.getWorlds()){
                    spleef.registerNewSigns(world);
                }
            }
        }, 20);
    }

    public List<Spleef> getSpleefs() {
        return spleefs;
    }

    public Map<Integer, String> getMisconfiguredReasons() {
        return misconfiguredReasons;
    }
}
