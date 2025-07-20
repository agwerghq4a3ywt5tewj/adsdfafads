package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chronos Staff - Divine item for the Time God
 * 
 * Abilities:
 * - Passive: Haste, slow aging effects, time perception
 * - Active: Right-click to create time dilation field that slows enemies
 * - Theme: Time manipulation, aging, temporal magic
 */
public class ChronosStaff extends DivineItem {
    
    private static final int DILATION_RADIUS = 12;
    private static final int COOLDOWN_SECONDS = 25;
    private static final int EFFECT_DURATION = 400; // 20 seconds
    
    public ChronosStaff() {
        super(
            GodType.TIME,
            Material.CLOCK,
            "§6§l⏰ Chronos Staff ⏰§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7An ancient staff that bends the flow of time,",
            "§7allowing mastery over temporal forces.",
            "",
            "§6§lPassive Abilities:",
            "§7• Haste II (faster actions)",
            "§7• Slow aging effects",
            "§7• Enhanced time perception",
            "",
            "§6§lActive Ability:",
            "§7• Right-click for Time Dilation",
            "§7• Slows all enemies within 12 blocks",
            "§7• Duration: §f20 seconds",
            "§7• Cooldown: §f25 seconds",
            "",
            "§8\"Time is a river that flows in all directions.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.EFFICIENCY, 5);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        List<Entity> nearbyEntities = player.getNearbyEntities(DILATION_RADIUS, DILATION_RADIUS, DILATION_RADIUS);
        int affectedCount = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity target = (LivingEntity) entity;
                
                // Apply time dilation effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, EFFECT_DURATION, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, EFFECT_DURATION, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, EFFECT_DURATION, 0));
                
                affectedCount++;
            }
        }
        
        if (affectedCount == 0) {
            player.sendMessage("§6§l⏰ Chronos Staff: §r§cNo enemies found to slow!");
            return false;
        }
        
        // Apply speed boost to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, EFFECT_DURATION, 2));
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 0.5f);
        
        // Messages
        player.sendMessage("§6§l⏰ TIME DILATION ACTIVATED! ⏰");
        player.sendMessage("§7Slowed §f" + affectedCount + "§7 enemies in temporal field!");
        player.sendMessage("§7Time flows differently around you...");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply haste for faster actions
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 1, false, false));
        
        // Apply saturation to represent slow aging
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§6§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Chronos Staff pulses with temporal energy!");
        player.sendMessage("§7Time itself bends to your will.");
        player.sendMessage("§7Right-click to create time dilation fields.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.2f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Chronos Staff is no longer with you.");
        player.sendMessage("§7Time returns to its normal flow.");
        
        // Remove time effects
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.SATURATION);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}