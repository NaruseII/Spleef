package fr.naruse.spleef.v1_12.util;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import org.bukkit.configuration.file.FileConfiguration;

public class SpleefPlayerStatistics {
    private SpleefPluginV1_12 pl;
    private long wins;
    private long loses;
    private long games;
    private FileConfiguration statistic;
    private String name;
    public SpleefPlayerStatistics(SpleefPluginV1_12 pl, String name){
        this.pl = pl;
        this.statistic = pl.configurations.getStatistics().getConfig();
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
        pl.configurations.getStatistics().saveConfig();
    }

    public void saveStatistics() {
        statistic.set(name + ".wins", wins);
        statistic.set(name + ".loses", loses);
        statistic.set(name + ".games", games);
        pl.configurations.getStatistics().saveConfig();
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
