package fr.naruse.spleef.spleef.bonus.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;

public class MoveToGoal {
    private Entity entity;
    private Location location;
    public MoveToGoal(Entity entity, Location location){
        this.entity = entity;
        this.location = location;
    }

    private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private Class<?> getCBClass(String cbClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version +cbClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public void execute(double speed) {
        try{
            Class<?> craftEntityClass = this.getCBClass("entity.CraftEntity");
            Method getHandle = craftEntityClass.getMethod("getHandle");
            Object craftEntity = getHandle.invoke(entity);
            Class<?> craftEntityInsentient = this.getNMSClass("EntityInsentient");
            Object entityInsentient = craftEntityInsentient.cast(craftEntity);
            Method getNavigation = craftEntityInsentient.getMethod("getNavigation");
            Object nav = getNavigation.invoke(entityInsentient);
            Method a = nav.getClass().getMethod("a", double.class, double.class, double.class);
            Object path = a.invoke(nav, location.getX(), location.getY(), location.getZ());
            if(path != null){
                a = nav.getClass().getMethod("a", path.getClass(), double.class);
                a.invoke(nav, path, speed);
            }
        }catch (Exception e){
            try{
                Class<?> craftEntityClass = this.getCBClass("entity.CraftEntity");
                Method getHandle = craftEntityClass.getMethod("getHandle");
                Object craftEntity = getHandle.invoke(entity);
                Class<?> craftEntityInsentient = this.getNMSClass("EntityInsentient");
                Object entityInsentient = craftEntityInsentient.cast(craftEntity);
                Method getNavigation = craftEntityInsentient.getMethod("getNavigation");
                Object nav = getNavigation.invoke(entityInsentient);
                Method a = nav.getClass().getMethod("a", double.class, double.class, double.class, int.class);
                Object path = a.invoke(nav, location.getX(), location.getY(), location.getZ(), 1);
                if(path != null){
                    a = nav.getClass().getMethod("a", path.getClass(), double.class);
                    a.invoke(nav, path, speed);
                }
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }

}
