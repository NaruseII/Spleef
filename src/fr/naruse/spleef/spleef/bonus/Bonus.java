package fr.naruse.spleef.spleef.bonus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.type.Spleef;
import fr.naruse.spleef.utils.CollectionManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Bonus {

    protected final Player p;
    protected final SpleefPlugin pl;
    protected final Spleef spleef;
    protected final BonusManager bonusManager;

    private final int woolColorId;
    private final String name;
    protected static final Random random = new Random();

    protected boolean isWoolGiven = false;
    protected boolean isSheepLaunched = false;
    protected Sheep sheep;
    private final List<IBonusAttribute> bonusAttributes = Lists.newArrayList();

    protected boolean applyVelocity = true;
    protected Location spawnLocation = null;
    protected boolean isMulticolor = false;

    protected boolean isCancelled = false;

    public Bonus(BonusManager bonusManager, Player p, int tickInterval, String name, int woolColorId){
        this.bonusManager = bonusManager;
        this.pl = bonusManager.getPlugin();
        this.spleef = bonusManager.getSpleef();
        this.p = p;
        this.name = name;
        this.woolColorId = woolColorId;
        runTaskTimerTick(tickInterval != 1);
        attributeTicker();
    }

    private int tick = 0;
    protected void runTaskTimerTick(boolean onTick){
        if(isCancelled()){
            return;
        }
        boolean run = false;
        if(onTick){
            if(tick >= 20){
                tick = 0;
                run = true;
            }else{
                tick++;
            }
        }
        boolean finalRun = run;
        Runnable runnable = () -> {
            if(onTick){
                if(finalRun){
                    run();
                }
            }else{
                run();
            }
            runTaskTimerTick(onTick);
        };
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
    }

    public void cancel(){
        this.cancel(true);
    }

    public void cancel(boolean removeFromAliveSet){
        this.isCancelled = true;
        if(removeFromAliveSet){
            this.bonusManager.getAliveBonus().remove(this);
        }
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public abstract void run();

    public void onSheepSpawned(Sheep sheep){ }

    public void giveWool(){
        if(isWoolGiven){
            return;
        }
        isWoolGiven = true;
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) woolColorId);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Lists.newArrayList());
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        p.getInventory().addItem(item);
    }

    public void launchSheep(){
        if(isSheepLaunched){
            return;
        }
        isSheepLaunched = true;
        this.sheep = (Sheep) p.getWorld().spawnEntity(spawnLocation == null ? p.getLocation() : spawnLocation, EntityType.SHEEP);
        sheep.setColor(DyeColor.getByWoolData((byte) woolColorId));
        if(applyVelocity){
            sheep.setVelocity(p.getLocation().getDirection().multiply(3.5).add(new Vector(0, 0.3, 0)));
        }
        sheep.setInvulnerable(true);
        onSheepSpawned(sheep);
        if(isMulticolor){
            sheep.setCustomName("jeb_");
            sheep.setCustomNameVisible(false);
        }
        bonusManager.getAliveBonus().add(this);
    }

    public void runSync(Runnable runnable){
        Bukkit.getScheduler().runTask(pl, runnable);
    }

    public void registerAttribute(IBonusAttribute bonusAttribute){
        bonusAttributes.add(bonusAttribute);
    }

    private void attributeTicker() {
        boolean isCancelled = isCancelled();
        Runnable runnable = () -> {
            if(isCancelled){
                cancel();
                return;
            }
            for (int i = 0; i < bonusAttributes.size(); i++) {
                IBonusAttribute bonusAttribute = bonusAttributes.get(i);
                bonusAttribute.onTick();
            }
            this.attributeTicker();
        };
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
    }

    protected void sendParticle(Location location, String particle, float xOffset, float yOffset, float zOffset, int count, float speed){
        Object object = Utils.Particle.buildPacket(Utils.Particle.fromName(Utils.Particle.getParticleNameFromNative(particle)), location.getX(), location.getY(), location.getZ(), xOffset, yOffset, zOffset, speed, count, 0);
        this.sendParticle(location, object);
    }

    protected void sendParticle(Location location, String particle, float offsetX, float offsetY, float offsetZ, int amount){
        this.sendParticle(location, particle, offsetX, offsetY, offsetZ, amount, 0f);
    }

    protected void sendParticle(Location location, Object packet){
        this.getNearbyPlayers(location, 50, 50, 50).forEach(entity -> Utils.Particle.sendPacket(entity, packet));
    }

    protected void sendParticle(ParticleBuffer buffer){
        for (Object packet : buffer.packets) {
            this.sendParticle(buffer.getLocation(), packet);
        }
    }

    public Stream<Entity> getNearbySheeps(Location location, double x, double y, double z){
        return bonusManager.getAliveBonus().stream().map((Function<Bonus, Entity>) bonus -> bonus.getSheep()).filter(entity -> entity != null && !entity.isDead()
                && Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.X) <= NumberConversions.square(x)
                && Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.Y) <= NumberConversions.square(y)
                && Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.Z) <= NumberConversions.square(z));
    }

    public Stream<Entity> getNearbySheepsAndPlayers(Location location, double x, double y, double z){
        return Stream.concat(this.getNearbySheeps(location, x, y, z), this.getNearbyPlayers(location, x, y, z));
    }

    public Stream<? extends Player> getNearbyPlayers(Location location, double x, double y, double z){
        return Bukkit.getOnlinePlayers().stream().filter(entity -> Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.X) <= NumberConversions.square(x)
                && Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.Y) <= NumberConversions.square(y)
                && Utils.distanceSquared(entity.getLocation(), location, Utils.Axis.Z) <= NumberConversions.square(z));
    }

    public void setMulticolor(boolean multicolor) {
        isMulticolor = multicolor;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getWoolColorId() {
        return woolColorId;
    }

    public void setApplyVelocity(boolean applyVelocity) {
        this.applyVelocity = applyVelocity;
    }

    public String getName() {
        return name;
    }

    public Sheep getSheep() {
        return sheep;
    }

    public Player getPlayer() {
        return p;
    }

    public static class ParticleBuffer {

        private Set<Object> packets = Sets.newHashSet();
        private Location location;

        public ParticleBuffer add(Location location, String particle, float offsetX, float offsetY, float offsetZ, int amount){
            packets.add(Utils.Particle.buildPacket(Utils.Particle.fromName(Utils.Particle.getParticleNameFromNative(particle)), location.getX(), location.getY(), location.getZ(), offsetX, offsetY, offsetZ, 0f, amount, 0));
            this.location = location;
            return this;
        }

        public ParticleBuffer add(Location location, String particle, float offsetX, float offsetY, float offsetZ, int amount, float speed){
            packets.add(Utils.Particle.buildPacket(Utils.Particle.fromName(Utils.Particle.getParticleNameFromNative(particle)), location.getX(), location.getY(), location.getZ(), offsetX, offsetY, offsetZ, speed, amount, 0));
            this.location = location;
            return this;
        }

        public Location getLocation() {
            return location;
        }
    }

}
