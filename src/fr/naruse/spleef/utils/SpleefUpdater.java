package fr.naruse.spleef.utils;

import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class SpleefUpdater {

    private static boolean updateAvailable = false;
    private static String currentVersion, onlineVersion;

    public static void checkNewVersion(SpleefPlugin pl, boolean sendMessageIfNoUpdate) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        service.submit(() -> {
            try {
                Thread.sleep(1000);
                pl.getLogger().log(Level.INFO, "[Updater]");
                pl.getLogger().log(Level.INFO, "[Updater] Checking for new versions...");
                pl.getLogger().log(Level.INFO, "[Updater]");
                if(needToUpdate(pl)){
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.WARNING, "[Updater] The plugin needs to be updated! https://www.spigotmc.org/resources/spleef.107351/");
                    pl.getLogger().log(Level.INFO, "[Updater]");

                    updateAvailable = true;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if((p.isOp() || p.hasPermission("spleef.help"))){
                            sendMessage(pl, p);
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
                    pl.getLogger().log(Level.INFO, "[Updater]");
                }
            } catch (Exception e) {
                pl.getLogger().log(Level.SEVERE, "Could not update the plugin. This does not change the functioning of the plugin");
                e.printStackTrace();
            }
        });
        service.shutdown();
    }

    private static boolean needToUpdate(SpleefPlugin pl) {
        try{
            currentVersion = pl.getDescription().getVersion();

            URL url = new URL("https://raw.githubusercontent.com/NaruseII/Spleef/master/src/plugin.yml");
            Scanner scanner = new Scanner(url.openStream());
            onlineVersion = null;
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

    public static void sendMessage(SpleefPlugin pl, Player p){
        p.sendMessage(pl.getMessageManager().get("needHasAnUpdate", new String[]{"currentVersion", "newVersion", "url"}, new String[]{currentVersion, onlineVersion, "https://www.spigotmc.org/resources/spleef.107351/"}));
    }

    public static boolean updateAvailable() {
        return updateAvailable;
    }
}
