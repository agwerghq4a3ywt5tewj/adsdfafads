package com.example.minecraftplugin.items.upgrades;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Represents materials used for upgrading divine items
 */
public class UpgradeMaterial {
    
    public enum UpgradeType {
        DIVINE_ESSENCE("Divine Essence", Material.NETHER_STAR, 
            Arrays.asList("§7Concentrated divine power", "§7Used to enhance divine items")),
        COSMIC_FRAGMENT("Cosmic Fragment", Material.END_CRYSTAL, 
            Arrays.asList("§7A shard of cosmic energy", "§7Enables legendary transformations")),
        REALITY_SHARD("Reality Shard", Material.ECHO_SHARD, 
            Arrays.asList("§7Fragment of pure reality", "§7Allows mythic upgrades")),
        TRANSCENDENT_CORE("Transcendent Core", Material.HEART_OF_THE_SEA, 
            Arrays.asList("§7Core of transcendent power", "§7Ultimate upgrade material")),
        FUSION_CATALYST("Fusion Catalyst", Material.DRAGON_BREATH, 
            Arrays.asList("§7Enables item fusion", "§7Combines divine powers"));
        
        private final String displayName;
        private final Material material;
        private final List<String> lore;
        
        UpgradeType(String displayName, Material material, List<String> lore) {
            this.displayName = displayName;
            this.material = material;
            this.lore = lore;
        }
        
        public String getDisplayName() { return displayName; }
        public Material getMaterial() { return material; }
        public List<String> getLore() { return lore; }
    }
    
    /**
     * Create an upgrade material item
     */
    public static ItemStack createUpgradeMaterial(UpgradeType type, int amount) {
        ItemStack item = new ItemStack(type.getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b§l" + type.getDisplayName() + "§r");
            
            List<String> lore = Arrays.asList(
                type.getLore().get(0),
                type.getLore().get(1),
                "",
                "§e§lUpgrade Material",
                "§7Use at Divine Forge to enhance items"
            );
            meta.setLore(lore);
            
            // Add glow effect
            meta.addEnchant(Enchantment.LOOTING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Check if an item is an upgrade material
     */
    public static UpgradeType getUpgradeType(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        for (UpgradeType type : UpgradeType.values()) {
            if (displayName.equals("§b§l" + type.getDisplayName() + "§r")) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Check if player has required materials for upgrade
     */
    public static boolean hasRequiredMaterials(org.bukkit.entity.Player player, UpgradeType type, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (getUpgradeType(item) == type) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }
    
    /**
     * Remove upgrade materials from player inventory
     */
    public static boolean removeUpgradeMaterials(org.bukkit.entity.Player player, UpgradeType type, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (getUpgradeType(item) == type) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    contents[i] = null;
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        player.getInventory().setContents(contents);
        return remaining == 0;
    }
}