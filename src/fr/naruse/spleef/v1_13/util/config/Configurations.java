package fr.naruse.spleef.v1_13.util.config;

import fr.naruse.spleef.manager.SpleefPluginV1_13;

public class Configurations {
    private MessagesConfiguration messages;
    private CommandsConfiguration commands;
    private StatisticsConfiguration statistics;
    public Configurations(SpleefPluginV1_13 pl){
        this.messages = new MessagesConfiguration(pl);
        this.commands = new CommandsConfiguration(pl);
        this.statistics = new StatisticsConfiguration(pl);
        setDefault(pl);
    }

    private void setDefault(SpleefPluginV1_13 pl) {
        if(pl.getConfig().getString("lang") == null){
            pl.getConfig().set("lang", "english");
        }else{
            pl.getConfig().set("lang", pl.getConfig().getString("lang"));
        }
        if(pl.getConfig().getString("times.wait") == null){
            pl.getConfig().set("times.wait", 10);
        }else{
            pl.getConfig().set("times.wait", pl.getConfig().getInt("times.wait"));
        }
        if(pl.getConfig().getString("allow.snowBalls") == null){
            pl.getConfig().set("allow.snowBalls", false);
        }else{
            pl.getConfig().set("allow.snowBalls", pl.getConfig().getBoolean("allow.snowBalls"));
        }
        if(pl.getConfig().getString("rewards.win") == null){
            pl.getConfig().set("rewards.win", 0);
        }else{
            pl.getConfig().set("rewards.win", pl.getConfig().getInt("rewards.win"));
        }
        if(pl.getConfig().getString("rewards.lose") == null){
            pl.getConfig().set("rewards.lose", 0);
        }else{
            pl.getConfig().set("rewards.lose", pl.getConfig().getInt("rewards.lose"));
        }
        if(pl.getConfig().getString("gameMode.team.glowing") == null){
            pl.getConfig().set("gameMode.team.glowing", false);
        }else{
            pl.getConfig().set("gameMode.team.glowing", pl.getConfig().getBoolean("gameMode.team.glowing"));
        }
        if(pl.getConfig().getString("rewards.command") == null){
            pl.getConfig().set("rewards.command", "null");
        }else{
            pl.getConfig().set("rewards.command", pl.getConfig().getString("rewards.command"));
        }
        if(pl.getConfig().getString("gameMode.melt.beforeMelt") == null){
            pl.getConfig().set("gameMode.melt.beforeMelt", 30);
        }else{
            pl.getConfig().set("gameMode.melt.beforeMelt", pl.getConfig().getInt("gameMode.melt.beforeMelt"));
        }
        if(pl.getConfig().getString("gameMode.melt.betweenMelt") == null){
            pl.getConfig().set("gameMode.melt.betweenMelt", 1);
        }else{
            pl.getConfig().set("gameMode.melt.betweenMelt", pl.getConfig().getInt("gameMode.melt.betweenMelt"));
        }
        if(pl.getConfig().getString("allow.lightning") == null){
            pl.getConfig().set("allow.lightning", 1);
        }else{
            pl.getConfig().set("allow.lightning", pl.getConfig().getBoolean("allow.lightning"));
        }
        pl.getConfig().set("allow.showTime", pl.getConfig().getBoolean("allow.showTime"));
        pl.getConfig().set("allow.magmaCream", pl.getConfig().getBoolean("allow.magmaCream"));
        pl.getConfig().set("commands.start", pl.getConfig().getString("commands.start"));
        pl.getConfig().set("commands.end", pl.getConfig().getString("commands.end"));
        pl.saveConfig();
    }

    public CommandsConfiguration getCommands() {
        return commands;
    }

    public MessagesConfiguration getMessages() {
        return messages;
    }

    public StatisticsConfiguration getStatistics() {
        return statistics;
    }
}
