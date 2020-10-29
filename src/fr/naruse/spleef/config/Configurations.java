package fr.naruse.spleef.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class Configurations {
    private JavaPlugin pl;

    private File messageFile;
    private FileConfiguration messageConfiguration;

    public Configurations(JavaPlugin pl) {
        this.pl = pl;
        reload();
    }

    public void reload() {
        this.messageFile = new File(pl.getDataFolder(), "messages.yml");
        this.messageConfiguration = new YamlConfiguration();

        try{
            if(!messageFile.exists()){
                messageFile.createNewFile();
                saveResource("resources/messages.yml", messageFile);
            }

            messageConfiguration.load(messageFile);

            Reader reader = new InputStreamReader(pl.getResource("resources/messages.yml"), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            messageConfiguration.setDefaults(defConfig);
        }catch (Exception e){
            e.printStackTrace();
        }

        saveConfigs();
    }

    private void saveResource(String path, File messageFile) {
        try{
            InputStream inputStream = pl.getResource(path);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            OutputStream outStream = new FileOutputStream(messageFile);
            outStream.write(buffer);

            inputStream.close();
            outStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void saveConfigs() {
        try{
            messageConfiguration.save(messageFile);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void reset(int id) {
        if(id == 0){
            if(messageFile.exists()){
                messageFile.delete();
            }
        }
        reload();
    }

    public FileConfiguration getMessageConfiguration() {
        return messageConfiguration;
    }
}
