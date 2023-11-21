package fr.naruse.api;

import com.rylinaux.plugman.PlugMan;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class NaruseAPIDownloader {

    public static boolean downloadPlugManX(JavaPlugin javaPlugin) {
        String name = "PlugManX";
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if(plugin != null) {
            return false;
        }

        File file = new File(javaPlugin.getDataFolder().getParentFile(), name+".jar");
        if(!downloadFile("https://github.com/NaruseII/SecondThreadAPI/blob/master/out/artifacts/NaruseSpigotAPI/"+name+".jar?raw=true", file)) {
            javaPlugin.getLogger().severe("Could not check online dependencies! " + name);
            return false;
        }

        try {
            Plugin pl = Bukkit.getPluginManager().loadPlugin(file);
            Bukkit.getPluginManager().enablePlugin(pl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void checkConfigAPI(JavaPlugin javaPlugin){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("NaruseAPI");
        if(plugin != null){
            String version = getVersion("https://raw.githubusercontent.com/NaruseII/ConfigAPI/master/src/plugin.yml");
            if(version.equals("error")){
                javaPlugin.getLogger().severe("Could not check online dependecies!");
                return;
            }else if(!version.equalsIgnoreCase(plugin.getDescription().getVersion())){
                PlugMan.getInstance().getPluginUtil().disable(plugin);
                PlugMan.getInstance().getPluginUtil().unload(plugin);
            }else{
                return;
            }
        }

        File file = new File(javaPlugin.getDataFolder().getParentFile(), "NaruseConfigAPI.jar");
        if(file.exists()){
            file.delete();
        }
        if(!downloadFile("https://github.com/NaruseII/ConfigAPI/blob/master/out/artifacts/NaruseAPI/NaruseAPI.jar?raw=true", file)){
            javaPlugin.getLogger().severe("Unable to download ConfigAPI");
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
            return;
        }
        try {
            PlugMan.getInstance().getPluginUtil().load("NaruseConfigAPI");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
        }
    }

    public static void checkSecondThreadAPI(JavaPlugin javaPlugin){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SecondThreadAPI");
        if(plugin != null){
            String version = getVersion("https://raw.githubusercontent.com/NaruseII/SecondThreadAPI/master/src/plugin.yml");
            if(version.equals("error")){
                javaPlugin.getLogger().severe("Could not check online dependecies!");
                return;
            }else if(!version.equalsIgnoreCase(plugin.getDescription().getVersion())){
                PlugMan.getInstance().getPluginUtil().disable(plugin);
                PlugMan.getInstance().getPluginUtil().unload(plugin);
            }else{
                return;
            }
        }

        File file = new File(javaPlugin.getDataFolder().getParentFile(), "NaruseSecondThreadAPI.jar");
        if(file.exists()){
            file.delete();
        }

        File oldfile = new File(javaPlugin.getDataFolder().getParentFile(), "SecondThreadAPI.jar");
        if(oldfile.exists()){
            oldfile.delete();
        }
        oldfile = new File(javaPlugin.getDataFolder().getParentFile(), "NaruseAPI.jar");
        if(oldfile.exists()){
            oldfile.delete();
        }

        if(!downloadFile("https://github.com/NaruseII/SecondThreadAPI/blob/master/out/artifacts/NaruseSpigotAPI/SecondThreadAPI.jar?raw=true", file)){
            javaPlugin.getLogger().severe("Unable to download SecondThreadAPI");
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
            return;
        }

        try {
            PlugMan.getInstance().getPluginUtil().load("NaruseSecondThreadAPI");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
        }
    }

    public static boolean checkDBAPI(JavaPlugin javaPlugin, boolean onlyIfPresent){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("DBAPI");
        if(plugin != null){
            String version = getVersion("https://raw.githubusercontent.com/NaruseII/DBAPI/master/src/plugin.yml");
            if(version.equals("error")){
                javaPlugin.getLogger().severe("Could not check online dependecies!");
                return false;
            }else if(!version.equalsIgnoreCase(plugin.getDescription().getVersion())){
                Bukkit.getPluginManager().disablePlugin(plugin);
            }else{
                return false;
            }
        }else if(onlyIfPresent){
            return false;
        }

        File file = new File(javaPlugin.getDataFolder().getParentFile(), "DBAPI.jar");
        if(file.exists()){
            file.delete();
        }

        if(!downloadFile("https://github.com/NaruseII/DBAPI/blob/master/out/artifacts/DBAPI/DBAPI.jar?raw=true", file)){
            javaPlugin.getLogger().severe("Unable to download DBAPI");
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
            return false;
        }
        try {
            Plugin pl = Bukkit.getPluginManager().loadPlugin(file);
            pl.onLoad();
            Bukkit.getPluginManager().enablePlugin(pl);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(javaPlugin);
            return false;
        }
        return true;
    }

    private static String getVersion(String urlString){
        try{
            URL url = new URL(urlString);
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                if(line.startsWith("version")){
                    return line.split(": ")[1];
                }
            }
        }catch (Exception e){ }
        return "error";
    }

    private static boolean downloadFile(String host, File dest) {
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(host).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(dest);
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}

