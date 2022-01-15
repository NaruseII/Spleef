package fr.naruse.spleef.ranking;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HolographicManager extends BukkitRunnable {

    private final SpleefPlugin pl;
    private Hologram hologram;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Map<Integer, List<String>> mapSortedList = Maps.newHashMap();
    private Map<String, Integer> winMap = Maps.newHashMap();

    public HolographicManager(SpleefPlugin pl) {
        this.pl = pl;
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> reloadLines());

        this.runTaskTimer(pl, 20, 20*60);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            Map<String, Integer> mapLoose = Maps.newHashMap();
            mapSortedList = Maps.newHashMap();
            List<Integer> list = Lists.newArrayList();

            for (Map.Entry<OfflinePlayer, SpleefPlayer> entry : pl.getSpleefPlayerRegistry().getSpleefPlayerHashMap()) {
                SpleefPlayer spleefPlayer = entry.getValue();
                int win = spleefPlayer.getStatistic(StatisticType.WIN);

                winMap.put(spleefPlayer.getUUID(), win);
                mapLoose.put(spleefPlayer.getUUID(), spleefPlayer.getStatistic(StatisticType.LOSE));
                if(!mapSortedList.containsKey(win)){
                    mapSortedList.put(win, Lists.newArrayList());
                }

                if(!list.contains(win)){
                    list.add(win);
                }

                mapSortedList.get(win).add(spleefPlayer.getUUID());
            }

            Collections.sort(list);
            Collections.reverse(list);

            Bukkit.getScheduler().runTask(pl, () -> {

                if(!reloadHologram()){
                    return;
                }

                this.hologram.clearLines();

                int lineCount = 0;

                List<String> alreadyFound = Lists.newArrayList();

                this.hologram.appendTextLine(pl.getMessageManager().get("hologram.title"));

                for (Integer integer : list) {
                    if(!mapSortedList.containsKey(integer)){
                        continue;
                    }
                    List<String> players = mapSortedList.get(integer);
                    for (String uuid : players) {
                        if (!winMap.containsKey(uuid)) {
                            continue;
                        }
                        int win = winMap.get(uuid);
                        if (lineCount >= 10) {
                            return;
                        }

                        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                        if (alreadyFound.contains(player.getName())) {
                            continue;
                        }

                        alreadyFound.add(player.getName());

                        String line = pl.getMessageManager().get("hologram.format", new String[]{"rank", "name", "wins", "loose"}, new String[]{(lineCount + 1)+"", player.getName(), win+"", mapSortedList.get(uuid)+""});
                        this.hologram.appendTextLine(line);
                        lineCount++;
                    }
                }
            });
        });
    }

    public boolean reloadHologram() {
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }

        if(!pl.getConfig().getBoolean("holographicRanking") || pl.getDatabaseManager() == null){
            return false;
        }

        Location location = Utils.getLocation(pl, "hologram.location");
        if(location == null){
            pl.getLogger().warning("Location for hologram is null. Did you removed the world ?");
            return false;
        }

        this.hologram = HologramsAPI.createHologram(pl, location);
        return true;
    }

    public void reloadLines() {
        if (this.hologram == null) {
            this.reloadHologram();

            if (this.hologram == null) {
                return;
            }else{
                run();
            }
        }
    }

    public void disable() {
        if (this.hologram != null) {
            this.hologram.delete();
        }
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Map<Integer, List<String>> getMapSortedList() {
        return mapSortedList;
    }

    public Map<String, Integer> getWinMap() {
        return winMap;
    }
}
