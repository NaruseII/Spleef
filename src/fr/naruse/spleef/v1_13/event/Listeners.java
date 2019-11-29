package fr.naruse.spleef.v1_13.event;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.game.spleef.Spleef;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class Listeners implements Listener {
    private SpleefPluginV1_13 pl;
    public Listeners(SpleefPluginV1_13 spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @EventHandler
    public void interact(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.getClickedBlock() == null){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK){
            if(hasPermission(p, "spleef.sign.break")){
                return;
            }
        }
        if(!(e.getClickedBlock().getState() instanceof Sign)){
            return;
        }
        Sign sign  = (Sign) e.getClickedBlock().getState();
        for(Spleef spleef : pl.spleefs.getSpleefs()){
            if(sign.getLine(0).equalsIgnoreCase("§c§l[§5"+spleef.getName()+"§c§l]")){
                pl.spleefs.addPlayer(p, spleef);
                e.setCancelled(true);
                break;
            }
        }
        if(!hasPermission(p, "spleef.sign.create")){
            return;
        }
        if(sign.getLine(0).equalsIgnoreCase("-!s!-")  && sign.getLine(3).equalsIgnoreCase("-!s!-")){
            if(sign.getLine(1).equalsIgnoreCase(sign.getLine(2))){
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(sign.getLine(1))){
                        sign.setLine(0, "§c§l[§5"+spleef.getName()+"§c§l]");
                        sign.update();
                        spleef.registerNewSigns(p.getWorld());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void command(PlayerCommandPreprocessEvent e){
        if(pl.spleefs.hasSpleef(e.getPlayer())){
            List<String> commands = pl.configurations.getCommands().getConfig().getStringList("commands");
            for(String s : commands){
                if(s.equalsIgnoreCase(e.getMessage().split(" ")[0])){
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            if (pl.spleefs.hasSpleef((Player) e.getWhoClicked())) {
                e.setCancelled(true);
            }
        }
    }

    private boolean hasPermission(Player p, String msg){
        if(!p.hasPermission(msg)){
            if(!p.getName().equalsIgnoreCase("NaruseII")){
                return false;
            }
        }
        return true;
    }
}
