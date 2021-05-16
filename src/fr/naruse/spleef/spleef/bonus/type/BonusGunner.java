package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.bonus.attribute.BonusAttributeFollower;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowball;

public class BonusGunner extends BonusColored {
    public BonusGunner(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§d§lGunner Sheep", 7, 8);
    }

    @Override
    public void onSheepSpawned(Sheep sheep) {
        super.onSheepSpawned(sheep);
        registerAttribute(new BonusAttributeFollower(this, 1.8) {
            @Override
            public void nearestPlayerFound(Player target) {
                super.nearestPlayerFound(target);
                sheep.setTarget(target);
                sheep.launchProjectile(Snowball.class);
            }
        });
    }

    @Override
    protected void onAction() {

    }
}
