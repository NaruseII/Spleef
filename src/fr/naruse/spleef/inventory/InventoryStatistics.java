package fr.naruse.spleef.inventory;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryStatistics extends AbstractInventory{
    private OfflinePlayer target;
    public InventoryStatistics(SpleefPlugin pl, Player p, OfflinePlayer target) {
        super(pl, p, pl.getMessageManager().get("inventory.statisticsTitle", new String[]{"name"}, new String[]{target.getName()}), 9, false);
        this.target = target;
        initInventory(inventory);
        p.openInventory(inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(target);
        if(pl.getDatabaseManager() == null){
            ItemStack itemStack = new ItemStack(Material.WOOL, 1, (byte) 10);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(pl.getMessageManager().get("inventory.notFound"));
            itemStack.setItemMeta(meta);
            inventory.setItem(4, itemStack);
            return;
        }
        ItemStack itemStack = new ItemStack(Material.WOOL, 1, (byte) 4);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(pl.getMessageManager().get("inventory.wins", new String[]{"value"}, new String[]{spleefPlayer.getStatistic(StatisticType.WIN)+""}));
        itemStack.setItemMeta(meta);
        inventory.setItem(3, itemStack);

        itemStack = new ItemStack(Material.WOOL, 1, (byte) 14);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(pl.getMessageManager().get("inventory.loses", new String[]{"value"}, new String[]{spleefPlayer.getStatistic(StatisticType.LOSE)+""}));
        itemStack.setItemMeta(meta);
        inventory.setItem(5, itemStack);
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {

    }
}
