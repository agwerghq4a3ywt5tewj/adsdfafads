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
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scepter of Banishment - Divine item for the Banishment God
 * 
 * Abilities:
 * - Passive: Fire Resistance and increased fire damage
 * - Active: Right-click to banish nearby enemies with knockback and fire
 * - Theme: Fire, exile, destruction
 */
public class ScepterOfBanishment extends DivineItem {
    
    private static final int BANISH_RADIUS = 8;
    private static final int COOLDOWN_SECONDS = 45;
    private static final int FIRE_DURATION = 100; // 5 seconds
    
    public ScepterOfBanishment() {
        super(
            GodType.BANISHMENT,
            Material.BLAZE_ROD,
            "§c§l🔥 Scepter of Banishment 🔥§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A blazing scepter wreathed in eternal flames,",
            "§7carrying the power to exile enemies to distant realms.",
            "",
            "§c§lPassive Abilities:",
            "§7• Fire Resistance",
            "§7• Increased fire damage dealt",
            "§7• Immunity to lava damage",
            "",
            "§c§lActive Ability:",
            "§7• Right-click to banish nearby enemies",
            "§7• Knockback and ignite enemies within 8 blocks",
            "§7• Cooldown: §f45 seconds",
            "",
            "§8\"Let the flames carry away what should not be.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.FIRE_ASPECT, 3);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        List<Entity> nearbyEntities = player.getNearbyEntities(BANISH_RADIUS, BANISH_RADIUS, BANISH_RADIUS);
        int banishedCount = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity target = (LivingEntity) entity;
                
                // Calculate knockback direction
                Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                direction.setY(0.5); // Add upward component
                direction.multiply(2.0); // Increase knockback strength
                
                // Apply knockback
                target.setVelocity(direction);
                
                // Set on fire
                target.setFireTicks(FIRE_DURATION);
                
                // Apply weakness effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
                
                banishedCount++;
            }
        }
        
        if (banishedCount == 0) {
            player.sendMessage("§c§l🔥 Scepter of Banishment: §r§cNo enemies found to banish!");
            return false;
        }
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.7f, 1.2f);
        
        // Messages
        player.sendMessage("§c§l🔥 DIVINE BANISHMENT! 🔥");
        player.sendMessage("§7Banished §f" + banishedCount + "§7 enemies with divine flames!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply fire resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        
        // Remove fire ticks if player is on fire
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§c§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Scepter of Banishment burns with eternal flames!");
        player.sendMessage("§7You are now immune to fire and can banish your enemies.");
        player.sendMessage("§7Right-click to unleash divine banishment.");
        
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Scepter of Banishment is no longer with you.");
        player.sendMessage("§7The protective flames dissipate.");
        
        // Remove fire resistance if it was from this item
        if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}