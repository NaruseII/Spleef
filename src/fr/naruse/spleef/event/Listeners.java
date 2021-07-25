package fr.naruse.spleef.event;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.utils.SpleefUpdater;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class Listeners implements Listener {
    private SpleefPlugin pl;
    public Listeners(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @EventHandler
    public void join(PlayerJoinEvent e){
        Player p = e.getPlayer();
        pl.getSpleefPlayerRegistry().registerPlayer(p).reloadStatistics();
        if((p.isOp() || p.hasPermission("spleef.help")) && SpleefUpdater.updateAvailable()){
            SpleefUpdater.sendMessage(pl, p);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        Spleef spleef = spleefPlayer.getCurrentSpleef();
        if(spleef != null){
            spleef.removePlayer(p);
        }
        pl.getSpleefPlayerRegistry().getSpleefPlayer(p).saveStatistics();
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }
        Player p = (Player) e.getWhoClicked();
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);

        if(e.getCurrentItem() != null && e.getCurrentItem().equals(Utils.LEAVE_ITEM) && spleefPlayer.hasSpleef()){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent e){
        Player p = e.getPlayer();
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);

        if(e.getItem() != null){
            if(e.getItem().equals(Utils.LEAVE_ITEM) && spleefPlayer.hasSpleef()){
                Spleef spleef = spleefPlayer.getCurrentSpleef();
                if(spleef != null){
                    spleef.removePlayer(p);
                }
                e.setCancelled(true);
            }
            return;
        }
        if(e.getClickedBlock() == null){
            return;
        }
        Block block = e.getClickedBlock();
        if(!(block.getState() instanceof Sign)){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK){
            if(p.hasPermission("spleef.sign.break")){
                return;
            }
        }
        Sign sign = (Sign) block.getState();
        for (int i = 0; i < pl.getSpleefs().getSpleefs().size(); i++) {
            Spleef spleef = pl.getSpleefs().getSpleefs().get(i);
            spleef.registerSign(sign);
            if(ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ChatColor.stripColor((spleef.getFullName())))){
                spleef.addPlayer(p, false);
                e.setCancelled(true);
                break;
            }
        }
        if(p.hasPermission("spleef.sign.create")) {
            if (sign.getLine(0).equalsIgnoreCase("-!s!-") && sign.getLine(3).equalsIgnoreCase("-!s!-")) {
                if (sign.getLine(1).equalsIgnoreCase(sign.getLine(2))) {
                    for (Spleef spleef : pl.getSpleefs().getSpleefs()) {
                        if (spleef.getName().equals(sign.getLine(1))) {
                            sign.setLine(0, spleef.getFullName());
                            sign.update();
                            spleef.registerSign(sign);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent e){
        Player p = e.getPlayer();
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        List<String> list = pl.getConfig().getStringList("disabledCommands");
        if(spleefPlayer != null && spleefPlayer.hasSpleef() && list.contains(e.getMessage())){
            e.setCancelled(true);
        }
    }

}
