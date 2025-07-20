package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mace of Divine Forging - Divine item for the Forge God
 * 
 * Abilities:
 * - Passive: Strength I and Haste I when held
 * - Passive: Enhanced combat damage from base mace mechanics
 * - Active: Right-click to repair held item or tools in inventory
 * - Conflicts with Heart of the Fallen God
 */
public class MaceOfDivineForging extends DivineItem {
    
    private static final int REPAIR_AMOUNT = 500; // Durability points to repair
    private static final int COOLDOWN_SECONDS = 30; // 30 second cooldown
    
    public MaceOfDivineForging() {
        super(
            GodType.FORGE,
            Material.MACE,
            "§6§l⚒ Mace of Divine Forging ⚒§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A mighty mace forged in the divine flames,",
            "§7pulsing with the power of creation and repair.",
            "",
            "§6§lPassive Abilities:",
            "§7• Strength I when held",
            "§7• Haste I for faster mining",
            "§7• Enhanced mace combat mechanics",
            "",
            "§6§lActive Ability:",
            "§7• Right-click to repair held items",
            "§7• Restores §6500 durability points",
            "§7• Cooldown: §f30 seconds",
            "",
            "§c§lConflicts with: §r§cHeart of the Fallen God",
            "",
            "§8\"In the forge, raw potential becomes perfection.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.SHARPNESS, 3);
        enchants.put(Enchantment.UNBREAKING, 5);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Find the best item to repair
        ItemStack targetItem = findRepairableItem(player);
        
        if (targetItem == null) {
            player.sendMessage("§6§l⚒ Mace of Divine Forging: §r§cNo repairable items found!");
            player.sendMessage("§7Hold a damaged tool, weapon, or armor piece to repair it.");
            return false;
        }
        
        // Check if item needs repair
        ItemMeta meta = targetItem.getItemMeta();
        if (!(meta instanceof Damageable)) {
            player.sendMessage("§6§l⚒ Mace of Divine Forging: §r§cThis item cannot be repaired!");
            return false;
        }
        
        Damageable damageable = (Damageable) meta;
        int currentDamage = damageable.getDamage();
        
        if (currentDamage == 0) {
            player.sendMessage("§6§l⚒ Mace of Divine Forging: §r§cThis item is already at full durability!");
            return false;
        }
        
        // Calculate repair amount
        int maxDurability = targetItem.getType().getMaxDurability();
        int newDamage = Math.max(0, currentDamage - REPAIR_AMOUNT);
        int actualRepair = currentDamage - newDamage;
        
        // Apply repair
        damageable.setDamage(newDamage);
        targetItem.setItemMeta(meta);
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
        
        // Messages
        String itemName = targetItem.getType().name().toLowerCase().replace("_", " ");
        double durabilityPercent = ((double)(maxDurability - newDamage) / maxDurability) * 100;
        
        player.sendMessage("§6§l⚒ Divine Repair Complete! ⚒");
        player.sendMessage("§7Repaired §f" + itemName + " §7by §6" + actualRepair + " §7durability points!");
        player.sendMessage("§7Item condition: §f" + String.format("%.1f", durabilityPercent) + "%");
        
        return true; // Ability was used successfully
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply Strength I and Haste I when held
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§6§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Mace of Divine Forging pulses with creative energy!");
        player.sendMessage("§7You feel the power of the forge flowing through you.");
        player.sendMessage("§7Right-click to repair damaged items.");
        
        // Play dramatic sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Mace of Divine Forging is no longer with you.");
        player.sendMessage("§7The creative energy of the forge dissipates.");
        
        // Remove any strength/haste effects from this item
        if (player.hasPotionEffect(PotionEffectType.STRENGTH)) {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            player.removePotionEffect(PotionEffectType.HASTE);
        }
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Find the best item to repair in player's inventory
     * Priority: Main hand -> Off hand -> Hotbar -> Rest of inventory
     */
    private ItemStack findRepairableItem(Player player) {
        // Check main hand first (excluding the mace itself)
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isRepairable(mainHand) && !isDivineItem(mainHand)) {
            return mainHand;
        }
        
        // Check off hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isRepairable(offHand) && !isDivineItem(offHand)) {
            return offHand;
        }
        
        // Check hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isRepairable(item) && !isDivineItem(item)) {
                return item;
            }
        }
        
        // Check rest of inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (isRepairable(item) && !isDivineItem(item)) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Check if an item can be repaired
     */
    private boolean isRepairable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // Check if item has durability
        if (item.getType().getMaxDurability() <= 0) {
            return false;
        }
        
        // Check if item is damaged
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            return false;
        }
        
        Damageable damageable = (Damageable) meta;
        return damageable.getDamage() > 0;
    }
    
    /**
     * Check if an item is a divine item (to avoid repairing divine items)
     */
    public boolean isDivineItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) {
            return false;
        }
        
        // Check if lore contains divine item indicator
        for (String line : lore) {
            if (line.contains("Divine Item of the")) {
                return true;
            }
        }
        
        return false;
    }
}