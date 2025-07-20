package com.example.minecraftplugin.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Shard of Atonement - one of 7 components needed to create the Key to Redemption
 */
public class ShardOfAtonement {
    
    private static final Material SHARD_MATERIAL = Material.AMETHYST_SHARD;
    private static final int TOTAL_SHARDS = 7;
    
    // Unique names for each shard
    private static final String[] SHARD_NAMES = {
        "Shard of Forgiveness",
        "Shard of Sacrifice", 
        "Shard of Courage",
        "Shard of Compassion",
        "Shard of Wisdom",
        "Shard of Justice",
        "Shard of Hope"
    };
    
    // Unique lore for each shard
    private static final String[][] SHARD_LORE = {
        {"§7A crystalline fragment that pulses with", "§7the gentle light of divine forgiveness.", "§8\"To forgive is to free oneself.\""},
        {"§7A shard that glows with the warmth of", "§7selfless sacrifice for others.", "§8\"True strength lies in giving.\""},
        {"§7A fragment that radiates the bold", "§7energy of unwavering courage.", "§8\"Fear is conquered by action.\""},
        {"§7A shard that emanates the soft glow", "§7of boundless compassion.", "§8\"Understanding heals all wounds.\""},
        {"§7A crystalline piece that shimmers with", "§7the deep knowledge of ancient wisdom.", "§8\"Knowledge without wisdom is hollow.\""},
        {"§7A fragment that burns with the righteous", "§7fire of divine justice.", "§8\"Justice tempered by mercy.\""},
        {"§7A shard that gleams with the eternal", "§7light of unbreakable hope.", "§8\"Hope is the last thing to die.\""}
    };
    
    /**
     * Create an ItemStack for a specific Shard of Atonement
     * @param shardNumber The shard number (1-7)
     * @return ItemStack representing the shard
     */
    public static ItemStack createShard(int shardNumber) {
        if (shardNumber < 1 || shardNumber > TOTAL_SHARDS) {
            throw new IllegalArgumentException("Shard number must be between 1 and " + TOTAL_SHARDS);
        }
        
        ItemStack shard = new ItemStack(SHARD_MATERIAL);
        ItemMeta meta = shard.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName("§d§l✦ " + SHARD_NAMES[shardNumber - 1] + " ✦§r");
            
            // Set lore
            List<String> lore = Arrays.asList(SHARD_LORE[shardNumber - 1]);
            lore.add("");
            lore.add("§7Shard: §f" + shardNumber + " §7of §f" + TOTAL_SHARDS);
            lore.add("§e§lCombine all 7 shards at a Redemption Altar");
            lore.add("§e§lto create the Key to Redemption!");
            meta.setLore(lore);
            
            // Add glow effect
            meta.addEnchant(Enchantment.FORTUNE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            shard.setItemMeta(meta);
        }
        
        return shard;
    }
    
    /**
     * Check if an ItemStack is a Shard of Atonement
     * @param item The ItemStack to check
     * @return true if it's a shard, false otherwise
     */
    public static boolean isShard(ItemStack item) {
        if (item == null || item.getType() != SHARD_MATERIAL) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        for (String shardName : SHARD_NAMES) {
            if (displayName.equals("§d§l✦ " + shardName + " ✦§r")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the shard number from an ItemStack
     * @param item The ItemStack to check
     * @return The shard number (1-7) or -1 if not a valid shard
     */
    public static int getShardNumber(ItemStack item) {
        if (!isShard(item)) {
            return -1;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        for (int i = 0; i < SHARD_NAMES.length; i++) {
            if (displayName.equals("§d§l✦ " + SHARD_NAMES[i] + " ✦§r")) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Check if a player has all 7 unique shards in their inventory
     * @param inventory The player's inventory contents
     * @return true if all 7 unique shards are present
     */
    public static boolean hasAllShards(ItemStack[] inventory) {
        boolean[] foundShards = new boolean[TOTAL_SHARDS];
        
        for (ItemStack item : inventory) {
            if (item == null) continue;
            
            int shardNumber = getShardNumber(item);
            if (shardNumber != -1) {
                foundShards[shardNumber - 1] = true;
            }
        }
        
        // Check if all shards are found
        for (boolean found : foundShards) {
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Remove all 7 shards from a player's inventory
     * @param inventory The player's inventory contents
     * @return true if all shards were successfully removed
     */
    public static boolean removeAllShards(ItemStack[] inventory) {
        if (!hasAllShards(inventory)) {
            return false;
        }
        
        boolean[] removedShards = new boolean[TOTAL_SHARDS];
        
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item == null) continue;
            
            int shardNumber = getShardNumber(item);
            if (shardNumber != -1 && !removedShards[shardNumber - 1]) {
                inventory[i] = null; // Remove the item
                removedShards[shardNumber - 1] = true;
            }
        }
        
        return true;
    }
    
    /**
     * Get the total number of shards required
     * @return The total number of shards (7)
     */
    public static int getTotalShards() {
        return TOTAL_SHARDS;
    }
    
    /**
     * Get the name of a specific shard
     * @param shardNumber The shard number (1-7)
     * @return The shard name
     */
    public static String getShardName(int shardNumber) {
        if (shardNumber < 1 || shardNumber > TOTAL_SHARDS) {
            return "Unknown Shard";
        }
        return SHARD_NAMES[shardNumber - 1];
    }
}