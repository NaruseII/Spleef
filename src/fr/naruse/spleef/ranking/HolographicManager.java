package fr.naruse.spleef.ranking;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class HolographicManager extends BukkitRunnable {

    private final SpleefPlugin pl;
    private Hologram hologram;

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

        this.hologram.clearLines();

        int lineCount = 0;

        List<String> alreadyFound = Lists.newArrayList();

        this.hologram.appendTextLine(pl.getMessageManager().get("hologram.title"));

        for (Map.Entry<OfflinePlayer, SpleefPlayer> entry : pl.getSpleefPlayerRegistry().getSpleefPlayerHashMap()) {
            SpleefPlayer spleefPlayer = entry.getValue();
            if (lineCount >= 10) {
                return;
            }

            OfflinePlayer player = entry.getKey();

            if (alreadyFound.contains(player.getName())) {
                continue;
            }

            alreadyFound.add(player.getName());

            String line = pl.getMessageManager().get("hologram.format", new String[]{"rank", "name", "wins", "loose"}, new String[]{(lineCount + 1)+"", player.getName(), spleefPlayer.getStatistic(StatisticType.WIN)+"", spleefPlayer.getStatistic(StatisticType.LOSE)+""});
            this.hologram.appendTextLine(line);
            lineCount++;
        }
    }
}
