package fr.naruse.spleef.v1_12.game.spleef.type;

import org.bukkit.entity.Player;

import java.util.List;

public interface TeamModeSpleef {

    List<List<Player>> teams();

    List<Player> redTeam();

    List<Player> blueTeam();

    List<Player> greenTeam();

    List<Player> yellowTeam();
}
