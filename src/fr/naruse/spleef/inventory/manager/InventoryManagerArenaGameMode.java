package fr.naruse.spleef.inventory.manager;

import com.google.common.collect.Lists;
import fr.naruse.api.ItemUtils;
import fr.naruse.api.inventory.AbstractInventory;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameType;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryManagerArenaGameMode extends AbstractInventory {

    private Spleef spleef;

    public InventoryManagerArenaGameMode(JavaPlugin pl, Player p, Spleef spleef) {
        super(pl, p, "§lSpleef Manager - §d§l"+spleef.getName(), 9*3, false);

        this.spleef = spleef;

        this.initInventory(this.inventory);
        p.openInventory(this.inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);

        for (GameType value : GameType.values()) {
            inventory.addItem(ItemUtils.buildItem(Material.TOTEM, "§a"+value.name(), false));
        }

        inventory.setItem(inventory.getSize()-1, ItemUtils.buildItem(Material.BARRIER, "§cBack", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if(slot == this.inventory.getSize()-1){
            new InventoryManagerArena(this.pl, player, this.spleef);
        }else if(itemStack.getType().name().contains("TOTEM")){
            GameType gameType = GameType.valueOf(itemStack.getItemMeta().getDisplayName().substring(2));
            player.performCommand("spleef setGameMode "+this.spleef.getName()+" "+gameType.name());
            new InventoryManagerArena(this.pl, player, this.spleef);
        }
    }

}
