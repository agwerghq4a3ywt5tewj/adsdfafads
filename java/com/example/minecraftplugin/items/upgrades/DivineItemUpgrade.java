package com.example.minecraftplugin.items.upgrades;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.enums.ItemRarity;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles upgrading divine items to higher rarities
 */
public class DivineItemUpgrade {
    
    /**
     * Upgrade requirements for each rarity level
     */
    private static final Map<ItemRarity, UpgradeRequirement> UPGRADE_REQUIREMENTS = new HashMap<>();
    
    static {
        UPGRADE_REQUIREMENTS.put(ItemRarity.ENHANCED, new UpgradeRequirement(
            UpgradeMaterial.UpgradeType.DIVINE_ESSENCE, 3,
            "Enhanced divine power with 25% stronger effects"
        ));
        
        UPGRADE_REQUIREMENTS.put(ItemRarity.LEGENDARY, new UpgradeRequirement(
            UpgradeMaterial.UpgradeType.COSMIC_FRAGMENT, 2,
            "Legendary variant with unique properties and abilities"
        ));
        
        UPGRADE_REQUIREMENTS.put(ItemRarity.MYTHIC, new UpgradeRequirement(
            UpgradeMaterial.UpgradeType.REALITY_SHARD, 1,
            "Mythic power that bends reality itself"
        ));
        
        UPGRADE_REQUIREMENTS.put(ItemRarity.TRANSCENDENT, new UpgradeRequirement(
            UpgradeMaterial.UpgradeType.TRANSCENDENT_CORE, 1,
            "Transcendent power beyond mortal comprehension"
        ));
    }
    
    /**
     * Check if an item can be upgraded
     */
    public static boolean canUpgrade(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemRarity currentRarity = getCurrentRarity(item);
        return currentRarity != ItemRarity.TRANSCENDENT; // Can't upgrade beyond transcendent
    }
    
