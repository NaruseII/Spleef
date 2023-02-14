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
    public Splegg(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby, boolean sheepBonusEnabled) {
        super(pl, id, name, isOpened, max, min, arena, spawn, lobby, sheepBonusEnabled);
    }

    @Override
    public void start() {
        scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboard.scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), ""}));
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));
        for (Player player : playerInGame) {
            if(pl.getConfig().getBoolean("randomSpawn")){
                player.teleport(getRandomLocationFrom(arena.clone()));
            }else{
                player.teleport(arena.clone());
                Vector vector = new Vector(Utils.RANDOM.nextDouble(), Utils.RANDOM.nextDouble(), Utils.RANDOM.nextDouble());
                if(Utils.RANDOM.nextBoolean()){
                    vector.setX(-vector.getX());
                }
                if(Utils.RANDOM.nextBoolean()){
                    vector.setZ(-vector.getZ());
                }
                player.setVelocity(vector);
            }
            player.getInventory().addItem(Utils.SNOWBALL.clone());
        }
    }

    @Override
    protected String getSignLine(String path) {
        return pl.getMessageManager().get("sign.splegg."+path, new String[]{"name", "size", "max", "min", "missing"},
                new String[]{getFullName(), playerInGame.size()+"", max+"", min+"", (min-playerInGame.size())+""});
    }
}
