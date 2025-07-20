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
 * Crimson Blade - Divine item for the Blood God
 * 
 * Abilities:
 * - Passive: Blood Frenzy (damage increases as health decreases), life steal
 * - Active: Right-click to enter berserker rage
 * - Theme: Combat, sacrifice, berserker rage
 */
public class CrimsonBlade extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 45;
    private static final int RAGE_DURATION = 200; // 10 seconds
    
    public CrimsonBlade() {
        super(
            GodType.BLOOD,
            Material.NETHERITE_SWORD,
            "§4§l⚔ Crimson Blade ⚔§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A blade thirsting for battle, growing stronger",
            "§7with each drop of blood spilled in combat.",
            "",
            "§4§lPassive Abilities:",
            "§7• Blood Frenzy: Damage increases as health decreases",
            "§7• Life Steal: Heal 2 hearts on successful attacks",
            "§7• Combat regeneration when injured",
            "",
            "§4§lActive Ability:",
            "§7• Right-click for Berserker Rage",
            "§7• Strength III, Speed II, Resistance I",
            "§7• Duration: §f10 seconds",
            "§7• Cooldown: §f45 seconds",
            "",
            "§8\"Strength is earned through struggle.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.SHARPNESS, 4);
        enchants.put(Enchantment.FIRE_ASPECT, 2);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        enchants.put(Enchantment.SWEEPING_EDGE, 3);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Enter berserker rage
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, RAGE_DURATION, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, RAGE_DURATION, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, RAGE_DURATION, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, RAGE_DURATION, 1));
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.6f);
        
        // Messages
        player.sendMessage("§4§l⚔ BERSERKER RAGE! ⚔");
        player.sendMessage("§7Blood boils with divine fury!");
        player.sendMessage("§7Your combat prowess reaches its peak!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double healthPercent = currentHealth / maxHealth;
        
        // Blood Frenzy: More strength when injured
        if (healthPercent < 0.5) {
            int strengthLevel = healthPercent < 0.25 ? 1 : 0;
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, strengthLevel, false, false));
        }
        
        // Combat regeneration when injured
        if (healthPercent < 0.75) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, false, false));
        }
        
        // Remove weakness effects (blood god doesn't get weak)
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§4§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Crimson Blade thirsts for battle!");
        player.sendMessage("§7Your strength grows with each wound.");
        player.sendMessage("§7Right-click to enter berserker rage.");
        
        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_AMBIENT, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Crimson Blade is no longer with you.");
        player.sendMessage("§7The bloodlust subsides.");
        
        // Remove blood effects
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}