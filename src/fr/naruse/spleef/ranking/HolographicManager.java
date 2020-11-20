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
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HolographicManager extends BukkitRunnable {

    private final SpleefPlugin pl;
    private Hologram hologram;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public HolographicManager(SpleefPlugin pl) {
        this.pl = pl;
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> reloadLines());

        this.runTaskTimer(pl, 20*60, 20*60);
    }

    @Override
    public void run() {
        reloadLines();
    }

    public void reloadHologram() {
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }

        if(!pl.getConfig().getBoolean("holographicRanking") || pl.getSqlManager() == null){
            return;
        }

        this.hologram = HologramsAPI.createHologram(pl, Utils.getLocation(pl, "hologram.location"));
    }

    public void reloadLines() {
        if (this.hologram == null) {
            this.reloadHologram();

            if (this.hologram == null) {
                return;
            }
        }

        executorService.submit(() -> {
            Map<String, Integer> map = Maps.newHashMap();
            Map<String, Integer> mapLoose = Maps.newHashMap();
            Map<Integer, List<String>> mapList = Maps.newHashMap();
            List<Integer> list = Lists.newArrayList();

            for (Map.Entry<OfflinePlayer, SpleefPlayer> entry : pl.getSpleefPlayerRegistry().getSpleefPlayerHashMap()) {
                SpleefPlayer spleefPlayer = entry.getValue();
                int win = spleefPlayer.getStatistic(StatisticType.WIN);

                map.put(spleefPlayer.getUUID(), win);
                mapLoose.put(spleefPlayer.getUUID(), spleefPlayer.getStatistic(StatisticType.LOSE));
                if(!mapList.containsKey(win)){
                    mapList.put(win, Lists.newArrayList());
                }

                if(!list.contains(win)){
                    list.add(win);
                }

                mapList.get(win).add(spleefPlayer.getUUID());
            }

            Collections.sort(list);
            Collections.reverse(list);

            Bukkit.getScheduler().runTask(pl, () -> {

                this.hologram.clearLines();

                int lineCount = 0;

                List<String> alreadyFound = Lists.newArrayList();

                this.hologram.appendTextLine(pl.getMessageManager().get("hologram.title"));

                for (Integer integer : list) {
                    if(!mapList.containsKey(integer)){
                        continue;
                    }
                    List<String> players = mapList.get(integer);
                    for (String uuid : players) {
                        if (!map.containsKey(uuid)) {
                            continue;
                        }
                        int win = map.get(uuid);
                        if (lineCount >= 10) {
                            return;
                        }

                        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                        if (alreadyFound.contains(player.getName())) {
                            continue;
                        }

                        alreadyFound.add(player.getName());

                        String line = pl.getMessageManager().get("hologram.format", new String[]{"rank", "name", "wins", "loose"}, new String[]{(lineCount + 1)+"", player.getName(), win+"", mapList.get(uuid)+""});
                        this.hologram.appendTextLine(line);
                        lineCount++;
                    }
                }
            });
        });
    }
}
