package fr.naruse.spleef.common.helper;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_12.util.SpleefPlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SpleefHelper {


    private static void addPlayerPoints(OfflinePlayer p, long points){
        if(playerPoints.containsKey(p)){
            playerPoints.remove(p);
        }
        playerPoints.put(p, points);
    }

    private static void addPlayers(){
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(getSpleefPlayer(p).getWins() != 0){
                addPlayerPoints(p, getSpleefPlayer(p).getWins());
            }
        }
    }

    private static HashMap<OfflinePlayer, SpleefPlayerStatistics> spleefPlayerHashMap = new HashMap<>();
    private static SpleefPlayerStatistics getSpleefPlayer(OfflinePlayer p) {
        if(spleefPlayerHashMap.containsKey(p)){
            spleefPlayerHashMap.get(p).refreshStatisticFromConfig();
            return spleefPlayerHashMap.get(p);
        }
        SpleefPlayerStatistics spleefPlayerStatistics = new SpleefPlayerStatistics(SpleefPlugin.INSTANCE.getSpleefPlugin(), p.getName());
        spleefPlayerHashMap.put(p, spleefPlayerStatistics);
        return spleefPlayerStatistics;
    }

    private static List<Long> intList = Lists.newArrayList();
    private static List<String> nameUsed = Lists.newArrayList();
    private static HashMap<OfflinePlayer, Long> playerPoints = new HashMap<>();
    public static List<OfflinePlayer> getPlayerRank(int place){
        addPlayers();
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
        int count = 0;
        for(long i : placeAndPlayer.keySet()){
            if((count+1) == place){
                if(placeAndPlayer.containsKey(i)){
                    return placeAndPlayer.get(i);
                }
                break;
            }
            count++;
        }
        return null;
    }

    public static String listToString(List<OfflinePlayer> list) {
        if(list == null){
            return "";
        }
        if(list.size() == 0){
            return "";
        }
        String s = ",";
        for(OfflinePlayer player : list){
            SpleefPlayerStatistics statistics = getSpleefPlayer(player);
            s += ", "+player.getName()+" §7(Wins: "+statistics.getWins()+", Loses: "+statistics.getLoses()+")";
        }
        s = s.replace(",, ", "");
        return s;
    }
}
