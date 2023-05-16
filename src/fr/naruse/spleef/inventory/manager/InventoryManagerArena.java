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

public class InventoryManagerArena extends AbstractInventory {

    private Spleef spleef;

    public InventoryManagerArena(JavaPlugin pl, Player p, Spleef spleef) {
        super(pl, p, "§lSpleef Manager - §d§l"+spleef.getName(), 9*3, false);

        this.spleef = spleef;

        this.initInventory(this.inventory);
        p.openInventory(this.inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);


        inventory.addItem(ItemUtils.buildItem(Material.BARRIER, "§cDelete", false));
        inventory.addItem(ItemUtils.buildItem(Material.SIGN, 1, this.spleef.isOpened() ? "§cClose arena" : "§aOpen arena", false,
                Lists.newArrayList("§5- §7Current state: "+(this.spleef.isOpened() ? "§aOpened" : "§cClosed"))));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Sheep Bonuses", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("spleef."+this.spleef.getId()+".sheepBonusEnabled") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Players will occasionally received an item (Wool) that when clicked, will launch a sheep with special abilities.",
                        "§cDisabled: §7No sheep.")));
        inventory.addItem(ItemUtils.buildItem(Material.WOOD_SWORD, "§aForce Start", false));
        inventory.addItem(ItemUtils.buildItem(Material.TNT, "§cForce Stop", false));

        GameType gameType;
        try {
            String type = pl.getConfig().getString("spleef." + this.spleef.getId() + ".gameType");
            if (type.equals("SPPLEGG")) {
                gameType = GameType.SPLEGG;
            } else {
                gameType = GameType.valueOf(type);
            }
        }catch (Exception e) {
            gameType = GameType.SPLEEF;
        }
        inventory.addItem(ItemUtils.buildItem(Material.TOTEM, 1, "§aChange Game Mode", false, Lists.newArrayList("§5- §7Current status: §a"+gameType.name())));
        inventory.addItem(ItemUtils.buildItem(Material.WOOL, "§aSheep Bonuses", false));

        inventory.setItem(inventory.getSize()-1, ItemUtils.buildItem(Material.BARRIER, "§cBack", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if(slot == this.inventory.getSize()-1){
            new InventoryManager((SpleefPlugin) this.pl, player);
            return;
        }else if(itemStack.getType().name().contains("BARRIER")){
            player.performCommand("spleef delete "+this.spleef.getName());
            player.closeInventory();
        }else if(itemStack.getType().name().contains("SIGN")){
            player.performCommand("spleef "+(this.spleef.isOpened() ? "close" : "open")+" "+this.spleef.getName());
        }else if(itemStack.getType().name().contains("COMMAND")){
            player.performCommand("spleef enableSheep "+this.spleef.getName());
        }else if(itemStack.getType().name().contains("WOOD_SWORD")){
            player.performCommand("spleef forceStart "+this.spleef.getName());
        }else if(itemStack.getType().name().contains("TNT")){
            player.performCommand("spleef forceStop "+this.spleef.getName());
        }else if(itemStack.getType().name().contains("TOTEM")){
            new InventoryManagerArenaGameMode(this.pl, player, this.spleef);
            return;
        }else if(itemStack.getType().name().contains("WOOL")){
            new InventoryManagerSheepBonuses((SpleefPlugin) this.pl, player, this.spleef);
            return;
        }

        new InventoryManagerArena(this.pl, player, this.spleef);
    }

}
