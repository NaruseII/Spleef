package fr.naruse.spleef.cmd;

import com.google.common.collect.Lists;
import fr.naruse.spleef.inventory.InventoryStatistics;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.GameType;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SpleefCommands implements CommandExecutor {
    private SpleefPlugin pl;
    public SpleefCommands(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            return sendMessage(sender, "onlyForPlayers");
        }
        Player p = (Player) sender;
        if(args.length == 0){
            return help(sender, 1);
        }

        //JOIN
        if(args[0].equalsIgnoreCase("join")){
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
            Spleef spleef = pl.getSpleefPlayerRegistry().getSpleefPlayer(p).getCurrentSpleef();
            if(spleef == null){
                return sendMessage(sender, "youAreNotInGame");
            }
            spleef.removePlayer(p);
            return true;
        }

        //STATS
        if(args[0].equalsIgnoreCase("stats")){
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


        /// ADMIN
        if(!p.hasPermission("spleef.help")){
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
                pl.getHolographicManager().reloadLines();
            }
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
                    pl.getHolographicManager().reloadHologram();
                    pl.getHolographicManager().reloadLines();
                }
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(HolographicRanking: "+pl.getConfig().getBoolean("holographicRanking")+")");
            }else if(args[1].equalsIgnoreCase("Lightnings")){
                pl.getConfig().set("lightnings", !pl.getConfig().getBoolean("lightnings"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Lightnings: "+pl.getConfig().getBoolean("lightnings")+")");
            }else if(args[1].equalsIgnoreCase("standingLimit")){
                pl.getConfig().set("standingLimit", !pl.getConfig().getBoolean("standingLimit"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Lightnings: "+pl.getConfig().getBoolean("standingLimit")+")");
            }else if(args[1].equalsIgnoreCase("tpToLastLoc")){
                pl.getConfig().set("tpToLastLoc", !pl.getConfig().getBoolean("tpToLastLoc"));
                pl.saveConfig();
                return sendNormalMessage(sender, pl.getMessageManager().get("commands.settingSaved")+" §7(Lightnings: "+pl.getConfig().getBoolean("tpToLastLoc")+")");
            }else{
                return help(sender, 2);
            }
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
            if(spleef.getPlayerInGame().size() < spleef.getMin()){
                return sendMessage(sender, "tooManyPlayers");
            }
            spleef.start();
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
                        breakdownSpleef += ", "+name;
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
            if(args.length < 3){
                return help(sender, 2);
            }
            double number;
            try{
                number = Double.parseDouble(args[2]);
            }catch (Exception e){
                return sendMessage(sender, "wrongNumber");
            }
            if(args[1].equalsIgnoreCase("win")){
                pl.getConfig().set("reward.win", number);
            }else if(args[1].equalsIgnoreCase("loose")){
                pl.getConfig().set("reward.loose", number);
            }else{
                return help(sender, 2);
            }
            pl.saveConfig();
            return sendMessage(sender, "settingSaved");
        }

        //CLEAR STATS
        if(args[0].equalsIgnoreCase("clearStats")){
            if(pl.getSqlManager() != null){
                pl.getSqlManager().clearAll();
            }
            return sendMessage(sender, "statsCleared");
        }

        //SET REWARD
        if(args[0].equalsIgnoreCase("setHologram")){
            pl.getConfig().set("hologram.location.x", p.getLocation().getBlock().getLocation().getX());
            pl.getConfig().set("hologram.location.y", p.getLocation().getBlock().getLocation().getY());
            pl.getConfig().set("hologram.location.z", p.getLocation().getBlock().getLocation().getZ());
            pl.getConfig().set("hologram.location.world", p.getWorld().getName());
            pl.saveConfig();
            if(pl.getHolographicManager() != null){
                pl.getHolographicManager().reloadHologram();
                pl.getHolographicManager().reloadLines();
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
        return false;
    }

    private boolean help(CommandSender sender, int page){
        sendNormalMessage(sender, "§e/§7spleef join <Spleef name>");
        sendNormalMessage(sender, "§e/§7spleef leave");
        sendNormalMessage(sender, "§e/§7spleef stats <[Player]>");
        if(sender.hasPermission("spleef.help")){
            if(page == 1){
                sendNormalMessage(sender, "§6/§7spleef help <[Page]>");
                sendNormalMessage(sender, "§6/§7spleef <Create, Delete> <Spleef name> <[Splegg, Bow]>");
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
                sendNormalMessage(sender, "§6/§7spleef setLang <French, English> §7(It will erase your changes)");
                sendNormalMessage(sender, "§6/§7spleef enable <BroadcastWin, HolographicRanking, Lightnings, StandingLimit, TpToLastLoc>");
                sendNormalMessage(sender, "§6/§7spleef forceStart <Spleef name>");
                sendNormalMessage(sender, "§6/§7spleef forceStop <Spleef name>");
                sendNormalMessage(sender, "§6/§7spleef forceJoin <Spleef name> <Player>");
                sendNormalMessage(sender, "§6/§7spleef list");
                sendNormalMessage(sender, "§6/§7spleef setReward <Win, Loose> <Number> §7(-1 means no reward)");
                sendNormalMessage(sender, "§6/§7spleef clearStats §7(Irreversible action)");
                sendNormalMessage(sender, "§bPage: §22/3");
            }else if(page == 3){
                sendNormalMessage(sender, "§6/§7spleef setHologram §7(Location)");
                sendNormalMessage(sender, "§6/§7spleef setGameMode <Spleef name> <Spleef, Splegg, Bow>");
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
