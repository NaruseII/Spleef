package fr.naruse.spleef.utils;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
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
            try{
                WIN_ITEM = ItemStack.deserialize(StatisticBuilder.GSON.fromJson(pl.getConfig().getString("reward.winItem"), StatisticBuilder.MAP_TYPE));
                Map<String, Object> map = StatisticBuilder.GSON.fromJson(pl.getConfig().getString("reward.winItemEnchants"), StatisticBuilder.MAP_TYPE);
                if(map != null){
                    map.forEach((name, i) -> WIN_ITEM.addUnsafeEnchantment(Enchantment.getByName(name), (int) Double.parseDouble(i.toString())));
                }
            }catch (Exception e){
                pl.getLogger().severe("Could not load WIN ITEM.");
            }
        }
    }

    public static Location getLocation(SpleefPlugin pl, String path) {
        if(!pl.getConfig().contains(path+".world")){
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
            }else if (javaVersion.startsWith("1.10")) {
                map.put("Java 1.10", entry);
            }else if (javaVersion.startsWith("1.11")) {
                map.put("Java 1.11", entry);
            }else if (javaVersion.startsWith("1.12")) {
                map.put("Java 1.12", entry);
            }else if (javaVersion.startsWith("1.13")) {
                map.put("Java 1.13", entry);
            }else if (javaVersion.startsWith("1.14")) {
                map.put("Java 1.14", entry);
            }else if (javaVersion.startsWith("1.15")) {
                map.put("Java 1.15", entry);
            }else if (javaVersion.startsWith("1.16")) {
                map.put("Java 1.16", entry);
            } else if (javaVersion.startsWith("1.17")) {
                map.put("Java 1.17", entry);
            } else if (javaVersion.startsWith("1.18")) {
                map.put("Java 1.18", entry);
            }else {
                map.put("Other", entry);
            }
            return map;
        }));
        metrics.addCustomChart(new Metrics.SimplePie("sheep_bonuses_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                for (Spleef spleef : pl.getSpleefs().getSpleefs()) {
                    if(spleef.getBonusManager() != null){
                        return "true";
                    }
                }
                return "false";
            }
        }));
        metrics.addCustomChart(new Metrics.SingleLineChart("arena_count", () -> pl.getSpleefs().getSpleefs().size()));
        metrics.addCustomChart(new Metrics.SingleLineChart("player_playing_count", () -> {
            int count = 0;
            for (Spleef spleef : pl.getSpleefs().getSpleefs()) {
                count += spleef.getPlayerInGame().size();
            }
            return count;
        }));
        metrics.addCustomChart(new Metrics.SimplePie("servers_name", () -> {
           if(pl.getConfig().getBoolean("allowShowIP2")){
               InetAddress address = getCurrentAddress();
               if(address == null){
                   return "offline";
               }
               return address.getHostAddress()+":"+Bukkit.getPort();
           }
           return "not shown";
        }));
    }

    private static InetAddress getCurrentAddress() {
        try{
            final Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress();
        }catch (Exception e){
            //e.printStackTrace();
        }
        return null;
    }

}
