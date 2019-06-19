package fr.naruse.spleef.v1_13.game.wager;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class Wager implements Listener {
    private SpleefPluginV1_13 pl;
    private Player player1;
    private Player player2;
    private Player lost = null;
    private Inventory inventory1 = Bukkit.createInventory(null, 9*3, "§2§l"+ Message.WAGER.getMessage());
    private Inventory inventory2 = Bukkit.createInventory(null, 9*3, "§2§l"+ Message.WAGER.getMessage());
    private Inventory inventoryFinal = null;
    private boolean wagerAccepted = false, player1Ready = false, player2Ready = false, player1FinalReady = false, player2FinalReady = false;
    private boolean wagerActive = false, rewardsGot = false;
    public boolean stop = false;
    public Wager(SpleefPluginV1_13 pl, Player p, Player p2) {
        this.pl = pl;
        this.player1 = p;
        this.player2 = p2;
    }

    public void init() {

    }

    public void stop() {
        pl.wagers.getWagerOfPlayer().remove(player1);
        pl.wagers.getWagerOfPlayer().remove(player2);
        stop = true;
    }

    public void win(Player p){
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl.getSpleefPlugin(), new Runnable() {
            @Override
            public void run() {
                List<ItemStack> list = Lists.newArrayList();
                for(int i = 10; i < 17; i++){
                    if(inventory1.getItem(i) != null){
                        list.add(inventory1.getItem(i));
                    }
                    if(inventory2.getItem(i) != null){
                        list.add(inventory2.getItem(i));
                    }
                }
                for(ItemStack item : list){
                    p.getInventory().addItem(item);
                }
                player1.sendMessage(Message.SPLEEF.getMessage()+" §6"+p.getName()+" §a"+ Message.WON_THE_WAGER.getMessage());
                player2.sendMessage(Message.SPLEEF.getMessage()+" §6"+p.getName()+" §a"+ Message.WON_THE_WAGER.getMessage());
                rewardsGot = true;
                decline();
            }
        },20);
    }

    public void decline(){
        if(!rewardsGot){
            for(int i = 10; i < 17; i++){
                if(inventory1.getItem(i) != null){
                    player1.getInventory().addItem(inventory1.getItem(i));
                }
                if(inventory2.getItem(i) != null){
                    player2.getInventory().addItem(inventory2.getItem(i));
                }
            }
        }
        pl.wagers.deleteWager(this);
    }

    public void accept(){
        wagerAccepted = true;
        generateInv();
        player1.openInventory(inventory1);
        player2.openInventory(inventory2);
    }

    @EventHandler
    public void clickInv(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }
        Player p = (Player) e.getWhoClicked();
        if(p != player1 && p != player2){
            return;
        }
        if(e.getClickedInventory() == p.getInventory()){
            return;
        }
        if(stop){
            return;
        }
        if(!wagerAccepted){
            e.setCancelled(true);
            return;
        }
        if(inventoryFinal != null){
            e.setCancelled(true);
        }
        if(wagerActive){
            e.setCancelled(true);
            return;
        }
        if(e.getCurrentItem() == null){
            return;
        }
        ItemStack item = e.getCurrentItem();
        if(item.getType() == Material.STAINED_GLASS_PANE){
            e.setCancelled(true);
            return;
        }
        if(e.getSlot() > 9*2 && e.getClickedInventory() != p.getInventory()){
            if(item.getType() == Material.WOOL){
                if(inventoryFinal == null){
                    e.setCancelled(true);
                    if(item.getData().getData() == 14){
                        player1.closeInventory();
                        player2.closeInventory();
                        player1.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_DECLINED.getMessage());
                        player2.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_DECLINED.getMessage());
                        decline();
                    }else if(item.getData().getData() == 5){
                        if(p == player1){
                            player1Ready = true;
                            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.AWAITING_VALIDATION.getMessage());
                            if(player2Ready){
                                p.closeInventory();
                                player2.closeInventory();
                                generateFinalInv();
                                player1.openInventory(inventoryFinal);
                                player2.openInventory(inventoryFinal);
                            }
                        }else{
                            player2Ready = true;
                            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.AWAITING_VALIDATION.getMessage());
                            if(player1Ready){
                                p.closeInventory();
                                player1.closeInventory();
                                generateFinalInv();
                                player1.openInventory(inventoryFinal);
                                player2.openInventory(inventoryFinal);
                            }
                        }
                    }
                }else if(e.getSlot() > 9*5){
                    e.setCancelled(true);
                    if(item.getData().getData() == 14){
                        player1.closeInventory();
                        player2.closeInventory();
                        player1.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_DECLINED.getMessage());
                        player2.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_DECLINED.getMessage());
                        decline();
                    }else if(item.getData().getData() == 5){
                        if(p == player1){
                            player1FinalReady = true;
                            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.AWAITING_VALIDATION.getMessage());
                            p.closeInventory();
                            if(player2FinalReady){
                                p.closeInventory();
                                player2.closeInventory();
                                inventoryFinal.remove(inventoryFinal.getItem(9*6+3));
                                inventoryFinal.remove(inventoryFinal.getItem(9*6+5));
                                wagerActive = true;
                                player1.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_ACTIVATED.getMessage());
                                player2.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_ACTIVATED.getMessage());
                            }
                        }else{
                            player2FinalReady = true;
                            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.AWAITING_VALIDATION.getMessage());
                            p.closeInventory();
                            if(player1FinalReady){
                                p.closeInventory();
                                player1.closeInventory();
                                wagerActive = true;
                                player1.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_ACTIVATED.getMessage());
                                player2.sendMessage(Message.SPLEEF.getMessage()+" §a"+ Message.WAGER_ACTIVATED.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void closeInv(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        if(p == player1 || p == player2){
            if(e.getInventory() == inventory1){
                if(!wagerAccepted){
                    decline();
                }
            }
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if(p == player1 || p == player2){
            if(!isWagerActive()){
                player1.closeInventory();
                player2.closeInventory();
                decline();
                return;
            }
            pl.wagers.loseWager(p);
            if(p == player1){
                win(player2);
            }else{
                win(player1);
            }
        }
    }

    private void generateInv() {
        inventory1 = Bukkit.createInventory(null, 9*3, "§2§l"+ Message.WAGER.getMessage()+" §8§l("+player1.getName()+" - "+player2.getName()+")");
        for(int i = 0; i < 10; i++){
            inventory1.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 17; i < 9*3; i += 9){
            inventory1.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 9; i < 9*3; i += 9){
            inventory1.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 9*2+1; i < 9*3; i++){
            inventory1.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        //
        inventory2 = Bukkit.createInventory(null, 9*3, "§2§l"+ Message.WAGER.getMessage()+" §8§l("+player1.getName()+" - "+player2.getName()+")");
        for(int i = 0; i < 10; i++){
            inventory2.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 17; i < 9*3; i += 9){
            inventory2.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 9; i < 9*3; i += 9){
            inventory2.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        for(int i = 9*2+1; i < 9*3; i++){
            inventory2.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) new Random().nextInt(8)));
        }
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 5);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Ok");
        item.setItemMeta(meta);
        inventory2.setItem(9*2+3, item);
        inventory1.setItem(9*2+3, item);
        item = new ItemStack(Material.WOOL, 1, (byte) 14);
        meta = item.getItemMeta();
        meta.setDisplayName("§4Stop");
        item.setItemMeta(meta);
        inventory2.setItem(9*2+5, item);
        inventory1.setItem(9*2+5, item);
    }

    private void generateFinalInv() {
        inventoryFinal = Bukkit.createInventory(null, 9*6, "§2§l"+ Message.WAGER.getMessage()+" §8§l("+player1.getName()+" - "+player2.getName()+")");
        for(int i = 0; i < 10; i++){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        for(int i = 17; i < 9*6; i += 9){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        for(int i = 9; i < 9*6; i += 9){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        for(int i = 9*2+1; i < 9*3; i++){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        for(int i = 9*3+1; i < 9*4; i++){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        for(int i = 9*5+1; i < 9*6; i++){
            inventoryFinal.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 12));
        }
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 5);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Ok");
        item.setItemMeta(meta);
        inventoryFinal.setItem(9*5+3, item);
        item = new ItemStack(Material.WOOL, 1, (byte) 14);
        meta = item.getItemMeta();
        meta.setDisplayName("§4Stop");
        item.setItemMeta(meta);
        inventoryFinal.setItem(9*5+5, item);
        for(int i = 10; i < 17; i++){
            if(inventory1.getItem(i) != null){
                inventoryFinal.setItem(i, inventory1.getItem(i));
            }
            if(inventory2.getItem(i) != null){
                inventoryFinal.setItem(i+9*3, inventory2.getItem(i));
            }
        }
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public boolean isWagerActive() {
        return wagerActive;
    }

    public Player getOtherPlayer(Player p){
        if(p == player1){
            return player2;
        }else{
            return player1;
        }
    }

    public void openInventory(Player p){
        if(p == player1){
            if(inventoryFinal != null){
                p.openInventory(inventoryFinal);
            }else{
                p.openInventory(inventory1);
            }
        }else if(p == player2){
            if(inventoryFinal != null){
                p.openInventory(inventoryFinal);
            }else{
                p.openInventory(inventory2);
            }
        }
    }

    public Player getLost() {
        return lost;
    }

    public void setLost(Player lost) {
        this.lost = lost;
    }
}
