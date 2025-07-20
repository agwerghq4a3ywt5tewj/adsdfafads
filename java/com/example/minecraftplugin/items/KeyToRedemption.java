package com.example.minecraftplugin.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Key to Redemption - used to free Prisoners of the Void
 */
public class KeyToRedemption {
    
    private static final Material KEY_MATERIAL = Material.NETHER_STAR;
    private static final String DISPLAY_NAME = "§b§l✦ Key to Redemption ✦§r";
    
    /**
     * Create an ItemStack for the Key to Redemption
     * @return ItemStack representing the key
     */
    public static ItemStack createKey() {
        ItemStack key = new ItemStack(KEY_MATERIAL);
        ItemMeta meta = key.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(DISPLAY_NAME);
            
            // Set lore
            List<String> lore = Arrays.asList(
                "§7Forged from the essence of atonement,",
                "§7this key can unlock the chains of the Void.",
                "",
                "§b§lUsage:",
                "§7• Right-click on a Prisoner of the Void",
                "§7• Instantly frees them from imprisonment",
                "§7• Teleports them to safety",
                "§7• Consumes this key upon use",
                "",
                "§c§lWarning: §r§cThis key is consumed on use!",
                "",
                "§8\"Only through sacrifice can true freedom be found.\""
            );
            meta.setLore(lore);
            
            // Add glow effect
            meta.addEnchant(Enchantment.FORTUNE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // Make it unbreakable (though it shouldn't take damage anyway)
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            
            key.setItemMeta(meta);
        }
        
        return key;
    }
    
    /**
     * Check if an ItemStack is a Key to Redemption
     * @param item The ItemStack to check
     * @return true if it's a Key to Redemption, false otherwise
     */
    public static boolean isKey(ItemStack item) {
        if (item == null || item.getType() != KEY_MATERIAL) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(DISPLAY_NAME);
    }
    
    /**
     * Get the material used for the Key to Redemption
     * @return The material (NETHER_STAR)
     */
    public static Material getKeyMaterial() {
        return KEY_MATERIAL;
    }
    
    /**
     * Get the display name of the Key to Redemption
     * @return The formatted display name
     */
    public static String getDisplayName() {
        return DISPLAY_NAME;
    }
}