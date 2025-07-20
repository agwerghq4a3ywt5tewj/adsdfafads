package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.enums.ItemRarity;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.items.upgrades.DivineItemUpgrade;
import com.example.minecraftplugin.items.upgrades.UpgradeMaterial;
import com.example.minecraftplugin.items.combinations.DivineItemCombination;
import com.example.minecraftplugin.items.combinations.CombinedDivineItem;
import com.example.minecraftplugin.items.legendary.LegendaryVariant;
import com.example.minecraftplugin.items.legendary.EternalHeartOfFallenGod;
import com.example.minecraftplugin.items.legendary.InfernoScepterOfBanishment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the Divine Forge system for upgrading and combining divine items
 */
public class DivineForgeManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final GodManager godManager;
    
    // Legendary variants registry
    private final Map<GodType, List<LegendaryVariant>> legendaryVariants;
    
    // Forge locations (for future expansion)
    private final Set<Location> forgeLocations;
    
    public DivineForgeManager(MinecraftPlugin plugin, GodManager godManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.godManager = godManager;
        this.legendaryVariants = new HashMap<>();
        this.forgeLocations = new HashSet<>();
        
        initializeLegendaryVariants();
        
        logger.info("Divine Forge Manager initialized with advanced item systems");
    }
    
    /**
     * Initialize legendary variants for each god
     */
    private void initializeLegendaryVariants() {
        // Fallen God legendary variants
        List<LegendaryVariant> fallenVariants = new ArrayList<>();
        fallenVariants.add(new EternalHeartOfFallenGod());
        legendaryVariants.put(GodType.FALLEN, fallenVariants);
        
        // Banishment God legendary variants
        List<LegendaryVariant> banishmentVariants = new ArrayList<>();
        banishmentVariants.add(new InfernoScepterOfBanishment());
        legendaryVariants.put(GodType.BANISHMENT, banishmentVariants);
        
        // TODO: Add legendary variants for other gods
        
        logger.info("Initialized legendary variants for " + legendaryVariants.size() + " gods");
    }
    
    /**
     * Attempt to upgrade a divine item
     */
    public UpgradeResult upgradeItem(Player player, ItemStack item) {
        if (!DivineItemUpgrade.canUpgrade(item)) {
            return new UpgradeResult(false, "Item cannot be upgraded further", null);
        }
        
        ItemRarity currentRarity = DivineItemUpgrade.getCurrentRarity(item);
        ItemRarity targetRarity = DivineItemUpgrade.getNextRarity(currentRarity);
        
        if (targetRarity == null) {
            return new UpgradeResult(false, "Item is already at maximum rarity", null);
        }
        
        DivineItemUpgrade.UpgradeRequirement requirement = DivineItemUpgrade.getUpgradeRequirement(targetRarity);
        
        // Check if player has required materials
        if (!UpgradeMaterial.hasRequiredMaterials(player, requirement.getMaterialType(), requirement.getAmount())) {
            return new UpgradeResult(false, 
                "Requires " + requirement.getAmount() + "x " + requirement.getMaterialType().getDisplayName(), 
                null);
        }
        
        // Calculate success chance
        double successChance = DivineItemUpgrade.getUpgradeSuccessChance(currentRarity, targetRarity);
        boolean success = Math.random() < successChance;
        
        // Remove materials regardless of success
        UpgradeMaterial.removeUpgradeMaterials(player, requirement.getMaterialType(), requirement.getAmount());
        
        if (success) {
            // Upgrade successful
            ItemStack upgradedItem = DivineItemUpgrade.upgradeItem(item, targetRarity);
            createUpgradeSuccessEffects(player, targetRarity);
            
            return new UpgradeResult(true, 
                "Successfully upgraded to " + targetRarity.getFormattedName(), 
                upgradedItem);
        } else {
            // Upgrade failed
            createUpgradeFailureEffects(player);
            
            return new UpgradeResult(false, 
                "Upgrade failed! (" + String.format("%.0f%%", successChance * 100) + " chance)", 
                null);
        }
    }
    
    /**
     * Attempt to combine divine items
     */
    public CombinationResult combineItems(Player player, List<ItemStack> items) {
        if (!DivineItemCombination.canCombine(items)) {
            return new CombinationResult(false, "These items cannot be combined", null);
        }
        
        // Check for fusion catalyst
        if (!UpgradeMaterial.hasRequiredMaterials(player, UpgradeMaterial.UpgradeType.FUSION_CATALYST, 1)) {
            return new CombinationResult(false, "Requires 1x Fusion Catalyst", null);
        }
        
        DivineItemCombination.CombinationResult result = DivineItemCombination.getCombinationResult(items);
        if (result == null) {
            return new CombinationResult(false, "Invalid combination", null);
        }
        
        // Remove fusion catalyst
        UpgradeMaterial.removeUpgradeMaterials(player, UpgradeMaterial.UpgradeType.FUSION_CATALYST, 1);
        
        // Remove source items from inventory
        for (ItemStack item : items) {
            player.getInventory().remove(item);
        }
        
        // Create combined item
        ItemStack combinedItem = DivineItemCombination.combineItems(items, result);
        createCombinationSuccessEffects(player, result);
        
        return new CombinationResult(true, 
            "Successfully created " + result.getDisplayName(), 
            combinedItem);
    }
    
    /**
     * Generate legendary variant instead of base item
     */
    public ItemStack generateLegendaryVariant(GodType god, ItemStack baseItem) {
        List<LegendaryVariant> variants = legendaryVariants.get(god);
        if (variants == null || variants.isEmpty()) {
            return baseItem; // No variants available
        }
        
        // Check each variant's spawn chance
        for (LegendaryVariant variant : variants) {
            if (variant.shouldSpawn()) {
                logger.info("Generated legendary variant: " + variant.getVariantName() + " for " + god.getDisplayName());
                return variant.createItemStack();
            }
        }
        
        return baseItem; // No variant spawned
    }
    
    /**
     * Create upgrade materials as rewards
     */
    public void grantUpgradeMaterials(Player player, UpgradeMaterial.UpgradeType type, int amount, String reason) {
        ItemStack material = UpgradeMaterial.createUpgradeMaterial(type, amount);
        player.getInventory().addItem(material);
        
        player.sendMessage("§b§l✦ UPGRADE MATERIAL OBTAINED! ✦");
        player.sendMessage("§7Received: §f" + amount + "x " + type.getDisplayName());
        player.sendMessage("§7Reason: §f" + reason);
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        
        logger.info("Granted " + amount + "x " + type.getDisplayName() + " to " + player.getName() + " for: " + reason);
    }
    
    /**
     * Create upgrade success effects
     */
    private void createUpgradeSuccessEffects(Player player, ItemRarity rarity) {
        Location location = player.getLocation();
        
        // Rarity-specific effects
        switch (rarity) {
            case ENHANCED:
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 30, 1, 1, 1, 0.2);
                player.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                break;
            case LEGENDARY:
                location.getWorld().spawnParticle(Particle.HEART, location, 50, 2, 2, 2, 0.3);
                location.getWorld().spawnParticle(Particle.FIREWORK, location, 40, 1.5, 1.5, 1.5, 0.2);
                player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                break;
            case MYTHIC:
                location.getWorld().spawnParticle(Particle.END_ROD, location, 60, 2.5, 2.5, 2.5, 0.4);
                location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 30, 2, 2, 2, 0.2);
                player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.5f);
                break;
            case TRANSCENDENT:
                location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 3, 3, 3, 0.5);
                location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 50, 2, 2, 2, 0.3);
                location.getWorld().spawnParticle(Particle.EXPLOSION, location, 5);
                player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.5f);
                player.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.3f);
                
                // Server announcement for transcendent upgrades
                plugin.getServer().broadcastMessage("§5§l" + player.getName() + " §r§5has forged a Transcendent divine item!");
                break;
        }
        
        player.sendMessage("§a§l✦ UPGRADE SUCCESSFUL! ✦");
        player.sendMessage("§7Your divine item has been enhanced to " + rarity.getFormattedName() + "§7!");
    }
    
    /**
     * Create upgrade failure effects
     */
    private void createUpgradeFailureEffects(Player player) {
        Location location = player.getLocation();
        
        location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 20, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.ASH, location, 30, 1.5, 1.5, 1.5, 0.2);
        
        player.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.8f);
        player.playSound(location, Sound.ENTITY_ITEM_BREAK, 1.0f, 0.5f);
        
        player.sendMessage("§c§l✗ UPGRADE FAILED! ✗");
        player.sendMessage("§7The divine energies were too unstable to contain.");
        player.sendMessage("§7Your materials have been consumed in the attempt.");
    }
    
    /**
     * Create combination success effects
     */
    private void createCombinationSuccessEffects(Player player, DivineItemCombination.CombinationResult result) {
        Location location = player.getLocation();
        
        // Epic combination effects
       location.getWorld().spawnParticle(Particle.EXPLOSION, location, 8);
       location.getWorld().spawnParticle(Particle.FIREWORK, location, 80, 3, 3, 3, 0.4);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 60, 2.5, 2.5, 2.5, 0.3);
       location.getWorld().spawnParticle(Particle.HEART, location, 40, 2, 2, 2, 0.2);
        
        // Lightning strikes for dramatic effect
        for (int i = 0; i < result.getSourceGods().size(); i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                location.getWorld().strikeLightningEffect(location.clone().add(
                    (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6));
            }, i * 10L);
        }
        
        player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.8f);
        player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.9f);
        
        player.sendMessage("§5§l★ DIVINE COMBINATION FORGED! ★");
        player.sendMessage("§7The powers of multiple gods have been unified!");
        
        // Server announcement for combinations
        plugin.getServer().broadcastMessage("§5§l" + player.getName() + " §r§5has forged " + result.getDisplayName() + "!");
    }
    
    /**
     * Get all available legendary variants for a god
     */
    public List<LegendaryVariant> getLegendaryVariants(GodType god) {
        return legendaryVariants.getOrDefault(god, new ArrayList<>());
    }
    
    /**
     * Get forge statistics
     */
    public Map<String, Object> getForgeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalVariants = 0;
        for (List<LegendaryVariant> variants : legendaryVariants.values()) {
            totalVariants += variants.size();
        }
        
        stats.put("total_legendary_variants", totalVariants);
        stats.put("gods_with_variants", legendaryVariants.size());
        stats.put("available_combinations", DivineItemCombination.getAllCombinations().size());
        stats.put("upgrade_materials", UpgradeMaterial.UpgradeType.values().length);
        stats.put("rarity_levels", ItemRarity.values().length);
        
        return stats;
    }
    
    /**
     * Inner class for upgrade results
     */
    public static class UpgradeResult {
        private final boolean success;
        private final String message;
        private final ItemStack resultItem;
        
        public UpgradeResult(boolean success, String message, ItemStack resultItem) {
            this.success = success;
            this.message = message;
            this.resultItem = resultItem;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public ItemStack getResultItem() { return resultItem; }
    }
    
    /**
     * Inner class for combination results
     */
    public static class CombinationResult {
        private final boolean success;
        private final String message;
        private final ItemStack resultItem;
        
        public CombinationResult(boolean success, String message, ItemStack resultItem) {
            this.success = success;
            this.message = message;
            this.resultItem = resultItem;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public ItemStack getResultItem() { return resultItem; }
    }
}