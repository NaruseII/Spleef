package fr.naruse.spleef.utils;

import fr.naruse.spleef.main.SpleefPlugin;

import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class SpleefUpdater {

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public static void checkNewVersion(SpleefPlugin pl) {

        service.submit(() -> {
            try {
                Thread.sleep(1000);
                pl.getLogger().log(Level.INFO, "[Updater]");
                pl.getLogger().log(Level.INFO, "[Updater] Checking for new versions...");
                pl.getLogger().log(Level.INFO, "[Updater]");
                if(needToUpdate(pl)){
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.WARNING, "[Updater] The plugin needs to be updated! https://www.spigotmc.org/resources/spleef.61787/");
                }else{
                    pl.getLogger().log(Level.INFO, "[Updater]");
                    pl.getLogger().log(Level.INFO, "[Updater] The plugin is up to date!");
                }
                pl.getLogger().log(Level.INFO, "[Updater]");
            } catch (Exception e) {
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
            e.printStackTrace();
        }
        return false;
    }
}
