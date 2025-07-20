package com.example.minecraftplugin.items.artifacts;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Represents ultimate artifacts - the most powerful divine items
 */
public abstract class UltimateArtifact extends DivineItem {
    
    protected final List<GodType> sourceGods;
    protected final String artifactType;
    protected final int powerLevel;
    
    public UltimateArtifact(String displayName, Material material, List<String> lore, 
                           List<GodType> sourceGods, String artifactType, int powerLevel) {
        super(
            null, // Ultimate artifacts transcend single gods
            material,
            displayName,
            createArtifactLore(lore, sourceGods, artifactType, powerLevel),
            createArtifactEnchantments(powerLevel),
            true
        );
        this.sourceGods = sourceGods;
        this.artifactType = artifactType;
        this.powerLevel = powerLevel;
    }
    
    /**
     * Create artifact-specific lore
     */
    private static List<String> createArtifactLore(List<String> baseLore, List<GodType> sourceGods, 
                                                  String artifactType, int powerLevel) {
        List<String> artifactLore = new ArrayList<>(baseLore);
        artifactLore.add("");
        artifactLore.add("§5§l✦ ULTIMATE ARTIFACT ✦");
        artifactLore.add("§7Type: §f" + artifactType);
        artifactLore.add("§7Power Level: §f" + powerLevel);
        artifactLore.add("§7Source Gods: §f" + sourceGods.size());
        artifactLore.add("");
        
        // List source gods
        for (GodType god : sourceGods) {
            artifactLore.add("§7• §f" + god.getDisplayName());
        }
        
        artifactLore.add("");
        artifactLore.add("§8\"Power beyond mortal comprehension.\"");
        
        return artifactLore;
    }
    
    /**
     * Create artifact-specific enchantments
     */
    private static Map<Enchantment, Integer> createArtifactEnchantments(int powerLevel) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        
        // Base enchantments scaled by power level
        enchants.put(Enchantment.UNBREAKING, 10 + powerLevel);
        enchants.put(Enchantment.MENDING, Math.min(5, powerLevel));
        enchants.put(Enchantment.LOOTING, Math.min(10, powerLevel * 2));
        
        // Power-specific enchantments
        if (powerLevel >= 5) {
            enchants.put(Enchantment.SHARPNESS, powerLevel);
            enchants.put(Enchantment.PROTECTION, powerLevel);
        }
        
        if (powerLevel >= 8) {
            enchants.put(Enchantment.EFFICIENCY, powerLevel);
            enchants.put(Enchantment.FORTUNE, Math.min(5, powerLevel - 3));
        }
        
        return enchants;
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l★ ULTIMATE ARTIFACT OBTAINED! ★");
        player.sendMessage("§7You have acquired " + getDisplayName() + "!");
        player.sendMessage("§7This artifact contains the power of " + sourceGods.size() + " gods!");
        player.sendMessage("§7Type: §f" + artifactType);
        player.sendMessage("§7Power Level: §f" + powerLevel);
        
        // Epic sound sequence
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.3f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
        
        // Server announcement for ultimate artifacts
        // Note: plugin reference not available in item classes
        // This would need to be handled differently in a real implementation
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply combined passive effects from all source gods
        for (GodType god : sourceGods) {
            applyGodPassiveEffect(player, god);
        }
        
        // Apply artifact-specific bonuses
        applyArtifactBonuses(player);
    }
    
    /**
     * Apply god-specific passive effects
     */
    private void applyGodPassiveEffect(Player player, GodType god) {
        switch (god) {
            case FALLEN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
                break;
            case BANISHMENT:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
                break;
            case ABYSSAL:
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));
                break;
            case SYLVAN:
                if (player.hasPotionEffect(PotionEffectType.POISON)) {
                    player.removePotionEffect(PotionEffectType.POISON);
                }
                break;
            case TEMPEST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
                break;
            case VEIL:
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
                }
                break;
            case FORGE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 1, false, false));
                break;
            case VOID:
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
                break;
            case TIME:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false));
                break;
            case BLOOD:
                double healthPercent = player.getHealth() / player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                if (healthPercent < 0.5) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false));
                }
                break;
            case CRYSTAL:
                if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                    player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                }
                break;
            case SHADOW:
                int lightLevel = player.getLocation().getBlock().getLightLevel();
                if (lightLevel <= 3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
                }
                break;
        }
    }
    
    /**
     * Apply artifact-specific bonuses
     */
    private void applyArtifactBonuses(Player player) {
        // Bonuses scale with power level
        int strengthLevel = Math.min(4, powerLevel / 2);
        int resistanceLevel = Math.min(3, powerLevel / 3);
        int luckLevel = Math.min(5, powerLevel);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, strengthLevel, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, resistanceLevel, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, luckLevel, false, false));
        
        // Ultimate artifacts provide absorption
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, powerLevel, false, false));
    }
    
    @Override
    public int getCooldownSeconds() {
        // Cooldown decreases with power level
        return Math.max(30, 120 - (powerLevel * 10));
    }
    
    /**
     * Execute artifact-specific ultimate ability
     */
    protected abstract boolean executeUltimateAbility(Player player, ItemStack item);
    
    /**
     * Get artifact power rating
     */
    public int getPowerRating() {
        return powerLevel * sourceGods.size();
    }
    
    /**
     * Check if this artifact is more powerful than another
     */
    public boolean isMorePowerfulThan(UltimateArtifact other) {
        return getPowerRating() > other.getPowerRating();
    }
    
    // Getters
    public List<GodType> getSourceGods() { return new ArrayList<>(sourceGods); }
    public String getArtifactType() { return artifactType; }
    public int getPowerLevel() { return powerLevel; }
}