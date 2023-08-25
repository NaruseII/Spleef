package fr.naruse.spleef.spleef.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.api.MathUtils;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.BlockBuffer;
import fr.naruse.spleef.utils.ScoreboardSign;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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
    protected BonusManager bonusManager;

    protected int time;
    protected ScoreboardSign scoreboardSign;
    protected final List<Player> playerInGame = Lists.newArrayList();
    protected final List<Player> spectators = Lists.newArrayList();
    protected final List<Sign> signs = Lists.newArrayList();
    protected final List<Block> blocks = Lists.newArrayList();
    protected final HashMap<Player, Block> lastPlayerBlock = Maps.newHashMap();
    protected final HashMap<Player, Integer> lastPlayerBlockTime = Maps.newHashMap();
    protected GameStatus currentStatus = GameStatus.WAIT;

    public Spleef(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby, boolean sheepBonusEnabled) {
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
        scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboard.scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), time+""}));

        if(sheepBonusEnabled){
            this.bonusManager = new BonusManager(pl, this);
        }

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
                }
            }else{
                time = pl.getConfig().getInt("timer.start");
            }
            if(pl.getConfig().getBoolean("noScoreboard")){
                if((time % 10 == 0 || time <= 5) && time != pl.getConfig().getInt("timer.start")){
                    sendMessage(pl.getMessageManager().get("gameStartsIn", new String[]{"time"}, new String[]{time+""}));
                }
            }else{
                scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboard.scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), time+""}));
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
                                    for (Block block : MathUtils.get2DCircleBlock(p.getLocation().getBlock().getRelative(0, -1, 0).getLocation(), j)) {
                                        removeBlockUnderFoot(block);
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
        scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboard.scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), ""}));
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));

        for (Player player : playerInGame) {
            if(pl.getConfig().getBoolean("randomSpawn")){
                player.teleport(getRandomLocationFrom(arena.clone()));
            }else{
                player.teleport(arena.clone());
                Vector vector = new Vector(Utils.RANDOM.nextDouble(), Utils.RANDOM.nextDouble(), Utils.RANDOM.nextDouble());
                if(Utils.RANDOM.nextBoolean()){
                    vector.setX(-vector.getX());
                }
                if(Utils.RANDOM.nextBoolean()){
                    vector.setZ(-vector.getZ());
                }
                player.setVelocity(vector);
            }
        }
        if(!pl.getConfig().getBoolean("instantGiveShovel")){
            sendMessage(getFullName()+" "+pl.getMessageManager().get("spadeDeliverIn"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> giveItems(), 20*5);
        }else{
            giveItems();
        }
    }

    protected void giveItems() {
        for (int i = 0; i < playerInGame.size(); i++) {
            playerInGame.get(i).getInventory().addItem(Utils.SPADE_ITEM);
            if(pl.getConfig().getBoolean("snowballs")){
                playerInGame.get(i).getInventory().addItem(Utils.SNOWBALL.clone());
            }
        }
    }

    public void disable(){
        restart();
        stop();
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
        if(bonusManager != null){
            bonusManager.cancel();
        }
    }

    public void restart(){
        List<Player> list = Lists.newArrayList();
        list.addAll(playerInGame);
        list.addAll(spectators);
        for(Player p : list){
            removePlayer(p);
        }
        playerInGame.clear();
        spectators.clear();

        for (Block block : blocks) {
            block.setType(Material.SNOW_BLOCK);
        }
        blocks.clear();

        currentStatus = GameStatus.WAIT;
        if(bonusManager != null){
            bonusManager.restart();
        }
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
        if(playerInGame.size() >= max && !forceJoin){
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("gameFull"));
            return false;
        }
        if(pl.getSpleefPlayerRegistry().getSpleefPlayer(p).hasSpleef()){
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("youHaveAGame"));
            return false;
        }
        if(pl.getConfig().getBoolean("joinWorldLock") && !p.getWorld().getName().equals(this.arena.getWorld().getName())) {
            p.sendMessage(getFullName() +" "+ pl.getMessageManager().get("notSameWorld"));
            return false;
        }

        playerInGame.add(p);

        sendMessage(getFullName() +" "+ pl.getMessageManager().get("joinSpleef", new String[]{"name"}, new String[]{p.getName()}));
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
        spleefPlayer.registerData(p);
        spleefPlayer.setLastLocation(p.getLocation());
        p.setGameMode(GameMode.SURVIVAL);
        p.getInventory().clear();
        p.getInventory().setHeldItemSlot(0);
        p.getInventory().setItem(8, Utils.LEAVE_ITEM);
        updateSigns();
        updateScoreboards();
        p.setInvulnerable(true);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());
        p.setFlying(false);
        p.setAllowFlight(false);
        if(lobby != null){
            p.teleport(lobby);
        }
        if(!pl.getConfig().getBoolean("noScoreboard")){
            p.setScoreboard(scoreboardSign.getScoreboard());
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
        boolean isInGame = playerInGame.contains(p);
        boolean isSpectator = spectators.contains(p);
        if(isInGame){
            sendMessage(getFullName() +" "+ pl.getMessageManager().get("leaveSpleef", new String[]{"name"}, new String[]{p.getName()}));
        }
        playerInGame.remove(p);
        p.getInventory().clear();
        p.updateInventory();
        updateSigns();
        updateScoreboards();
        if(!pl.getConfig().getBoolean("noScoreboard")){
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        p.setVelocity(new Vector());
        p.setFallDistance(0f);

        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);

        if(pl.getConfig().getBoolean("tpToLastLoc")){
            p.teleport(spleefPlayer.getLastLocation());
        }else{
            p.teleport(spawn);
        }

        p.setInvulnerable(false);
        p.setFoodLevel(20);
        p.setHealth(p.getMaxHealth());

        spleefPlayer.setCurrentSpleef(null);
        spleefPlayer.giveBackData(p);
        if(currentStatus == GameStatus.GAME && isSpectator){
            spleefPlayer.incrementStatistic(StatisticType.LOSE, 1);
            spleefPlayer.saveStatistics();
            if(pl.getVaultManager() != null){
                pl.getVaultManager().giveLooseReward(p);
            }
            if(pl.getConfig().contains("reward.looseCommand")){
                String cmd = pl.getConfig().getString("reward.looseCommand").substring(1).replace("{player}", p.getName());
                pl.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
        spectators.remove(p);
        if(isInGame){
            checkWin();
        }
    }

    public void updateScoreboards(){
        scoreboardSign.clearLines();
        for (int i = 0; i < playerInGame.size(); i++) {
            scoreboardSign.setLine(0, pl.getMessageManager().get("scoreboard.scoreboardFormat", new String[]{"name"}, new String[]{playerInGame.get(i).getName()}));
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
        if(ChatColor.stripColor(sign.getLine(0)).equals(ChatColor.stripColor(getFullName()))){
            if(!signs.contains(sign)){
                signs.add(sign);
            }
        }
        updateSigns();
    }

    public void updateSigns() {
        for(Sign sign : signs){
            if(sign.getChunk().isLoaded()){
                updateSign(sign);
            }
        }
    }

    public void updateSign(Sign sign){
        if(sign.getChunk().isLoaded()){
            sign.getChunk().load();
        }
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
        SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);

        if(!isSpectatorEnabled()){

            p.getInventory().clear();
            p.updateInventory();
            updateSigns();
            updateScoreboards();
            if(!pl.getConfig().getBoolean("noScoreboard")){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            p.setVelocity(new Vector());
            p.setFallDistance(0f);

            if(pl.getConfig().getBoolean("tpToLastLoc")){
                p.teleport(spleefPlayer.getLastLocation());
            }else{
                p.teleport(spawn);
            }

            spleefPlayer.setCurrentSpleef(null);
            spleefPlayer.giveBackData(p);

            p.setInvulnerable(false);
            p.setFireTicks(0);
            p.setFoodLevel(20);
            p.setHealth(p.getMaxHealth());
            if(pl.getVaultManager() != null){
                pl.getVaultManager().giveLooseReward(p);
            }
            if(pl.getConfig().contains("reward.looseCommand")){
                String cmd = pl.getConfig().getString("reward.looseCommand").substring(1).replace("{player}", p.getName());
                pl.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }else{
            spectators.add(p);
            p.teleport(arena);
            p.setGameMode(GameMode.SPECTATOR);
        }

        spleefPlayer.incrementStatistic(StatisticType.LOSE, 1);
        spleefPlayer.saveStatistics();

        checkWin();
    }

    public void checkWin() {
        if(playerInGame.size() == 1 && currentStatus == GameStatus.GAME && min > 1){
            Player p = playerInGame.get(0);
            if(pl.getConfig().getBoolean("broadcastWin")){
                Bukkit.broadcastMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{p.getName()}));
            }else if(pl.getConfig().getBoolean("broadcastWinWorld")){
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if(onlinePlayer.getWorld().getName().equals(spawn.getWorld().getName())){
                        onlinePlayer.sendMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{p.getName()}));
                    }
                }
            }else{
                p.sendMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{p.getName()}));
            }
            playerInGame.remove(p);
            p.getInventory().clear();
            p.updateInventory();
            updateSigns();
            updateScoreboards();
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(p);
            if (pl.getConfig().getBoolean("tpToLastLoc")) {
                p.teleport(spleefPlayer.getLastLocation());
              } else {
                p.teleport(this.spawn);
              } 
            p.setInvulnerable(false);
            spleefPlayer.setCurrentSpleef(null);
            spleefPlayer.incrementStatistic(StatisticType.WIN, 1);
            spleefPlayer.saveStatistics();
            spleefPlayer.giveBackData(p);
            if(pl.getVaultManager() != null){
                pl.getVaultManager().giveWinReward(p);
            }
            if(pl.getConfig().contains("reward.winCommand")){
                String cmd = pl.getConfig().getString("reward.winCommand").substring(1).replace("{player}", p.getName());
                pl.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            if(Utils.WIN_ITEM != null){
                p.getInventory().addItem(Utils.WIN_ITEM);
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

    public int getMax() {
        return max;
    }

    public int getId() {
        return id;
    }

    public List<Player> getPlayerInGame() {
        return playerInGame;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public boolean isOpened() {
        return isOpened;
    }

    private int getBlockStandingTime(){
        return pl.getConfig().getInt("timer.blockStanding");
    }

    private boolean isSpectatorEnabled(){
        return pl.getConfig().getBoolean("spectator");
    }

    private int getSnowballCooldown(){
        return pl.getConfig().getInt("snowballCooldown");
    }

    public BonusManager getBonusManager() {
        return bonusManager;
    }

    public Location getArena() {
        return arena;
    }

    public Location getSpawn() {
        return spawn;
    }

    public Location getLobby() {
        return lobby;
    }

    public int getCurrentTimer() {
        return time;
    }

    protected Location getRandomLocationFrom(Location arena) {
        Location location = arena.clone();
        int needToUp = 0;
        for (int i = 0; i < 10; i++) {
            if(location.getBlock().getType() != Material.SNOW_BLOCK){
                needToUp++;
                location.add(0, -1, 0);
            }
        }
        if(location.getBlock().getType() != Material.SNOW_BLOCK){
            pl.getLogger().log(Level.WARNING, "Can't find any snow blocks on arena location !");
            return arena.clone();
        }
        for (int i = 0; i < Utils.RANDOM.nextInt(50)+20; i++) {
            Location newLoc = location.clone().add(Utils.RANDOM.nextBoolean() ? 1 : -1, 0, Utils.RANDOM.nextBoolean() ? 1 : -1);
            if(newLoc.getBlock().getType() == Material.SNOW_BLOCK){
                location = newLoc;
            }
        }

        location.add(0, needToUp, 0);
        return location;
    }

    public void destroyBlock(Player p, Block b){
        this.destroyBlock(p, b, false);
    }

    public void destroyBlock(Player p, Block b, boolean fromSheep){
        Runnable runnable = () -> {
            blocks.add(b);
            b.setType(Material.AIR);
            if(p != null && pl.getConfig().getBoolean("snowballs")){
                if(fromSheep && !pl.getConfig().getBoolean("snowballSheepBreakBlock")){
                    return;
                }
                this.giveSnowball(p);
            }
        };
        if(Bukkit.isPrimaryThread()){
            runnable.run();
        }else{
            Bukkit.getScheduler().runTask(pl, runnable);
        }
    }

    public void destroyBlock(Player p, BlockBuffer blockBuffer){
        this.destroyBlock(p, blockBuffer, 0);
    }

    public void destroyBlock(Player p, BlockBuffer blockBuffer, boolean fromSheep){
        this.destroyBlock(p, blockBuffer, 0, fromSheep);
    }

    public void destroyBlock(Player p, BlockBuffer blockBuffer, int tickDelay){
        this.destroyBlock(p, blockBuffer, tickDelay, false);
    }

    public void destroyBlock(Player p, BlockBuffer blockBuffer, int tickDelay, boolean fromSheep){
        Runnable runnable = () -> {
            for (Block b : blockBuffer) {
                blocks.add(b);
                b.setType(Material.AIR);
                if(p != null && pl.getConfig().getBoolean("snowballs")){
                    if(fromSheep && !pl.getConfig().getBoolean("snowballSheepBreakBlock")){
                        continue;
                    }
                    this.giveSnowball(p);
                }
            }
        };
        if(Bukkit.isPrimaryThread() && tickDelay == 0){
            runnable.run();
        }else{
            if(tickDelay == 0){
                Bukkit.getScheduler().runTask(pl, runnable);
            }else{
                Bukkit.getScheduler().runTaskLater(pl, runnable, tickDelay);
            }
        }
    }

    protected void removeBlockUnderFoot(Block block) {
        if (block.getType() == Material.SNOW_BLOCK) {
            blocks.add(block);
            block.setType(Material.AIR);
            if(pl.getConfig().getBoolean("lightnings")){
                block.getWorld().strikeLightningEffect(block.getLocation());
            }
        }
    }

    protected void giveSnowball(Player p){
        int amount = 0;
        for (ItemStack itemStack : p.getInventory()) {
            if(itemStack != null && itemStack.getType() == Material.SNOW_BALL){
                amount += itemStack.getAmount();
            }
        }

        if(amount >= pl.getConfig().getInt("snowballMaxAmount")){
            return;
        }
        p.getInventory().addItem(Utils.SNOWBALL.clone());
    }

    @EventHandler
    public void chunkLoad(ChunkLoadEvent e){
        for(BlockState state : e.getChunk().getTileEntities()){
            if(state instanceof Sign){
                Sign sign = (Sign) state;
                if(sign.getLine(0).equals(getFullName())){
                    if(!signs.contains(sign)){
                        signs.add(sign);
                        updateSign(sign);
                    }
                }
            }
        }
    }

    @EventHandler
    public void shoot(ProjectileHitEvent e){
        if(e.getEntity() instanceof Snowball && e.getEntity().getShooter() instanceof Player){
            Player p = (Player) e.getEntity().getShooter();
            if(!hasPlayer(p) || currentStatus == GameStatus.WAIT){
                return;
            }
            this.giveSnowball(p);
            if(e.getHitBlock() != null && e.getHitBlock().getType() == Material.SNOW_BLOCK){
                Block block = e.getHitBlock();
                blocks.add(block);
                block.setType(Material.AIR);
            }
        }
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
        if(!Utils.SPADE_ITEM.equals(e.getPlayer().getEquipment().getItemInMainHand()) && !Utils.SPADE_ITEM.equals(e.getPlayer().getEquipment().getItemInOffHand())){
            e.setCancelled(true);
            return;
        }
        if(e.getBlock().getType() == Material.SNOW_BLOCK && currentStatus == GameStatus.GAME){
            e.setDropItems(false);
            this.destroyBlock(p, e.getBlock());
        }else{
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void craftEvent(CraftItemEvent e){
        if(e.getWhoClicked() instanceof Player && hasPlayer((Player) e.getWhoClicked())){
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

    @EventHandler
    public void fly(PlayerToggleFlightEvent e){
        if(hasPlayer(e.getPlayer()) && !e.getPlayer().hasPermission("spleef.help")){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void worldSwitch(PlayerChangedWorldEvent e){
        Player p = e.getPlayer();
        if(this.playerInGame.contains(p) && pl.getConfig().getBoolean("joinWorldLock") && !p.getWorld().getName().equals(this.arena.getWorld().getName())) {
            World world = e.getPlayer().getWorld();
            Location location = e.getPlayer().getLocation();

            if(currentStatus == GameStatus.GAME){
                this.makeLose(p);
            }else{
                this.removePlayer(p);
            }

            if(!p.getWorld().getName().equals(world.getName())){
                Bukkit.getScheduler().runTaskLater(this.pl, () -> p.teleport(location), 20*3);
            }
        }
    }

    private HashMap<Player, Long> snowballCooldownMap = Maps.newHashMap();

    @EventHandler
    public void throwEvent(ProjectileLaunchEvent e){
        if(e.getEntity() instanceof Snowball && playerInGame.contains(e.getEntity().getShooter()) && getSnowballCooldown() != 0){
            Player p = (Player) e.getEntity().getShooter();
            if(snowballCooldownMap.containsKey(p)){
                long millis = System.currentTimeMillis()-snowballCooldownMap.get(p);
                if(millis < getSnowballCooldown()*1000){
                    e.setCancelled(true);
                    return;
                }
            }
            snowballCooldownMap.put(p, System.currentTimeMillis());
        }
    }

    public List<Block> getDestroyedBlocks() {
        return this.blocks;
    }
}
