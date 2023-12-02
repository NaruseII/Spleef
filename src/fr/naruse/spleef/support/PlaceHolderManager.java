package fr.naruse.spleef.support;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.ranking.HologramPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.type.Spleef;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        String args[] = params.split("_");

        if(params.startsWith("top")){
            HologramPlugin holographicManager = this.pl.getHolographicManager();

            int place = 1;
            try {
                place = Integer.valueOf(args[1]);
                if(place < 1){
                    place = 1;
                }
            } catch (Exception e) {
            }

            if (holographicManager == null) {
                return "HolographicDisplays Not Found";
            }

            List<Integer> sortedWins = Lists.newArrayList(holographicManager.getSortedMap().keySet());
            Collections.reverse(sortedWins);
            if (sortedWins.size() < place) {
                return "No Data";
            }

            if(params.endsWith("names")) {
                List<String> doubled = Lists.newArrayList();
                ((List<OfflinePlayer>) holographicManager.getSortedMap().get(sortedWins.get(place-1)))
                        .stream()
                        .map(player -> player.getName())
                        .forEach(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                if(doubled.contains(s)){
                                    return;
                                }
                                doubled.add(s.trim());
                            }
                        });
                return doubled.stream().collect(Collectors.joining(", "));
            }

            if(params.endsWith("wins")) {
                OfflinePlayer offlinePlayer = ((List<OfflinePlayer>) holographicManager.getSortedMap().get(sortedWins.get(place-1))).stream().findFirst().get();
                if(offlinePlayer != null){
                    SpleefPlayer player = pl.getSpleefPlayerRegistry().getSpleefPlayer(offlinePlayer);
                    if(player != null){
                        return player.getStatistic(StatisticType.WIN)+"";
                    }
                }
                return "";
            }

            return "Argument Not Found";
        }

        Spleef spleef = getSpleefByName(args[0]);
        if(spleef == null){
            SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
            if(spleefPlayer == null){//%spleef_win_s%
                return "Spleef Not Found";
            }
            if(args[0].equalsIgnoreCase("win")){
                return spleefPlayer.getStatistic(StatisticType.WIN)+"";
            }else if(args[0].equalsIgnoreCase("loose")){
                return spleefPlayer.getStatistic(StatisticType.LOSE)+"";
            }else{
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(player);
                if(spleefPlayer == null){
                    return "Spleef Not Found";
                }
                if(args[1].equalsIgnoreCase("win")){
                    return spleefPlayer.getStatistic(StatisticType.WIN)+"";
                }else if(args[2].equalsIgnoreCase("loose")){
                    return spleefPlayer.getStatistic(StatisticType.LOSE)+"";
                }
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
        else if(args.length >= 2 && args[1].equalsIgnoreCase("timer") && args[2].equalsIgnoreCase("left")){
            return spleef.getCurrentTimer()+"";
        }
        else if(args.length >= 3 && args[1].equalsIgnoreCase("player") && args[3].equalsIgnoreCase("name")){
            int place = 1;
            try {
                place = Integer.valueOf(args[2]);
                if(place < 1){
                    place = 1;
                }
            } catch (Exception e) {
            }
            if(spleef.getPlayerInGame().isEmpty() || place < 0 || spleef.getPlayerInGame().size() < place){
                return "No Player";
            }
            return spleef.getPlayerInGame().get(place-1).getName();
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
