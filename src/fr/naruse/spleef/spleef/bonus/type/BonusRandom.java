package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.Bonus;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.entity.Player;

public class BonusRandom extends Bonus {
    public BonusRandom(BonusManager bonusManager, Player p) {
        super(bonusManager, p, 20, "§kRandom Sheep", 0);
    }

    @Override
    public void launchSheep() {
        try {
            Bonus bonus = bonusManager.giveBonus(p);
            runSync(() -> bonus.launchSheep());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bonusRun() {

    }
}
