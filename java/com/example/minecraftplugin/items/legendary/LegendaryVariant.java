package com.example.minecraftplugin.items.legendary;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents legendary variants of divine items with unique properties
 */
public abstract class LegendaryVariant extends DivineItem {
    
    protected final DivineItem baseItem;
    protected final String variantName;
    protected final double spawnChance;
    
    public LegendaryVariant(DivineItem baseItem, String variantName, String displayName, 
                           List<String> additionalLore, double spawnChance) {
        super(
            baseItem.getGodType(),
            baseItem.getMaterial(),
            displayName,
            createLegendaryLore(baseItem.getLore(), additionalLore),
            createLegendaryEnchantments(baseItem.getEnchantments()),
            true
        );
        this.baseItem = baseItem;
        this.variantName = variantName;
        this.spawnChance = spawnChance;
    }
    
    /**
     * Create legendary lore by combining base lore with variant-specific additions
     */
    private static List<String> createLegendaryLore(List<String> baseLore, List<String> additionalLore) {
        List<String> legendaryLore = new java.util.ArrayList<>(baseLore);
        legendaryLore.add("");
        legendaryLore.add("§c§l✦ LEGENDARY VARIANT ✦");
        legendaryLore.addAll(additionalLore);
        return legendaryLore;
    }
    
    /**
     * Create enhanced enchantments for legendary variants
     */
    private static Map<Enchantment, Integer> createLegendaryEnchantments(Map<Enchantment, Integer> baseEnchants) {
        Map<Enchantment, Integer> legendary = new HashMap<>(baseEnchants);
        
        // Enhance all existing enchantments
        for (Map.Entry<Enchantment, Integer> entry : legendary.entrySet()) {
            legendary.put(entry.getKey(), entry.getValue() + 3);
        }
        
        // Add legendary-specific enchantments
        legendary.put(Enchantment.LOOTING, 5);
        legendary.put(Enchantment.MENDING, 2);
        
        return legendary;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Execute base ability first
        boolean baseResult = baseItem.onRightClick(player, item);
        
        // Then execute legendary enhancement
        boolean legendaryResult = executeLegendaryAbility(player, item);
        
        return baseResult || legendaryResult;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply base passive effects
        baseItem.applyPassiveEffects(player, item);
        
        // Apply legendary enhancements
        applyLegendaryPassiveEffects(player, item);
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§c§l★ LEGENDARY VARIANT OBTAINED! ★");
        player.sendMessage("§7You have acquired " + getDisplayName() + "!");
        player.sendMessage("§7This legendary variant has unique properties!");
        player.sendMessage("§7Variant: §c" + variantName);
        player.sendMessage("§7Spawn Chance: §f" + String.format("%.2f%%", spawnChance * 100));
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lLegendary power fades... §r§c" + getDisplayName() + " is no longer with you.");
        baseItem.onLost(player, item);
        removeLegendaryEffects(player);
    }
    
    @Override
    public int getCooldownSeconds() {
        return Math.max(1, baseItem.getCooldownSeconds() - 10); // Reduced cooldown for legendary
    }
    
    /**
     * Execute legendary-specific ability
     */
    protected abstract boolean executeLegendaryAbility(Player player, ItemStack item);
    
    /**
     * Apply legendary-specific passive effects
     */
    protected abstract void applyLegendaryPassiveEffects(Player player, ItemStack item);
    
    /**
     * Remove legendary effects
     */
    protected abstract void removeLegendaryEffects(Player player);
    
    /**
     * Check if this variant should spawn instead of base item
     */
    public boolean shouldSpawn() {
        return Math.random() < spawnChance;
    }
    
    // Getters
    public DivineItem getBaseItem() { return baseItem; }
    public String getVariantName() { return variantName; }
    public double getSpawnChance() { return spawnChance; }
}