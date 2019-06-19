package fr.naruse.spleef.v1_12.game.duel;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_12.api.event.cancellable.SpleefCancellableWithReasonEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.duels.SpleefDuelsAcceptDuelEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.duels.SpleefDuelsDeclineEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.duels.SpleefDuelsInviteEvent;
import fr.naruse.spleef.v1_12.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;

public class Duels implements Listener {
    private SpleefPluginV1_12 pl;
    private HashMap<Player, Player> inviteOfPlayer = new HashMap<>();
    private HashMap<Player, Player> receiveOfPlayer = new HashMap<>();
    private List<Player> playerInDuel = Lists.newArrayList();
    public Duels(SpleefPluginV1_12 spleefPlugin) {
        this.pl = spleefPlugin;
    }

    public boolean invite(Player p, Player target){
        if(pl.spleefs.hasSpleef(p) || pl.spleefs.hasSpleef(target)){
            p.sendMessage("§c"+Message.ONE_PLAYER_HAS_A_GAME.getMessage());
            return false;
        }

        SpleefCancellableWithReasonEvent scwre;
        if(new SpleefAPIEventInvoker(scwre = new SpleefDuelsInviteEvent(pl, p, target)).isCancelled()){
            if(scwre.getReason() != null){
                p.sendMessage("§c"+scwre.getReason());
                target.sendMessage("§c"+scwre.getReason());
            }
            return false;
        }

        inviteOfPlayer.put(p, target);
        receiveOfPlayer.put(target, p);
        p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_SENT.getMessage());
        target.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_RECEIVED_BY.getMessage()+" §6"+p.getName()+"§a.");
        return true;
    }

    public void decline(Player p, boolean sendMessage){
        SpleefCancellableWithReasonEvent scwre;
        if(new SpleefAPIEventInvoker(scwre = new SpleefDuelsDeclineEvent(pl, p)).isCancelled()){
            if(scwre.getReason() != null){
                p.sendMessage("§c"+scwre.getReason());
            }
            return;
        }

        if(inviteOfPlayer.containsKey(p)){
            if(sendMessage){
                inviteOfPlayer.get(p).sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_DECLINED.getMessage());
                p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_DECLINED.getMessage());
            }
            receiveOfPlayer.remove(inviteOfPlayer.get(p));
            inviteOfPlayer.remove(p);
            return;
        }
        if(receiveOfPlayer.containsKey(p)){
            if(sendMessage){
                receiveOfPlayer.get(p).sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_DECLINED.getMessage());
                p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_DECLINED.getMessage());
            }
            inviteOfPlayer.remove(receiveOfPlayer.get(p));
            receiveOfPlayer.remove(p);
            return;
        }
        if(sendMessage){
            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.YOU_DO_NOT_HAVE_A_DUEL.getMessage());
        }
    }

    public boolean acceptDuel(Player p){
        if(!playerHasDuel(p)){
            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.YOU_DO_NOT_HAVE_A_DUEL.getMessage());
            return false;
        }
        if(!receiveOfPlayer.containsKey(p)){
            p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.YOU_DO_NOT_HAVE_A_DUEL.getMessage());
            return false;
        }

        SpleefCancellableWithReasonEvent scwre;
        if(new SpleefAPIEventInvoker(scwre = new SpleefDuelsAcceptDuelEvent(pl, p)).isCancelled()){
            if(scwre.getReason() != null){
                p.sendMessage("§c"+scwre.getReason());
            }
            return false;
        }

        Player target = receiveOfPlayer.get(p);
        playerInDuel.add(p);
        playerInDuel.add(target);
        p.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_ACCEPTED.getMessage());
        target.sendMessage(Message.SPLEEF.getMessage()+" §a"+Message.DUEL_ACCEPTED.getMessage());
        return true;
    }

    public boolean duelActive(Player p){
        if(playerHasDuel(p)){
            return playerInDuel.contains(p);
        }
        return false;
    }

    public boolean playerHasDuel(Player p){
        if(inviteOfPlayer.containsKey(p) || receiveOfPlayer.containsKey(p)){
            return true;
        }
        return false;
    }

    public Player getOtherPlayer(Player p){
        if(!playerHasDuel(p)){
            return null;
        }
        if(inviteOfPlayer.containsKey(p)){
            return inviteOfPlayer.get(p);
        }
        return receiveOfPlayer.get(p);
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        decline(p, false);
    }

    public HashMap<Player, Player> getInviteOfPlayer() {
        return inviteOfPlayer;
    }

    public HashMap<Player, Player> getReceiveOfPlayer() {
        return receiveOfPlayer;
    }

    public List<Player> getPlayerInDuel() {
        return playerInDuel;
    }
}
