package fr.naruse.spleef.spleef;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.type.BowSpleef;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.spleef.type.Splegg;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.logging.Level;

public class Spleefs {

    private SpleefPlugin pl;
    private List<Spleef> spleefs = Lists.newArrayList();

    public Spleefs(SpleefPlugin pl) {
        this.pl = pl;
        reload();
    }

    public void reload() {
        for (Spleef spleef : spleefs) {
            spleef.disable();
        }
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
                    pl.getLogger().warning("can't recognize GameType for Spleef '"+name+"'");
                    continue;
                }

                Location arena = Utils.getLocation(pl, "spleef."+i+".location.arena");
                if(arena == null){
                    pl.getLogger().warning("Location ARENA not found for Spleef '"+name+"'");
                    continue;
                }
                Location spawn = Utils.getLocation(pl, "spleef."+i+".location.spawn");
                if(spawn == null){
                    pl.getLogger().warning("Location SPAWN not found for Spleef '"+name+"'");
                    continue;
                }
                Location lobby = Utils.getLocation(pl, "spleef."+i+".location.lobby");

                if(!pl.getConfig().contains("spleef."+i+".min")){
                    pl.getLogger().warning("Minimum not found for Spleef '"+name+"'");
                    continue;
                }
                int min = pl.getConfig().getInt("spleef."+i+".min");

                if(!pl.getConfig().contains("spleef."+i+".max")){
                    pl.getLogger().warning("Maximum not found for Spleef '"+name+"'");
                    continue;
                }
                int max = pl.getConfig().getInt("spleef."+i+".max");

                Spleef spleef;
                switch (gameType){
                    case SPLEGG:
                        spleef = new Splegg(pl, i, name, isOpened, max, min, arena, spawn, lobby);
                        break;
                    case BOW:
                        spleef = new BowSpleef(pl, i, name, isOpened, max, min, arena, spawn, lobby);
                        break;
                    default:
                        spleef = new Spleef(pl, i, name, isOpened, max, min, arena, spawn, lobby);
                }

                pl.getServer().getPluginManager().registerEvents(spleef, pl);
                spleefs.add(spleef);
            }
        }
        pl.getLogger().log(Level.INFO, ""+spleefs.size()+" spleefs found");
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
}
