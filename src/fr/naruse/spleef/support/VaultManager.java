package fr.naruse.spleef.support;

import fr.naruse.spleef.main.SpleefPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {

    private final SpleefPlugin pl;
    private Economy economy = null;
    private double winReward = -1;
    private double looseReward = -1;

    public VaultManager(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
        reload();
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    public void giveWinReward(Player p){
        if(winReward == -1){
            return;
        }
        economy.depositPlayer(p, winReward);
    }

    public void giveLooseReward(Player p){
        if(looseReward == -1){
            return;
        }
        economy.depositPlayer(p, looseReward);
    }

    public void reload() {
        this.winReward = pl.getConfig().getDouble("reward.win");
        this.looseReward = pl.getConfig().getDouble("reward.loose");
    }
}
