package fr.naruse.spleef.utils;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.spleef.bonus.Bonus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

public class Utils {

    public static final Random RANDOM = new Random();
    public static ItemStack LEAVE_ITEM;
    public static ItemStack SPADE_ITEM;
    public static final ItemStack SNOWBALL;
    public static final ItemStack BOW;
    public static ItemStack WIN_ITEM;

    static {
        LEAVE_ITEM = new ItemStack(Material.BARRIER);

        SPADE_ITEM = new ItemStack(Material.GOLD_SPADE);
        ItemMeta meta = SPADE_ITEM.getItemMeta();
        meta.setUnbreakable(true);
        SPADE_ITEM.setItemMeta(meta);

        SNOWBALL = new ItemStack(Material.SNOW_BALL, 64);
        BOW = new ItemStack(Material.BOW);
        meta = BOW.getItemMeta();
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 3, true);
        BOW.setItemMeta(meta);
    }

    public static void formatItems(SpleefPlugin pl){
        Material material = Material.BARRIER;
        try{
            material = Material.valueOf(pl.getConfig().getString("leaveItem.type"));
        }catch (Exception e){
            pl.getLogger().log(Level.SEVERE, "Cannot find item type '"+pl.getConfig().getString("leaveItem.type")+"'");
        }
        LEAVE_ITEM = new ItemStack(material, 1, (byte) pl.getConfig().getInt("leaveItem.data"));
        ItemMeta meta = LEAVE_ITEM.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(pl.getMessageManager().get("leaveItem"));
        LEAVE_ITEM.setItemMeta(meta);
        SNOWBALL.setAmount(pl.getConfig().getInt("snowballsGivenOnBlockBreak"));

        if(pl.getConfig().getBoolean("diamondSpade")){
            SPADE_ITEM.setType(Material.DIAMOND_SPADE);
        }

        if(pl.getConfig().contains("reward.winItem")){
            WIN_ITEM = ItemStack.deserialize(StatisticBuilder.GSON.fromJson(pl.getConfig().getString("reward.winItem"), StatisticBuilder.MAP_TYPE));
        }
    }

    public static Location getLocation(SpleefPlugin pl, String path) {
        if(!pl.getConfig().contains(path+".x")){
            return null;
        }
        double x = pl.getConfig().getDouble(path+".x");
        double y = pl.getConfig().getDouble(path+".y");
        double z = pl.getConfig().getDouble(path+".z");
        int yaw = 0;
        int pitch = 0;
        if(pl.getConfig().contains(path+".yaw")){
            yaw = pl.getConfig().getInt(path+".yaw");
            pitch = pl.getConfig().getInt(path+".pitch");
        }
        World world;
        try{
            world = Bukkit.getWorld(pl.getConfig().getString(path+".world"));
        } catch (Exception e){
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static boolean areLocationsEquals(Location a, Location b) {
        if (a == null) {
            return false;
        } else if (b.getClass() != a.getClass()) {
            return false;
        } else {
            Location other = b;
            if (a.getWorld() != other.getWorld() && (a.getWorld() == null || !a.getWorld().equals(other.getWorld()))) {
                return false;
            } else if (Math.abs(Double.doubleToLongBits(a.getX()) - Double.doubleToLongBits(other.getX())) > 3) {
                return false;
            } else if (Math.abs(Double.doubleToLongBits(a.getZ()) - Double.doubleToLongBits(other.getZ())) > 3) {
                return false;
            }
            return true;
        }
    }

    public static org.bukkit.util.Vector genVector(Location a, Location b) {
        double dX = a.getX() - b.getX();
        double dY = a.getY() - b.getY();
        double dZ = a.getZ() - b.getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);
        return new org.bukkit.util.Vector(x, z, y);
    }


    public static List<Block> getCircle(Location center, int r){
        final List<Block> list = Lists.newArrayList();
        for(double x = -r; x <= r; x++){
            for(double z = -r; z <= r; z++){
                if((int) center.clone().add(x, 0, z).distanceSquared(center) == Math.sqrt(r)){
                    list.add(center.clone().add(x, 0, z).getBlock());
                }
            }
        }
        return list;
    }

    public static List<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for(int i = 0;i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }

    public static List<Location> getSphere(Location loc, int radius, int height, boolean hollow, boolean sphere, int plusY){
        List<Location> circleblocks = new ArrayList<Location>();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        for(int x = cx - radius; x <= cx + radius; x++){
            for (int z = cz - radius; z <= cz + radius; z++){
                for(int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++){
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

                    if(dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))){
                        Location l = new Location(loc.getWorld(), x, y + plusY, z);
                        circleblocks.add(l);
                    }
                }
            }
        }

        return circleblocks;
    }

    public static void addCharts(SpleefPlugin pl, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> pl.getConfig().getString("currentLang", "english")));
        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            String javaVersion = System.getProperty("java.version");
            Map<String, Integer> entry = new HashMap<>();
            entry.put(javaVersion, 1);
            if (javaVersion.startsWith("1.7")) {
                map.put("Java 1.7", entry);
            } else if (javaVersion.startsWith("1.8")) {
                map.put("Java 1.8", entry);
            } else if (javaVersion.startsWith("1.9")) {
                map.put("Java 1.9", entry);
            } else {
                map.put("Other", entry);
            }
            return map;
        }));
    }

    public static double distanceSquared(Location o, Location b, Axis axis) {
        if (o == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null location");
        } else if (o.getWorld() != null && b.getWorld() != null) {
            if (o.getWorld() != b.getWorld()) {
                throw new IllegalArgumentException("Cannot measure distance between " + b.getWorld().getName() + " and " + o.getWorld().getName());
            } else {
                return axis == Axis.X ? NumberConversions.square(b.getX() - o.getX()) : axis == Axis.Y ? NumberConversions.square(b.getY() - o.getY()) : NumberConversions.square(b.getZ() - o.getZ());
            }
        } else {
            throw new IllegalArgumentException("Cannot measure distance to a null world");
        }
    }


    public enum Axis {

        X, Y, Z

    }

    public static final class PacketParticle {

        private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        private static final double DOUBLE_VERSION;
        static {
            String version = VERSION.replace("v", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("_", ".");
            DOUBLE_VERSION = Double.valueOf(version.trim().substring(0, version.length()-2));
        }

        private static Class<?> getNMSClass(String nmsClassString) {
            try{
                String name = "net.minecraft.server." + VERSION + nmsClassString;
                Class<?> nmsClass = Class.forName(name);
                return nmsClass;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private static Class getParticleClass(){
            if(DOUBLE_VERSION >= 1.13){
                return getNMSClass("Particles");
            }
            return getNMSClass("EnumParticle");
        }

        public static Object fromName(String particleName) {
            try {
                return getParticleClass().getField(particleName.toUpperCase()).get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Object buildPacket(Object particle, double x, double y, double z, float xOffset, float yOffset, float zOffset, float speed, int count, int yReduced){
            try{
                if(DOUBLE_VERSION >= 1.13){
                    Constructor constructor = getNMSClass("PacketPlayOutWorldParticles").getConstructor(getNMSClass("ParticleParam"), boolean.class, double.class,
                            double.class, double.class, float.class, float.class, float.class, float.class, int.class);
                    return constructor.newInstance(particle, true, x, y-yReduced, z, xOffset, yOffset, zOffset, speed, count);
                }
                Constructor constructor = getNMSClass("PacketPlayOutWorldParticles").getConstructor(getNMSClass("EnumParticle"), boolean.class, float.class,
                        float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);
                return constructor.newInstance(particle, true, (float) x, (float) y-yReduced, (float) z, xOffset, yOffset, zOffset, speed, count, new int[0]);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public static void sendPacket(Entity nearbyEntity, Object object) {
            try{
                Object craftPlayer = nearbyEntity.getClass().getMethod("getHandle").invoke(nearbyEntity);
                Object connection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
                connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, object);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public static String getParticleNameFromNative(String name) {
            if(DOUBLE_VERSION == 1.13) {
                return "a";
            }else  if(DOUBLE_VERSION > 1.13) {
                switch (name){
                    case "EXPLOSION_LARGE": return "EXPLOSION";
                    case "SMOKE_LARGE": return "LARGE_SMOKE";
                    case "SMOKE_NORMAL": return "SMOKE";
                    case "EXPLOSION_HUGE": return "EXPLOSION_EMITTER";
                    case "TOWN_AURA": return "TOTEM_OF_UNDYING";
                    case "SPELL_WITCH": return "WITCH";
                }
            }
            return name;
        }
    }

    public static double offSet(double d, int offSet){
        double dd = RANDOM.nextInt(offSet)/100 + RANDOM.nextDouble();
        return d+(RANDOM.nextBoolean() ? - dd: dd);
    }

    public static class FollowingParticle {

        private final Bonus bonus;
        private Entity target = null;
        private Location locationTarget = null;
        private final String enumParticle;
        private final Location start;
        private final int speed;

        private boolean isDone = false;
        private boolean stopOnTouchTarget = true;
        private boolean isOnTarget = false;

        public FollowingParticle(Bonus bonus, Entity target, String enumParticle, Location start, int speed) {
            this.bonus = bonus;
            this.target = target;
            this.enumParticle = enumParticle;
            this.start = start;
            this.speed = speed;

            this.start.setX(offSet(start.getX(), 250));
            this.start.setY(offSet(start.getY(), 150));
            this.start.setZ(offSet(start.getZ(), 250));
        }

        public FollowingParticle(Bonus bonus, Location target, String enumParticle, Location start, int speed) {
            this.bonus = bonus;
            this.locationTarget = target;
            this.enumParticle = enumParticle;
            this.start = start;
            this.speed = speed;

            this.start.setX(offSet(start.getX(), 250));
            this.start.setY(offSet(start.getY(), 150));
            this.start.setZ(offSet(start.getZ(), 250));
        }

        public void onAsyncParticleTouchTarget(Entity target) { }
        
        public FollowingParticle start(){
            Runnable runnable = () -> {
                if(this.isDone){
                    return;
                }
                this.effect();
                this.start();
            };
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
            return this;
        }

        public void effect(){
            if(this.target != null && this.target.isDead()){
                this.isDone = true;
                return;
            }

            boolean skipAdd = false;

            if((this.target != null && this.target.getLocation().distanceSquared(this.start) < 0.49) || (this.locationTarget != null && this.locationTarget.distanceSquared(this.start) < 0.49)){
                if(stopOnTouchTarget){
                    this.isDone = true;
                    this.onAsyncParticleTouchTarget(target);
                    return;
                }
                this.isOnTarget = true;
                skipAdd = true;
            }

            if(!skipAdd){
                double xToAdd = Math.abs(this.start.getX()-(this.target == null ? this.locationTarget.getX() : this.target.getLocation().getX()))/this.speed;
                double yToAdd = Math.abs(this.start.getY()-(this.target == null ? this.locationTarget.getY() : this.target.getLocation().getY()))/this.speed;
                double zToAdd = Math.abs(this.start.getZ()-(this.target == null ? this.locationTarget.getZ() : this.target.getLocation().getZ()))/this.speed;
                this.start.add(this.needToAddPositive(Axis.X) ? xToAdd : -xToAdd,
                        this.needToAddPositive(Axis.Y) ? yToAdd : -yToAdd,
                        this.needToAddPositive(Axis.Z) ? zToAdd : -zToAdd);
            }

            this.bonus.sendParticle(this.start, this.enumParticle, 0, 0, 0, 1);
        }

        private boolean needToAddPositive(Axis axis){
            switch (axis){
                case X:
                    return !(this.start.getX()-(this.target == null ? this.locationTarget.getX() : this.target.getLocation().getX()) > 0);
                case Y:
                    return !(this.start.getY()-(this.target == null ? this.locationTarget.getY() : this.target.getLocation().getY()) > 0);
                default:
                    return !(this.start.getZ()-(this.target == null ? this.locationTarget.getZ() : this.target.getLocation().getZ()) > 0);
            }
        }

        public boolean isDone() {
            return this.isDone;
        }

        public void setDone(boolean done) {
            isDone = done;
        }

        public boolean isOnTarget() {
            return isOnTarget;
        }

        public void setLocationTarget(Location locationTarget) {
            this.locationTarget = locationTarget;
            this.isOnTarget = false;
        }

        public FollowingParticle setStopOnTouchTarget(boolean stopOnTouchTarget) {
            this.stopOnTouchTarget = stopOnTouchTarget;
            return this;
        }

        public void setStart(Location start){
            this.start.setX(offSet(start.getX(), 250));
            this.start.setY(offSet(start.getY(), 150));
            this.start.setZ(offSet(start.getZ(), 250));
        }
    }

    public static class FollowingParticlePath {

        private final List<Location> locations;
        private final Utils.FollowingParticle[] particles;
        private int currentIndex;

        private boolean isCancelled = false;

        public FollowingParticlePath(List<Location> locations, Utils.FollowingParticle[] particles, int startIndex) {
            this.locations = locations;
            this.currentIndex = startIndex;
            this.particles = particles;
        }

        public FollowingParticlePath start(){
            Runnable runnable = () -> {

                if(isCancelled){
                    for (FollowingParticle particle : this.particles) {
                        particle.setDone(true);
                    }
                    return;
                }

                boolean next = false;
                for (Utils.FollowingParticle particle : particles) {
                    if(particle.isOnTarget()){
                        next = true;
                    }else{
                        next = false;
                    }
                }

                this.start();

                if(!next){
                    return;
                }

                this.currentIndex++;
                if(this.currentIndex >= locations.size()){
                    this.currentIndex = 0;
                }
                for (Utils.FollowingParticle particle : this.particles) {
                    particle.setLocationTarget(locations.get(this.currentIndex));
                }
            };
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(runnable);
            return this;
        }

        public void setCancelled(boolean cancelled) {
            this.isCancelled = cancelled;
        }
    }

}
