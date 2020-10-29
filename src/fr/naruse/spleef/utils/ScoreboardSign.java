package fr.naruse.spleef.utils;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardSign {
    private Scoreboard sb;
    private Objective obj;
    public ScoreboardSign(){
        this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
        this.obj = sb.registerNewObjective("spleef", "dummy");
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setLine(int line, String name){
        obj.getScore(name).setScore(line);
    }

    public void clearLines(){
        for(String line : sb.getEntries()){
            sb.resetScores(line);
        }
    }

    public Scoreboard getScoreboard() {
        return sb;
    }

    public Objective getObjective() {
        return obj;
    }
}
