package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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
 * Wings of Tempest - Divine item for the Tempest God
 * 
 * Abilities:
 * - Passive: Slow falling, jump boost, speed
 * - Active: Right-click to launch into the air with wind burst
 * - Theme: Sky, storms, lightning, flight
 */
public class WingsOfTempest extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 20;
    private static final double LAUNCH_POWER = 2.0;
    
    public WingsOfTempest() {
        super(
            GodType.TEMPEST,
            Material.ELYTRA,
            "§f§l⚡ Wings of Tempest ⚡§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7Ethereal wings that shimmer with storm energy,",
            "§7granting mastery over the skies and winds.",
            "",
            "§f§lPassive Abilities:",
            "§7• Slow Falling",
            "§7• Jump Boost II",
            "§7• Speed I",
            "",
            "§f§lActive Ability:",
            "§7• Right-click to launch skyward",
            "§7• Wind burst propels you upward",
            "§7• Cooldown: §f20 seconds",
            "",
            "§8\"The sky knows no boundaries, nor should you.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Launch player into the air
        Vector velocity = player.getVelocity();
        velocity.setY(LAUNCH_POWER);
        player.setVelocity(velocity);
        
        // Apply temporary slow falling
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0));
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 2.0f);
        
        // Messages
        player.sendMessage("§f§l⚡ WIND BURST! ⚡");
        player.sendMessage("§7The tempest carries you skyward!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply flight-related effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§f§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Wings of Tempest shimmer with storm energy!");
        player.sendMessage("§7You feel the power of the skies flowing through you.");
        player.sendMessage("§7Right-click to launch into the air with wind burst.");
        
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.5f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Wings of Tempest are no longer with you.");
        player.sendMessage("§7The storm energy dissipates.");
        
        // Remove flight effects
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}