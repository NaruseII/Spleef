package fr.naruse.spleef.ranking;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.async.ThreadGlobal;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class HologramPlugin<T> {

    protected final SpleefPlugin pl;
    protected long millis = System.currentTimeMillis()-50000;
    protected Location location;
    protected T hologram;

    private Map<Integer, List<OfflinePlayer>> sortedMap;

    public HologramPlugin(SpleefPlugin pl) {
        this.pl = pl;

        this.reload();

        CollectionManager.INFINITE_SECOND_THREAD_RUNNABLE_SET.add(() -> {
            if(System.currentTimeMillis()-this.millis > 60000 && this.isHologramPlaced() && pl.getSpleefPlayerRegistry() != null){
                this.millis = System.currentTimeMillis();

                this.sortedMap = Maps.newHashMap();

                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {

                    SpleefPlayer playerStatistics = pl.getSpleefPlayerRegistry().getSpleefPlayer(offlinePlayer);
                    if(playerStatistics == null){
                        continue;
                    }

                    int points = playerStatistics.getStatistic(StatisticType.WIN);
                    if(points <= 0){
                        continue;
                    }

                    if (!this.sortedMap.containsKey(points)) {
                        this.sortedMap.put(points, Lists.newArrayList());
                    }
                    this.sortedMap.get(points).add(offlinePlayer);
                }

                List<Integer> list = Lists.newArrayList(this.sortedMap.keySet());
                Collections.sort(list);
                Collections.reverse(list);

                List<String> lines = Lists.newArrayList();

                int count = 1;
                for (Integer aDouble : list) {
                    for (OfflinePlayer offlinePlayer : this.sortedMap.get(aDouble)) {
                        if(count > 10){
                            break;
                        }

                        int loose = 0;
                        SpleefPlayer playerStatistics = pl.getSpleefPlayerRegistry().getSpleefPlayer(offlinePlayer);
                        if(playerStatistics != null){
                            loose = playerStatistics.getStatistic(StatisticType.LOSE);
                        }

                        String line = pl.getMessageManager().get("hologram.format", new String[]{"rank", "name", "wins", "loose"},
                                new String[]{count+"", offlinePlayer.getName(), aDouble+"", loose+""});
                        lines.add(line);
                        count++;
                    }
                    if(count > 10){
                        break;
                    }
                }

                ThreadGlobal.runSync(() -> {

                    if(this.isHologramPlaced()){
                        this.insertLines(lines);
                    }

                });

            }
        });

    }

    protected abstract void insertLines(List<String> lines);

    protected abstract void deleteHologram();

    protected abstract void createHologram();

    protected boolean isHologramPlaced(){
        return this.hologram != null;
    }


    public void reload(){
        if(this.isHologramPlaced()){
            this.deleteHologram();
            this.hologram = null;
        }

        if(!pl.getConfig().getBoolean("holographicRanking") || pl.getDatabaseManager() == null){
            return;
        }

        this.location = Utils.getLocation(pl, "hologram.location");
        if(this.location != null){
            this.createHologram();
            this.millis = 0;
        }else{
            pl.getLogger().warning("Location for hologram is null. Did you removed the world ?");
        }
    }

    public void onDisable(){
        if(this.hologram != null){
            this.deleteHologram();
        }
    }

    public Map<Integer, List<OfflinePlayer>> getSortedMap() {
        return this.sortedMap;
    }
}
