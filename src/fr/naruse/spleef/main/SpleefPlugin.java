package fr.naruse.spleef.main;

import fr.naruse.spleef.common.Utils;
import fr.naruse.spleef.manager.AbstractSpleefPlugin;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import org.bukkit.plugin.java.JavaPlugin;

public class SpleefPlugin extends JavaPlugin {
    public static SpleefPlugin INSTANCE;
    private AbstractSpleefPlugin spleefPlugin;
    @Override
    public void onEnable(){
        super.onEnable();
        this.INSTANCE = this;
        if(spleefPlugin == null){
            double version = Utils.getBukkitVersion();
            if(version >= 1.13) {
                this.spleefPlugin = new SpleefPluginV1_13(this);
            }else {
                this.spleefPlugin = new SpleefPluginV1_12(this);
            }
        }
        this.spleefPlugin.onEnable();
    }

    @Override
    public void onLoad(){
        double version = Utils.getBukkitVersion();
        if(version >= 1.13) {
            this.spleefPlugin = new SpleefPluginV1_13(this);
        }else {
            this.spleefPlugin = new SpleefPluginV1_12(this);
        }
        this.spleefPlugin.onLoad();
    }

    @Override
    public void onDisable(){
        super.onDisable();
        this.spleefPlugin.onDisable();
    }

    public AbstractSpleefPlugin getSpleefPlugin() {
        return spleefPlugin;
    }
}
