package fr.naruse.spleef.inventory.manager;

import com.google.common.collect.Lists;
import fr.naruse.api.ItemUtils;
import fr.naruse.api.inventory.AbstractInventory;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.Spleefs;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManager extends AbstractInventory {

    public InventoryManager(SpleefPlugin pl, Player p) {
        super(pl, p, "§lSpleef Manager", 9*6);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);

        Spleefs spleefs = ((SpleefPlugin) pl).getSpleefs();
        for (Spleef spleef : spleefs.getSpleefs()) {
            inventory.addItem(ItemUtils.buildItem(Material.GOLD_SPADE, 1, "§a"+spleef.getName(), false, Lists.newArrayList(
                    "§5- §7Max players: §f"+spleef.getMax(),
                    "§5- §7Min players: §f"+spleef.getMin(),
                    "§5- §7Current status: §f"+(spleef.getCurrentStatus() == GameStatus.WAIT ? "§cwaiting" : "§ain game"),
                    "§5- §7Players in game: §f"+spleef.getPlayerInGame().size()
            )));
        }

        inventory.setItem(inventory.getSize()-3, ItemUtils.buildItem(Material.PAPER, 1, "§aLang", false));
        inventory.setItem(inventory.getSize()-4, ItemUtils.buildItem(Material.STANDING_BANNER, 1, "§4Delete statistics", false, Lists.newArrayList("§cIrreversible action.", "§7Will delete all stored stats.")));
        inventory.setItem(inventory.getSize()-5, ItemUtils.buildItem(Material.WATER_BUCKET, "§aReload all arenas", false));
        inventory.setItem(inventory.getSize()-6, ItemUtils.buildItem(Material.COMMAND, "§5Arena Global Rules", false));
        inventory.setItem(inventory.getSize()-7, ItemUtils.buildItem(Material.GOLDEN_APPLE, 1, "§5Download DBAPI", false, Lists.newArrayList("§7SQL tool that Spleef uses to perform optimized storage of statistics.")));

        inventory.setItem(inventory.getSize()-1, ItemUtils.buildItem(Material.BARRIER, "§cQuit", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if(slot == this.inventory.getSize()-1){
            player.closeInventory();
        }else if(itemStack.getType().name().contains("GOLD_SPADE")){
            Spleefs spleefs = ((SpleefPlugin) pl).getSpleefs();
            for (Spleef spleef : spleefs.getSpleefs()) {
                if(spleef.getName().equals(itemStack.getItemMeta().getDisplayName().substring(2))){
                    new InventoryManagerArena(this.pl, player, spleef);
                    break;
                }
            }
        }else if(itemStack.getType().name().contains("WATER_BUCKET")){
            player.performCommand("spleef reload");
        }else if(itemStack.getType().name().contains("COMMAND")){
            new InventoryManagerGameRules(this.pl, player);
        }else if(itemStack.getType().name().contains("GOLDEN_APPLE")){
            player.performCommand("spleef downloadDBAPI");
        }else if(itemStack.getType().name().contains("STANDING_BANNER")){
            player.performCommand("spleef clearStats");
        }else if(itemStack.getType().name().contains("PAPER")){
            new InventoryManagerLang(this.pl, player);
        }
    }
}