    /**
     * Get the current rarity of an item
     */
    public static ItemRarity getCurrentRarity(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return ItemRarity.DIVINE; // Default rarity
        }
        
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            for (ItemRarity rarity : ItemRarity.values()) {
                if (line.contains(rarity.getFormattedName())) {
                    return rarity;
                }
            }
        }
        
        return ItemRarity.DIVINE; // Default if no rarity found
    }
    
    /**
     * Get the next rarity level
     */
    public static ItemRarity getNextRarity(ItemRarity current) {
        ItemRarity[] rarities = ItemRarity.values();
        for (int i = 0; i < rarities.length - 1; i++) {
            if (rarities[i] == current) {
                return rarities[i + 1];
            }
        }
        return null; // Already at max rarity
    }
    
    /**
     * Get upgrade requirements for next level
     */
    public static UpgradeRequirement getUpgradeRequirement(ItemRarity targetRarity) {
        return UPGRADE_REQUIREMENTS.get(targetRarity);
    }
    
    /**
     * Upgrade a divine item to the next rarity level
     */
    public static ItemStack upgradeItem(ItemStack originalItem, ItemRarity targetRarity) {
        if (originalItem == null) {
            return null;
        }
        
        ItemStack upgradedItem = originalItem.clone();
        ItemMeta meta = upgradedItem.getItemMeta();
        
        if (meta == null) {
            return originalItem;
        }
        
        // Update display name with rarity
        String originalName = meta.getDisplayName();
        String newName = targetRarity.getColorCode() + "§l" + 
                        originalName.replaceAll("§[0-9a-fk-or]", "").replaceAll("§l", "");
        meta.setDisplayName(newName);
        
        // Update lore
        List<String> newLore = new ArrayList<>();
        List<String> originalLore = meta.getLore();
        
        if (originalLore != null) {
            // Copy original lore, removing old rarity indicators
            for (String line : originalLore) {
                boolean isRarityLine = false;
                for (ItemRarity rarity : ItemRarity.values()) {
                    if (line.contains(rarity.getFormattedName())) {
                        isRarityLine = true;
                        break;
                    }
                }
                if (!isRarityLine) {
                    newLore.add(line);
                }
            }
        }
        
        // Add new rarity information
        newLore.add("");
        newLore.add(targetRarity.getFormattedName() + " §7Divine Item");
        newLore.add("§7" + targetRarity.getDescription());
        
        // Add upgrade bonuses based on rarity
        addRarityBonuses(newLore, targetRarity);
        
        meta.setLore(newLore);
        
        // Enhance enchantments based on rarity
        enhanceEnchantments(upgradedItem, targetRarity);
        
        upgradedItem.setItemMeta(meta);
        
        return upgradedItem;
    }
    
    /**
     * Add rarity-specific bonuses to lore
     */
    private static void addRarityBonuses(List<String> lore, ItemRarity rarity) {
        lore.add("");
        lore.add("§e§lRarity Bonuses:");
        
        switch (rarity) {
            case ENHANCED:
                lore.add("§7• +25% effect strength");
                lore.add("§7• -20% cooldown reduction");
                break;
            case LEGENDARY:
                lore.add("§7• +50% effect strength");
                lore.add("§7• -40% cooldown reduction");
                lore.add("§7• Unique legendary properties");
                break;
            case MYTHIC:
                lore.add("§7• +100% effect strength");
                lore.add("§7• -60% cooldown reduction");
                lore.add("§7• Reality manipulation effects");
                lore.add("§7• Passive aura abilities");
                break;
            case TRANSCENDENT:
                lore.add("§7• +200% effect strength");
                lore.add("§7• -80% cooldown reduction");
                lore.add("§7• Transcendent cosmic powers");
                lore.add("§7• Dimensional manipulation");
                lore.add("§7• Omnipotent abilities");
                break;
        }
    }
    
    /**
     * Enhance enchantments based on rarity
     */
    private static void enhanceEnchantments(ItemStack item, ItemRarity rarity) {
        Map<Enchantment, Integer> currentEnchants = item.getEnchantments();
        
        // Remove existing enchantments
        for (Enchantment enchant : currentEnchants.keySet()) {
            item.removeEnchantment(enchant);
        }
        
        // Add enhanced enchantments
        for (Map.Entry<Enchantment, Integer> entry : currentEnchants.entrySet()) {
            int newLevel = entry.getValue();
            
            switch (rarity) {
                case ENHANCED:
                    newLevel += 1;
                    break;
                case LEGENDARY:
                    newLevel += 2;
                    break;
                case MYTHIC:
                    newLevel += 4;
                    break;
                case TRANSCENDENT:
                    newLevel += 8;
                    break;
            }
            
            item.addUnsafeEnchantment(entry.getKey(), newLevel);
        }
        
        // Add rarity-specific enchantments
        addRarityEnchantments(item, rarity);
    }
    
    /**
     * Add rarity-specific enchantments
     */
    private static void addRarityEnchantments(ItemStack item, ItemRarity rarity) {
        switch (rarity) {
            case LEGENDARY:
                item.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
                break;
            case MYTHIC:
                item.addUnsafeEnchantment(Enchantment.FORTUNE, 5);
                item.addUnsafeEnchantment(Enchantment.MENDING, 2);
                break;
            case TRANSCENDENT:
                item.addUnsafeEnchantment(Enchantment.FORTUNE, 10);
                item.addUnsafeEnchantment(Enchantment.MENDING, 5);
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 20);
                break;
        }
    }
    
    /**
     * Calculate upgrade success chance
     */
    public static double getUpgradeSuccessChance(ItemRarity currentRarity, ItemRarity targetRarity) {
        switch (targetRarity) {
            case ENHANCED:
                return 0.9; // 90% success
            case LEGENDARY:
                return 0.7; // 70% success
            case MYTHIC:
                return 0.5; // 50% success
            case TRANSCENDENT:
                return 0.3; // 30% success
            default:
                return 1.0;
        }
    }
    
    /**
     * Inner class for upgrade requirements
     */
    public static class UpgradeRequirement {
        private final UpgradeMaterial.UpgradeType materialType;
        private final int amount;
        private final String description;
        
        public UpgradeRequirement(UpgradeMaterial.UpgradeType materialType, int amount, String description) {
            this.materialType = materialType;
            this.amount = amount;
            this.description = description;
        }
        
        public UpgradeMaterial.UpgradeType getMaterialType() { return materialType; }
        public int getAmount() { return amount; }
        public String getDescription() { return description; }
    }
}