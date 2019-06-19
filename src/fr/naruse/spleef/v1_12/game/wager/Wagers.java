package fr.naruse.spleef.v1_12.game.wager;

import com.google.common.collect.Lists;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import fr.naruse.spleef.v1_12.api.SpleefAPIEventInvoker;
import fr.naruse.spleef.v1_12.api.event.cancellable.wager.SpleefWagerDeleteEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.wager.SpleefWagerInviteEvent;
import fr.naruse.spleef.v1_12.api.event.cancellable.wager.SpleefWagerLoseEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class Wagers {
    private SpleefPluginV1_12 pl;
    private List<Wager> wagers = Lists.newArrayList();
    private HashMap<Player, Wager> wagerOfPlayer = new HashMap<>();
    public Wagers(SpleefPluginV1_12 pl){
        this.pl = pl;
    }

    public boolean createWager(Player p, Player p2){
        if(wagerOfPlayer.containsKey(p) || wagerOfPlayer.containsKey(p2)){
            return false;
        }
        Wager wager = new Wager(pl, p, p2);
        if(new SpleefAPIEventInvoker(new SpleefWagerInviteEvent.Pre(pl, p, p2, wager)).isCancelled()){
            return false;
        }
        wagers.add(wager);
        wagerOfPlayer.put(p, wager);
        wagerOfPlayer.put(p2, wager);
        Bukkit.getPluginManager().registerEvents(wager, pl.getSpleefPlugin());
        wager.init();
        new SpleefAPIEventInvoker(new SpleefWagerInviteEvent.Post(pl, p, p2, wager));
        return true;
    }

    public boolean deleteWager(Wager wager){
        if(!wagers.contains(wager)){
            return false;
        }
        if(new SpleefAPIEventInvoker(new SpleefWagerDeleteEvent.Pre(pl, wager)).isCancelled()){
            return false;
        }
        wager.stop();
        wagers.remove(wager);
        new SpleefAPIEventInvoker(new SpleefWagerDeleteEvent.Post(pl, wager));
        return true;
    }

    public boolean loseWager(Player p){
        if(!wagerOfPlayer.containsKey(p)){
            return false;
        }
        Wager wager = wagerOfPlayer.get(p);
        if(new SpleefAPIEventInvoker(new SpleefWagerLoseEvent.Pre(pl, p, wager)).isCancelled()){
            return false;
        }
        if(wager.getLost() == null){
            wager.setLost(p);
        }else{
            wager.win(p);
        }
        new SpleefAPIEventInvoker(new SpleefWagerLoseEvent.Pre(pl, p, wager));
        return true;
    }

    public void disable(){
        for(int i = 0; i < wagers.size(); i++){
            Wager wager = wagers.get(i);
            wager.decline();
        }
    }

    public boolean hasWager(Player p){
        return wagerOfPlayer.containsKey(p);
    }

    public HashMap<Player, Wager> getWagerOfPlayer() {
        return wagerOfPlayer;
    }

    public List<Wager> getWagers() {
        return wagers;
    }

}
