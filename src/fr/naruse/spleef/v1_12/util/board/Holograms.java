package fr.naruse.spleef.v1_12.util.board;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_12.api.event.cancellable.game.SpleefHologramsUpdateEvent;
import fr.naruse.spleef.v1_12.util.Message;
import fr.naruse.spleef.v1_12.util.SpleefPlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Holograms extends BukkitRunnable {
    private SpleefPluginV1_12 pl;
    private Hologram hologram;
    private Location location = null;
    private HashMap<OfflinePlayer, Long> playerPoints = new HashMap<>();
    private boolean isRunning = false;
    public Holograms(SpleefPluginV1_12 spleefPlugin) {
        this.pl = spleefPlugin;
        if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14")){
            return;
        }
        if(pl.getConfig().getString("holograms.location.x") == null){
            return;
        }
        if(!pl.getConfig().getBoolean("holograms.enable")){
            return;
        }
        this.location = new Location(Bukkit.getWorld(pl.getConfig().getString("holograms.location.world")), pl.getConfig().getDouble("holograms.location.x"),
                pl.getConfig().getDouble("holograms.location.y"), pl.getConfig().getDouble("holograms.location.z"));
        if(location == null){
            return;
        }
        if(!pl.otherPluginSupport.getHolographicDisplaysPlugin().isPresent()){
            Bukkit.getConsoleSender().sendMessage(Message.SPLEEF.getMessage()+" §cWhere is HolographicDisplays ?");
            return;
        }
        this.hologram = HologramsAPI.createHologram(pl.getSpleefPlugin(), location);
        for(int i = 0; i != 6; i++){
            hologram.appendTextLine("");
        }
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(pl.getConfig().getString(p.getUniqueId().toString()) != null){
                long points = pl.getConfig().getInt(p.getUniqueId().toString());
                playerPoints.put(p, points);
            }
        }
        hologram.insertTextLine(0, "§6"+ Message.SPLEEF_PLAYER_RANKING.getMessage()+"§c§c§l");
        this.runTaskTimer(pl.getSpleefPlugin(), 20*5, 20*5);
        this.run();
        this.isRunning = true;
    }

    @Override
    public void run() {
        addPlayers();
        HashMap<Long, List<OfflinePlayer>> placeAndPlayer = getLeaderBoard();
        int count = 1, count2 = 5;
        for(int o = placeAndPlayer.size()-1; o >= 0; o--){
            String name = "§d-§6"+count+":,";
            long i = intList.get(o);
            if(placeAndPlayer.containsKey(i)){
                for(OfflinePlayer p : placeAndPlayer.get(i)){
                    if(p != null){
                        name += ", §a"+p.getName()+" §e(§6Wins: "+getSpleefPlayer(p).getWins()+"§e, §6Loses: "
                                +getSpleefPlayer(p).getLoses()+"§e)";
                    }
                }
                name = name.replace(",,", "");
                SpleefHologramsUpdateEvent shue = new SpleefHologramsUpdateEvent(pl, name, this);
                if(!new SpleefAPIEventInvoker(shue).isCancelled()){
                    name = shue.getLine();
                    hologram.removeLine(count);
                    hologram.insertTextLine(count, name);
                }
                if(count == 5 || count2 == 1){
                    break;
                }
                count++;
                count2--;
            }
        }
        intList.clear();
    }

    private List<Long> intList = Lists.newArrayList();
    private List<String> nameUsed = Lists.newArrayList();
    private HashMap<Long, List<OfflinePlayer>> getLeaderBoard(){
        HashMap<Long, List<OfflinePlayer>> pAndP = new HashMap<>();
        for(OfflinePlayer p : playerPoints.keySet()){
            if(!nameUsed.contains(p.getName())){
                long lives = playerPoints.get(p);
                intList.add(lives);
                if(!pAndP.containsKey(lives)){
                    pAndP.put(lives, Lists.newArrayList());
                }
                pAndP.get(lives).add(p);
                nameUsed.add(p.getName());
            }
        }
        Collections.sort(intList);
        nameUsed.clear();
        HashMap<Long, List<OfflinePlayer>> placeAndPlayer = new HashMap<>();
        for(long i : intList){
            placeAndPlayer.put(i, pAndP.get(i));
        }
        return placeAndPlayer;
    }

    public void addPlayerPoints(OfflinePlayer p, long points){
        if(playerPoints.containsKey(p)){
            playerPoints.remove(p);
        }
        playerPoints.put(p, points);
    }

    public void removeLeaderBoard(){
        if(isRunning){
            this.cancel();
        }
        if(hologram != null){
            hologram.delete();
        }
    }

    public void addPlayers(){
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(getSpleefPlayer(p).getWins() != 0){
                addPlayerPoints(p, getSpleefPlayer(p).getWins());
            }
        }
    }

    private HashMap<OfflinePlayer, SpleefPlayerStatistics> spleefPlayerHashMap = new HashMap<>();
    private SpleefPlayerStatistics getSpleefPlayer(OfflinePlayer p) {
        if(spleefPlayerHashMap.containsKey(p)){
            spleefPlayerHashMap.get(p).refreshStatisticFromConfig();
            return spleefPlayerHashMap.get(p);
        }
        SpleefPlayerStatistics spleefPlayerStatistics = new SpleefPlayerStatistics(pl, p.getName());
        spleefPlayerHashMap.put(p, spleefPlayerStatistics);
        return spleefPlayerStatistics;
    }
}
