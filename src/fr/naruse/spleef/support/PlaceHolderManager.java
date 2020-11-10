package fr.naruse.spleef.support;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.type.Spleef;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceHolderManager extends PlaceholderExpansion {

    private SpleefPlugin pl;

    public PlaceHolderManager(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @Override
    public String getIdentifier() {
        return "spleef";
    }

    @Override
    public String getAuthor() {
        return "Naruse";
    }

    @Override
    public String getVersion() {
        return pl.getDescription().getVersion();
    }


    @Override
    public String onPlaceholderRequest(Player p, String params) {
        return super.onRequest(p, params);
    }

    @Override
    public String onRequest(OfflinePlayer p, String params) {
        if (!params.contains("_")) {
            return "Not Found";
        }
        String args[] = params.split("_");
        Spleef spleef = getSpleefByName(args[0]);
        if(spleef == null){
            SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
            if(spleefPlayer == null){
                return "Spleef Not Found";
            }
            if(args[0].equalsIgnoreCase("win")){
                return spleefPlayer.getStatistic(StatisticType.WIN)+"";
            }else if(args[0].equalsIgnoreCase("loose")){
                return spleefPlayer.getStatistic(StatisticType.LOSE)+"";
            }
            return "Spleef Not Found";
        }

        if(args[1].equalsIgnoreCase("isOpened")){
            return spleef.isOpened() ? pl.getMessageManager().get("papi.opened") : pl.getMessageManager().get("papi.closed");
        }
        else if(args[1].equalsIgnoreCase("inGame")){
            return spleef.getCurrentStatus() == GameStatus.GAME ? pl.getMessageManager().get("papi.inGame") : pl.getMessageManager().get("papi.notInGame");
        }
        else if(args[1].equalsIgnoreCase("size")){
            return spleef.getPlayerInGame().size()+"";
        }
        else if(args[1].equalsIgnoreCase("min")){
            return spleef.getMin()+"";
        }
        else if(args[1].equalsIgnoreCase("max")){
            return spleef.getMax()+"";
        }

        return "Argument Not Found";
    }

    private Spleef getSpleefByName(String name){
        for (int i = 0; i < pl.getSpleefs().getSpleefs().size(); i++) {
            Spleef spleef = pl.getSpleefs().getSpleefs().get(i);
            if(spleef.getName().equals(name)){
                return spleef;
            }
        }
        return null;
    }
}
