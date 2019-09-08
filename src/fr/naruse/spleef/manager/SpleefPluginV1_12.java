package fr.naruse.spleef.manager;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fr.naruse.common.main.CommonPlugin;
import fr.naruse.spleef.common.placeholder.SpleefPlaceholder;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.v1_12.api.SpleefAPI;
import fr.naruse.spleef.v1_12.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_12.api.event.cancellable.game.SpleefDisablingEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.game.SpleefEnablingEvent;
import fr.naruse.spleef.v1_12.cmd.SpleefCommands;
import fr.naruse.spleef.v1_12.event.Listeners;
import fr.naruse.spleef.v1_12.game.duel.Duels;
import fr.naruse.spleef.v1_12.game.spleef.Spleefs;
import fr.naruse.spleef.v1_12.game.wager.Wagers;
import fr.naruse.spleef.v1_12.util.Logs;
import fr.naruse.spleef.v1_12.util.board.Holograms;
import fr.naruse.spleef.v1_12.util.config.Configurations;
import fr.naruse.spleef.v1_12.util.support.OtherPluginSupport;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SpleefPluginV1_12 extends AbstractSpleefPlugin {
    public Spleefs spleefs;
    public Configurations configurations;
    public WorldEditPlugin worldEditPlugin;
    public OtherPluginSupport otherPluginSupport;
    public Wagers wagers;
    public Duels duels;
    public Holograms holograms;
    public SpleefAPI spleefAPI;
    public static SpleefPluginV1_12 INSTANCE;
    public CommonPlugin commonPlugin;
    private SpleefPlaceholder spleefPlaceholder;
    public SpleefPluginV1_12(SpleefPlugin spleefPlugin) {
        super(spleefPlugin);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.otherPluginSupport = new OtherPluginSupport();
        this.saveConfig();
        this.configurations = new Configurations(this);
        this.spleefPlaceholder = new SpleefPlaceholder(getSpleefPlugin());
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
                commonPlugin = (CommonPlugin) Bukkit.getPluginManager().getPlugin("NaruseResourcesCommon");
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
