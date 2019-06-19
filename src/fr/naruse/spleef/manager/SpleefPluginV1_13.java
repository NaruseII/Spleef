package fr.naruse.spleef.manager;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.v1_13.api.SpleefAPI;
import fr.naruse.spleef.v1_13.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefDisablingEvent;
import fr.naruse.spleef.v1_13.api.event.cancellable.game.SpleefEnablingEvent;
import fr.naruse.spleef.v1_13.cmd.SpleefCommands;
import fr.naruse.spleef.v1_13.event.Listeners;
import fr.naruse.spleef.v1_13.game.duel.Duels;
import fr.naruse.spleef.v1_13.game.spleef.Spleefs;
import fr.naruse.spleef.v1_13.game.wager.Wagers;
import fr.naruse.spleef.v1_13.util.Logs;
import fr.naruse.spleef.v1_13.util.board.Holograms;
import fr.naruse.spleef.v1_13.util.config.Configurations;
import fr.naruse.spleef.v1_13.util.support.OtherPluginSupport;
import org.bukkit.Bukkit;

public class SpleefPluginV1_13 extends AbstractSpleefPlugin {
    public Spleefs spleefs;
    public Configurations configurations;
    public WorldEditPlugin worldEditPlugin;
    public OtherPluginSupport otherPluginSupport;
    public Wagers wagers;
    public Duels duels;
    public Holograms holograms;
    public SpleefAPI spleefAPI;
    public static SpleefPluginV1_13 INSTANCE;
    public SpleefPluginV1_13(SpleefPlugin spleefPlugin) {
        super(spleefPlugin);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.otherPluginSupport = new OtherPluginSupport();
        this.saveConfig();
        this.configurations = new Configurations(this);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getSpleefPlugin(), new Runnable() {
            @Override
            public void run() {
                Logs logs = new Logs();
                if(new SpleefAPIEventInvoker(new SpleefEnablingEvent.Pre(INSTANCE)).isCancelled()){
                    return;
                }
                holograms = new Holograms(INSTANCE);
                spleefs = new Spleefs(INSTANCE);
                wagers = new Wagers(INSTANCE);
                duels = new Duels(INSTANCE);
                getCommand("spleef").setExecutor(new SpleefCommands(INSTANCE));
                Bukkit.getPluginManager().registerEvents(new Listeners(INSTANCE), INSTANCE.getSpleefPlugin());
                Bukkit.getPluginManager().registerEvents(duels, INSTANCE.getSpleefPlugin());
                new SpleefAPIEventInvoker(new SpleefEnablingEvent.Post(INSTANCE));
                logs.stop();
            }
        });
    }

    @Override
    public void onDisable() {
        if(new SpleefAPIEventInvoker(new SpleefDisablingEvent.Pre(this)).isCancelled()){
            return;
        }
        if(spleefs != null){
            spleefs.onDisable();
        }
        if(wagers != null){
            wagers.disable();
        }
        holograms.removeLeaderBoard();
        new SpleefAPIEventInvoker(new SpleefDisablingEvent.Post(this));
    }

    @Override
    public void onLoad() {
        this.spleefAPI = new SpleefAPI(this);
    }
}
