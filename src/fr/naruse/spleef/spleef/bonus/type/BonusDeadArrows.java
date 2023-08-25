package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.List;

public class BonusDeadArrows extends BonusColored {

    public BonusDeadArrows(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§1§lDead Arrows Sheep", 1, 7);
    }

    @Override
    protected void onAction() {
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        sendParticle(sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 3, 2, 2);

        this.currentLocation = sheep.getLocation().clone();
        this.startY = this.currentLocation.getBlockY();
        onRun();
    }

    private Location currentLocation;
    private int startY;
    private List<Hyperbol> hyperbolList = Lists.newArrayList();
    private boolean arrowLaunched = false;
    private int tick = 0;

    private void onRun() {
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(new Runnable() {
            @Override
            public void run() {
                if(spleef.getCurrentStatus() == GameStatus.WAIT){
                    return;
                }

                if(arrowLaunched){

                    for (int i = 0; i < hyperbolList.size(); i++) {
                        Hyperbol hyperbol = hyperbolList.get(i);
                        hyperbol.calculate(tick);

                        if(hyperbol.getLocation().getBlock().getType() == Material.SNOW_BLOCK){
                            spleef.destroyBlock(p, hyperbol.getLocation().getBlock(), true);
                        }
                        if(hyperbol.getLocation().getY() < -280){
                            hyperbolList.remove(hyperbol);
                        }

                        sendParticle(hyperbol.getLocation(), Particle.getEnumParticle().TOTEM_OF_UNDYING(), 0, 0, 0, 1);
                    }

                    if(hyperbolList.size() == 0){
                        return;
                    }

                    tick++;
                }else{
                    arrowLaunched = true;
                    currentLocation.add(0, 30, 0);
                    sendParticle(currentLocation, Particle.getEnumParticle().EXPLOSION_HUGE(), 2, 3, 2, 2);
                    sendParticle(currentLocation, Particle.getEnumParticle().FIRE(), 4, 4, 4, 20);

                    for (int i = 0; i < 40; i++) {
                        launchArrow(currentLocation.clone());
                    }
                }

                onRun();
            }
        });
    }

    private void launchArrow(Location currentLocation) {
        Hyperbol hyperbol = new Hyperbol(this.currentLocation.clone(), -(0.00001+random.nextInt(999)*0.00001), 0, this.currentLocation.getY());
        this.hyperbolList.add(hyperbol);
    }

    @Override
    protected void onTick() {

    }

    private class Hyperbol {

        private Location location;
        private double a;
        private double b;
        private double c;
        private final boolean isX = random.nextBoolean();
        private final boolean isZ = random.nextBoolean();
        private final boolean isXNegative = random.nextBoolean();
        private final boolean isZNegative = random.nextBoolean();

        public Hyperbol(Location location, double a, double b, double c) {
            this.location = location;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public void calculate(int tick){
            double y = this.a*NumberConversions.square(tick) + this.b*tick + this.c;
            this.location.setY(y);
            this.location.add((this.isX ? 0.1 : 0)* (this.isXNegative ? -1 : 1), 0, (this.isZ ? 0.1 : 0)* (this.isZNegative ? -1 : 1));
        }

        public Location getLocation() {
            return this.location;
        }
    }
}
