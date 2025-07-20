package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trident of the Abyss - Divine item for the Abyssal God
 * 
 * Abilities:
 * - Passive: Water breathing, faster swimming, night vision underwater
 * - Active: Right-click to create a water breathing aura for nearby players
 * - Theme: Ocean depths, water mastery
 */
public class TridentOfTheAbyss extends DivineItem {
    
    private static final int AURA_RADIUS = 10;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int AURA_DURATION = 1200; // 60 seconds
    
    public TridentOfTheAbyss() {
        super(
            GodType.ABYSSAL,
            Material.TRIDENT,
            "§b§l🔱 Trident of the Abyss 🔱§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A trident forged in the deepest ocean trenches,",
            "§7pulsing with the power of the endless depths.",
            "",
            "§b§lPassive Abilities:",
            "§7• Water Breathing",
            "§7• Dolphin's Grace (faster swimming)",
            "§7• Night Vision underwater",
            "",
            "§b§lActive Ability:",
            "§7• Right-click to grant water breathing aura",
            "§7• Affects all players within 10 blocks",
            "§7• Duration: §f60 seconds",
            "§7• Cooldown: §f60 seconds",
            "",
            "§8\"The depths hold secrets older than land.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.LOYALTY, 3);
        enchants.put(Enchantment.IMPALING, 5);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        List<Player> nearbyPlayers = player.getWorld().getPlayers();
        int affectedCount = 0;
        
        for (Player target : nearbyPlayers) {
            if (target.getLocation().distance(player.getLocation()) <= AURA_RADIUS) {
                // Grant water breathing and dolphin's grace
                target.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, AURA_DURATION, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, AURA_DURATION, 1));
                
                if (target != player) {
                    target.sendMessage("§b§l🔱 Abyssal Blessing: §r§bYou have been granted the power of the depths!");
                }
                affectedCount++;
            }
        }
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_AMBIENT, 0.8f, 1.2f);
        
        // Messages
        player.sendMessage("§b§l🔱 ABYSSAL AURA ACTIVATED! 🔱");
        player.sendMessage("§7Granted water mastery to §f" + affectedCount + "§7 players!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply water breathing
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));
        
        // Apply dolphin's grace for faster swimming
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, false, false));
        
        // Apply night vision when underwater
        if (player.isInWater()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§b§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Trident of the Abyss flows with oceanic power!");
        player.sendMessage("§7You can now breathe underwater and swim like a dolphin.");
        player.sendMessage("§7Right-click to share the blessing of the depths.");
        
        player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_LOOP, 0.5f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Trident of the Abyss is no longer with you.");
        player.sendMessage("§7The oceanic blessing dissipates.");
        
        // Remove water-related effects
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}