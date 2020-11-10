package fr.naruse.spleef.spleef.type;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class Splegg extends Spleef {
    public Splegg(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby) {
        super(pl, id, name, isOpened, max, min, arena, spawn, lobby);
    }

    @Override
    public void start() {
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));
        for (Player player : playerInGame) {
            player.teleport(arena);

            player.teleport(getRandomLocationFrom(arena));
            player.getInventory().addItem(Utils.SNOWBALL);
        }
    }

    @EventHandler
    public void shoot(ProjectileHitEvent e){
        if(e.getEntity() instanceof Snowball && e.getEntity().getShooter() instanceof Player){
            Player p = (Player) e.getEntity().getShooter();
            if(!hasPlayer(p) || currentStatus == GameStatus.WAIT){
                return;
            }
            p.getInventory().addItem(Utils.SNOWBALL);
            if(e.getHitBlock() != null && e.getHitBlock().getType() == Material.SNOW_BLOCK){
                Block block = e.getHitBlock();
                blocks.add(block);
                block.setType(Material.AIR);
            }
        }
    }

    @Override
    protected String getSignLine(String path) {
        return pl.getMessageManager().get("sign.splegg."+path, new String[]{"name", "size", "max", "min", "missing"},
                new String[]{getFullName(), playerInGame.size()+"", max+"", min+"", (min-playerInGame.size())+""});
    }
}
