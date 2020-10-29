package fr.naruse.spleef.main;

import fr.naruse.spleef.cmd.SpleefCommands;
import fr.naruse.spleef.config.Configurations;
import fr.naruse.spleef.event.Listeners;
import fr.naruse.spleef.manager.MessageManager;
import fr.naruse.spleef.spleef.Spleefs;
import fr.naruse.spleef.player.SpleefPlayerRegistry;
import fr.naruse.spleef.sql.SQLManager;
import fr.naruse.spleef.ranking.HolographicManager;
import fr.naruse.spleef.support.VaultManager;
import fr.naruse.spleef.utils.SpleefUpdater;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class SpleefPlugin extends JavaPlugin {

    private Configurations configurations;
    private MessageManager.StringManager messageManager;
    private Spleefs spleefs;
    private SpleefPlayerRegistry spleefPlayerRegistry;

    private SQLManager sqlManager;
    private VaultManager vaultManager;
    private HolographicManager holographicManager;

    @Override
    public void onEnable() {
        super.onEnable();
        if(!new File(getDataFolder(), "config.yml").exists()){
            saveResource("config.yml", false);
        }

        registerDependencies();

        this.configurations = new Configurations(this);
        this.messageManager = new MessageManager.StringManager(this);
        this.spleefPlayerRegistry = new SpleefPlayerRegistry(this);

        getCommand("spleef").setExecutor(new SpleefCommands(this));
        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        this.spleefs = new Spleefs(this);

        SpleefUpdater.checkNewVersion(this);
    }

    private void registerDependencies() {
        if(getServer().getPluginManager().getPlugin("DBAPI") != null){
            getLogger().log(Level.INFO, "DBAPI found");
            this.sqlManager = new SQLManager(this);
        }
        if(getServer().getPluginManager().getPlugin("Vault") != null){
            getLogger().log(Level.INFO, "Vault found");
            this.vaultManager = new VaultManager(this);
        }
        if(getServer().getPluginManager().getPlugin("HolographicDisplays") != null){
            getLogger().log(Level.INFO, "HolographicDisplays found");
            this.holographicManager = new HolographicManager(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        for (int i = 0; i < spleefs.getSpleefs().size(); i++) {
            spleefs.getSpleefs().get(i).disable();
        }
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

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public HolographicManager getHolographicManager() {
        return holographicManager;
    }
}
