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
 * Convergence Nexus - Ultimate divine item granted upon achieving Divine Convergence
 * 
 * Abilities:
 * - Passive: All divine powers combined, reality manipulation
 * - Active: Right-click to unleash Divine Convergence Wave
 * - Theme: Master of All Divinity, transcends all limitations
 */
public class ConvergenceNexus extends DivineItem {
    
    private static final int CONVERGENCE_RADIUS = 20;
    private static final int COOLDOWN_SECONDS = 120; // 2 minutes
    private static final int WAVE_DURATION = 600; // 30 seconds
    
    public ConvergenceNexus() {
        super(
            null, // No specific god - represents all gods
            Material.NETHER_STAR,
            "§5§l✦ Convergence Nexus ✦§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7The ultimate manifestation of divine power,",
            "§7containing the essence of all twelve gods.",
            "",
            "§5§lPassive Abilities:",
            "§7• All divine powers combined",
            "§7• Reality manipulation mastery",
            "§7• Transcendent cosmic awareness",
            "§7• Immunity to all negative effects",
            "",
            "§5§lActive Ability:",
            "§7• Right-click for Divine Convergence Wave",
            "§7• Affects all entities within 20 blocks",
            "§7• Grants divine blessings to allies",
            "§7• Banishes hostile entities",
            "§7• Duration: §f30 seconds",
            "§7• Cooldown: §f2 minutes",
            "",
            "§d§l★ MASTER OF ALL DIVINITY ★",
            "",
            "§8\"I am become divine, transcendent of all limitations.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        enchants.put(Enchantment.FORTUNE, 5);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        List<Entity> nearbyEntities = player.getNearbyEntities(CONVERGENCE_RADIUS, CONVERGENCE_RADIUS, CONVERGENCE_RADIUS);
        int alliesBlessed = 0;
        int enemiesBanished = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                // Grant divine blessings to all players
                grantDivineBlessings(target);
                alliesBlessed++;
            } else if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entity;
                // Banish hostile entities
                banishHostileEntity(player, target);
                enemiesBanished++;
            }
        }
        
        // Apply convergence effects to the user
        applyConvergenceEffects(player);
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 2.0f);
        
        // Messages
        player.sendMessage("§5§l✦ DIVINE CONVERGENCE UNLEASHED! ✦");
        player.sendMessage("§7The power of all twelve gods flows through you!");
        if (alliesBlessed > 0) {
            player.sendMessage("§7Blessed §a" + alliesBlessed + "§7 allies with divine power!");
        }
        if (enemiesBanished > 0) {
            player.sendMessage("§7Banished §c" + enemiesBanished + "§7 hostile entities!");
        }
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply all divine passive effects combined
        
        // Fallen God effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2, false, false));
        
        // Banishment God effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
        
        // Abyssal God effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, false, false));
        
        // Sylvan God effects
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            player.removePotionEffect(PotionEffectType.POISON);
        }
        
        // Tempest God effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false));
        
        // Veil God effects
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false));
        
        // Forge God effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 2, false, false));
        
        // Additional convergence effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 3, false, false));
        
        // Remove all negative effects
        removeNegativeEffects(player);
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l★ DIVINE CONVERGENCE ACHIEVED! ★");
        player.sendMessage("§7You have mastered all twelve gods and transcended mortality!");
        player.sendMessage("§7The Convergence Nexus contains the power of all divinity.");
        player.sendMessage("§7You are now the Master of All Divinity!");
        player.sendMessage("§7Right-click to unleash convergence waves.");
        
        // Dramatic effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.3f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.5f);
        
        // Set max health to ultimate level
        org.bukkit.attribute.AttributeInstance healthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(60.0); // 30 hearts
        }
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine convergence fades... §r§cThe Convergence Nexus is no longer with you.");
        player.sendMessage("§7Your transcendent power diminishes.");
        
        // Reset max health to normal
        org.bukkit.attribute.AttributeInstance healthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
            if (player.getHealth() > 20.0) {
                player.setHealth(20.0);
            }
        }
        
        // Remove convergence effects
        removeNegativeEffects(player);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Grant divine blessings to an ally
     */
    private void grantDivineBlessings(Player target) {
        // Grant powerful beneficial effects
        target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, WAVE_DURATION, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, WAVE_DURATION, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, WAVE_DURATION, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, WAVE_DURATION, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, WAVE_DURATION, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, WAVE_DURATION, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, WAVE_DURATION, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, WAVE_DURATION, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, WAVE_DURATION, 2));
        
        // Remove negative effects
        removeNegativeEffects(target);
        
        // Heal to full
        target.setHealth(target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setSaturation(20);
        
        target.sendMessage("§5§l✦ Divine Blessing Received! ✦");
        target.sendMessage("§7You have been blessed by the Master of All Divinity!");
    }
    
    /**
     * Banish a hostile entity
     */
    private void banishHostileEntity(Player player, LivingEntity target) {
        // Calculate banishment direction (away from player)
        Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        direction.setY(0.8); // Add upward component
        direction.multiply(3.0); // Strong knockback
        
        // Apply banishment effects
        target.setVelocity(direction);
        target.damage(10.0, player);
        target.setFireTicks(200); // 10 seconds of fire
        
        // Apply debuffs
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 1));
    }
    
    /**
     * Apply special convergence effects to the user
     */
    private void applyConvergenceEffects(Player player) {
        // Ultimate power effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, WAVE_DURATION, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, WAVE_DURATION, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, WAVE_DURATION, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, WAVE_DURATION, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, WAVE_DURATION, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, WAVE_DURATION, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, WAVE_DURATION, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, WAVE_DURATION, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, WAVE_DURATION, 0));
        
        // Heal to full
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Clear negative effects
        removeNegativeEffects(player);
    }
    
    /**
     * Remove all negative effects from a player
     */
    private void removeNegativeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.removePotionEffect(PotionEffectType.UNLUCK);
        player.removePotionEffect(PotionEffectType.BAD_OMEN);
    }
}