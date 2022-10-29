package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.entity.Player;

public class BonusMegaFloorFixer extends BonusFloorFixer {

    public BonusMegaFloorFixer(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§4§lMega §d§lFloor Fixer Sheep", 400);
    }
}
