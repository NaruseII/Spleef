package fr.naruse.spleef.utils;

import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class SpleefUpdater {

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static boolean needToRestart = false;

    public static void checkNewVersion(SpleefPlugin pl, boolean sendMessageIfNoUpdate) {
        service.submit(() -> {
            try {
                Thread.sleep(1000);
                pl.getLogger().log(Level.INFO, "[Updater]");
                pl.getLogger().log(Level.INFO, "[Updater] Checking for new versions...");
                pl.getLogger().log(Level.INFO, "[Updater]");
                if(needToUpdate(pl)){
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.WARNING, "[Updater] The plugin needs to be updated! https://www.spigotmc.org/resources/spleef.61787/");
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.WARNING, "[Updater] Trying update...");

                    File runningJar = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                    pl.getLogger().log(Level.WARNING, "[Updater] Download URL found: https://github.com/NaruseII/Spleef/blob/master/updater/Spleef.jar?raw=true");
                    downloadFile(pl, new URL("https://github.com/NaruseII/Spleef/blob/master/updater/Spleef.jar?raw=true"), runningJar, true);
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.INFO, "[Updater] Plugin was auto-updated!");
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.INFO, "[Updater] Please reload or restart your server.");
                    if(Bukkit.getPluginManager().getPlugin("PlugMan") != null){
                        if(pl.getHolographicManager() != null &&  pl.getHolographicManager().getHologram() != null){
                            pl.getHolographicManager().getHologram().delete();
                        }
                        pl.getLogger().log(Level.INFO, "[Updater] the Plugin is reloading...");
                        Bukkit.getScheduler().runTaskLater(pl, () -> {
                            try{
                                Method method = Class.forName("com.rylinaux.plugman.util.PluginUtil").getDeclaredMethod("reload", Plugin.class);
                                method.invoke(null, pl);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }, 5);
                    }else{
                        needToRestart = true;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if((p.isOp() || p.hasPermission("spleef.help"))){
                                p.sendMessage(pl.getMessageManager().get("needToRestart"));
                            }
                        }
                    }
                }else{
                    if(sendMessageIfNoUpdate){
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if((p.isOp() || p.hasPermission("spleef.help"))){
                                p.sendMessage(pl.getMessageManager().get("upToDate"));
                            }
                        }
                    }
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.INFO, "[Updater] The plugin is up to date!");
                }
                pl.getLogger().log(Level.INFO, "[Updater]");
            } catch (Exception e) {
                pl.getLogger().log(Level.SEVERE, "Could not update the plugin. This does not change the functioning of the plugin");
                e.printStackTrace();
            }
        });
    }

    private static boolean needToUpdate(SpleefPlugin pl) {
        try{
            String currentVersion = pl.getDescription().getVersion();

            URL url = new URL("https://raw.githubusercontent.com/NaruseII/Spleef/master/src/plugin.yml");
            Scanner scanner = new Scanner(url.openStream());
            String onlineVersion = null;
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                if(line.startsWith("version")){
                    onlineVersion = line.split(": ")[1];
                    break;
                }
            }
            if(onlineVersion == null){
                pl.getLogger().log(Level.SEVERE, "Could not check the online version. This does not change the functioning of the plugin");
                return false;
            }

            pl.getLogger().log(Level.INFO, "[Updater] Local version: "+currentVersion);
            pl.getLogger().log(Level.INFO, "[Updater] Online version: "+onlineVersion);

            if(currentVersion.equals(onlineVersion)){
                return false;
            }else{
                return true;
            }
        }catch (Exception e){
            pl.getLogger().log(Level.SEVERE, "Could not check the online version. This does not change the functioning of the plugin");
        }
        return false;
    }

    private static String getDownloadHost(SpleefPlugin pl){
        try{
            URL url = new URL("https://raw.githubusercontent.com/NaruseII/Spleef/master/updater/updater.txt");
            Scanner scanner = new Scanner(url.openStream());
            return scanner.nextLine();
        }catch (Exception e){
            pl.getLogger().log(Level.SEVERE, "Could not get the download URL. This does not change the functioning of the plugin");
        }
        return null;
    }

    private static boolean downloadFile(SpleefPlugin pl, URL host, File dest, boolean log) {
        try (BufferedInputStream in = new BufferedInputStream(host.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            pl.getLogger().log(Level.INFO, "[Updater] Update ended.");
        } catch (IOException e) {
            pl.getLogger().log(Level.SEVERE, "Could not download the update. This does not change the functioning of the plugin");
            if(log){
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private static final DecimalFormat df = new DecimalFormat("0.####");
    private static String byteToMB(long bytes){
        String result = df.format(bytes*0.000001);
        if(!result.contains(",")){
            result += ",00";
        }
        return result;
    }

    private static long fileSize(URL url){
        try {
            URLConnection connection = url.openConnection();
            int fileLength = connection.getContentLength();
            return fileLength;
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean needToRestart() {
        return needToRestart;
    }
}
