package fr.naruse.spleef.spleef.bonus.type;

import fr.naruse.api.MathUtils;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.spleef.bonus.IFriendlyBonus;
import fr.naruse.spleef.spleef.bonus.utils.MoveToGoal;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BonusSheepRepulsion extends BonusColored implements IFriendlyBonus {
    public BonusSheepRepulsion(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§9§lDeviant Sheep", 11, 8);
        setApplyVelocity(false);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if(sheep != null && !sheep.isDead()){
            sheep.setTarget(p);
            List<Entity> stream = getNearbySheeps(sheep.getLocation(), 10, 5, 10, true, p).filter(entity -> entity != sheep).collect(Collectors.toList());;
            runSync(() -> {
                new MoveToGoal(sheep, p.getLocation()).execute(2);
                stream.forEach(entity -> entity.setVelocity(MathUtils.genVector(sheep.getLocation(), entity.getLocation()).multiply(1.5).setY(0.5)));
            });
        }
    }

    @Override
    protected void onAction() {

    }
}
