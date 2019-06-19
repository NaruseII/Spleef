package fr.naruse.spleef.v1_13.util.board;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefScoreboardUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardSign {
    private Scoreboard sb;
    private Objective obj;
    private Team blueTeam, redTeam;
    public ScoreboardSign(){
        this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
        this.obj = sb.registerNewObjective("dac", "dummy");
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        redTeam = sb.registerNewTeam("red");
        if(!Bukkit.getVersion().contains("1.8")) {
            redTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        redTeam.setPrefix("§c");
        blueTeam = sb.registerNewTeam("blue");
        if(!Bukkit.getVersion().contains("1.8")) {
            blueTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        blueTeam.setPrefix("§3");
    }

    public void setLine(int line, String msg){
        SpleefScoreboardUpdateEvent ssue = new SpleefScoreboardUpdateEvent(SpleefPluginV1_13.INSTANCE, msg, line, this);
        if(new SpleefAPIEventInvoker(ssue).isCancelled()){
            return;
        }
        line = ssue.getScore();
        msg = ssue.getLine();
        obj.getScore(msg).setScore(line);
    }

    public void clearLines(){
        for(String line : sb.getEntries()){
            sb.resetScores(line);
        }
    }

    public String getTimer(int time){
        int mins = time/60;
        int secondes = time-(mins*60);
        if(secondes < 10){
            return mins+":0"+secondes;
        }
        return mins+":"+secondes;
    }

    public Objective getObjective() {
        return obj;
    }

    public Scoreboard getScoreboard() {
        return sb;
    }

    public Team getBlueTeam() {
        return blueTeam;
    }

    public Team getRedTeam() {
        return redTeam;
    }
}
