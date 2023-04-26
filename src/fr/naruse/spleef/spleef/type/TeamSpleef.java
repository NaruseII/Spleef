package fr.naruse.spleef.spleef.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamSpleef extends Spleef {

    private static final Map<Integer, ChatColor> chatColorByOrdinal = Maps.newHashMap();
    static {
        for (ChatColor value : ChatColor.values()) {
            chatColorByOrdinal.put(value.ordinal(), value);
        }
    }

    private Team[] teams;
    private Map<Player, Team> playerTeamMap = Maps.newHashMap();

    public TeamSpleef(SpleefPlugin pl, int id, String name, boolean isOpened, int max, int min, Location arena, Location spawn, Location lobby, int teamCount, boolean sheepBonusEnabled) {
        super(pl, id, name, isOpened, max, min, arena, spawn, lobby, sheepBonusEnabled);
        this.teams = new Team[teamCount];

        List<DyeColor> dyeColors = Lists.newArrayList(DyeColor.values());
        Collections.shuffle(dyeColors);

        List<BlockFace> faces = Lists.newArrayList(BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST);
        Collections.shuffle(faces);

        for (int i = 0; i < this.teams.length; i++) {
            DyeColor dyeColor = dyeColors.get(i);
            this.teams[i] = new Team(dyeColor, faces.get(i), chatColorByOrdinal.get(dyeColor.ordinal()));
        }
    }

    @Override
    public void start() {
        scoreboardSign.getObjective().setDisplayName(pl.getMessageManager().get("scoreboard.scoreboardName", new String[]{"name", "time"}, new String[]{getFullName(), ""}));
        time = pl.getConfig().getInt("timer.start");
        currentStatus = GameStatus.GAME;
        sendMessage(getFullName()+" "+pl.getMessageManager().get("gameStarts"));

        for (Player player : playerInGame) {
            Team team = this.findTeam(player);

            player.teleport(arena.clone());
            Vector vector = new Vector(1+Utils.RANDOM.nextDouble(), Utils.RANDOM.nextDouble(), 1+Utils.RANDOM.nextDouble());
            if(team.getBlockFace() == BlockFace.NORTH){
                vector.setX(-vector.getX());
            }
            if(team.getBlockFace() == BlockFace.WEST){
                vector.setZ(-vector.getZ());
            }
            player.setVelocity(vector);
        }
        if(!pl.getConfig().getBoolean("instantGiveShovel")){
            sendMessage(getFullName()+" "+pl.getMessageManager().get("spadeDeliverIn"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> giveItems(), 20*5);
        }else{
            giveItems();
        }

        for (Team team : this.teams) {
            team.giveStuff();
        }
    }

    @Override
    public void restart() {
        super.restart();
        for (Team team : this.teams) {
            team.getPlayers().clear();
        }
        this.playerTeamMap.clear();
    }

    @Override
    public void makeLose(Player p) {
        Team team = this.playerTeamMap.get(p);
        if(team != null){
            team.getPlayers().remove(p);
        }
        super.makeLose(p);
    }

    @Override
    public void checkWin() {
        Team team = null;
        for (Team t : this.teams) {
            if(t.getPlayers().size() != 0){
                if(team == null){
                    team = t;
                }else{
                    team = null;
                    break;
                }
            }
        }
        if(team != null && currentStatus == GameStatus.GAME && min > 1){

            String winners = team.getChatColor()+team.getPlayers().stream().map(player -> player.getName()).collect(Collectors.joining(", "));

            if(pl.getConfig().getBoolean("broadcastWin")){
                Bukkit.broadcastMessage(getFullName()+" "+pl.getMessageManager().get("playersWins", new String[]{"name"}, new String[]{winners}));
            }else if(pl.getConfig().getBoolean("broadcastWinWorld")){
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if(onlinePlayer.getWorld().getName().equals(spawn.getWorld().getName())){
                        onlinePlayer.sendMessage(getFullName()+" "+pl.getMessageManager().get("playerWins", new String[]{"name"}, new String[]{winners}));
                    }
                }
            }else{
                for (Player p : team.getPlayers()) {
                    p.sendMessage(getFullName()+" "+pl.getMessageManager().get("playersWins", new String[]{"name"}, new String[]{winners}));
                }
            }

            for (Player p : team.getPlayers()) {
                playerInGame.remove(p);
                p.getInventory().clear();
                p.updateInventory();
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
            }

            updateSigns();
            updateScoreboards();

            restart();
        }
        if(playerInGame.size() == 0){
            restart();
        }
    }


    @Override
    protected String getSignLine(String path) {
        return pl.getMessageManager().get("sign.team."+path, new String[]{"name", "size", "max", "min", "missing"},
                new String[]{getFullName(), playerInGame.size()+"", max+"", min+"", (min-playerInGame.size())+""});
    }

    private Team findTeam(Player player) {
        List<Team> list = Lists.newArrayList(this.teams);
        Collections.sort(list);
        Team team = list.get(0);
        team.getPlayers().add(player);
        this.playerTeamMap.put(player, team);
        return team;
    }

    private class Team implements Comparable<Team> {

        private final List<Player> players = Lists.newArrayList();
        private final DyeColor color;
        private final BlockFace blockFace;
        private final ChatColor chatColor;

        public Team(DyeColor color, BlockFace blockFace, ChatColor chatColor) {
            this.color = color;
            this.blockFace = blockFace;
            this.chatColor = chatColor;
        }

        public void giveStuff(){
            ItemStack helmet = this.buildItem(Material.LEATHER_HELMET);
            ItemStack chestplate = this.buildItem(Material.LEATHER_CHESTPLATE);
            ItemStack leggings = this.buildItem(Material.LEATHER_LEGGINGS);
            ItemStack boots = this.buildItem(Material.LEATHER_BOOTS);
            for (Player player : players) {
                player.getEquipment().setHelmet(helmet);
                player.getEquipment().setChestplate(chestplate);
                player.getEquipment().setLeggings(leggings);
                player.getEquipment().setBoots(boots);
            }
        }

        private ItemStack buildItem(Material material){
            ItemStack itemStack = new ItemStack(material);
            LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            itemMeta.setColor(color.getColor());
            itemMeta.setUnbreakable(true);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public BlockFace getBlockFace() {
            return blockFace;
        }

        public DyeColor getColor() {
            return color;
        }

        public ChatColor getChatColor() {
            return chatColor;
        }

        @Override
        public int compareTo(Team o) {
            return this.players.size() > o.getPlayers().size() ? 1 : this.players.size() == o.getPlayers().size() ? 0 : -1;
        }
    }

}
