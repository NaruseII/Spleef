package fr.naruse.spleef.v1_12.game.spleef.type;

import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.game.spleef.SpleefGameMode;
import fr.naruse.spleef.v1_12.game.spleef.Spleef;
import fr.naruse.spleef.v1_12.game.wager.Wager;
import fr.naruse.spleef.v1_12.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NormalSpleef extends Spleef {
    public NormalSpleef(SpleefPluginV1_12 pl, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen) {
        super(pl, SpleefGameMode.NORMAL, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen);
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
                    sendMessage(getNAME()+" §6"+player.getName()+"§c "+Message.LEAVED_THE_GAME.getMessage());
                    getMain().spleefs.removePlayer(player);
                }
            }
        }
    }

    @Override
    public boolean addPlayer(Player p) {
        if(!getPlayerInGame().contains(p)){
            if(getGame().GAME){
                p.sendMessage(getNAME()+"§c "+Message.IN_GAME.getMessage());
                return false;
            }
            if(!p.isOp()){
                if(getPlayerInGame().size() >= getMax()){
                    p.sendMessage(getNAME()+"§c "+Message.FULL_GAME.getMessage());
                    return false;
                }
            }
            runNormalJoin(p);
            getPlayerInGame().add(p);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            ItemStack item = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c"+Message.LEAVE_THIS_GAME.getMessage());
            item.setItemMeta(meta);
            if(allowMagmaCream()){
                p.getInventory().setItem(8, item);
            }
            sendMessage(getNAME()+" §6"+p.getName()+"§a "+Message.JOINED_THE_GAME.getMessage());
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

    }

    @Override
    public void start() {
        this.runNormalStart();
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
}
