package fr.naruse.spleef.v1_13.game.spleef;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefPlayerJoinArenaEvent;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefPlayerQuitArenaEvent;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefReloadingEvent;
import fr.naruse.spleef.v1_13.api.event.cancellable.loading.SpleefLoadingArenasEvent;
import fr.naruse.spleef.v1_13.game.spleef.type.*;
import fr.naruse.spleef.v1_13.util.Message;
import fr.naruse.spleef.v1_13.util.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class Spleefs {
    private SpleefPluginV1_13 pl;
    private List<Spleef> spleefs = Lists.newArrayList();
    private HashMap<Player, Spleef> spleefOfPlayer = new HashMap<>();
    private HashMap<Player, SpleefPlayer> spleefPlayerOfPlayer = new HashMap<>();
    public Spleefs(SpleefPluginV1_13 pl) {
        this.pl = pl;
        int count = 0;
        for(int i = 0; i != 999; i++){
            if(pl.getConfig().getString("spleef."+i+".name") != null){
                String name = pl.getConfig().getString("spleef."+i+".name");
                if(pl.getConfig().getString("spleef."+i+".spleef.x") != null){
                    if(pl.getConfig().getString("spleef."+i+".spawn.x") != null){
                        if(pl.getConfig().getString("spleef."+i+".min") != null){
                            if(pl.getConfig().getString("spleef."+i+".max") != null){

                                if(!new SpleefAPIEventInvoker(new SpleefLoadingArenasEvent.Pre(pl, name)).isCancelled()){

                                    try{
                                        World wLoc = Bukkit.getWorld(pl.getConfig().getString("spleef."+i+".spleef.world"));
                                        World wSpawn = Bukkit.getWorld(pl.getConfig().getString("spleef."+i+".spawn.world"));
                                        if(wLoc == null || wSpawn == null){
                                            Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cA world is not found.");
                                            return;
                                        }else{
                                            Location spleefLoc = new Location(wLoc, pl.getConfig().getDouble("spleef."+i+".spleef.x"),
                                                    pl.getConfig().getDouble("spleef."+i+".spleef.y"), pl.getConfig().getDouble("spleef."+i+".spleef.z"),
                                                    pl.getConfig().getInt("spleef."+i+".spleef.yaw"), pl.getConfig().getInt("spleef."+i+".spleef.pitch"));
                                            Location spleefSpawn = new Location(wSpawn, pl.getConfig().getDouble("spleef."+i+".spawn.x"),
                                                    pl.getConfig().getDouble("spleef."+i+".spawn.y"), pl.getConfig().getDouble("spleef."+i+".spawn.z"),
                                                    pl.getConfig().getInt("spleef."+i+".spawn.yaw"), pl.getConfig().getInt("spleef."+i+".spawn.pitch"));
                                            Location spleefLobby = null;
                                            if(pl.getConfig().getString("spleef."+i+".lobby") != null){
                                                World wLob = Bukkit.getWorld(pl.getConfig().getString("spleef."+i+".lobby.world"));
                                                spleefLobby = new Location(wLob, pl.getConfig().getDouble("spleef."+i+".lobby.x"),
                                                        pl.getConfig().getDouble("spleef."+i+".lobby.y"), pl.getConfig().getDouble("spleef."+i+".lobby.z"),
                                                        pl.getConfig().getInt("spleef."+i+".lobby.yaw"), pl.getConfig().getInt("spleef."+i+".lobby.pitch"));
                                            }
                                            Location a = null, b = null;
                                            if(pl.getConfig().getString("spleef."+i+".region.a.x") != null && pl.getConfig().getString("spleef."+i+".region.b.x") != null){
                                                a = new Location(Bukkit.getWorld(pl.getConfig().getString("spleef."+i+".region.a.world")),
                                                        pl.getConfig().getDouble("spleef."+i+".region.a.x"), pl.getConfig().getDouble("spleef."+i+".region.a.y"),
                                                        pl.getConfig().getDouble("spleef."+i+".region.a.z"));
                                                b = new Location(Bukkit.getWorld(pl.getConfig().getString("spleef."+i+".region.b.world")),
                                                        pl.getConfig().getDouble("spleef."+i+".region.b.x"), pl.getConfig().getDouble("spleef."+i+".region.b.y"),
                                                        pl.getConfig().getDouble("spleef."+i+".region.b.z"));
                                            }
                                            int max = pl.getConfig().getInt("spleef."+i+".max");
                                            int min = pl.getConfig().getInt("spleef."+i+".min");
                                            boolean isOpen = pl.getConfig().getBoolean("spleef."+i+".isOpen");
                                            if(pl.getConfig().getString("spleef."+i+".gameMode") == null){
                                                pl.getConfig().set("spleef."+i+".gameMode", SpleefGameMode.NORMAL.name());
                                                pl.saveConfig();
                                            }
                                            SpleefGameMode gameMode = SpleefGameMode.valueOf(pl.getConfig().getString("spleef."+i+".gameMode"));
                                            if(gameMode == null){
                                                gameMode = SpleefGameMode.NORMAL;
                                            }
                                            Spleef spleef = null;
                                            switch (gameMode){
                                                case NORMAL:{
                                                    spleef = new NormalSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                                case DUEL:{
                                                    spleef = new DuelSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                                case TWO_TEAM:{
                                                    spleef = new TwoTeamSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                                case SPLEGG:{
                                                    spleef = new SpleegSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                                case BOW:{
                                                    spleef = new BowSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                                case MELTING:{
                                                    if(a == null || b == null){
                                                        Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cMelting spleef need a region. §7(Spleef name: "+name+")");
                                                        return;
                                                    }
                                                    spleef = new MeltingSpleef(pl, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen).buildRegion(a, b);
                                                    break;
                                                }
                                            }
                                            Bukkit.getPluginManager().registerEvents(spleef, pl.getSpleefPlugin());
                                            for(World world : Bukkit.getWorlds()){
                                                spleef.registerNewSigns(world);
                                            }
                                            spleefs.add(spleef);

                                            new SpleefAPIEventInvoker(new SpleefLoadingArenasEvent.Post(pl, name, spleef, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen, gameMode, a, b));

                                            count++;
                                        }
                                    }catch (Exception e){
                                        Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cEither a world is not found, either a number was wrote wrong.");
                                    }

                                }else{
                                    Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §6The loading for "+name+" was cancelled.");
                                }

                            }else{
                                Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cThe number MAX does not exist for spleef named "+name+".");
                            }
                        }else{
                            Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cThe number MIN does not exist for spleef named "+name+".");
                        }
                    }else{
                        Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cThe location SPAWN does not exist for spleef named "+name+".");
                    }
                }else{
                    Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cThe location ARENA does not exist for spleef named "+name+".");
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §a"+count+" spleefs found.");
    }

    public void addPlayer(Player p, Spleef spleef){
        if(!spleef.isOpen()){
            p.sendMessage("§c§l[§5"+spleef.getName()+"§c§l] §c"+ Message.SPLEEF_CLOSED.getMessage());
            return;
        }
        if(!spleefOfPlayer.containsKey(p)){
            if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                if(!pl.wagers.getWagerOfPlayer().get(p).isWagerActive()){
                    p.sendMessage("§c"+ Message.CANT_JOIN_WAGER_NOT_ACTIVE.getMessage());
                    return;
                }
            }
            if(pl.duels.duelActive(p)){
                if(spleef.getGameMode() != SpleefGameMode.DUEL){
                    p.sendMessage("§c"+ Message.YOU_ONLY_CAN_JOIN_DUEL_SPLEEF.getMessage());
                    return;
                }
            }
            SpleefPlayer spleefPlayer = new SpleefPlayer(pl, p);
            spleefPlayer.registerInventory();
            spleefPlayer.registerGameMode();
            spleefPlayer.registerIsFlying();
            spleefPlayerOfPlayer.put(p, spleefPlayer);
            if(!new SpleefAPIEventInvoker(new SpleefPlayerJoinArenaEvent.Pre(pl, spleef, p)).isCancelled()){
                if(spleef.addPlayer(p)){
                    spleefOfPlayer.put(p, spleef);
                    new SpleefAPIEventInvoker(new SpleefPlayerJoinArenaEvent.Post(pl, spleef, p));
                    return;
                }
            }
        }else{
            p.sendMessage("§c§l[§5"+spleef.getName()+"§c§l] §c"+ Message.YOU_ALREADY_IN_GAME.getMessage());
        }
    }

    public boolean removePlayer(Player p){
        if(spleefOfPlayer.containsKey(p)){
            try{
                if(!new SpleefAPIEventInvoker(new SpleefPlayerQuitArenaEvent.Pre(pl, spleefOfPlayer.get(p), p)).isCancelled()){
                    spleefOfPlayer.get(p).removePlayer(p);
                    new SpleefAPIEventInvoker(new SpleefPlayerQuitArenaEvent.Post(pl, p));
                }
            }catch (Exception e){
                p.teleport(spleefOfPlayer.get(p).getSpleefSpawn());
            }
            spleefOfPlayer.remove(p);
            if(pl.getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            if(spleefPlayerOfPlayer.containsKey(p)){
                SpleefPlayer spleefPlayer = spleefPlayerOfPlayer.get(p);
                p.getInventory().clear();
                spleefPlayer.setPlayerInventory();
                spleefPlayer.setPlayerGameMode();
                spleefPlayer.setIsFlying();
                spleefPlayer.getSpleefPlayerStatistics().addGames(1);
                spleefPlayer.getSpleefPlayerStatistics().addLoses(1);
                spleefPlayer.getSpleefPlayerStatistics().saveStatistics();
            }
            p.setFireTicks(0);
            return true;
        }
        return false;
    }

    public void reload(){
        for(Spleef spleef : spleefs){
            if(!new SpleefAPIEventInvoker(new SpleefReloadingEvent.Pre(pl, spleef)).isCancelled()){
                spleef.restart(false);
                spleef.stop();
                spleef.onDisable(true);
            }
        }
        pl.spleefs = new Spleefs(pl);
        new SpleefAPIEventInvoker(new SpleefReloadingEvent.Post(pl, pl.spleefs.getSpleefs()));
    }

    public boolean hasSpleef(Player p){
        return spleefOfPlayer.containsKey(p);
    }

    public List<Spleef> getSpleefs() {
        return spleefs;
    }

    public void onDisable() {
        for(Spleef spleef : getSpleefs()){
            spleef.onDisable(true);
        }
    }

    public SpleefPlayer getSpleefPlayer(Player p) {
        if(spleefPlayerOfPlayer.containsKey(p)){
            return spleefPlayerOfPlayer.get(p);
        }
        SpleefPlayer spleefPlayer = new SpleefPlayer(pl, p);
        spleefPlayer.registerInventory();
        spleefPlayer.registerGameMode();
        spleefPlayer.registerIsFlying();
        spleefPlayerOfPlayer.put(p, spleefPlayer);
        return spleefPlayer;
    }

    public boolean hasSpleefPlayer(Player p){
        return spleefPlayerOfPlayer.containsKey(p);
    }
}