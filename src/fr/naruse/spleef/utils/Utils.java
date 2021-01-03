package fr.naruse.spleef.utils;

import com.google.common.collect.Lists;
import fr.naruse.spleef.main.SpleefPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class Utils {

    public static final Random RANDOM = new Random();
    public static ItemStack LEAVE_ITEM;
    public static final ItemStack SPADE_ITEM;
    public static final ItemStack SNOWBALL;
    public static final ItemStack BOW;

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
            } else if (Double.doubleToLongBits(a.getY()) != Double.doubleToLongBits(other.getY())) {
                return false;
            } else if (Math.abs(Double.doubleToLongBits(a.getZ()) - Double.doubleToLongBits(other.getZ())) > 3) {
                return false;
            } else if (Float.floatToIntBits(a.getPitch()) != Float.floatToIntBits(other.getPitch())) {
                return false;
            } else {
                return Float.floatToIntBits(a.getYaw()) == Float.floatToIntBits(other.getYaw());
            }
        }
    }

    public static List<Block> getCircle(Location center, int r){
        final List<Block> list = Lists.newArrayList();
        for(double x = -r; x <= r; x++){
            for(double z = -r; z <= r; z++){
                if((int) center.clone().add(x, 0, z).distance(center) == r){
                    list.add(center.clone().add(x, 0, z).getBlock());
                }
            }
        }
        return list;
    }
}
