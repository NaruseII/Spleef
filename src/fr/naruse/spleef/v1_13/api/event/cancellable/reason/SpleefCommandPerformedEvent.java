package fr.naruse.spleef.v1_13.api.event.cancellable.reason;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.cancellable.SpleefCancellableWithReasonEvent;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

@SpleefCancellable
public class SpleefCommandPerformedEvent extends SpleefCancellableWithReasonEvent {
    private Player player;
    private Command command;
    private String msg;
    private String[] args;
    public SpleefCommandPerformedEvent(SpleefPluginV1_13 pl, Player player, Command command, String msg, String[] args) {
        super(pl, "SpleefCommandPerformedEvent");
        this.player = player;
        this.command = command;
        this.msg = msg;
        this.args = args;
    }

    public Player getPlayer() {
        return player;
    }

    public Command getCommand() {
        return command;
    }

    public String getMsg() {
        return msg;
    }

    public String[] getArgs() {
        return args;
    }
}
