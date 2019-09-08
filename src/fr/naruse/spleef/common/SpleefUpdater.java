package fr.naruse.spleef.common;

import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class SpleefUpdater implements Listener {
    private SpleefPlugin pl;
    private boolean needToUpdate = false;
    public SpleefUpdater(SpleefPlugin pl) {
        this.pl = pl;
        String host = "https://huntiescraft.net/app/webroot/plugins/Spleef.jar";
        File spleefFile = new File(pl.getDataFolder().getParent(), "Spleef.jar");
        if(!spleefFile.exists()){
            Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §4Spleef can't be updated if it is renamed !");
            return;
        }
        if(spleefFile.length() == Utils.fileSize(host)){
            Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §5Spleef up to date !");
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, pl);

        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l]");
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §cHey ! §5New version of Spleef is available !");
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §5Please download it ! §ehttps://www.spigotmc.org/resources/spleef.61787/");
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l]");
        needToUpdate = true;
    }

    @EventHandler
    public void join(PlayerJoinEvent e){
        if(needToUpdate){
            Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
                @Override
                public void run() {
                    e.getPlayer().sendMessage("§c§l[§3Spleef§c§l] §cSpleef plugin needs to be updated !");
                }
            }, 20);
        }
    }
}
