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

public class DuelSpleef extends Spleef {
    public DuelSpleef(SpleefPluginV1_12 pl, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen) {
        super(pl, SpleefGameMode.DUEL, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen);
        this.setMax(2);
        this.setMin(2);
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
        if (getPlayerInGame().contains(p)) {
            getPlayerInGame().remove(p);
            p.teleport(getSpleefSpawn());
            p.getInventory().clear();
            updateSigns();
            updateScoreboards();
            if(getMain().getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            getMain().duels.decline(p, false);
            if (getGame().GAME) {
                getMain().wagers.loseWager(p);
            } else {
                if (getMain().wagers.hasWager(p)) {
                    Player player = getMain().wagers.getWagerOfPlayer().get(p).getOtherPlayer(p);
                    if (!getPlayerInGame().contains(player)) {
                        return;
                    }
                    sendMessage(getNAME() + " §6" + player.getName() + "§c " + Message.LEAVED_THE_GAME.getMessage());
                    getMain().spleefs.removePlayer(player);
                }else if (getMain().duels.duelActive(p)) {
                    Player player = getMain().duels.getOtherPlayer(p);
                    if (!getPlayerInGame().contains(player)) {
                        return;
                    }
                    sendMessage(getNAME() + " §6" + player.getName() + "§c " + Message.LEAVED_THE_GAME.getMessage());
                    getMain().spleefs.removePlayer(player);
                }
            }
        }
    }

    @Override
    public boolean addPlayer(Player p) {
        if (!getPlayerInGame().contains(p)) {
            if (getGame().GAME) {
                p.sendMessage(getNAME() + "§c " + Message.IN_GAME.getMessage());
                return false;
            }
            if (!p.isOp()) {
                if (getPlayerInGame().size() >= getMax()) {
                    p.sendMessage(getNAME() + "§c " + Message.FULL_GAME.getMessage());
                    return false;
                }
            }
            if(getMain().duels.duelActive(p)){
                restart(false);
            }
            runNormalJoin(p);
            getPlayerInGame().add(p);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            ItemStack item = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c" + Message.LEAVE_THIS_GAME.getMessage());
            item.setItemMeta(meta);
            if(allowMagmaCream()){
                p.getInventory().setItem(8, item);
            }
            sendMessage(getNAME() + " §6" + p.getName() + "§a " + Message.JOINED_THE_GAME.getMessage());
            updateSigns();
            updateScoreboards();
            if(getMain().getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(getScoreboardSign().getScoreboard());
            }
            p.getInventory().setHeldItemSlot(1);
            if (getMain().wagers.getWagerOfPlayer().containsKey(p)) {
                Wager wager = getMain().wagers.getWagerOfPlayer().get(p);
                if (!getPlayerInGame().contains(wager.getPlayer1())) {
                    getMain().spleefs.addPlayer(wager.getPlayer1(), this);
                }
                if (!getPlayerInGame().contains(wager.getPlayer2())) {
                    getMain().spleefs.addPlayer(wager.getPlayer2(), this);
                }
            }else if(getMain().duels.duelActive(p)){
                Player target = getMain().duels.getOtherPlayer(p);
                if(target != null){
                    if(!getPlayerInGame().contains(target)){
                        getMain().spleefs.addPlayer(target, this);
                    }
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
            sign.setLine(2, Message.SignColorTag.CLOSE_LINE2.getColorTag()+Message.SPLEEF_CLOSED.getMessage());
            sign.setLine(3, "");
            sign.update();
        }else{
            if(getGame().WAIT){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                if(getPlayerInGame().size() >= getMin()){
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_0.getColorTag()+Message.READY.getMessage());
                }else{
                    sign.setLine(2, Message.SignColorTag.OPEN_WAIT_LINE3_1.getColorTag()+(getMin()-getPlayerInGame().size())+" "+Message.MISSING.getMessage());
                }
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }else if(getGame().GAME){
                sign.setLine(0, "§c§l[§5"+getName()+"§c§l]");
                sign.setLine(1, Message.SignColorTag.OPEN_WAIT_LINE2_2.getColorTag()+getPlayerInGame().size()+"/"+getMax());
                sign.setLine(2, Message.SignColorTag.OPEN_GAME_LINE4_NORMAL.getColorTag()+Message.IN_GAME.getMessage());
                sign.setLine(3, Message.SignColorTag.OPEN_GAME_LINE4_OTHER.getColorTag()+" "+getGameMode().getName()+" Mode");
                sign.update();
            }
        }
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
