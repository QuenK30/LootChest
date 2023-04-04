package fr.quenk.lootchest;/*
 * Project: LootChest
 * Date: 02/04/2023
 * Author: QuenK
 */

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class ChestUtils {

    public ItemStack getCustomTextureHead(String value, int amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    public void getChestList(Player player) {
        player.sendMessage("§7[§a!§7] §aLootChest §7- §aListe des coffres :");

        // Récupération de la liste des coffres
        ConfigurationSection chestsSection = LootChest.getInstance().getChestConfig().getConfigurationSection("chest");

        // Parcours de chaque coffre et affichage de son nom et de ses items
        if (chestsSection != null) {
            for (String chestKey : chestsSection.getKeys(false)) {
                ConfigurationSection chestSection = chestsSection.getConfigurationSection(chestKey);
                if (chestSection != null) {
                    String chestName = chestSection.getString("name");
                    String special = chestSection.getString("special");
                    int chance = chestSection.getInt("chance");
                    List<String> itemsList = chestSection.getStringList("items");

                    // Construction du message à afficher
                    StringBuilder messageBuilder = new StringBuilder();
                    if(special != null && special.equalsIgnoreCase("true")) {
                        messageBuilder.append("§6- §e").append(chestName).append(" ").append(chance).append("%").append(" (§aSpécial§e) : ");
                    } else {
                        messageBuilder.append("§6- §e").append(chestName).append(" ").append(chance).append("%").append(" : ");
                    }
                    for (String itemString : itemsList) {
                        String[] itemSplit = itemString.split(";");
                        if (itemSplit.length == 2) {
                            messageBuilder.append(itemSplit[1]).append("x ").append(itemSplit[0]).append(", ");
                        }else {
                            if(itemSplit.length == 3) {
                                // si le nombre d'item est égal à 3, alors c'est un item avec un nom custom
                                messageBuilder.append(itemSplit[1]).append("x ").append(itemSplit[0]).append(" (").append(itemSplit[2]).append("§e), ");
                            }else{
                                if(itemSplit.length == 4) {
                                    // si le nombre d'item est égal à 4, alors c'est un item avec un nom custom et une texture custom
                                    messageBuilder.append(itemSplit[1]).append("x ").append(itemSplit[0]).append(" (").append(itemSplit[2]).append("§e), ");
                                }
                            }
                        }
                    }
                    String message = messageBuilder.toString().trim();
                    if (message.endsWith(",")) {
                        message = message.substring(0, message.length() - 1);
                    }

                    // Envoi du message au joueur
                    String messageWithColor = ChatColor.translateAlternateColorCodes('&', message);
                    player.sendMessage(messageWithColor);
                }
            }
        }
    }

    //Donner un coffre aléatoire a un joueur
    public String getRandomChestName() {
        double totalChance = 0.0; // total des chances pour tous les coffres
        Map<String, Double> chestChances = new HashMap<>(); // taux de drop de chaque coffre

        // Charger le fichier YAML et récupérer les taux de drop de chaque coffre
        File chestFile = new File(LootChest.getInstance().getDataFolder(), "chest.yml");
        if (chestFile.exists()) {
            FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chestFile);
            ConfigurationSection chests = chestConfig.getConfigurationSection("chest");

            for (String chestName : chests.getKeys(false)) {
                ConfigurationSection chest = chests.getConfigurationSection(chestName);
                double chance = chest.getInt("chance");
                boolean special = chest.getBoolean("special");
                if(!special) {
                    chestChances.put(chestName, chance);
                    totalChance += chance;
                }
            }
        }

        // Calculer le pourcentage de chaque coffre
        for (Map.Entry<String, Double> entry : chestChances.entrySet()) {
            double percentage = entry.getValue() / totalChance * 100.0;
            chestChances.put(entry.getKey(), percentage);
        }

        // Sélectionner un coffre aléatoire en fonction des pourcentages
        double rand = Math.random() * 100.0;
        double accum = 0.0;
        for (Map.Entry<String, Double> entry : chestChances.entrySet()) {
            accum += entry.getValue();
            if (rand <= accum) {
                return entry.getKey();
            }
        }

        // Si on arrive ici, c'est qu'il y a eu un problème avec les taux de drop
        return null;
    }

    public String getRandomChestNameSpecial(){
        double totalChance = 0.0; // total des chances pour tous les coffres
        Map<String, Double> chestChances = new HashMap<>(); // taux de drop de chaque coffre

        // Charger le fichier YAML et récupérer les taux de drop de chaque coffre
        File chestFile = new File(LootChest.getInstance().getDataFolder(), "chest.yml");
        if (chestFile.exists()) {
            FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chestFile);
            ConfigurationSection chests = chestConfig.getConfigurationSection("chest");

            for (String chestName : chests.getKeys(false)) {
                ConfigurationSection chest = chests.getConfigurationSection(chestName);
                double chance = chest.getDouble("chance");
                boolean special = chest.getBoolean("special");
                if(special){
                    chestChances.put(chestName, chance);
                    totalChance += chance;
                }
            }
        }

        // Calculer le pourcentage de chaque coffre
        for (Map.Entry<String, Double> entry : chestChances.entrySet()) {
            double percentage = entry.getValue() / totalChance * 100.0;
            chestChances.put(entry.getKey(), percentage);
        }

        // Sélectionner un coffre aléatoire en fonction des pourcentages
        double rand = Math.random() * 100.0;
        double accum = 0.0;
        for (Map.Entry<String, Double> entry : chestChances.entrySet()) {
            accum += entry.getValue();
            if (rand <= accum) {
                return entry.getKey();
            }
        }

        // Si aucun coffre n'a été sélectionné, retourner null
        return null;
    }

    //Ouvrir un inventaire avec les items d'un coffre aléatoire
    public Inventory openRandomChest(Player player) {
        // Récupérer le nom du coffre aléatoire
        String chestName = getRandomChestName();

        if(chestName == null) {
            player.sendMessage("§cErreur : aucun coffre n'a été trouvé");
            return null;
        }

        // Récupérer les items du coffre
        File chestFile = new File(LootChest.getInstance().getDataFolder(), "chest.yml");

        if (chestFile.exists()) {
            FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chestFile);
            ConfigurationSection chests = chestConfig.getConfigurationSection("chest");
            ConfigurationSection chest = chests.getConfigurationSection(chestName);
            List<String> items = chest.getStringList("items");

            // Créer l'inventaire
            Inventory inventory = Bukkit.createInventory(null, 27, "§aCoffre aléatoire");



            // Ajouter les items à l'inventaire
            for (String itemString : items) {
                String[] itemSplit = itemString.split(";");
                if (itemSplit.length == 2) {
                    // Si le nombre d'item est égal à 2, alors c'est un item normal
                    ItemStack item = new ItemStack(Material.getMaterial(itemSplit[0]), Integer.parseInt(itemSplit[1]));
                    inventory.setItem(inventory.firstEmpty(),item);
                }else {
                    if(itemSplit.length == 3) {
                        // si le nombre d'item est égal à 3, alors c'est un item avec un nom custom
                        ItemStack item = new ItemStack(Material.getMaterial(itemSplit[0]), Integer.parseInt(itemSplit[1]));
                        ItemMeta itemMeta = item.getItemMeta();
                        String name = ChatColor.translateAlternateColorCodes('&', itemSplit[2]);
                        itemMeta.setDisplayName(name);
                        item.setItemMeta(itemMeta);
                        inventory.setItem(inventory.firstEmpty(),item);
                    }else{
                        if(itemSplit.length == 4) {
                            // si le nombre d'item est égal à 4, alors c'est un item avec un nom custom et une texture custom
                            ItemStack item = getCustomTextureHead(itemSplit[3], Integer.parseInt(itemSplit[1]));
                            ItemMeta itemMeta = item.getItemMeta();
                            String name = ChatColor.translateAlternateColorCodes('&', itemSplit[2]);
                            itemMeta.setDisplayName(name);
                            item.setItemMeta(itemMeta);
                            inventory.setItem(inventory.firstEmpty(),item);
                        }
                    }
                }
            }
            System.out.println("Le coffre aléatoire est: " + chestName);
            // Vérifier si le coffre a un item spécial ou non
            ConfigurationSection itemSpecial = LootChest.getInstance().getChestConfig().getConfigurationSection("itemspecial");
            // boucle pour ajouter les items spéciaux
            if (itemSpecial != null) {

                for (String itemString : itemSpecial.getKeys(false)){
                    // récupérer les informations de l'item
                    String itemMaterial = itemSpecial.getString(itemString + ".type");
                    int position = itemSpecial.getInt(itemString + ".position");
                    int itemAmount = itemSpecial.getInt(itemString + ".amount");
                    String itemName = itemSpecial.getString(itemString + ".name");
                    List<String> itemLore = itemSpecial.getStringList(itemString + ".lore");
                    List<String> itemEnchant = itemSpecial.getStringList(itemString + ".enchantments");
                    boolean itemUnbreakable = itemSpecial.getBoolean(itemString + ".unbreakable");
                    List<String> attributes = itemSpecial.getStringList(itemString + ".attributes");
                    String coffre = itemSpecial.getString(itemString + ".chest");
                    boolean hideAttributes = itemSpecial.getBoolean(itemString + ".attributesinvisible");
                    boolean hideEnchantments = itemSpecial.getBoolean(itemString + ".enchantmentsinvisible");
                    if(coffre !=null && !coffre.equals(chestName)){
                        System.out.println("Le coffre n'est pas le bon");
                        continue;
                    }
                    // créer l'item
                    ItemStack item = new ItemStack(Material.getMaterial(itemMaterial), itemAmount);
                    ItemMeta itemMeta = item.getItemMeta();
                    // ajouter le nom
                    String name = ChatColor.translateAlternateColorCodes('&', itemName);
                    itemMeta.setDisplayName(name);
                    // ajouter la lore
                    List<String> lore = new ArrayList<>();
                    for (String loreString : itemLore){
                        String loreName = ChatColor.translateAlternateColorCodes('&', loreString);
                        lore.add(loreName);
                    }
                    itemMeta.setLore(lore);
                    // ajouter les enchantements
                    for (String enchantString : itemEnchant){
                        System.out.println("Enchantement : " + enchantString);
                        String[] enchantSplit = enchantString.split(";");
                        if (enchantSplit.length == 2) {
                            String enchantName = enchantSplit[0];
                            int level = Integer.parseInt(enchantSplit[1]);
                            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                            if (enchant == null) {
                                System.out.println("Enchantement non trouvé : " + enchantName);
                                continue;
                            }
                            if (itemMeta instanceof EnchantmentStorageMeta) {
                                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
                                if(item.getType() == Material.ENCHANTED_BOOK){
                                    enchantmentStorageMeta.addStoredEnchant(enchant, level, true);
                                    enchantmentStorageMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                                    System.out.println("Enchantement ajouté : " + enchantName + " " + level);
                                }
                            } else {

                                itemMeta.addEnchant(enchant, level, true);
                                System.out.println("Enchantement ajouté : " + enchantName + " (" + enchant.getKey().getKey() + ") " + level);
                            }
                        }
                    }
                    // ajouter les attributs
                    for (String attributeString : attributes){
                        String[] attributeSplit = attributeString.split(";");
                        if (attributeSplit.length == 2) {
                            Attribute attribute = Attribute.valueOf(attributeSplit[0]);
                            double value = Double.parseDouble(attributeSplit[1]);
                            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attribute.name(), value, AttributeModifier.Operation.ADD_NUMBER);
                            itemMeta.addAttributeModifier(attribute, modifier);
                            // mettre invisible l'attribut
                            System.out.println("Attribut ajouté: " + attributeSplit[0] + " " + attributeSplit[1]);
                        }
                    }
                    if(hideAttributes){
                        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        System.out.println("Attributs cachés sur: " + itemString);
                    }
                    if(hideEnchantments){
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        System.out.println("Enchantements cachés sur: " + itemString);
                    }
                    // ajouter si l'item est indestructible
                    if (itemUnbreakable){
                        itemMeta.setUnbreakable(true);
                    }
                    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    // ajouter les meta à l'item
                    item.setItemMeta(itemMeta);
                    // ajouter l'item à l'inventaire
                    inventory.setItem(position,item);
                }
            }

            //vérifier si le coffre contient une potion
            ConfigurationSection potionSpecial = LootChest.getInstance().getChestConfig().getConfigurationSection("potion");
            // boucle pour ajouter les potions spéciales
            if(potionSpecial != null){
                for(String potionString : potionSpecial.getKeys(false)){
                    String potionType = potionSpecial.getString(potionString + ".type");
                    int potionPosition = potionSpecial.getInt(potionString + ".position");
                    int potionAmount = potionSpecial.getInt(potionString + ".amount");
                    String potionName = potionSpecial.getString(potionString + ".name");
                    List<String> potionLore = potionSpecial.getStringList(potionString + ".lore");
                    List<String> potionEffect = potionSpecial.getStringList(potionString + ".effect");
                    String potionChest = potionSpecial.getString(potionString + ".chest");
                    boolean hidePotionEffect = potionSpecial.getBoolean(potionString + ".particles");
                    boolean hidePotionIcon = potionSpecial.getBoolean(potionString + ".icon");
                    boolean hideEncantments = potionSpecial.getBoolean(potionString + ".hideenchantments");
                    boolean hideAttributes = potionSpecial.getBoolean(potionString + ".hideattributes");
                    List<String> attributes = potionSpecial.getStringList(potionString + ".attributes");
                    List<String> enchants = potionSpecial.getStringList(potionString + ".enchantments");
                    boolean hideEffect = potionSpecial.getBoolean(potionString + ".hideeffects");

                    if(potionChest !=null && !potionChest.equals(chestName)){
                        System.out.println("Le coffre n'est pas le bon");
                        continue;
                    }

                    //créer la potion
                    ItemStack potion = new ItemStack(Material.getMaterial(potionType), potionAmount);
                    PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
                    //ajouter le nom
                    String name = ChatColor.translateAlternateColorCodes('&', potionName);
                    potionMeta.setDisplayName(name);
                    //ajouter la lore
                    List<String> lore = new ArrayList<>();
                    for (String loreString : potionLore){
                        String loreName = ChatColor.translateAlternateColorCodes('&', loreString);
                        lore.add(loreName);
                    }
                    potionMeta.setLore(lore);
                    //ajouter les effets
                    for (String effectString : potionEffect){
                        String[] effectSplit = effectString.split(";");
                        if (effectSplit.length == 3) {
                            String effectName = effectSplit[0];
                            int level = Integer.parseInt(effectSplit[1]);
                            int duration = Integer.parseInt(effectSplit[2]);
                            PotionEffectType effect = PotionEffectType.getByName(effectName);
                            System.out.println("Effet : " + effectName + " " + level + " " + duration);
                            if (effect == null) {
                                System.out.println("Effet invalide : " + effectName);
                            } else {
                                potionMeta.addCustomEffect(new PotionEffect(effect, duration, level,true,hidePotionEffect,hidePotionIcon), true);
                                System.out.println("Effet ajouté : " + effectName + " " + level + " " + duration + " " + hidePotionEffect + " " + hidePotionIcon);
                            }
                        }
                    }

                    //ajouter les enchantements
                    for (String enchantString : enchants){
                        String[] enchantSplit = enchantString.split(";");
                        if (enchantSplit.length == 2) {
                            String enchantName = enchantSplit[0];
                            int level = Integer.parseInt(enchantSplit[1]);
                            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                            if (enchant == null) {
                                System.out.println("Enchantement invalide : " + enchantName);
                            } else {
                                potionMeta.addEnchant(enchant, level, true);
                                System.out.println("Enchantement ajouté : " + enchantName + " (" + enchant.getKey().getKey() + ") " + level);

                            }
                        }
                    }

                    //ajouter les attributs
                    for (String attributeString : attributes){
                        String[] attributeSplit = attributeString.split(";");
                        if (attributeSplit.length == 2) {
                            Attribute attribute = Attribute.valueOf(attributeSplit[0]);
                            double value = Double.parseDouble(attributeSplit[1]);
                            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attribute.name(), value, AttributeModifier.Operation.ADD_NUMBER);
                            potionMeta.addAttributeModifier(attribute, modifier);
                            // mettre invisible l'attribut
                            System.out.println("Attribut ajouté: " + attributeSplit[0] + " " + attributeSplit[1]);
                        }
                    }


                    if (hideAttributes){
                        potionMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        System.out.println("Attributs cachés");
                    }


                    if (hideEncantments){
                        potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        System.out.println("Enchantements cachés");
                    }
                    if (hideEffect){
                        potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                        System.out.println("Effets cachés");
                    }


                    //ajouter les meta à la potion
                    potion.setItemMeta(potionMeta);
                    //ajouter la potion à l'inventaire
                    inventory.setItem(potionPosition,potion);

                }
            }

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            // Ouvrir l'inventaire
            return inventory;
        }
        return null;
    }

    public Inventory openSpecialRandomChest(Player player) {
        // Récupérer le nom du coffre aléatoire
        String chestName = getRandomChestNameSpecial();

        // Récupérer les items du coffre
        File chestFile = new File(LootChest.getInstance().getDataFolder(), "chest.yml");
        if (chestFile.exists()) {
            FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chestFile);
            ConfigurationSection chests = chestConfig.getConfigurationSection("chest");
            ConfigurationSection chest = chests.getConfigurationSection(chestName);
            List<String> items = chest.getStringList("items");



            // Créer l'inventaire
            Inventory inventory = Bukkit.createInventory(null, 27, "§aCoffre aléatoire");

            // Ajouter les items à l'inventaire
            for (String itemString : items) {
                String[] itemSplit = itemString.split(";");
                if (itemSplit.length == 2) {
                    // Si le nombre d'item est égal à 2, alors c'est un item normal
                    ItemStack item = new ItemStack(Material.getMaterial(itemSplit[0]), Integer.parseInt(itemSplit[1]));
                    inventory.setItem(inventory.firstEmpty(),item);
                }else {
                    if(itemSplit.length == 3) {
                        // si le nombre d'item est égal à 3, alors c'est un item avec un nom custom
                        ItemStack item = new ItemStack(Material.getMaterial(itemSplit[0]), Integer.parseInt(itemSplit[1]));
                        ItemMeta itemMeta = item.getItemMeta();
                        String name = ChatColor.translateAlternateColorCodes('&', itemSplit[2]);
                        itemMeta.setDisplayName(name);
                        item.setItemMeta(itemMeta);
                        inventory.setItem(inventory.firstEmpty(),item);
                    }else{
                        if(itemSplit.length == 4) {
                            // si le nombre d'item est égal à 4, alors c'est un item avec un nom custom et une texture custom
                            ItemStack item = getCustomTextureHead(itemSplit[3], Integer.parseInt(itemSplit[1]));
                            ItemMeta itemMeta = item.getItemMeta();
                            String name = ChatColor.translateAlternateColorCodes('&', itemSplit[2]);
                            itemMeta.setDisplayName(name);
                            item.setItemMeta(itemMeta);
                            inventory.setItem(inventory.firstEmpty(),item);
                        }
                    }
                }
            }
            System.out.println("Le coffre aléatoire est: " + chestName);
            int chance = chest.getInt("chance");
            if(chance <= 5){ //Si la chance est inférieur ou égale a 5
                for(Player players : Bukkit.getOnlinePlayers()){
                    players.playSound(players.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
            // Vérifier si le coffre a un item spécial ou non
            ConfigurationSection itemSpecial = LootChest.getInstance().getChestConfig().getConfigurationSection("itemspecial");
            // boucle pour ajouter les items spéciaux
            if (itemSpecial != null) {

                for (String itemString : itemSpecial.getKeys(false)){
                    // récupérer les informations de l'item
                    String itemMaterial = itemSpecial.getString(itemString + ".type");
                    int position = itemSpecial.getInt(itemString + ".position");
                    int itemAmount = itemSpecial.getInt(itemString + ".amount");
                    String itemName = itemSpecial.getString(itemString + ".name");
                    List<String> itemLore = itemSpecial.getStringList(itemString + ".lore");
                    List<String> itemEnchant = itemSpecial.getStringList(itemString + ".enchantments");
                    boolean itemUnbreakable = itemSpecial.getBoolean(itemString + ".unbreakable");
                    List<String> attributes = itemSpecial.getStringList(itemString + ".attributes");
                    String coffre = itemSpecial.getString(itemString + ".chest");
                    if(coffre !=null && !coffre.equals(chestName)){
                        System.out.println("Le coffre n'est pas le bon");
                        continue;
                    }
                    // créer l'item
                    ItemStack item = new ItemStack(Material.getMaterial(itemMaterial), itemAmount);
                    ItemMeta itemMeta = item.getItemMeta();
                    // ajouter le nom
                    String name = ChatColor.translateAlternateColorCodes('&', itemName);
                    itemMeta.setDisplayName(name);
                    // ajouter la lore
                    List<String> lore = new ArrayList<>();
                    for (String loreString : itemLore){
                        String loreName = ChatColor.translateAlternateColorCodes('&', loreString);
                        lore.add(loreName);
                    }
                    itemMeta.setLore(lore);
                    // ajouter les enchantements
                    for (String enchantString : itemEnchant){
                        System.out.println("Enchantement : " + enchantString);
                        String[] enchantSplit = enchantString.split(";");
                        if (enchantSplit.length == 2) {
                            String enchantName = enchantSplit[0];
                            int level = Integer.parseInt(enchantSplit[1]);
                            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                            if (enchant == null) {
                                System.out.println("Enchantement non trouvé : " + enchantName);
                                continue;
                            }
                            if (itemMeta instanceof EnchantmentStorageMeta) {
                                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
                                if(item.getType() == Material.ENCHANTED_BOOK){
                                    enchantmentStorageMeta.addStoredEnchant(enchant, level, true);
                                    enchantmentStorageMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                                    System.out.println("Enchantement ajouté : " + enchantName + " " + level);
                                }
                            } else {

                                itemMeta.addEnchant(enchant, level, true);
                                System.out.println("Enchantement ajouté : " + enchantName + " (" + enchant.getKey().getKey() + ") " + level);
                            }
                        }
                    }
                    // ajouter les attributs
                    for (String attributeString : attributes){
                        String[] attributeSplit = attributeString.split(";");
                        if (attributeSplit.length == 2) {
                            Attribute attribute = Attribute.valueOf(attributeSplit[0]);
                            double value = Double.parseDouble(attributeSplit[1]);
                            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attribute.name(), value, AttributeModifier.Operation.ADD_NUMBER);
                            itemMeta.addAttributeModifier(attribute, modifier);
                            // mettre invisible l'attribut
                            System.out.println("Attribut ajouté: " + attributeSplit[0] + " " + attributeSplit[1]);
                        }
                    }
                    boolean hideAttributes = itemSpecial.getBoolean(itemString + ".attributesinvisible");
                    boolean hideEnchantments = itemSpecial.getBoolean(itemString + ".enchantmentsinvisible");
                    if(hideAttributes){
                        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        System.out.println("Attributs cachés sur: " + itemString);
                    }
                    if(hideEnchantments){
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        System.out.println("Enchantements cachés sur: " + itemString);
                    }

                    // ajouter si l'item est indestructible
                    if (itemUnbreakable){
                        itemMeta.setUnbreakable(true);
                    }




                    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    // ajouter les meta à l'item

                    item.setItemMeta(itemMeta);
                    // ajouter l'item à l'inventaire
                    inventory.setItem(position,item);
                }
            }

            //vérifier si le coffre contient une potion
            ConfigurationSection potionSpecial = LootChest.getInstance().getChestConfig().getConfigurationSection("potion");
            // boucle pour ajouter les potions spéciales
            if(potionSpecial != null){
                for(String potionString : potionSpecial.getKeys(false)){
                    String potionType = potionSpecial.getString(potionString + ".type");
                    int potionPosition = potionSpecial.getInt(potionString + ".position");
                    int potionAmount = potionSpecial.getInt(potionString + ".amount");
                    String potionName = potionSpecial.getString(potionString + ".name");
                    List<String> potionLore = potionSpecial.getStringList(potionString + ".lore");
                    List<String> potionEffect = potionSpecial.getStringList(potionString + ".effect");
                    String potionChest = potionSpecial.getString(potionString + ".chest");
                    boolean hideEncantments = potionSpecial.getBoolean(potionString + ".hideenchantments");
                    boolean hideAttributes = potionSpecial.getBoolean(potionString + ".hideattributes");
                    List<String> attributes = potionSpecial.getStringList(potionString + ".attributes");
                    List<String> enchants = potionSpecial.getStringList(potionString + ".enchantments");
                    boolean hideEffects = potionSpecial.getBoolean(potionString + ".hideeffects");

                    if(potionChest !=null && !potionChest.equals(chestName)){
                        System.out.println("Le coffre n'est pas le bon");
                        continue;
                    }

                    //créer la potion
                    ItemStack potion = new ItemStack(Material.getMaterial(potionType), potionAmount);
                    PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
                    //ajouter le nom
                    String name = ChatColor.translateAlternateColorCodes('&', potionName);
                    potionMeta.setDisplayName(name);
                    //ajouter la lore
                    List<String> lore = new ArrayList<>();
                    for (String loreString : potionLore){
                        String loreName = ChatColor.translateAlternateColorCodes('&', loreString);
                        lore.add(loreName);
                    }
                    potionMeta.setLore(lore);
                    //ajouter les effets
                    for (String effectString : potionEffect){
                        String[] effectSplit = effectString.split(";");
                        if (effectSplit.length == 3) {
                            String effectName = effectSplit[0];
                            int level = Integer.parseInt(effectSplit[1]);
                            int duration = Integer.parseInt(effectSplit[2]);
                            PotionEffectType effect = PotionEffectType.getByName(effectName);
                            if (effect == null) {
                                System.out.println("Effet invalide : " + effectName);
                            } else {
                                potionMeta.addCustomEffect(new PotionEffect(effect, duration, level), true);
                                System.out.println("Effet ajouté : " + effectName + " " + level + " " + duration);
                            }
                        }
                    }
                    //ajouter les enchantements
                    for (String enchantString : enchants){
                        String[] enchantSplit = enchantString.split(";");
                        if (enchantSplit.length == 2) {
                            String enchantName = enchantSplit[0];
                            int level = Integer.parseInt(enchantSplit[1]);
                            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                            if (enchant == null) {
                                System.out.println("Enchantement invalide : " + enchantName);
                            } else {
                                potionMeta.addEnchant(enchant, level, true);
                                System.out.println("Enchantement ajouté : " + enchantName + " (" + enchant.getKey().getKey() + ") " + level);

                            }
                        }
                    }

                    //ajouter les attributs
                    for (String attributeString : attributes){
                        String[] attributeSplit = attributeString.split(";");
                        if (attributeSplit.length == 2) {
                            Attribute attribute = Attribute.valueOf(attributeSplit[0]);
                            double value = Double.parseDouble(attributeSplit[1]);
                            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), attribute.name(), value, AttributeModifier.Operation.ADD_NUMBER);
                            potionMeta.addAttributeModifier(attribute, modifier);
                            // mettre invisible l'attribut
                            System.out.println("Attribut ajouté: " + attributeSplit[0] + " " + attributeSplit[1]);
                        }
                    }



                    if (hideAttributes){
                        potionMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        System.out.println("Attributs cachés");
                    }

                    if (hideEncantments){
                        potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        System.out.println("Enchantements cachés");
                    }

                    if (hideEffects){
                        potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                        System.out.println("Effets cachés");
                    }



                    potion.setItemMeta(potionMeta);
                    // ajouter la potion à l'inventaire
                    inventory.setItem(potionPosition,potion);

                }
            }

            // Ouvrir l'inventaire
            return inventory;
        }
        return null;
    }

    // Afficher la liste des items spéciaux
    public void getSpecialItemsList(Player player){
        player.sendMessage("§7[§a!§7] §aLootChest §7- §aListe des items spéciaux :");

        // Récupération de la liste des coffres
        ConfigurationSection chestsSection = LootChest.getInstance().getChestConfig().getConfigurationSection("itemspecial");
        ConfigurationSection potionSection = LootChest.getInstance().getChestConfig().getConfigurationSection("potion");
        if(chestsSection != null && potionSection != null) {
            for (String iKey : chestsSection.getKeys(false)) {
                for (String pKey : potionSection.getKeys(false)) {
                    ConfigurationSection iSection = chestsSection.getConfigurationSection(iKey);
                    ConfigurationSection pSection = potionSection.getConfigurationSection(pKey);
                    if (iSection != null && pSection != null) {
                        String name = iSection.getString("name");
                        String type = iSection.getString("type");
                        int position = iSection.getInt("position");
                        String chestName = iSection.getString("chest");
                        List<String> lore = iSection.getStringList("lore");
                        List<String> enchants = iSection.getStringList("enchants");
                        boolean unbreakable = iSection.getBoolean("unbreakable");
                        int amount = iSection.getInt("amount");

                        //potion
                        String potionName = pSection.getString("name");
                        String potionType = pSection.getString("type");
                        int potionPosition = pSection.getInt("position");
                        String potionChestName = pSection.getString("chest");
                        List<String> potionLore = pSection.getStringList("lore");
                        List<String> potionEnchants = pSection.getStringList("effect");
                        int potionAmount = pSection.getInt("amount");

                        // Construction du message à afficher
                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append("§6- §e").append(name).append(" (").append(type).append(") : ");
                        messageBuilder.append("§6- §e").append("Position : ").append(position).append(" ");
                        messageBuilder.append("§6- §e").append("Coffre : ").append(chestName).append(" ");
                        messageBuilder.append("§6- §e").append("Lore : ").append(lore).append(" ");
                        messageBuilder.append("§6- §e").append("Enchantements : ").append(enchants).append(" ");
                        messageBuilder.append("§6- §e").append("Unbreakable : ").append(unbreakable).append(" ");
                        messageBuilder.append("§6- §e").append("Quantité : ").append(amount).append("\n");

                        //potion
                        messageBuilder.append("§6- §e").append(potionName).append(" (").append(potionType).append(") : ");
                        messageBuilder.append("§6- §e").append("Position : ").append(potionPosition).append(" ");
                        messageBuilder.append("§6- §e").append("Coffre : ").append(potionChestName).append(" ");
                        messageBuilder.append("§6- §e").append("Lore : ").append(potionLore).append(" ");
                        messageBuilder.append("§6- §e").append("Effets : ").append(potionEnchants).append(" ");
                        messageBuilder.append("§6- §e").append("Quantité : ").append(potionAmount).append("\n");

                        String messageWithColor = ChatColor.translateAlternateColorCodes('&', messageBuilder.toString());
                        player.sendMessage(messageWithColor);

                    }
                }
            }
        }
    }

}
