package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for all divine items associated with the Fallen Gods
 */
public abstract class DivineItem {
    
    protected final GodType godType;
    protected final Material material;
    protected final String displayName;
    protected final List<String> lore;
    protected final Map<Enchantment, Integer> enchantments;
    protected final boolean unbreakable;
    
    public DivineItem(GodType godType, Material material, String displayName, 
                     List<String> lore, Map<Enchantment, Integer> enchantments, boolean unbreakable) {
        this.godType = godType;
        this.material = material;
        this.displayName = displayName;
        this.lore = new ArrayList<>(lore);
        this.enchantments = enchantments;
        this.unbreakable = unbreakable;
    }
    
    /**
     * Create the ItemStack for this divine item
     */
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(displayName);
            
            // Set lore
            List<String> finalLore = new ArrayList<>(lore);
            finalLore.add("");
            finalLore.add("§7Divine Item of the " + godType.getDisplayName());
            finalLore.add("§8" + godType.getTheme());
            meta.setLore(finalLore);
            
            // Set unbreakable
            if (unbreakable) {
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            
            // Hide enchantments if needed
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        // Add enchantments
        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }
        
        return item;
    }
    
    /**
     * Check if an ItemStack is this divine item
     */
    public boolean isDivineItem(ItemStack item) {
        if (item == null || item.getType() != material) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(displayName);
    }
    
    /**
     * Handle right-click action (active ability)
     */
    public abstract boolean onRightClick(Player player, ItemStack item);
    
    /**
     * Handle passive effects when item is held/worn
     */
    public abstract void applyPassiveEffects(Player player, ItemStack item);
    
    /**
     * Handle when item is obtained by player
     */
    public abstract void onObtained(Player player, ItemStack item);
    
    /**
     * Handle when item is lost by player
     */
    public abstract void onLost(Player player, ItemStack item);
    
    /**
     * Get the cooldown for this item's active ability (in seconds)
     */
    public abstract int getCooldownSeconds();
    
    // Getters
    public GodType getGodType() {
        return godType;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public List<String> getLore() {
        return new ArrayList<>(lore);
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    
    public boolean isUnbreakable() {
        return unbreakable;
    }
}