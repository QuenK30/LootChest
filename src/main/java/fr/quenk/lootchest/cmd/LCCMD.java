package fr.quenk.lootchest.cmd;/*
 * Project: LootChest
 * Date: 02/04/2023
 * Author: QuenK
 */

import fr.quenk.lootchest.ChestUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LCCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.hasPermission("lootchest.admin")){
                ChestUtils chestUtils = new ChestUtils();
                if(args.length == 0) {
                    player.sendMessage("§7[§a!§7] §aLootChest §7- §aCommands :");
                    player.sendMessage("§7[§a!§7] §a/lootchest §7- §aShows the commands");
                    player.sendMessage("§7[§a!§7] §a/lootchest list §7- §aShows the list of the chests");
                    player.sendMessage("§7[§a!§7] §a/lootchest give §7- §aGives you a LootChest");
                }else if(args.length == 1){
                    if(args[0].equalsIgnoreCase("list")){
                        chestUtils.getChestList(player);
                    }else if(args[0].equalsIgnoreCase("give")){
                        ItemStack itemStack = new ItemStack(Material.CHEST);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName("§aCoffre LootChest");
                        itemStack.setItemMeta(itemMeta);
                        player.getInventory().addItem(itemStack);
                        player.sendMessage("§7[§a!§7] §aYou have received a LootChest !");
                    }else{
                        player.sendMessage("§7[§c!§7] §cUnknown command !");
                    }
                }else{
                    if(args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("items")) {
                        chestUtils.getSpecialItemsList(player);
                    }
                }
            }else{
                player.sendMessage("§7[§c!§7] §cYou don't have the permission to use this command !");
            }
        }else {
            sender.sendMessage("§7[§c!§7] §cYou must be a player to use this command !");
        }
        return false;
    }
}
