package fr.naruse.spleef.v1_12.util;

import fr.naruse.spleef.manager.AbstractSpleefPlugin;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import org.bukkit.configuration.file.FileConfiguration;

public class SpleefPlayerStatistics {
    private SpleefPluginV1_12 spleefPluginV1_12;
    private SpleefPluginV1_13 spleefPluginV1_13;
    private long wins;
    private long loses;
    private long games;
    private FileConfiguration statistic;
    private String name;
    public SpleefPlayerStatistics(AbstractSpleefPlugin pl, String name){
        if(pl instanceof SpleefPluginV1_12){
            this.spleefPluginV1_12 = (SpleefPluginV1_12) pl;
            this.statistic = spleefPluginV1_12.configurations.getStatistics().getConfig();
        }
        if(pl instanceof SpleefPluginV1_13){
            this.spleefPluginV1_13 = (SpleefPluginV1_13) pl;
            this.statistic = spleefPluginV1_13.configurations.getStatistics().getConfig();
        }
        this.name = name;
        refreshStatisticFromConfig();
    }

    public void refreshStatisticFromConfig(){
        if(statistic.getString(name+".wins") == null){
            createStatistics();
        }
        this.wins = statistic.getLong(name+".wins");
        this.loses = statistic.getLong(name+".loses");
        this.games = statistic.getLong(name+".games");
    }

    private void createStatistics(){
        statistic.set(name+".wins", 0);
        statistic.set(name+".loses", 0);
        statistic.set(name+".games", 0);
        if(spleefPluginV1_12 != null){
            spleefPluginV1_12.configurations.getStatistics().saveConfig();
        }else{
            spleefPluginV1_13.configurations.getStatistics().saveConfig();
        }
    }

    public void saveStatistics() {
        statistic.set(name + ".wins", wins);
        statistic.set(name + ".loses", loses);
        statistic.set(name + ".games", games);
        if(spleefPluginV1_12 != null){
            spleefPluginV1_12.configurations.getStatistics().saveConfig();
        }else{
            spleefPluginV1_13.configurations.getStatistics().saveConfig();
        }
    }

    public long getWins() {
        return wins;
    }

    public long getLoses() {
        return loses;
    }

    public void addGames(int games){
        this.games += games;
    }

    public void addWins(int wins){
        this.wins += wins;
    }

    public void addLoses(int loses){
        this.loses += loses;
    }
}
