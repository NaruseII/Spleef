package fr.naruse.spleef.v1_13.game.spleef.type;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.game.spleef.Spleef;
import fr.naruse.spleef.v1_13.game.spleef.SpleefGameMode;
import fr.naruse.spleef.v1_13.game.wager.Wager;
import fr.naruse.spleef.v1_13.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class TwoTeamSpleef extends Spleef implements TeamModeSpleef {
    private List<List<Player>> teams = Lists.newArrayList();
    private List<Player> redTeam = Lists.newArrayList();
    private List<Player> blueTeam = Lists.newArrayList();
    public TwoTeamSpleef(SpleefPluginV1_13 pl, String name, Location spleefLoc, Location spleefSpawn, Location spleefLobby, int min, int max, boolean isOpen) {
        super(pl, SpleefGameMode.TWO_TEAM, name, spleefLoc, spleefSpawn, spleefLobby, min, max, isOpen);
    }

    @Override
    public void runScheduler() {
        if (getGame().WAIT) {
            getScoreboardSign().getObjective().setDisplayName(getNAME() + " §6" + getStartTimer());
            if (getStartTimer() != 0) {
                if (getPlayerInGame().size() >= getMin()) {
                    setStartTimer(getStartTimer() - 1);
                } else {
                    setStartTimer(getOriginalStartTimer());
                }
            } else if (getStartTimer() == 0) {
                setStartTimer(getOriginalStartTimer());
                if (getPlayerInGame().size() >= getMin()) {
                    start();
                } else {
                    sendMessage(getNAME() + " §c" + Message.NOT_ENOUGH_PLAYER.getMessage() + " (" + getPlayerInGame().size() + "/" + getMin() + ")");
                }
            }
        } else if (getGame().GAME) {
            if (redTeam.size() == 0) {
                Bukkit.broadcastMessage(getNAME() +" §7"+ Message.RED_TEAM.getMessage()+" §7" + Message.WINS_THE_GAME.getMessage());
                if (getMain().otherPluginSupport.getVaultPlugin().getEconomy() != null) {
                    if (getMain().getConfig().getInt("rewards.win") != 0) {
                        for(Player p : redTeam){
                            getMain().otherPluginSupport.getVaultPlugin().getEconomy().depositPlayer(p, getMain().getConfig().getDouble("rewards.win"));
                        }
                    }
                }
                for(Player p : redTeam){
                    getMain().wagers.loseWager(p);
                }
                restart(false);
            }else if (blueTeam.size() == 0) {
                Bukkit.broadcastMessage(getNAME() +" §7"+ Message.BLUE_TEAM.getMessage()+" §7" + Message.WINS_THE_GAME.getMessage());
                if (getMain().otherPluginSupport.getVaultPlugin().getEconomy() != null) {
                    if (getMain().getConfig().getInt("rewards.win") != 0) {
                        for(Player p : blueTeam){
                            getMain().otherPluginSupport.getVaultPlugin().getEconomy().depositPlayer(p, getMain().getConfig().getDouble("rewards.win"));
                        }
                    }
                }
                for(Player p : blueTeam){
                    getMain().wagers.loseWager(p);
                }
                restart(false);
            }
            if (getPlayerInGame().size() == 0) {
                restart(false);
            }
            for (int i = 0; i < getPlayerInGame().size(); i++) {
                Player p = getPlayerInGame().get(i);
                if (p == null) {
                    break;
                }
                if (p.getLocation().getBlock().getType() == Material.LAVA || p.getLocation().getBlock().getType() == Material.STATIONARY_LAVA ||
                        p.getLocation().getBlock().getType() == Material.WATER || p.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                    if(redTeam.contains(p)){
                        sendMessage(getNAME() + " §4" + p.getName() + " §c" + Message.FELL_INTO_THE_LAVA.getMessage());
                    }else if(blueTeam.contains(p)){
                        sendMessage(getNAME() + " §3" + p.getName() + " §c" + Message.FELL_INTO_THE_LAVA.getMessage());
                    }
                    getMain().spleefs.removePlayer(p);
                    if (getMain().otherPluginSupport.getVaultPlugin().getEconomy() != null) {
                        if (getMain().getConfig().getInt("rewards.lose") != 0) {
                            getMain().otherPluginSupport.getVaultPlugin().getEconomy().depositPlayer(getPlayerInGame().get(0), getMain().getConfig().getDouble("rewards.lose"));
                        }
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(getMain().getSpleefPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            p.setHealth(20);
                            p.setFoodLevel(20);
                        }
                    }, 40);
                }
            }
        }
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
            getScoreboardSign().getBlueTeam().removePlayer(p);
            getScoreboardSign().getRedTeam().removePlayer(p);
            if(getMain().getConfig().getBoolean("scoreboard.enable")){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            redTeam.remove(p);
            blueTeam.remove(p);
            p.setGlowing(false);
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
        this.runNormalStart();
        boolean reverse = false;
        for(Player p : getPlayerInGame()){
            if(reverse){
                getScoreboardSign().getBlueTeam().addPlayer(p);
                p.setVelocity(new Vector(1.5F, 0.5F, 1.5F));
                blueTeam.add(p);
                p.setGlowing(getMain().getConfig().getBoolean("gameMode.team.glowing"));
            }else{
                getScoreboardSign().getRedTeam().addPlayer(p);
                p.setVelocity(new Vector(-1.5F, 0.5F, -1.5F));
                redTeam.add(p);
                p.setGlowing(getMain().getConfig().getBoolean("gameMode.team.glowing"));
            }
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            reverse = !reverse;
        }
        updateScoreboards();
    }

    @Override
    public void restart(boolean notOnDisable) {
        this.runNormalRestart(notOnDisable);
        for(List<Player> list : teams){
            list.clear();
        }
    }

    @Override
    public void updateScoreboards() {
        getScoreboardSign().clearLines();
        int count = 0;
        for(Player p : getPlayerInGame()){
            if(redTeam.contains(p)){
                getScoreboardSign().setLine(count, "§c"+p.getName());
            }else{
                getScoreboardSign().setLine(count, "§3"+p.getName());
            }
            count++;
        }
    }

    @Override
    public boolean postShowTime(int timeInSecond) {
        return true;
    }

    @Override
    public List<List<Player>> teams() {
        return this.teams;
    }

    @Override
    public List<Player> redTeam() {
        return this.redTeam;
    }

    @Override
    public List<Player> blueTeam() {
        return this.blueTeam;
    }

    @Override
    public List<Player> greenTeam() {
        return Lists.newArrayList();
    }

    @Override
    public List<Player> yellowTeam() {
        return Lists.newArrayList();
    }
}
