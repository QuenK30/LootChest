package fr.quenk.lootchest;

import fr.quenk.lootchest.cmd.LCCMD;
import fr.quenk.lootchest.events.EventManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class LootChest extends JavaPlugin {

    private FileConfiguration chestConfig;
    private File chestFile;

    private FileConfiguration chestLocConfig;
    private File chestLocFile;
    private static LootChest instance;

    @Override
    public void onLoad() {
        instance = this;
    }
    @Override
    public void onEnable() {
        getCommand("lootchest").setExecutor(new LCCMD());
        new EventManager(instance).registerEvents();
        createChestConfig();
        createChestLocConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void createChestConfig() {
        chestFile = new File(getDataFolder(), "chest.yml");
        if (!chestFile.exists()) {
            chestFile.getParentFile().mkdirs();
            saveResource("chest.yml", false);
        }
        chestConfig = new YamlConfiguration().loadConfiguration(chestFile);
    }

    public FileConfiguration getChestConfig() {
        return chestConfig;
    }

    public void createChestLocConfig() {
        chestLocFile = new File(getDataFolder(), "chest_location.yml");
        if (!chestLocFile.exists()) {
            chestLocFile.getParentFile().mkdirs();
            saveResource("chest_location.yml", false);
        }
        chestLocConfig = new YamlConfiguration().loadConfiguration(chestLocFile);
    }

    public FileConfiguration getChestLocConfig() {
        return chestLocConfig;
    }

    public static LootChest getInstance() {
        return instance;
    }

    public void saveChestLocConfig() {
        try {
            chestLocConfig.save(chestLocFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
