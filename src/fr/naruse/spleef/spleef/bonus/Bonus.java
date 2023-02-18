package fr.naruse.spleef.spleef.bonus;

import com.google.common.collect.Lists;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.async.Runner;
import fr.naruse.api.particle.IParticle;
import fr.naruse.api.particle.Particle;
import fr.naruse.api.particle.sender.ParticleBuffer;
import fr.naruse.api.particle.sender.ParticleSender;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.bonus.type.BonusMelt;
import fr.naruse.spleef.spleef.type.Spleef;
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
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Bonus extends Runner {

    protected final Player p;
    protected final SpleefPlugin pl;
    protected final Spleef spleef;
    protected final BonusManager bonusManager;
    private final int tickInterval;

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
        this.tickInterval = tickInterval;

        this.start();
        this.attributeTicker();
    }

    private int tick = 0;
    @Override
    public void run() {
        if(isCancelled()){
            return;
        }
        boolean run = false;
        boolean onTick = this.tickInterval != 1;
        if(onTick){
            if(this.tick >= 20){
                this.tick = 0;
                run = true;
            }else{
                this.tick++;
            }
        }

        if(onTick){
            if(run){
               this.bonusRun();
            }
        }else{
            this.bonusRun();
        }
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

    public abstract void bonusRun();

    public void onRestart() {};

    public void onSheepSpawned(Sheep sheep){ }

    public void giveWool(){
        if(isWoolGiven){
            return;
        }
        isWoolGiven = true;
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) woolColorId);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = Lists.newArrayList();

        String desc = pl.getMessageManager().get("bonuses."+getClass().getSimpleName());
        if(!desc.isEmpty()){
            if(desc.contains("\n")){
                lore = Lists.newArrayList(desc.split("\n"));
            }else{
                lore.add(desc);
            }
        }

        meta.setLore(lore);
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        p.getInventory().addItem(item);
    }

    public void launchSheep(){
        if(isSheepLaunched){
            return;
        }
        isSheepLaunched = true;
        this.sheep = (Sheep) p.getWorld().spawnEntity(spawnLocation == null ? (this instanceof  BonusMelt ? p.getLocation() : p.getEyeLocation()) : spawnLocation, EntityType.SHEEP);
        sheep.setColor(DyeColor.getByWoolData((byte) woolColorId));
        if(applyVelocity){
            sheep.setVelocity(p.getLocation().getDirection().multiply(3.5).add(new Vector(0, 0.3, 0)));
        }
        sheep.setInvulnerable(true);
        onSheepSpawned(sheep);
        if(isMulticolor){
            sheep.setCustomNameVisible(true);
            sheep.setCustomName("jeb_");
        }
        bonusManager.getAliveBonus().add(this);
    }

    public void runSync(Runnable runnable){
        Bukkit.getScheduler().runTask(pl, runnable);
    }

    public void runSyncLater(Runnable runnable, int ticks){
        Bukkit.getScheduler().runTaskLater(pl, runnable, ticks);
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

    public void sendParticle(Location location, IParticle particle, float xOffset, float yOffset, float zOffset, int count, float speed){
        Particle.buildParticle(location, particle, xOffset, yOffset, zOffset, count, speed).toNearbyFifty();
    }

    public void sendParticle(Location location, IParticle particle, float offsetX, float offsetY, float offsetZ, int amount){
        this.sendParticle(location, particle, offsetX, offsetY, offsetZ, amount, 0f);
    }

    public void sendParticle(ParticleBuffer buffer){
        buffer.send(ParticleSender.buildToNearbyFifty());
    }

    public Stream<Entity> getNearbySheeps(Location location, double x, double y, double z){
        return this.getNearbySheeps(location, x, y, z, false, null);
    }

    public Stream<Entity> getNearbySheeps(Location location, double x, double y, double z, boolean filterFriendlyBonuses, Player owner){
        return bonusManager.getAliveBonus().stream().filter(bonus -> filterFriendlyBonuses ? !(bonus.getPlayer() == owner && bonus instanceof IFriendlyBonus) : true).map((Function<Bonus, Entity>) bonus -> bonus.getSheep()).filter(entity -> entity != null && !entity.isDead()
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.X) <= NumberConversions.square(x)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Y) <= NumberConversions.square(y)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Z) <= NumberConversions.square(z));
    }

    public Stream<Entity> getNearbySheepsAndPlayers(Location location, double x, double y, double z){
        return Stream.concat(this.getNearbySheeps(location, x, y, z), this.getNearbyPlayers(location, x, y, z));
    }

    public Stream<? extends Player> getNearbyPlayers(Location location, double x, double y, double z){
        return Bukkit.getOnlinePlayers().stream().filter(entity -> MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.X) <= NumberConversions.square(x)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Y) <= NumberConversions.square(y)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Z) <= NumberConversions.square(z));
    }

    public Stream<Entity> getNearbyEntities(Location location, double x, double y, double z){
        return CollectionManager.ASYNC_ENTITY_LIST.getList().stream().filter(entity ->
                MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.X) <= NumberConversions.square(x)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Y) <= NumberConversions.square(y)
                && MathUtils.distanceSquared(entity.getLocation(), location, MathUtils.Axis.Z) <= NumberConversions.square(z));
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

}
