package fr.naruse.spleef.spleef.type;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BowSpleef extends Spleef {

    private final List<Entity> tnts = Lists.newArrayList();

    public BowSpleef(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby) {
        super(pl, id, name, isOpened, max, min, arena, spawn, lobby);
    }

    @Override
    public void start() {
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));
        changeNeighbours();
        for (Player player : playerInGame) {
            player.teleport(arena);

            player.teleport(getRandomLocationFrom(arena));
            player.getInventory().addItem(Utils.BOW);
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
        }
    }

    @Override
    public void restart() {
        List<Player> list = Lists.newArrayList();
        for(Player p : playerInGame){
            list.add(p);
        }
        for(Player p : list){
            removePlayer(p);
        }
        playerInGame.clear();

        for (Block block : blocks) {
            block.setType(Material.TNT);
        }
        blocks.clear();

        currentStatus = GameStatus.WAIT;

        updateSigns();
    }

    @Override
    public void stop() {
        super.stop();
        changeNeighboursBack();
    }

    private void changeNeighbours(){
        Location clone = arena.clone();
        clone.setY(clone.getWorld().getMaxHeight());
        while (clone.getBlockY() >= 0){
            changeNeighbours(clone.clone(), Material.SNOW_BLOCK, Material.TNT);
            clone.setY(clone.getY()-1);
        }
    }

    private void changeNeighboursBack(){
        Location clone = arena.clone();
        clone.setY(clone.getWorld().getMaxHeight());
        while (clone.getBlockY() >= 0){
            changeNeighbours(clone.clone(), Material.TNT, Material.SNOW_BLOCK);
            clone.setY(clone.getY()-1);
        }
    }

    private void changeNeighbours(Location location, Material origin, Material finality){
        for (Block neighbour : getNeighbours(location)) {
            if(neighbour.getType() == origin){
                neighbour.setType(finality);
                changeNeighbours(neighbour.getLocation(), origin, finality);
            }
        }
    }

    private Block[] getNeighbours(Location location){
        return new Block[]{location.clone().add(1, 0, 0).getBlock(), location.clone().add(1, 0, 1).getBlock(),
                location.clone().add(0, 0, 1).getBlock(), location.clone().add(0, 0, 0).getBlock(),
                location.clone().add(-1, 0, 0).getBlock(), location.clone().add(-1, 0, -1).getBlock(),
                location.clone().add(0, 0, -1).getBlock(), location.clone().add(1, 0, -1).getBlock(),
                location.clone().add(-1, 0, 1).getBlock()};
    }

    @EventHandler
    public void shoot(ProjectileHitEvent e){
        if(e.getEntity() instanceof Arrow && e.getEntity().getShooter() instanceof Player){
            Player p = (Player) e.getEntity().getShooter();
            if(!hasPlayer(p) || currentStatus == GameStatus.WAIT){
                return;
            }
            p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            if(e.getHitBlock() != null && e.getHitBlock().getType() == Material.TNT){
                Block block = e.getHitBlock();
                blocks.add(block);
                block.setType(Material.AIR);
                tnts.add(block.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT));
            }
        }
    }

    @EventHandler
    public void explode(EntityExplodeEvent e){
        if(tnts.contains(e.getEntity())){
            e.setCancelled(true);
            tnts.remove(e.getEntity());
        }
    }

    @Override
    protected String getSignLine(String path) {
        return pl.getMessageManager().get("sign.bow" +
                        "."+path, new String[]{"name", "size", "max", "min", "missing"},
                new String[]{getFullName(), playerInGame.size()+"", max+"", min+"", (min-playerInGame.size())+""});
    }

    @Override
    protected void removeBlockUnderFoot(Block block) {
        if (block.getType() == Material.TNT) {
            blocks.add(block);
            block.setType(Material.AIR);
            tnts.add(block.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT));
            if(pl.getConfig().getBoolean("lightnings")){
                block.getWorld().strikeLightningEffect(block.getLocation());
            }
        }
    }
}
