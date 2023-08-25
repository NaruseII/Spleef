package fr.naruse.spleef.inventory.manager;

import com.google.common.collect.Lists;
import fr.naruse.api.ItemUtils;
import fr.naruse.api.inventory.AbstractInventory;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryManagerGameRules extends AbstractInventory {

    public InventoryManagerGameRules(JavaPlugin pl, Player p) {
        super(pl, p, "§lSpleef Manager - §6§lGame Rules", 9*4);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        this.setDecoration(Material.STAINED_GLASS_PANE, true);

        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Broadcast Win", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("broadcastWin") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When a game is won by a player, the win message will be broadcast on all players on the server.",
                        "§cDisabled: §7Message only sent to the winner.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Broadcast Win World", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("broadcastWinWorld") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When a game is won by a player, the win message will be a","§7broadcast on all players on the current world.",
                        "§cDisabled: §7Message only sent to the winner.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Holographic Ranking", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("holographicRanking") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7The holographic ranking will be displayed.","§f(See related commands /spleef setHologram and statistics must be enabled)",
                        "§cDisabled: §7No ranking will be displayed.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Lightnings", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("lightnings") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When a player stands on the same block, an effect will destroy blocks under his feet.","§7(Only the lightning effect, blocks will still get destroyed)",
                        "§cDisabled: §7No lightning effect")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Teleport to Last Location", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("tpToLastLoc") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When a player loose, he will get teleported to his last location before joining the arena.",
                        "§cDisabled: §7Teleport player to the arena 'Spawn' location.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Snowballs", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("snowballs") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When players destroy blocks, they will received snowballs","§7that knockback others and destroy hit block.",
                        "§cDisabled: §7No snowballs.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Instant Give Shovel", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("instantGiveShovel") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7On game start, players will instantly receive a spade.",
                        "§cDisabled: §7On game start, players will have 5 seconds to run away before the spade will be given.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Random Spawn", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("randomSpawn") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7On game start, players will be randomly teleported to a block.",
                        "§cDisabled: §7On game start, players will all spawn on the same spot.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Spectator", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("spectator") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7When a player loose, he will be set in spectator mode until the game ends.",
                        "§cDisabled: §7Once the player loose, he can go straight back to the business at hand.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Diamond Spade", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("diamondSpade") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Instead of a golden spade, it is a diamond one.",
                        "§cDisabled: §7The good old golden spade.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2YAML Statistics", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("yamlStatistics") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Player's statistics will be stored in a config file locally on the plugin config folder.",
                        "§cDisabled: §7No statistics or SQL statistics if DBAPI detected.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Check For Updates", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("checkForUpdates") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Broadcast message when an update is found. (Update are supposed to be automatic)",
                        "§cDisabled: §7No update check. (Updates are still executed automatically)")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Join World Lock", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("joinWorldLock") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Player will not be able to join an arena they are not on the same world as the arena.",
                        "§7If they teleport to another world, they will be kick from the arena they are in.",
                        "§cDisabled: §7No matter the world, players will be able to join.")));
        inventory.addItem(ItemUtils.buildItem(Material.COMMAND, 1, "§2Snowball on Sheep Break Block", false,
                Lists.newArrayList("§5- §7Current status: "+(pl.getConfig().getBoolean("snowballSheepBreakBlock") ? "§aEnabled" : "§cDisabled"),
                        "§aEnabled: §7Player will receive snowballs when a sheep breaks blocks.",
                        "§cDisabled: §7No snowballs are given when a sheep breaks a block.")));

        inventory.setItem(inventory.getSize()-1, ItemUtils.buildItem(Material.BARRIER, "§cBack", false));
    }

    @Override
    protected void actionPerformed(Player player, ItemStack itemStack, InventoryAction inventoryAction, int slot) {
        if(slot == this.inventory.getSize()-1){
            new InventoryManager((SpleefPlugin) this.pl, player);
            return;
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Broadcast Win")){
            player.performCommand("spleef enable BroadcastWin");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Broadcast Win World")){
            player.performCommand("spleef enable BroadcastWinWorld");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Holographic Ranking")){
            player.performCommand("spleef enable HolographicRanking");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Lightnings")){
            player.performCommand("spleef enable Lightnings");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Teleport to Last Location")){
            player.performCommand("spleef enable tpToLastLoc");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Snowballs")){
            player.performCommand("spleef enable snowballs");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Instant Give Shovel")){
            player.performCommand("spleef enable instantGiveShovel");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Random Spawn")){
            player.performCommand("spleef enable randomSpawn");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Spectator")){
            player.performCommand("spleef enable spectator");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Diamond Spade")){
            player.performCommand("spleef enable diamondSpade");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2YAML Statistics")){
            player.performCommand("spleef enable yamlStatistics");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Check For Updates")){
            player.performCommand("spleef enable checkForUpdates");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Join World Lock")){
            player.performCommand("spleef enable joinWorldLock");
        }else if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§2Snowball on Sheep Break Block")){
            player.performCommand("spleef enable snowballSheepBreakBlock");
        }

        new InventoryManagerGameRules(this.pl, player);
    }
}
