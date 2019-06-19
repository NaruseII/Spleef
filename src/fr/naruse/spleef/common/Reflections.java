package fr.naruse.spleef.common;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.manager.SpleefPluginV1_12;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflections {

    public static void setNoGravity(Entity e, boolean isNoGravity) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> craftEntityClass = getCBClass("entity.CraftEntity");
        Method getHandle = craftEntityClass.getMethod("getHandle");
        Object craftEntity = getHandle.invoke(e);
        Method getNavigation = getNMSClass("EntityArmorStand").getMethod("setNoGravity", boolean.class);
        getNavigation.invoke(craftEntity, isNoGravity);
    }

    public static void setInvisible(Entity e, boolean isInvislbe) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> craftEntityClass = getCBClass("entity.CraftEntity");
        Method getHandle = craftEntityClass.getMethod("getHandle");
        Object craftEntity = getHandle.invoke(e);
        Method getNavigation = getNMSClass("EntityArmorStand").getMethod("setInvisible", boolean.class);
        getNavigation.invoke(craftEntity, isInvislbe);
    }

    private static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private static Class<?> getCBClass(String cbClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version +cbClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    public static Selection getSelection(SpleefPlugin pl, Player p) {
        try{
            WorldEditPlugin worldEditPlugin = ((SpleefPluginV1_12) pl.getSpleefPlugin()).worldEditPlugin;
            Class worldEditPluginClass = worldEditPlugin.getClass();

            Method getSelectionMethod = worldEditPluginClass.getDeclaredMethod("getSelection", Player.class);
            Selection selection = (Selection) getSelectionMethod.invoke(worldEditPlugin, p);
            return selection;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
