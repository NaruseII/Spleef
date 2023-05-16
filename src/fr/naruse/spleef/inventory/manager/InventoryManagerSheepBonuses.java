package fr.naruse.spleef.inventory.manager;

import com.google.common.collect.Lists;
import fr.naruse.api.ItemUtils;
import fr.naruse.api.inventory.AbstractInventory;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryManagerSheepBonuses extends AbstractInventory {

    private Spleef spleef;

    public InventoryManagerSheepBonuses(SpleefPlugin pl, Player p, Spleef spleef) {
        super(pl, p, "§lSpleef Manager - §d§l"+spleef.getName(), 9 * 6, false);

        this.spleef = spleef;

        this.initInventory(this.inventory);
        p.openInventory(this.inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);

        for (Class<? extends Bonus> bonus : BonusManager.getBonuses()) {
            String path = "spleef."+this.spleef.getId()+".bonus."+bonus.getSimpleName();

            inventory.addItem(ItemUtils.buildItem(Material.WOOL, 1, "§a"+bonus.getSimpleName(), false, Lists.newArrayList(
                    "§5- §7Current status: "+(!this.pl.getConfig().contains(path) || this.pl.getConfig().getBoolean(path) ? "§aEnabled" : "§cDisabled")
            )));
        }

        inventory.setItem(inventory.getSize() - 1, ItemUtils.buildItem(Material.BARRIER, "§cBack", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if (slot == this.inventory.getSize() - 1) {
            new InventoryManager((SpleefPlugin) this.pl, player);
        }else if(itemStack.getType() == Material.WOOL){
            player.performCommand("spleef bonus "+this.spleef.getName()+" enable "+itemStack.getItemMeta().getDisplayName().substring(2));
            new InventoryManagerSheepBonuses((SpleefPlugin) this.pl, player, this.spleef);
        }
    }
}
