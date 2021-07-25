package fr.naruse.spleef.main;

import fr.naruse.spleef.cmd.SpleefCommands;
import fr.naruse.spleef.config.Configurations;
import fr.naruse.spleef.event.Listeners;
import fr.naruse.spleef.manager.MessageManager;
import fr.naruse.spleef.spleef.Spleefs;
import fr.naruse.spleef.player.SpleefPlayerRegistry;
import fr.naruse.spleef.database.DatabaseSQLManager;
import fr.naruse.spleef.ranking.HolographicManager;
import fr.naruse.spleef.database.DatabaseYAMLManagerDatabase;
import fr.naruse.spleef.database.IDatabaseManager;
import fr.naruse.spleef.support.PlaceHolderManager;
import fr.naruse.spleef.support.VaultManager;
import fr.naruse.spleef.utils.Metrics;
import fr.naruse.spleef.utils.SpleefUpdater;
import fr.naruse.spleef.utils.ThreadGlobal;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class SpleefPlugin extends JavaPlugin {

    private Configurations configurations;
    private MessageManager.StringManager messageManager;
    private Spleefs spleefs;
    private SpleefPlayerRegistry spleefPlayerRegistry;

    private IDatabaseManager databaseManager;
    private VaultManager vaultManager;
    private HolographicManager holographicManager;

    @Override
    public void onEnable() {
        super.onEnable();

        if(!new File(getDataFolder(), "config.yml").exists()){
            saveResource("config.yml", false);
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            registerDependencies();

            this.configurations = new Configurations(this);
            this.messageManager = new MessageManager.StringManager(this);
            this.spleefPlayerRegistry = new SpleefPlayerRegistry(this);

            getCommand("spleef").setExecutor(new SpleefCommands(this));
            getServer().getPluginManager().registerEvents(new Listeners(this), this);

            this.spleefs = new Spleefs(this);

            SpleefUpdater.checkNewVersion(this, false);

            Utils.formatItems(this);

            ThreadGlobal.launch();
        }, 20);

        Utils.addCharts(this, new Metrics(this, 9924));
    }

    private void registerDependencies() {
        boolean yamlStats = getConfig().getBoolean("yamlStatistics");
        if(yamlStats){
            getLogger().log(Level.INFO, "Using YAML statistic system");
            this.databaseManager = new DatabaseYAMLManagerDatabase(this);
        }else if(getServer().getPluginManager().getPlugin("DBAPI") != null){
            getLogger().log(Level.INFO, "DBAPI found");
            this.databaseManager = new DatabaseSQLManager(this);
        }
        if(getServer().getPluginManager().getPlugin("Vault") != null){
            getLogger().log(Level.INFO, "Vault found");
            this.vaultManager = new VaultManager(this);
        }
        if(getServer().getPluginManager().getPlugin("HolographicDisplays") != null){
            getLogger().log(Level.INFO, "HolographicDisplays found");
            this.holographicManager = new HolographicManager(this);
        }
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            getLogger().log(Level.INFO, "PlaceholderAPI found");
            new PlaceHolderManager(this).register();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if(spleefs != null){
            for (int i = 0; i < spleefs.getSpleefs().size(); i++) {
                spleefs.getSpleefs().get(i).disable();
            }
        }
        if(holographicManager != null){
            holographicManager.disable();
        }
        ThreadGlobal.shutdown();
    }

    public Spleefs getSpleefs() {
        return spleefs;
    }

    public Configurations getConfigurations() {
        return configurations;
    }

    public MessageManager.StringManager getMessageManager() {
        return messageManager;
    }

    public SpleefPlayerRegistry getSpleefPlayerRegistry() {
        return spleefPlayerRegistry;
    }

    public IDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public HolographicManager getHolographicManager() {
        return holographicManager;
    }
}
