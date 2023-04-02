package fr.quenk.lootchest.events;/*
 * Project: LootChest
 * Date: 02/04/2023
 * Author: QuenK
 */

import fr.quenk.lootchest.LootChest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class EventManager {
    private LootChest plugin;

    public EventManager(LootChest plugin) {
        this.plugin = plugin;
    }
    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new ChestListener(), plugin);
    }
}
