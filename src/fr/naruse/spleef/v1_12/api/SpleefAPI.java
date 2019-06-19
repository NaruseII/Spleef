package fr.naruse.spleef.v1_12.api;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.List;

public class SpleefAPI {
    private SpleefPluginV1_12 pl;
    private List<Listener> spleefListeners = Lists.newArrayList();
    public SpleefAPI(SpleefPluginV1_12 pl) {
        this.pl = pl;
    }

    protected void sendMessage(String msg){
        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §c§l[§3EventAPI§c§l] §2"+msg);
    }

    public void registerEvents(Listener spleefListener){
        spleefListeners.add(spleefListener);
    }

    protected List<Listener> getSpleefListeners(){
        return spleefListeners;
    }
}
