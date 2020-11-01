package fr.naruse.spleef.spleef;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.utils.ScoreboardSign;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

public class Spleef extends BukkitRunnable implements Listener {
    protected final SpleefPlugin pl;
    protected final int id;
    protected final String name;
    protected boolean isOpened;
    protected final int max;
    protected final int min;
    protected final Location arena;
    protected final Location spawn;
    protected final Location lobby;

    protected int time;
    protected ScoreboardSign scoreboardSign;
    protected final List<Player> playerInGame = Lists.newArrayList();
    protected final List<Sign> signs = Lists.newArrayList();
    protected final List<Block> blocks = Lists.newArrayList();
    protected final HashMap<Player, Block> lastPlayerBlock = Maps.newHashMap();
    protected final HashMap<Player, Integer> lastPlayerBlockTime = Maps.newHashMap();
    protected GameStatus currentStatus = GameStatus.WAIT;

    public Spleef(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby) {
        this.pl = pl;
        this.id = id;
        this.name = name;
        this.isOpened = isOpened;
        this.max = max;
        this.min = min;
        this.arena = arena;
        this.spawn = spawn;
        this.lobby = lobby;

        this.scoreboardSign = new ScoreboardSign();
        this.time = pl.getConfig().getInt("timer.start");
        scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), time+""}));

        this.runTaskTimer(pl, 20, 20);
    }

    @Override
    public void run() {
        if(currentStatus == GameStatus.WAIT){
            if(playerInGame.size() >= min){
                if(time <= 0){
                    start();
                }else{
                    time--;
                    scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), time+""}));
                }
            }else{
                time = pl.getConfig().getInt("timer.start");
            }
        }else{
            if(playerInGame.size() == 0){
                restart();
                return;
            }
            for (int i = 0; i < playerInGame.size(); i++) {
                Player p = playerInGame.get(i);
                if(pl.getConfig().getBoolean("standingLimit")){
                    if(!lastPlayerBlock.containsKey(p)){
                        lastPlayerBlock.put(p, p.getLocation().getBlock());
                        lastPlayerBlockTime.put(p, getBlockStandingTime());
                    }else{
                        if(Utils.areLocationsEquals(lastPlayerBlock.get(p).getLocation(), p.getLocation().getBlock().getLocation())){
                            int time = lastPlayerBlockTime.get(p);
                            if(time <= 0){
                                for (int j = 0; j < 3; j++) {
                                    for (Block block : Utils.getCircle(p.getLocation().getBlock().getRelative(0, -1, 0).getLocation(), j)) {
                                        if (block.getType() == Material.SNOW_BLOCK) {
                                            blocks.add(block);
                                            block.setType(Material.AIR);
                                            if(pl.getConfig().getBoolean("lightnings")){
                                                block.getWorld().strikeLightningEffect(block.getLocation());
                                            }
                                        }
                                    }
                                }
                            }else{
                                if(time == getBlockStandingTime()-3){
                                    p.sendMessage(getFullName()+" "+pl.getMessageManager().get("dontStayOnTheSameBlock"));
                                }
                                lastPlayerBlockTime.put(p, time-1);
                            }
                        }else{
                            lastPlayerBlock.put(p, p.getLocation().getBlock());
                            lastPlayerBlockTime.put(p, getBlockStandingTime());
                        }
                    }
                }
                if(p.getLocation().getBlock().getType().name().contains("WATER") || p.getLocation().getBlock().getType().name().contains("LAVA")){
                    makeLose(p);
                }
            }
        }
    }

    public void start() {
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));
        for (Player player : playerInGame) {
            player.teleport(getRandomLocationFrom(arena));
        }
        sendMessage(getFullName()+" "+pl.getMessageManager().get("spadeDeliverIn"));
        Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> {
            for (int i = 0; i < playerInGame.size(); i++) {
                playerInGame.get(i).getInventory().addItem(Utils.SPADE_ITEM);
            }
        }, 20*5);
    }

    public void disable(){
        stop();
        restart();
        for(Sign sign : signs){
            sign.setLine(0, getFullName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }
    }

    public void stop(){
        if(!isCancelled()){
            cancel();
        }
    }

    public void restart(){
        List<Player> list = Lists.newArrayList();
        for(Player p : playerInGame){
            list.add(p);
        }
        for(Player p : list){
            removePlayer(p);
        }
        playerInGame.clear();

        for (Block block : blocks) {
            block.setType(Material.SNOW_BLOCK);
        }
        blocks.clear();

        currentStatus = GameStatus.WAIT;

        updateSigns();
    }

    public boolean addPlayer(Player p, boolean forceJoin){
        if(currentStatus == GameStatus.GAME && !forceJoin) {
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("inGame"));
            return false;
        }
        if(!isOpened && !forceJoin){
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("closed"));
            return false;
        }
        if(pl.getSpleefPlayerRegistry().getSpleefPlayer(p).hasSpleef()){
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("youHaveAGame"));
            return false;
        }

        p.setScoreboard(scoreboardSign.getScoreboard());
        playerInGame.add(p);

        sendMessage(getFullName() +" "+ pl.getMessageManager().get("joinSpleef", new String[]{"name"}, new String[]{p.getName()}));
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        spleefPlayer.registerInventory(p);
        p.getInventory().clear();
        p.getInventory().setHeldItemSlot(0);
        p.getInventory().setItem(8, Utils.LEAVE_ITEM);
        updateSigns();
        updateScoreboards();
        p.setInvulnerable(true);
        p.setGameMode(GameMode.SURVIVAL);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());
        if(lobby != null){
            p.teleport(lobby);
        }
        pl.getSpleefPlayerRegistry().getSpleefPlayer(p).setCurrentSpleef(this);
        if(currentStatus == GameStatus.GAME){
            p.teleport(arena);
            p.getInventory().addItem(Utils.SPADE_ITEM);
            Vector vector = new Vector(0, 3, 0);
            p.setVelocity(vector);
        }
        return true;
    }

    public void removePlayer(Player p) {
        if(!playerInGame.contains(p)){
            return;
        }
        sendMessage(getFullName() +" "+ pl.getMessageManager().get("leaveSpleef", new String[]{"name"}, new String[]{p.getName()}));
        playerInGame.remove(p);
        p.getInventory().clear();
        p.updateInventory();
        updateSigns();
        updateScoreboards();
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        p.setVelocity(new Vector());
        p.teleport(spawn);
        p.setInvulnerable(false);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        spleefPlayer.setCurrentSpleef(null);
        spleefPlayer.setPlayerInventory(p);
        if(currentStatus == GameStatus.GAME){
            spleefPlayer.incrementStatistic(StatisticType.LOSE, 1);
            spleefPlayer.saveStatistics();
            if(pl.getVaultManager() != null){
                pl.getVaultManager().giveLooseReward(p);
            }
            checkWin();
        }
    }

    public void updateScoreboards(){
        scoreboardSign.clearLines();
        for (int i = 0; i < playerInGame.size(); i++) {
            scoreboardSign.setLine(0, pl.getMessageManager().get("scoreboardFormat", new String[]{"name"}, new String[]{playerInGame.get(i).getName()}));
        }
    }

    public void registerNewSigns(World world) {
        for(Chunk c : world.getLoadedChunks()){
            for(BlockState state : c.getTileEntities()){
                if(state instanceof Sign){
                    Sign sign = (Sign) state;
                    if(sign.getLine(0).equals(getFullName())){
                        if(!signs.contains(sign)){
                            signs.add(sign);
                        }
                    }
                }
            }
        }
        updateSigns();
    }

    public void registerSign(Sign sign) {
        if(sign.getLine(0).equals(getFullName())){
            if(!signs.contains(sign)){
                signs.add(sign);
            }
        }
        updateSigns();
    }

    public void updateSigns() {
        for(Sign sign : signs){
            if(!isOpened){
                sign.setLine(0, getSignLine("isClosed.line1"));
                sign.setLine(1, getSignLine("isClosed.line2"));
                sign.setLine(2, getSignLine("isClosed.line3"));
                sign.setLine(3, getSignLine("isClosed.line4"));
                sign.update();
            }else{
                if(currentStatus == GameStatus.WAIT){
                    sign.setLine(0, getSignLine("isOpened.isWaiting.line1"));
                    if(playerInGame.size() >= (int) (max*0.8)){
                        sign.setLine(1, getSignLine("isOpened.isWaiting.line2.1"));
                    }else if(playerInGame.size() >= (int) (max*0.6)){
                        sign.setLine(1, getSignLine("isOpened.isWaiting.line2.2"));
                    }else {
                        sign.setLine(1, getSignLine("isOpened.isWaiting.line2.3"));
                    }
                    if(playerInGame.size() >= min){
                        sign.setLine(2, getSignLine("isOpened.isWaiting.line3.1"));
                    }else{
                        sign.setLine(2, getSignLine("isOpened.isWaiting.line3.2"));
                    }
                    sign.setLine(3, getSignLine("isOpened.isWaiting.line4"));
                    sign.update();
                }else if(currentStatus == GameStatus.GAME){
                    sign.setLine(0, getSignLine("isOpened.isPlaying.line1"));
                    if(playerInGame.size() >= (int) (max*0.8)){
                        sign.setLine(1, getSignLine("isOpened.isPlaying.line2.1"));
                    }else if(playerInGame.size() >= (int) (max*0.6)){
                        sign.setLine(1, getSignLine("isOpened.isPlaying.line2.2"));
                    }else {
                        sign.setLine(1, getSignLine("isOpened.isPlaying.line2.3"));
                    }
                    sign.setLine(2, getSignLine("isOpened.isPlaying.line3"));
                    sign.setLine(3, getSignLine("isOpened.isPlaying.line4"));
                    sign.update();
                }
            }
        }
    }

    protected String getSignLine(String path){
        return pl.getMessageManager().get("sign.spleef."+path, new String[]{"name", "size", "max", "min", "missing"},
                new String[]{getFullName(), playerInGame.size()+"", max+"", min+"", (min-playerInGame.size())+""});
    }

    public void sendMessage(String msg){
        for (int i = 0; i < playerInGame.size(); i++) {
            playerInGame.get(i).sendMessage(msg);
        }
    }

    public void makeLose(Player p) {
        sendMessage(getFullName()+" "+pl.getMessageManager().get("playerFell", new String[]{"name"}, new String[]{p.getName()}));
        playerInGame.remove(p);
        p.getInventory().clear();
        p.updateInventory();
        updateSigns();
        updateScoreboards();
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        p.setVelocity(new Vector());
        p.teleport(spawn);
        p.setInvulnerable(false);
        p.setFireTicks(0);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());
        checkWin();
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        spleefPlayer.setCurrentSpleef(null);
        spleefPlayer.incrementStatistic(StatisticType.LOSE, 1);
        spleefPlayer.saveStatistics();
        spleefPlayer.setPlayerInventory(p);
        if(pl.getVaultManager() != null){
            pl.getVaultManager().giveLooseReward(p);
        }
    }

    public void checkWin() {
        if(playerInGame.size() == 1){
            Player p = playerInGame.get(0);
            if(pl.getConfig().getBoolean("broadcastWin")){
                Bukkit.broadcastMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{p.getName()}));
            }else{
                p.sendMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{p.getName()}));
            }
            playerInGame.remove(p);
            p.getInventory().clear();
            p.updateInventory();
            updateSigns();
            updateScoreboards();
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            p.teleport(spawn);
            p.setInvulnerable(false);
            SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
            spleefPlayer.setCurrentSpleef(null);
            spleefPlayer.incrementStatistic(StatisticType.WIN, 1);
            spleefPlayer.saveStatistics();
            spleefPlayer.setPlayerInventory(p);
            if(pl.getVaultManager() != null){
                pl.getVaultManager().giveWinReward(p);
            }
            restart();
        }else if(playerInGame.size() == 0){
            restart();
        }
    }

    public void open() {
        isOpened = true;
        updateSigns();
    }

    public void close() {
        isOpened = false;
        updateSigns();
    }

    public String getFullName(){
        return pl.getMessageManager().get("spleefTag")+name;
    }

    public String getName() {
        return name;
    }

    public boolean hasPlayer(Player p){
        return playerInGame.contains(p);
    }

    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    public int getMin() {
        return min;
    }

    public List<Player> getPlayerInGame() {
        return playerInGame;
    }

    private int getBlockStandingTime(){
        return pl.getConfig().getInt("timer.blockStanding");
    }

    private Location getRandomLocationFrom(Location arena) {
        Location location = arena.clone();
        boolean needToUp = false;
        if(location.getBlock().getType() != Material.SNOW_BLOCK){
            needToUp = true;
            location.add(0, -1, 0);
        }
        for (int i = 0; i < Utils.RANDOM.nextInt(50)+20; i++) {
            short flag1 = (short) (-2+Utils.RANDOM.nextInt(4));
            short flag2 = (short) (-2+Utils.RANDOM.nextInt(4));
            Location newLoc = location.clone().add(flag1, 0, flag2);
            if(newLoc.getBlock().getType() == Material.SNOW_BLOCK){
                location = newLoc;
            }
        }

        if(needToUp){
            location.add(0, 1, 0);
        }
        return location;
    }

    @EventHandler
    public void move(PlayerMoveEvent e){
        Player p = e.getPlayer();
        if(!hasPlayer(p) || currentStatus == GameStatus.WAIT){
            return;
        }
        if(p.getLocation().getBlock().getType().name().contains("WATER") || p.getLocation().getBlock().getType().name().contains("LAVA")){
            makeLose(p);
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if(!hasPlayer(p)){
            return;
        }
        if(e.getBlock().getType() == Material.SNOW_BLOCK && currentStatus == GameStatus.GAME){
            e.setDropItems(false);
            blocks.add(e.getBlock());
        }else{
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void foodChange(FoodLevelChangeEvent e){
        if(e.getEntity() instanceof Player && hasPlayer((Player) e.getEntity())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void drop(PlayerDropItemEvent e){
        if(hasPlayer(e.getPlayer())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pickUp(PlayerPickupItemEvent e){
        if(hasPlayer(e.getPlayer())){
            e.setCancelled(true);
        }
    }
}
