package fr.naruse.spleef.v1_13.util;

import org.bukkit.Bukkit;

public class Logs {
    private long millis;
    public Logs() {
        this.millis = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §c§l[§3Logs§c§l] §aEnabling...");
    }

    public void stop(){
        double time = System.currentTimeMillis()-millis;
        time /= 1000;
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §c§l[§3Logs§c§l] §aEnabling done! It took §2"+time+"§a seconds.");
    }

    private String getTimer(long time){
        long mins = time/60;
        long secondes = time-(mins*60);
        if(secondes < 10){
            return mins+":0"+secondes;
        }
        return mins+":"+secondes;
    }

}
