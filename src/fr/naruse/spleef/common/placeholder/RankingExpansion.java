package fr.naruse.spleef.common.placeholder;

import fr.naruse.spleef.common.helper.SpleefHelper;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.v1_12.util.SpleefPlayerStatistics;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class RankingExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "spleef";
    }

    @Override
    public String getAuthor() {
        return "naruse";
    }

    @Override
    public String getVersion() {
        return SpleefPlugin.INSTANCE.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        return this.onRequest(p, params);
    }

    @Override
    public String onRequest(OfflinePlayer p, String identifier) {
        //RANK
        if(identifier.equals("first")){
            List<OfflinePlayer> players = SpleefHelper.getPlayerRank(1);
            if(players == null){
                return super.onRequest(p, identifier);
            }
            String name = ",";
            for(OfflinePlayer player : players){
                name += ", "+player.getName();
            }
            return name.replace(",, ", "");
        }
        if(identifier.equals("second")){
            List<OfflinePlayer> players = SpleefHelper.getPlayerRank(2);
            if(players == null){
                return super.onRequest(p, identifier);
            }
            String name = ",";
            for(OfflinePlayer player : players){
                name += ", "+player.getName();
            }
            return name.replace(",, ", "");
        }
        if(identifier.equals("third")){
            List<OfflinePlayer> players = SpleefHelper.getPlayerRank(3);
            if(players == null){
                return super.onRequest(p, identifier);
            }
            String name = ",";
            for(OfflinePlayer player : players){
                name += ", "+player.getName();
            }
            return name.replace(",, ", "");
        }
        if(identifier.equals("fourth")){
            List<OfflinePlayer> players = SpleefHelper.getPlayerRank(4);
            if(players == null){
                return super.onRequest(p, identifier);
            }
            String name = ",";
            for(OfflinePlayer player : players){
                name += ", "+player.getName();
            }
            return name.replace(",, ", "");
        }
        if(identifier.equals("fifth")){
            List<OfflinePlayer> players = SpleefHelper.getPlayerRank(5);
            if(players == null){
                return super.onRequest(p, identifier);
            }
            String name = ",";
            for(OfflinePlayer player : players){
                name += ", "+player.getName();
            }
            return name.replace(",, ", "");
        }
        //WINS
        if(identifier.equals("wins")){
            SpleefPlayerStatistics spleefPlayerStatistics = new SpleefPlayerStatistics(SpleefPlugin.INSTANCE.getSpleefPlugin(), p.getName());
            spleefPlayerStatistics.refreshStatisticFromConfig();
            return spleefPlayerStatistics.getWins()+"";
        }
        //LOSES
        if(identifier.equals("loses")){
            SpleefPlayerStatistics spleefPlayerStatistics = new SpleefPlayerStatistics(SpleefPlugin.INSTANCE.getSpleefPlugin(), p.getName());
            spleefPlayerStatistics.refreshStatisticFromConfig();
            return spleefPlayerStatistics.getLoses()+"";
        }
        return null;
    }
}
