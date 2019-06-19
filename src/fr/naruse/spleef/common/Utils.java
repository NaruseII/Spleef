package fr.naruse.spleef.common;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class Utils {

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

    public static boolean compare(Location a, Location b){
        if(a.getWorld() != b.getWorld()){
            return false;
        }
        if(a.getX() != b.getX()){
            return false;
        }
        if(a.getZ() != b.getZ()){
            return false;
        }
        if(a.getYaw() != b.getYaw()){
            return false;
        }
        if(a.getPitch() != b.getPitch()){
            return false;
        }
        return true;
    }

    public static double getBukkitVersion(){
        if(Bukkit.getVersion().contains("1.14")){
            return 1.14;
        }
        if(Bukkit.getVersion().contains("1.13")){
            return 1.13;
        }
        if(Bukkit.getVersion().contains("1.12")){
            return 1.12;
        }
        if(Bukkit.getVersion().contains("1.11")){
            return 1.11;
        }
        if(Bukkit.getVersion().contains("1.10")){
            return 1.10;
        }
        if(Bukkit.getVersion().contains("1.9")){
            return 1.9;
        }
        if(Bukkit.getVersion().contains("1.8")){
            return 1.8;
        }
        return 0.0;
    }
}
