package fr.naruse.spleef.v1_13.util.support;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import org.bukkit.Bukkit;

public class HolographicDisplaysPlugin {
    private HolographicDisplays holographicDisplays;
    private boolean isPresent = false;
    public HolographicDisplaysPlugin(){
        this.holographicDisplays = (HolographicDisplays) Bukkit.getPluginManager().getPlugin("HolographicDisplays");
        this.isPresent = holographicDisplays != null;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public HolographicDisplays getHolographicDisplays() {
        return holographicDisplays;
    }
}
