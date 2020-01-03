package fr.naruse.spleef.v1_13.game.spleef.type;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.game.spleef.Spleef;
import fr.naruse.spleef.v1_13.game.spleef.SpleefGameMode;
import fr.naruse.spleef.v1_13.game.wager.Wager;
import fr.naruse.spleef.v1_13.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;

public class SpleegSpleef extends Spleef implements Listener {
    public SpleegSpleef(SpleefPluginV1_13 pl, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen) {
        super(pl, SpleefGameMode.SPLEGG, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen);
        Bukkit.getPluginManager().registerEvents(this, getMain().getSpleefPlugin());
    }

    @Override
    public void runScheduler() {
        this.runNormalScheduler();
    }

    @Override
    public void runTickScheduler() {

    }

    @Override
    public void removePlayer(Player p) {
        if(getPlayerInGame().contains(p)){
            getPlayerInGame().remove(p);
            p.teleport(getSpleefSpawn());
            p.getInventory().clear();
            updateSigns();
            updateScoreboards();
            if(getMain().getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            if(getGame().GAME){
                getMain().wagers.loseWager(p);
            }else{
                if(getMain().wagers.hasWager(p)){
                    Player player = getMain().wagers.getWagerOfPlayer().get(p).getOtherPlayer(p);
                    if(!getPlayerInGame().contains(player)){
                        return;
                    }
                    sendMessage(getNAME()+" §6"+player.getName()+"§c "+ Message.LEAVED_THE_GAME.getMessage());
                    getMain().spleefs.removePlayer(player);
                }
            }
        }
    }

    @Override
    public boolean addPlayer(Player p) {
        if(!getPlayerInGame().contains(p)){
            if(getGame().GAME){
                p.sendMessage(getNAME()+"§c "+ Message.IN_GAME.getMessage());
                return false;
            }
            if(!p.isOp()){
                if(getPlayerInGame().size() >= getMax()){
                    p.sendMessage(getNAME()+"§c "+ Message.FULL_GAME.getMessage());
                    return false;
                }
            }
            runNormalJoin(p);
            getPlayerInGame().add(p);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            ItemStack item = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c"+ Message.LEAVE_THIS_GAME.getMessage());
            item.setItemMeta(meta);
            if(allowMagmaCream()){
                p.getInventory().setItem(8, item);
            }
            sendMessage(getNAME()+" §6"+p.getName()+"§a "+ Message.JOINED_THE_GAME.getMessage());
            updateSigns();
            updateScoreboards();
            if(getMain().getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(getScoreboardSign().getScoreboard());
            }
            p.getInventory().setHeldItemSlot(1);
            if(getMain().wagers.getWagerOfPlayer().containsKey(p)){
                Wager wager =  getMain().wagers.getWagerOfPlayer().get(p);
                if(!getPlayerInGame().contains(wager.getPlayer1())){
                    getMain().spleefs.addPlayer(wager.getPlayer1(), this);
                }
                if(!getPlayerInGame().contains(wager.getPlayer2())){
                    getMain().spleefs.addPlayer(wager.getPlayer2(), this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateSigns(Sign sign) {
        if(!isOpen()){
            sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
            sign.setLine(1, "");
            sign.setLine(2, Message.SignColorTag.CLOSE_LINE2.getColorTag()+ Message.SPLEEF_CLOSED.getMessage());
            sign.setLine(3, "");
            sign.update();
        }else{
            if(getGame().WAIT){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                if(getPlayerInGame().size() >= getMin()){
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_0.getColorTag()+ Message.READY.getMessage());
                }else{
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_1.getColorTag()+(getMin()-getPlayerInGame().size())+" "+ Message.MISSING.getMessage());
                }
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }else if(getGame().GAME){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                sign.setLine(2, Message.SignColorTag.OPEN_GAME_LINE4_NORMAL.getColorTag()+ Message.IN_GAME.getMessage());
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }
        }
    }

    @Override
    public void start() {
        sendMessage(getNAME()+" §a"+ Message.GAME_START.getMessage());
        getGame().WAIT = false;
        getGame().GAME = true;
        for(Player p : getPlayerInGame()){
            p.teleport(getSpleefLoc());
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*5, 1, false, false));
            if(new Random().nextBoolean()){
                if(new Random().nextBoolean()){
                    p.setVelocity(new Vector(-0.5F, 0.5F, 0.5F));
                }else{
                    p.setVelocity(new Vector(-0.5F, 0.5F, -0.5F));
                }
            }else{
                if(new Random().nextBoolean()){
                    p.setVelocity(new Vector(-0.5F, 0.5F, -0.5F));
                }else{
                    p.setVelocity(new Vector(0.5F, 0.5F, -0.5F));
                }
            }
        }
        getScoreboardSign().getObjective().setDisplayName(getNAME());
        Bukkit.getScheduler().scheduleSyncDelayedTask(getMain().getSpleefPlugin(), new Runnable() {
            @Override
            public void run() {
                for(Player p : getPlayerInGame()){
                    ItemStack item;
                    ItemMeta meta;
                    if(!allowGoldShovel()){
                        Material material = Material.DIAMOND_SPADE;
                        item = new ItemStack(material);
                        meta = item.getItemMeta();
                        meta.setUnbreakable(true);
                        item.setItemMeta(meta);
                        p.getInventory().addItem(item);
                    }
                    p.getInventory().addItem(new ItemStack(Material.EGG, 16*8));
                }
            }
        },20*5);
        setTimeInSecond(0);
    }

    @Override
    public void restart(boolean notOnDisable) {
        this.runNormalRestart(notOnDisable);
    }

    @Override
    public void updateScoreboards() {
        this.runNormalUpdateScoreboards();
    }

    @Override
    public boolean postShowTime(int timeInSecond) {
        return true;
    }

    @EventHandler
    public void hitEvent(ProjectileHitEvent e){
        if(!(e.getEntity().getShooter() instanceof Player)){
           return;
        }
        if(!getPlayerInGame().contains(e.getEntity().getShooter())){
            return;
        }
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof Egg)){
           return;
        }
        if(e.getHitEntity() != null){
            e.getHitEntity().setVelocity(genVector(((Player) e.getEntity().getShooter()).getLocation(), e.getHitEntity().getLocation()).multiply(0.5));
        }
        if(e.getHitBlock() == null) {
            return;
        }
        if(getAuthorizedMaterial().contains(e.getHitBlock().getType()) && getGame().GAME){
            getBlocks().add(e.getHitBlock());
            getBlocksOfRegionVerif().remove(e.getHitBlock());
            getTypeOfLocationHashMap().put(e.getHitBlock().getLocation(), e.getHitBlock().getType());
            e.getHitBlock().setType(Material.AIR);
            for(Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)){
                if(entity instanceof Chicken){
                    if(entity.getLocation().getWorld().getName() == getSpleefSpawn().getWorld().getName()){
                        if(getSpleefSpawn().distance(e.getEntity().getLocation()) <= 100 && getGame().GAME){
                            entity.remove();
                        }
                    }
                }
            }
        }else{
            Block b = e.getHitBlock();
            if(materialHashMap.containsKey(b)){
                b.setType(materialHashMap.get(b));
                b.setData(dataHashMap.get(b));
            }
        }
    }

    private HashMap<Block, Material> materialHashMap = new HashMap<>();
    private HashMap<Block, Byte> dataHashMap = new HashMap<>();
    @EventHandler
    public void blockChange(ProjectileLaunchEvent e){
        if(!(e.getEntity().getShooter() instanceof Player)){
            return;
        }
        if(!getPlayerInGame().contains(e.getEntity().getShooter())){
            return;
        }
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof Egg)){
            return;
        }
        BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 100);
        Block hitBlock = null;
        while (iterator.hasNext()) {
            hitBlock = iterator.next();
            if (hitBlock.getType().getId() != 0) {
                break;
            }
        }
        if(getAuthorizedMaterial().contains(hitBlock.getType())){
            return;
        }
        materialHashMap.put(hitBlock, hitBlock.getType());
        dataHashMap.put(hitBlock, hitBlock.getData());
        getTypeOfLocationHashMap().put(hitBlock.getLocation(), hitBlock.getType());
    }

    @EventHandler
    public void entitySpawnEvent(EntitySpawnEvent e){
        if(e.getEntity() instanceof Chicken){
            if(e.getEntity().getLocation().getWorld().getName() == getSpleefSpawn().getWorld().getName()){
                if(getSpleefSpawn().distance(e.getEntity().getLocation()) <= 100 && getGame().GAME){
                    e.setCancelled(true);
                    e.getEntity().remove();
                }
            }
        }
    }
}
