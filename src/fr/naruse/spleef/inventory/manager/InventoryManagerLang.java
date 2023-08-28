package fr.naruse.spleef.inventory.manager;

import com.google.common.collect.Lists;
import fr.naruse.api.ItemUtils;
import fr.naruse.api.inventory.AbstractInventory;
import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryManagerLang extends AbstractInventory {

    public InventoryManagerLang(JavaPlugin pl, Player p) {
        super(pl, p, "§lSpleef Manager" , 9*3);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);

        inventory.setItem(9+2, ItemUtils.buildItem(Material.PAPER, 1, "§aEnglish", false, Lists.newArrayList(
                "§5- §7Current status: "+(pl.getConfig().getString("currentLang").equals("english") ? "§aSelected" : "§cNot selected")
        )));
        inventory.setItem(9+3, ItemUtils.buildItem(Material.PAPER, 1, "§aFrench", false, Lists.newArrayList(
                "§5- §7Current status: "+(pl.getConfig().getString("currentLang").equals("french") ? "§aSelected" : "§cNot selected")
        )));
        inventory.setItem(9+5, ItemUtils.buildItem(Material.PAPER, 1, "§aRussian", false, Lists.newArrayList(
                "§5- §7Current status: "+(pl.getConfig().getString("currentLang").equals("russian") ? "§aSelected" : "§cNot selected")
        )));
        inventory.setItem(9+6, ItemUtils.buildItem(Material.PAPER, 1, "§aGerman", false, Lists.newArrayList(
                "§5- §7Current status: "+(pl.getConfig().getString("currentLang").equals("german") ? "§aSelected" : "§cNot selected")
        )));

        inventory.setItem(inventory.getSize()-1, ItemUtils.buildItem(Material.BARRIER, "§cBack", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if(slot == this.inventory.getSize()-1){
            new InventoryManager((SpleefPlugin) this.pl, player);
            return;
        }else if(slot == 9+2){
            player.performCommand("spleef setLang english");
            player.performCommand("spleef reload");
        }else if(slot == 9+3){
            player.performCommand("spleef setLang french");
            player.performCommand("spleef reload");
        }else if(slot == 9+5){
            player.performCommand("spleef setLang russian");
            player.performCommand("spleef reload");
        }else if(slot == 9+6){
            player.performCommand("spleef setLang german");
            player.performCommand("spleef reload");
        }

        new InventoryManagerLang(this.pl, player);
    }

}
