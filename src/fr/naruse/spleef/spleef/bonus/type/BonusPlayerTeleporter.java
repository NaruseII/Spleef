package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public class BonusPlayerTeleporter extends BonusColored {
    public BonusPlayerTeleporter(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§d§lTeleport Sheep", 6, 8);
    }

    @Override
    protected void onTick() {
        super.onTick();
        Optional<? extends Player> optional = getNearbyPlayers(sheep.getLocation(), 4, 5, 4).filter(entity -> entity != p).findFirst();
        if(!optional.isPresent()){
            return;
        }
        Player player = optional.get();

        timer = 0;
        if(p.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
            runSync(() -> {
                Location targetLoc = player.getLocation();
                player.teleport(p.getLocation());
                p.teleport(targetLoc);
            });
        }
    }

    @Override
    protected void onAction() {

    }
}
