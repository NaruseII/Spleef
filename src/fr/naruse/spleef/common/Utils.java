package fr.naruse.spleef.common;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
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

    public static long fileSize(String host){
        try {
            URL url = new URL(host);
            URLConnection connection = url.openConnection();
            int fileLength = connection.getContentLength();
            return fileLength;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long byteToMB(long bytes){
        DecimalFormat df = new DecimalFormat("0.######");
        return Long.valueOf(df.format(bytes*0.000001));
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static File downloadFile(String host, File dest){
        try{
            InputStream is = null;
            OutputStream os = null;
            try {
                Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §dDownloading new Spleef.jar...");
                if(dest.exists()){
                    if(dest.length() == Utils.fileSize(host)){
                        return dest;
                    }else{
                        dest.delete();
                    }
                }
                URL url = new URL(host);
                URLConnection connection = url.openConnection();
                int fileLength = connection.getContentLength();
                if (fileLength == -1) {
                    System.out.println("Invalide URL or file.");
                    return null;
                }
                is = connection.getInputStream();
                os = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                int count = 0;
                DecimalFormat df = new DecimalFormat("0.##");
                int loop = 0;
                while ((length = is.read(buffer)) > 0) {
                    count += length;
                    if(loop >= 50){
                        loop = 0;
                        Bukkit.getConsoleSender().sendMessage("§c§l[§3Spleef§c§l] §dDownloading...("+df.format(count*0.000001)+" MB/"+df.format(fileLength*0.000001)+" MB)");
                    }else{
                        loop++;
                    }
                    os.write(buffer, 0, length);
                }
            } finally {
                if(is != null){
                    is.close();
                    os.close();
                }
            }
            return dest;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
