package fr.naruse.spleef.v1_13.util.config;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommandsConfiguration {
    private SpleefPluginV1_13 pl;
    private File commandsFile;
    private FileConfiguration commands;
    public CommandsConfiguration(SpleefPluginV1_13 spleefPlugin) {
        this.pl = spleefPlugin;
        createConfig();
    }

    private void createConfig(){
        commandsFile = new File(pl.getDataFolder(), "commands.yml");
        commands = new YamlConfiguration();
        try {
            if(!commandsFile.exists()){
                commandsFile.createNewFile();
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§3[Spleef] §C There is an error with the configuration Messages.yml. You should perform a reload.");
            e.printStackTrace();
        }
        try{
            commands.load(commandsFile);
        }catch(Exception e){
            e.printStackTrace();
        }
        saveConfig();
    }

    public void saveConfig(){
        try {
            commands.save(commandsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig(){
        return this.commands;
    }
}
