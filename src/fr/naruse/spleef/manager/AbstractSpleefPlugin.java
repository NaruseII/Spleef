package fr.naruse.spleef.manager;

import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.InputStream;

public abstract class AbstractSpleefPlugin {
    private SpleefPlugin spleefPlugin;
    public AbstractSpleefPlugin(SpleefPlugin spleefPlugin){
        this.spleefPlugin = spleefPlugin;
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §6AbstractVersion : Using Spigot "+Bukkit.getBukkitVersion()+".");
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public abstract void onLoad();

    public FileConfiguration getConfig(){
        return this.spleefPlugin.getConfig();
    }

    public void saveConfig(){
        this.spleefPlugin.saveConfig();
    }

    public PluginCommand getCommand(String name){
        return this.spleefPlugin.getCommand(name);
    }

    public File getDataFolder(){
        return this.spleefPlugin.getDataFolder();
    }

    public InputStream getResource(String name){
        return this.spleefPlugin.getResource(name);
    }

    public SpleefPlugin getSpleefPlugin() {
        return spleefPlugin;
    }
}
