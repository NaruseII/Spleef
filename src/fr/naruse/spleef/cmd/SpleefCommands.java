package fr.naruse.spleef.cmd;

import com.google.common.collect.Lists;
import fr.naruse.api.NaruseAPIDownloader;
import fr.naruse.spleef.database.DatabaseSQLManager;
import fr.naruse.spleef.inventory.InventoryStatistics;
import fr.naruse.spleef.inventory.manager.InventoryManager;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.player.statistic.StatisticType;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.GameType;
import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.utils.SpleefUpdater;
import fr.naruse.spleef.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpleefCommands implements CommandExecutor {
    private SpleefPlugin pl;
    public SpleefCommands(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0){
            return help(sender, 1);
        }

        //JOIN
        if(args[0].equalsIgnoreCase("join")){
            Player p = null;
            if(args.length > 3 && sender.hasPermission("spleef.console.join")){
                p = Bukkit.getPlayer(args[2]);
            }
            if(p == null && !(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            if(p == null){
                p = (Player) sender;
            }

            if(args.length < 2){
                return help(sender, 1);
            }
            Spleef spleef = getSpleefByName(args[1]);
            if(spleef == null){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            spleef.addPlayer(p, false);
            return true;
        }

        //LEAVE
        if(args[0].equalsIgnoreCase("leave")){
            Player p = null;
            if(args.length > 3 && sender.hasPermission("spleef.console.join")){
                p = Bukkit.getPlayer(args[2]);
            }
            if(p == null && !(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            if(p == null){
                p = (Player) sender;
            }

            Spleef spleef = pl.getSpleefPlayerRegistry().getSpleefPlayer(p).getCurrentSpleef();
            if(spleef == null){
                return sendMessage(sender, "youAreNotInGame");
            }
            spleef.removePlayer(p);
            return true;
        }

        //STATS
        if(args[0].equalsIgnoreCase("stats")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            OfflinePlayer target = p;
            if(args.length > 1){
                target = Bukkit.getOfflinePlayer(args[1]);
            }
            if(target == null){
                return sendMessage(sender, "playerNotFound");
            }
            new InventoryStatistics(pl, p, target);
            return true;
        }

        //JOIN QUEUE
        if(args[0].equalsIgnoreCase("joinQueue")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            Spleef spleef = null;
            for (int i = 0; i < pl.getSpleefs().getSpleefs().size(); i++) {
                Spleef sp = pl.getSpleefs().getSpleefs().get(i);
                if(spleef == null || sp.getPlayerInGame().size() >= spleef.getPlayerInGame().size()){
                    if(sp.getMax() < sp.getPlayerInGame().size()){
                        spleef = sp;
                    }
                }
            }
            if(spleef == null && pl.getSpleefs().getSpleefs().size() != 0){
                spleef = pl.getSpleefs().getSpleefs().get(0);
            }
            if(spleef != null){
                p.performCommand("spleef join "+spleef.getName());
            }
            return true;
        }


        /// ADMIN
        if(!sender.hasPermission("spleef.help")){
            return sendMessage(sender, "youDontHaveThePermission");
        }
        if(args[0].equalsIgnoreCase("help")){
            int page = 1;
            if(args.length > 1){
                try{
                    page = Integer.valueOf(args[1]);
                }catch (Exception e){
                    return sendMessage(sender, "wrongNumber");
                }
            }
            return help(sender, page);
        }

        //CREATE
        if(args[0].equalsIgnoreCase("create")){
            if(args.length < 2){
                return help(sender, 1);
            }
            GameType gameType = GameType.SPLEEF;
            if(args.length > 2){
                try{
                    gameType = GameType.valueOf(args[2].toUpperCase());
                }catch (Exception e){
                    return help(sender, 1);
                }
            }
            int id = getNextIdAvailable();
            if(id == -1){
                return sendMessage(sender, "error");
            }
            if(getIdByName(args[1]) != -1){
                return sendMessage(sender, "alreadyExists");
            }
            pl.getConfig().set("spleef."+id+".name", args[1]);
            pl.getConfig().set("spleef."+id+".isOpened", true);
            pl.getConfig().set("spleef."+id+".gameType", gameType.name());
            pl.saveConfig();
            return sendMessage(sender, "spleefCreated");
        }

        //DELETE
        if(args[0].equalsIgnoreCase("delete")){
            if(args.length < 2){
                return help(sender, 1);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            pl.getConfig().set("spleef."+id, null);
            pl.saveConfig();
            return sendMessage(sender, "spleefDeleted");
        }

        //RELOAD
        if(args[0].equalsIgnoreCase("reload")){
            pl.getSpleefs().reload();
            if(pl.getVaultManager() != null){
                pl.getVaultManager().reload();
            }
            if(pl.getHolographicManager() != null){
                pl.getHolographicManager().reload();
            }
            pl.getConfigurations().reload();
            Utils.formatItems(pl);
            return sendMessage(sender, "reload");
        }

        //SET MIN
        if(args[0].equalsIgnoreCase("setMin")){
            if(args.length < 3){
                return help(sender, 1);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            int value;
            try{
                value = Integer.parseInt(args[2]);
            }catch (Exception e){
                return sendMessage(sender, "wrongNumber");
            }
            pl.getConfig().set("spleef."+id+".min", value);
            pl.saveConfig();
            return sendMessage(sender, "settingMinSaved");
        }

        //SET MAX
        if(args[0].equalsIgnoreCase("setMax")){
            if(args.length < 3){
                return help(sender, 1);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            int value;
            try{
                value = Integer.parseInt(args[2]);
            }catch (Exception e){
                return sendMessage(sender, "wrongNumber");
            }
            pl.getConfig().set("spleef."+id+".max", value);
            pl.saveConfig();
            return sendMessage(sender, "settingMaxSaved");
        }

        //SET LOCATION
        if(args[0].equalsIgnoreCase("setArena") || args[0].equalsIgnoreCase("setSpawn") || args[0].equalsIgnoreCase("setLobby")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            if(args.length < 2){
                return help(sender, 1);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }

            String location;
            if(args[0].equalsIgnoreCase("setArena")){
                location = ".arena";
            }else if(args[0].equalsIgnoreCase("setSpawn")){
                location = ".spawn";
            }else{
                location = ".lobby";
            }

            pl.getConfig().set("spleef."+id+".location"+location+".x", p.getLocation().getX());
            pl.getConfig().set("spleef."+id+".location"+location+".y", p.getLocation().getY());
            pl.getConfig().set("spleef."+id+".location"+location+".z", p.getLocation().getZ());
            pl.getConfig().set("spleef."+id+".location"+location+".yaw", p.getLocation().getYaw());
            pl.getConfig().set("spleef."+id+".location"+location+".pitch", p.getLocation().getPitch());
            pl.getConfig().set("spleef."+id+".location"+location+".world", p.getWorld().getName());
            pl.saveConfig();
            return sendMessage(sender, "settingLocationSaved"+location);
        }

        //OPEN
        if (args[0].equalsIgnoreCase("open")) {
            if(args.length < 2){
                return help(sender, 1);
            }
            Spleef spleef = getSpleefByName(args[1]);
            int id = getIdByName(args[1]);
            if(spleef == null || id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }

            pl.getConfig().set("spleef."+id+".isOpen", true);
            pl.saveConfig();
            spleef.open();
            return sendMessage(sender, "spleefOpened");
        }

        //CLOSE
        if (args[0].equalsIgnoreCase("close")) {
            if(args.length < 2){
                return help(sender, 1);
            }
            Spleef spleef = getSpleefByName(args[1]);
            int id = getIdByName(args[1]);
            if(spleef == null || id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }

            pl.getConfig().set("spleef."+id+".isOpen", false);
            pl.saveConfig();
            spleef.close();
            return sendMessage(sender, "spleefClosed");
        }

        //SET TIMER
        if(args[0].equalsIgnoreCase("setTimer")){
            if(args.length < 3){
                return help(sender, 2);
            }
            int number;
            try{
                number = Integer.parseInt(args[2]);
            }catch (Exception e){
                return sendMessage(sender, "wrongNumber");
            }
            if(args[1].equalsIgnoreCase("start")){
                pl.getConfig().set("timer.start", number);
                pl.saveConfig();
            }else if(args[1].equalsIgnoreCase("blockstanding")){
                pl.getConfig().set("timer.blockStanding", number);
                pl.saveConfig();
            }else{
                return help(sender, 1);
            }
            return sendMessage(sender, "timerChanged");
        }

        //LANG
        if(args[0].equalsIgnoreCase("setLang")){
            if(args.length < 2){
                return help(sender, 2);
            }
            if(args[1].equalsIgnoreCase("french")){
                pl.getMessageManager().setLang("french");
                pl.getConfig().set("currentLang", "french");
                pl.saveConfig();
                pl.getConfigurations().reset(0);
                pl.getConfigurations().reload();
                return sendMessage(sender, "langChanged");
            }else if(args[1].equalsIgnoreCase("english")){
                pl.getMessageManager().setLang("english");
                pl.getConfig().set("currentLang", "english");
                pl.saveConfig();
                pl.getConfigurations().reset(0);
                pl.getConfigurations().reload();
                return sendMessage(sender, "langChanged");
            }else if(args[1].equalsIgnoreCase("russian")){
                pl.getMessageManager().setLang("russian");
                pl.getConfig().set("currentLang", "russian");
                pl.saveConfig();
                pl.getConfigurations().reset(0);
                pl.getConfigurations().reload();
                return sendMessage(sender, "langChanged");
            }else if(args[1].equalsIgnoreCase("german")){
                pl.getMessageManager().setLang("german");
                pl.getConfig().set("currentLang", "german");
                pl.saveConfig();
                pl.getConfigurations().reset(0);
                pl.getConfigurations().reload();
                return sendMessage(sender, "langChanged");
            }else{
                return sendMessage(sender, "argumentNotFound", new String[]{"arg"}, new String[]{args[1]});
            }
        }

        //ENABLE
        if(args[0].equalsIgnoreCase("enable")){
            if(args.length < 2){
                return help(sender, 2);
            }

            //BROADCAST
            if(args[1].equalsIgnoreCase("BroadcastWin")){
                pl.getConfig().set("broadcastWin", !pl.getConfig().getBoolean("broadcastWin"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(BroadcastWin: "+pl.getConfig().getBoolean("broadcastWin")+")");
            }else if(args[1].equalsIgnoreCase("HolographicRanking")){
                pl.getConfig().set("holographicRanking", !pl.getConfig().getBoolean("holographicRanking"));
                pl.saveConfig();
                if(pl.getHolographicManager() != null){
                    pl.getHolographicManager().reload();
                }
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(HolographicRanking: "+pl.getConfig().getBoolean("holographicRanking")+")");
            }else if(args[1].equalsIgnoreCase("Lightnings")){
                pl.getConfig().set("lightnings", !pl.getConfig().getBoolean("lightnings"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Lightnings: "+pl.getConfig().getBoolean("lightnings")+")");
            }else if(args[1].equalsIgnoreCase("standingLimit")){
                pl.getConfig().set("standingLimit", !pl.getConfig().getBoolean("standingLimit"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(StandingLimit: "+pl.getConfig().getBoolean("standingLimit")+")");
            }else if(args[1].equalsIgnoreCase("tpToLastLoc")){
                pl.getConfig().set("tpToLastLoc", !pl.getConfig().getBoolean("tpToLastLoc"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(TpToLastLoc: "+pl.getConfig().getBoolean("tpToLastLoc")+")");
            }else if(args[1].equalsIgnoreCase("snowballs")){
                pl.getConfig().set("snowballs", !pl.getConfig().getBoolean("snowballs"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Snowballs: "+pl.getConfig().getBoolean("snowballs")+")");
            }else if(args[1].equalsIgnoreCase("instantGiveShovel")){
                pl.getConfig().set("instantGiveShovel", !pl.getConfig().getBoolean("instantGiveShovel"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(InstantGiveShovel: "+pl.getConfig().getBoolean("instantGiveShovel")+")");
            }else if(args[1].equalsIgnoreCase("randomSpawn")){
                pl.getConfig().set("randomSpawn", !pl.getConfig().getBoolean("randomSpawn"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(RandomSpawn: "+pl.getConfig().getBoolean("randomSpawn")+")");
            }else if(args[1].equalsIgnoreCase("spectator")){
                pl.getConfig().set("spectator", !pl.getConfig().getBoolean("spectator"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Spectator: "+pl.getConfig().getBoolean("spectator")+")");
            }else if(args[1].equalsIgnoreCase("broadcastWinWorld")){
                pl.getConfig().set("broadcastWinWorld", !pl.getConfig().getBoolean("broadcastWinWorld"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(BroadcastWinWorld: "+pl.getConfig().getBoolean("broadcastWinWorld"));
            }else if(args[1].equalsIgnoreCase("diamondSpade")){
                pl.getConfig().set("diamondSpade", !pl.getConfig().getBoolean("diamondSpade"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(DiamondSpade: "+pl.getConfig().getBoolean("diamondSpade")+")");
            }else if(args[1].equalsIgnoreCase("yamlStatistics")){
                pl.getConfig().set("yamlStatistics", !pl.getConfig().getBoolean("yamlStatistics"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(YAMLStatistics: "+pl.getConfig().getBoolean("yamlStatistics")+") §c(Need server restart/reload)§7)");
            }else if(args[1].equalsIgnoreCase("checkForUpdates")){
                pl.getConfig().set("checkForUpdates", !pl.getConfig().getBoolean("checkForUpdates"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(CheckForUpdates: "+pl.getConfig().getBoolean("checkForUpdates")+")");
            } else if(args[1].equalsIgnoreCase("JoinWorldLock")){
                pl.getConfig().set("joinWorldLock", !pl.getConfig().getBoolean("joinWorldLock"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(JoinWorldLock: "+pl.getConfig().getBoolean("joinWorldLock")+")");
            } else if(args[1].equalsIgnoreCase("snowballSheepBreakBlock")){
                pl.getConfig().set("snowballSheepBreakBlock", !pl.getConfig().getBoolean("snowballSheepBreakBlock"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(SnowballSheepBreakBlock: "+pl.getConfig().getBoolean("snowballSheepBreakBlock")+")");
            }else{
                return help(sender, 2);
            }
        }

        //SPECIFY IP
        if(args[0].equalsIgnoreCase("specifyMyIP") && args.length == 2){
            pl.getConfig().set("serverIP", args[1]);
            pl.saveConfig();
            return sendNormalMessage(sender, "§aIP registered. Thanks! §7(IP: "+args[1]+")");
        }

        //FORCE START
        if(args[0].equalsIgnoreCase("forceStart")){
            if(args.length < 2){
                return help(sender, 2);
            }
            Spleef spleef = getSpleefByName(args[1]);
            if(spleef == null){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            if(spleef.getCurrentStatus() == GameStatus.GAME){
                return sendMessage(sender, "gameAlreadyStarted");
            }
            if(spleef.getPlayerInGame().size() <= 0){
                return sendMessage(sender, "notEnoughPlayers", new String[]{"size", "min"}, new String[]{spleef.getPlayerInGame().size()+"", spleef.getMin()+""});
            }
            spleef.start();
            spleef.checkWin();
            return sendMessage(sender, "gameStarted");
        }

        //FORCE STOP
        if(args[0].equalsIgnoreCase("forceStop")){
            if(args.length < 2){
                return help(sender, 2);
            }
            Spleef spleef = getSpleefByName(args[1]);
            if(spleef == null){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            if(spleef.getCurrentStatus() == GameStatus.WAIT){
                return sendMessage(sender, "gameAlreadyStopped");
            }
            spleef.restart();
            return sendMessage(sender, "gameStopped");
        }

        //FORCE JOIN
        if(args[0].equalsIgnoreCase("forceJoin")){
            if(args.length < 3){
                return help(sender, 2);
            }
            Spleef spleef = getSpleefByName(args[1]);
            if(spleef == null){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            Player target = Bukkit.getPlayer(args[2]);
            if(target == null){
                return sendMessage(sender, "playerNotFound");
            }
            if(pl.getSpleefPlayerRegistry().getSpleefPlayer(target).hasSpleef()){
                return sendMessage(sender, "playerInGame");
            }
            spleef.addPlayer(target, true);
            return sendMessage(sender, "playerAdded");
        }

        //LIST
        if(args[0].equalsIgnoreCase("list")){
            String activeSpleef = ",,";
            String breakdownSpleef = ",,";

            List<String> list = Lists.newArrayList();
            for (int i = 0; i < pl.getSpleefs().getSpleefs().size(); i++) {
                Spleef spleef = pl.getSpleefs().getSpleefs().get(i);
                activeSpleef += ", "+spleef.getName();
                list.add(spleef.getName());
            }

            activeSpleef = activeSpleef.replace(",,, ", "");
            for(int i = 0; i != 999; i++){
                if(pl.getConfig().contains("spleef."+i+".name")){
                    String name = pl.getConfig().getString("spleef."+i+".name");
                    if(!list.contains(name)){
                        String reason = pl.getSpleefs().getMisconfiguredReasons().get(i);
                        breakdownSpleef += ", "+name+(reason == null ? "" : " ("+reason+")");
                    }
                }
            }

            breakdownSpleef = breakdownSpleef.replace(",,, ", "");
            if(breakdownSpleef.contains(",,")){
                breakdownSpleef = "";
            }
            if(activeSpleef.contains(",,")){
                activeSpleef = "";
            }

            sendMessage(sender, "listNotGoodSpleefs", new String[]{"list"}, new String[]{breakdownSpleef});
            return sendMessage(sender, "listGoodSpleefs", new String[]{"list"}, new String[]{activeSpleef});
        }

        //SET REWARD
        if(args[0].equalsIgnoreCase("setReward")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            if(args.length < 2){
                return help(sender, 2);
            }
            if(args[1].equalsIgnoreCase("winItem")){
                ItemStack itemStack = p.getItemInHand();
                if(itemStack == null){
                    pl.getConfig().set("reward.winItem", null);
                }else{
                    pl.getConfig().set("reward.winItem", StatisticBuilder.GSON.toJson(itemStack.serialize()));
                    Map<String, Integer> map = new HashMap<>();
                    itemStack.getEnchantments().forEach((enchantment, integer) -> map.put(enchantment.getName(), integer));
                    pl.getConfig().set("reward.winItemEnchants", StatisticBuilder.GSON.toJson(map));
                }
            }else {
                if(args.length < 3){
                    return help(sender, 2);
                }
                double number;
                try{
                    number = Double.parseDouble(args[2]);
                }catch (Exception e){

                    if(!args[2].startsWith("/")){
                        return sendMessage(sender, "wrongNumber");
                    }else{
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            stringBuilder.append(args[i]).append(" ");
                        }
                        String fullCommand = stringBuilder.substring(0, stringBuilder.length()-1);

                        // Is COMMAND
                        if(args[1].equalsIgnoreCase("win")){
                            if(args[2].equalsIgnoreCase("/-1")){
                                pl.getConfig().set("reward.winCommand", null);
                            }else{
                                pl.getConfig().set("reward.winCommand", fullCommand);
                            }
                        }else if(args[1].equalsIgnoreCase("loose")){
                            if(args[2].equalsIgnoreCase("/-1")){
                                pl.getConfig().set("reward.looseCommand", null);
                            }else{
                                pl.getConfig().set("reward.looseCommand", fullCommand);
                            }
                        }else{
                            return help(sender, 2);
                        }
                        pl.saveConfig();
                        return sendMessage(sender, "settingSaved");
                    }
                }
                if(args[1].equalsIgnoreCase("win")){
                    pl.getConfig().set("reward.win", number);
                }else if(args[1].equalsIgnoreCase("loose")){
                    pl.getConfig().set("reward.loose", number);
                }else{
                    return help(sender, 2);
                }
            }
            pl.saveConfig();
            return sendMessage(sender, "settingSaved");
        }

        //CLEAR STATS
        if(args[0].equalsIgnoreCase("clearStats")){
            if(pl.getDatabaseManager() != null){
                pl.getDatabaseManager().clearAll();
            }
            return sendMessage(sender, "statsCleared");
        }

        //SET REWARD
        if(args[0].equalsIgnoreCase("setHologram")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            pl.getConfig().set("hologram.location.x", p.getLocation().getBlock().getLocation().getX());
            pl.getConfig().set("hologram.location.y", p.getLocation().getBlock().getLocation().getY());
            pl.getConfig().set("hologram.location.z", p.getLocation().getBlock().getLocation().getZ());
            pl.getConfig().set("hologram.location.world", p.getWorld().getName());
            pl.saveConfig();
            if(pl.getHolographicManager() != null){
                pl.getHolographicManager().reload();
            }
            return sendMessage(sender, "hologramSaved");
        }

        //SET GAMEMODE
        if(args[0].equalsIgnoreCase("setGameMode")){
            if(args.length < 3){
                return help(sender, 3);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            GameType gameType = GameType.SPLEEF;
            if(args.length > 2){
                try{
                    gameType = GameType.valueOf(args[2].toUpperCase());
                }catch (Exception e){
                    return help(sender, 3);
                }
            }
            pl.getConfig().set("spleef."+id+".gameType", gameType.name());
            pl.saveConfig();
            return sendMessage(sender, "gameModeChanged");
        }

        //CHECK UPDATE
        if(args[0].equalsIgnoreCase("checkUpdate")){
            SpleefUpdater.checkNewVersion(pl, true);
            NaruseAPIDownloader.checkSecondThreadAPI(pl);
            return sendMessage(sender, "checkStarted");
        }

        //SET STATS
        if(args[0].equalsIgnoreCase("setStats")){
            if(args.length < 4){
                return help(sender, 3);
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            int value;
            try{
                value = Integer.valueOf(args[3]);
            }catch (Exception e){
                return sendMessage(sender, "wrongNumber");
            }

            SpleefPlayer spleefPlayer = pl.getSpleefPlayerRegistry().getSpleefPlayer(target);
            if(spleefPlayer == null){
                return sendMessage(sender, "playerNotFound");
            }
            if(pl.getDatabaseManager() == null){
                return sendMessage(sender, "sqlNotFound");
            }

            if(args[2].equalsIgnoreCase("win")){
                spleefPlayer.setStatistic(StatisticType.WIN, value);
            }else if(args[2].equalsIgnoreCase("loose")){
                spleefPlayer.setStatistic(StatisticType.LOSE, value);
            }else{
                return sendMessage(sender, "argumentNotFound", new String[]{"arg"}, new String[]{args[2]});
            }
            spleefPlayer.saveStatistics();
            return sendMessage(sender, "statisticSaved");
        }


        //DISABLED COMMANDS
        if(args[0].equalsIgnoreCase("disabledCommands")){
            if(args.length < 2){
                return help(sender, 3);
            }

            if(args[1].equalsIgnoreCase("clear")){
                pl.getConfig().set("disabledCommands", null);
                pl.saveConfig();
                return sendMessage(sender, "disabledCommandsCleared");
            }else if(args[1].equalsIgnoreCase("list")){
                List<String> list = pl.getConfig().getStringList("disabledCommands");
                if(list.size() == 0){
                    return sendMessage(sender, "disabledCommandsIsEmpty");
                }
                sendMessage(sender, "disabledCommands");
                for (String s1 : list) {
                    sendNormalMessage(sender, "§5- §e"+s1);
                }
                return true;
            }

            if(args.length < 3){
                return help(sender, 3);
            }

            StringBuilder stringBuilder = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; i++) {
                stringBuilder.append(" "+args[i]);
            }

            List<String> list = pl.getConfig().getStringList("disabledCommands");

            if(args[1].equalsIgnoreCase("add")){
                if(!list.contains(stringBuilder.toString())){
                    list.add(stringBuilder.toString());
                }
                pl.getConfig().set("disabledCommands", list);
                pl.saveConfig();
                return sendMessage(sender, "disabledCommandsAdded");
            }else if(args[1].equalsIgnoreCase("remove")){
                list.remove(stringBuilder.toString());
                pl.getConfig().set("disabledCommands", list);
                pl.saveConfig();
                return sendMessage(sender, "disabledCommandsRemoved");
            }else{
                return sendMessage(sender, "argumentNotFound", new String[]{"arg"}, new String[]{args[1]});
            }
        }

        //GIVE ALL BONUSES
        if(args[0].equalsIgnoreCase("giveAllBonuses")){
            if(!(sender instanceof Player)){
                return sendMessage(sender, "onlyForPlayers");
            }
            Player p = (Player) sender;

            Spleef spleef = pl.getSpleefPlayerRegistry().getSpleefPlayer(p).getCurrentSpleef();
            if(spleef == null){
                p.sendMessage(pl.getMessageManager().get("youNeedToBeInGame"));
            }
            if(spleef.getBonusManager() == null){
                return sendNormalMessage(p, pl.getMessageManager().get("settingShouldBeEnabled", new String[]{"setting"}, new String[]{"SheepBonuses"}));
            }
            for (Class<? extends Bonus> bonus : BonusManager.getBonuses()) {
                spleef.getBonusManager().giveBonus(p, bonus.getSimpleName());
            }
            return true;
        }

        //BONUS
        if(args[0].equalsIgnoreCase("bonus")){
            if(args.length < 3){
                return help(sender, 3);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }
            sendMessage(sender, "bonusList");
            if(args[2].equalsIgnoreCase("list")){
                for (Class<? extends Bonus> bonus : BonusManager.getBonuses()) {
                    String path = "spleef."+id+".bonus."+bonus.getSimpleName();
                    if(!pl.getConfig().contains(path)){
                        pl.getConfig().set(path, true);
                    }
                    boolean isEnabled = pl.getConfig().getBoolean(path);
                    sendNormalMessage(sender, "§5- §e"+bonus.getSimpleName()+" §6(Enabled: "+(isEnabled ? "§aEnabled" : "§cDisabled")+"§6)");
                }
                pl.saveConfig();
                return true;
            }else if(args[2].equalsIgnoreCase("enable")){
                if(args.length < 4){
                    return help(sender, 3);
                }
                for (Class<? extends Bonus> bonus : BonusManager.getBonuses()) {
                    if(bonus.getSimpleName().equalsIgnoreCase(args[3])){
                        String path = "spleef."+id+".bonus."+bonus.getSimpleName();
                        if(!pl.getConfig().contains(path)){
                            pl.getConfig().set(path, true);
                        }
                        boolean finalBoolean;
                        pl.getConfig().set(path, finalBoolean = !pl.getConfig().getBoolean(path));

                        return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7("+bonus.getSimpleName()+": "+finalBoolean+")");
                    }
                }
                return sendMessage(sender, "argumentNotFound", new String[]{"arg"}, new String[]{args[3]});
            }
            return true;
        }

        //DOWNLOAD DBAPI
        if(args[0].equalsIgnoreCase("downloadDBAPI")){
            if(NaruseAPIDownloader.checkDBAPI(this.pl, false)){
                return this.sendNormalMessage(sender, "§aDBAPI was downloaded and launched, but you will need to edit its configuration. Then, a server reload/restart must be performed.");
            }else{
                return this.sendNormalMessage(sender, "§cAn error occured while downloading DBAPI.");
            }
        }

        //ENABLE BONUS
        if(args[0].equalsIgnoreCase("enableSheep")){
            if(args.length < 2){
                return help(sender, 3);
            }
            int id = getIdByName(args[1]);
            if(id == -1){
                return sendMessage(sender, "spleefNotFound", new String[]{"name"}, new String[]{args[1]});
            }

            boolean newValue = pl.getConfig().contains("spleef."+id+".sheepBonusEnabled") ? !pl.getConfig().getBoolean("spleef."+id+".sheepBonusEnabled") : false;
            pl.getConfig().set("spleef."+id+".sheepBonusEnabled", newValue);
            pl.saveConfig();
            return sendNormalMessage(sender, "§aSetting saved. §6(SheepBonus: "+newValue+"§6)");
        }

        // ALLOWSHOWIP
        if(args[0].equalsIgnoreCase("allowShowIp")){
            if(args.length < 2){
                return false;
            }

            pl.getConfig().set("allowShowIPAsked2", true);
            if(args[1].equalsIgnoreCase("no")){
                pl.getConfig().set("allowShowIP2", false);
            }else{
                pl.getConfig().set("allowShowIP2", true);
            }
            pl.saveConfig();
            return this.sendNormalMessage(sender, "§aSetting saved.");
        }

        // MANAGER
        if(args[0].equalsIgnoreCase("manager")){
            if(sender instanceof Player){
                new InventoryManager(pl, (Player) sender);
            }

            return true;
        }

        return false;
    }

    private boolean help(CommandSender sender, int page){
        sendNormalMessage(sender, "§e/§7spleef join <Spleef name>");
        sendNormalMessage(sender, "§e/§7spleef leave");
        sendNormalMessage(sender, "§e/§7spleef stats <[Player]>");
        sendNormalMessage(sender, "§e/§7spleef joinQueue");
        if(sender.hasPermission("spleef.help")){
            if(page == 1){
                sendNormalMessage(sender, "§6/§7spleef help <[Page]>");
                sendNormalMessage(sender, "§6/§7spleef <Create, Delete> <Spleef name> <[Splegg, Bow, Team_Two, Team_Three, Team_Four]>");
                sendNormalMessage(sender, "§6/§7spleef reload");
                sendNormalMessage(sender, "§6/§7spleef setMin <Spleef name> <Number>");
                sendNormalMessage(sender, "§6/§7spleef setMax <Spleef name> <Number>");
                sendNormalMessage(sender, "§6/§7spleef setArena <Spleef name> §7(Location -> arena's spawn)");
                sendNormalMessage(sender, "§6/§7spleef setSpawn <Spleef name> §7(Location -> end location)");
                sendNormalMessage(sender, "§6/§7spleef setLobby <Spleef name> §7(Location & Optional)");
                sendNormalMessage(sender, "§6/§7spleef <Open, Close> <Spleef name>");
                sendNormalMessage(sender, "§bPage: §21/3");
            }else if(page == 2){
                sendNormalMessage(sender, "§6/§7spleef setTimer <Start, BlockStanding> <Number>");
                sendNormalMessage(sender, "§6/§7spleef setLang <French, English, Russian, German> §7(It will erase your changes)");
                sendNormalMessage(sender, "§6/§7spleef enable <BroadcastWin, HolographicRanking, Lightnings, StandingLimit, TpToLastLoc, Snowballs," +
                        " InstantGiveShovel, RandomSpawn, Spectator, BroadcastWinWorld, DiamondSpade, YAMLStatistics, CheckForUpdates, JoinWorldLock, SnowballSheepBreakBlock>");
                sendNormalMessage(sender, "§6/§7spleef forceStart <Spleef name>");
                sendNormalMessage(sender, "§6/§7spleef forceStop <Spleef name>");
                sendNormalMessage(sender, "§6/§7spleef forceJoin <Spleef name> <Player>");
                sendNormalMessage(sender, "§6/§7spleef list");
                sendNormalMessage(sender, "§6/§7spleef setReward WinItem §7(No item will delete saved item) (Select item in hand & need /spleef reload) ('{player}' for player's name)");
                sendNormalMessage(sender, "§6/§7spleef setReward <Win, Loose> <[Number]> §7(-1 means no reward)");
                sendNormalMessage(sender, "§6/§7spleef setReward <Win, Loose> <Command> §7('/-1' means no reward, will delete saved command | Command executed by the console)");
                sendNormalMessage(sender, "§6/§7spleef clearStats §7(Irreversible action)");
                sendNormalMessage(sender, "§bPage: §22/3");
            }else if(page == 3){
                sendNormalMessage(sender, "§6/§7spleef setHologram §7(Location)");
                sendNormalMessage(sender, "§6/§7spleef setGameMode <Spleef name> <Spleef, Splegg, Bow, Team_Two, Team_Three, Team_Four>");
                sendNormalMessage(sender, "§6/§7spleef checkUpdate");
                sendNormalMessage(sender, "§6/§7spleef setStats <Player> <Win, Loose> <Number>");
                sendNormalMessage(sender, "§6/§7spleef disabledCommands <Add, Remove> <Command>");
                sendNormalMessage(sender, "§6/§7spleef disabledCommands <Clear, List>");
                sendNormalMessage(sender, "§6/§7spleef giveAllBonuses");
                sendNormalMessage(sender, "§6/§7spleef bonus <Spleef Name> <List, Enable> <[Bonus Name]>");
                sendNormalMessage(sender, "§6/§7spleef downloadDBAPI");
                sendNormalMessage(sender, "§6/§7spleef enableSheep <Spleef name>");
                sendNormalMessage(sender, "§6/§7spleef manager");
                sendNormalMessage(sender, "§bPage: §23/3");
            }
        }
        return true;
    }

    private boolean sendMessage(CommandSender sender, String msg, String[] toReplace, String[] replacedBy){
        sender.sendMessage(pl.getMessageManager().get("commands."+msg, toReplace, replacedBy));
        return true;
    }

    private boolean sendMessage(CommandSender sender, String msg){
        sender.sendMessage(pl.getMessageManager().get("commands."+msg));
        return true;
    }

    private boolean sendNormalMessage(CommandSender sender, String msg){
        sender.sendMessage(msg);
        return true;
    }

    private int getNextIdAvailable(){
        for (int i = 0; i < 9999; i++) {
            if(!pl.getConfig().contains("spleef."+i+".name")){
                return i;
            }
        }
        return -1;
    }

    private int getIdByName(String name) {
        for (int i = 0; i < 9999; i++) {
            if(pl.getConfig().contains("spleef."+i+".name") && pl.getConfig().getString("spleef."+i+".name").equals(name)){
                return i;
            }
        }
        return -1;
    }

    private Spleef getSpleefByName(String name){
        for (int i = 0; i < pl.getSpleefs().getSpleefs().size(); i++) {
            Spleef spleef = pl.getSpleefs().getSpleefs().get(i);
            if(spleef.getName().equals(name)){
                return spleef;
            }
        }
        return null;
    }
}
