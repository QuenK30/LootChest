package fr.quenk.lootchest.events;/*
 * Project: LootChest
 * Date: 02/04/2023
 * Author: QuenK
 */

import fr.quenk.lootchest.ChestUtils;
import fr.quenk.lootchest.LootChest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ChestListener implements Listener {

    @EventHandler
    public void onInteractWithShulker(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ChestUtils chestUtils = new ChestUtils();
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (block != null && block.getType().toString().contains("CHEST")) {
                Location location = block.getLocation();
                FileConfiguration chestLocConfig = LootChest.getInstance().getChestLocConfig();
                if (chestLocConfig.contains("chest")) {
                    ConfigurationSection chests = chestLocConfig.getConfigurationSection("chest");
                    for (String key : chests.getKeys(false)) {
                        ConfigurationSection chest = chests.getConfigurationSection(key);
                        int x = chest.getInt("x");
                        int y = chest.getInt("y");
                        int z = chest.getInt("z");
                        String world = chest.getString("world");
                        boolean open = chest.getBoolean("open");
                        if (x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ() && world.equalsIgnoreCase(location.getWorld().getName())) {
                            if(open){
                                player.sendMessage("§7[§c!§7] §cCe coffre a déjà été ouvert !");
                                return;
                            }
                            if(player.getInventory().getItemInMainHand().getType() == Material.BEDROCK){
                                player.openInventory(chestUtils.openSpecialRandomChest(player));
                                System.out.println("Special chest opened at location " + location + " with ID " + key);
                                LootChest.getInstance().getChestLocConfig().set("chest." + key + ".open", true);
                                event.setCancelled(true);
                                return; // Sortir de la boucle for dès qu'un coffre est ouvert
                            }
                            player.openInventory(chestUtils.openRandomChest(player));
                            System.out.println("Chest opened at location " + location+ " with ID " + key);
                            LootChest.getInstance().getChestLocConfig().set("chest." + key + ".open", true);
                            event.setCancelled(true);
                            return; // Sortir de la boucle for dès qu'un coffre est ouvert
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlaceShulker(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();
        if(block.getType().toString().contains("CHEST") && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§aCoffre LootChest")){
            if(!LootChest.getInstance().getChestLocConfig().contains("chest.1")){
                LootChest.getInstance().getChestLocConfig().set("chest.1.x", block.getX());
                LootChest.getInstance().getChestLocConfig().set("chest.1.y", block.getY());
                LootChest.getInstance().getChestLocConfig().set("chest.1.z", block.getZ());
                LootChest.getInstance().getChestLocConfig().set("chest.1.world", block.getWorld().getName());
                LootChest.getInstance().getChestLocConfig().set("chest.1.open", false);
                LootChest.getInstance().saveChestLocConfig();
            }else{
                int i = 1;
                while(LootChest.getInstance().getChestLocConfig().contains("chest." + i)){
                    i++;
                }
                LootChest.getInstance().getChestLocConfig().set("chest." + i + ".x", block.getX());
                LootChest.getInstance().getChestLocConfig().set("chest." + i + ".y", block.getY());
                LootChest.getInstance().getChestLocConfig().set("chest." + i + ".z", block.getZ());
                LootChest.getInstance().getChestLocConfig().set("chest." + i + ".world", block.getWorld().getName());
                LootChest.getInstance().getChestLocConfig().set("chest." + i + ".open", false);
                LootChest.getInstance().saveChestLocConfig();
            }
            player.sendMessage("§7[§a!§7] §aTu as placé un coffre LootChest !");
        }
    }

    @EventHandler
    public void onBreakChest(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if(block.getType().toString().contains("CHEST")){
            Location location = block.getLocation();
            FileConfiguration chestLocConfig = LootChest.getInstance().getChestLocConfig();
            if (chestLocConfig.contains("chest")) {
                ConfigurationSection chests = chestLocConfig.getConfigurationSection("chest");
                for (String key : chests.getKeys(false)) {
                    ConfigurationSection chest = chests.getConfigurationSection(key);
                    int x = chest.getInt("x");
                    int y = chest.getInt("y");
                    int z = chest.getInt("z");
                    String world = chest.getString("world");
                    if (x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ() && world.equalsIgnoreCase(location.getWorld().getName())) {
                        LootChest.getInstance().getChestLocConfig().set("chest." + key, null);
                        LootChest.getInstance().saveChestLocConfig();
                        player.sendMessage("§7[§a!§7] §aTu as détruit un coffre LootChest !");
                    }
                }
            }
        }
    }
}
