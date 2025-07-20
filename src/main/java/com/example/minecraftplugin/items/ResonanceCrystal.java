package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
 * Resonance Crystal - Divine item for the Crystal God
 * 
 * Abilities:
 * - Passive: Crystal shield when sneaking, ore sense
 * - Active: Right-click to create sonic boom that damages enemies
 * - Theme: Crystals, sound, vibration, harmony
 */
public class ResonanceCrystal extends DivineItem {
    
    private static final int ORE_SENSE_RADIUS = 20;
    private static final int SONIC_BOOM_RADIUS = 8;
    private static final int COOLDOWN_SECONDS = 20;
    
    public ResonanceCrystal() {
        super(
            GodType.CRYSTAL,
            Material.AMETHYST_SHARD,
            "§d§l💎 Resonance Crystal 💎§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A perfect crystal that resonates with the",
            "§7harmonic frequencies of creation itself.",
            "",
            "§d§lPassive Abilities:",
            "§7• Crystal Shield when sneaking",
            "§7• Ore Sense (see valuable ores)",
            "§7• Immunity to mining fatigue",
            "",
            "§d§lActive Ability:",
            "§7• Right-click for Sonic Boom",
            "§7• Damages enemies within 8 blocks",
            "§7• Pushes enemies away with sound",
            "§7• Cooldown: §f20 seconds",
            "",
            "§8\"Every crystal holds a note of creation's song.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.FORTUNE, 3);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        List<Entity> nearbyEntities = player.getNearbyEntities(SONIC_BOOM_RADIUS, SONIC_BOOM_RADIUS, SONIC_BOOM_RADIUS);
        int affectedCount = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity target = (LivingEntity) entity;
                
                // Damage and knockback
                target.damage(6.0, player);
                
                // Calculate knockback direction
                org.bukkit.util.Vector direction = target.getLocation().toVector()
                    .subtract(player.getLocation().toVector()).normalize();
                direction.setY(0.3);
                direction.multiply(1.5);
                target.setVelocity(direction);
                
                // Apply confusion
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
                
                affectedCount++;
            }
        }
        
        if (affectedCount == 0) {
            player.sendMessage("§d§l💎 Resonance Crystal: §r§cNo enemies found for sonic boom!");
            return false;
        }
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
        
        // Messages
        player.sendMessage("§d§l💎 SONIC BOOM! 💎");
        player.sendMessage("§7Crystal harmonics shatter the air!");
        player.sendMessage("§7Affected §f" + affectedCount + "§7 enemies with sonic waves!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Crystal shield when sneaking
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 0, false, false));
        }
        
        // Remove mining fatigue
        if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
        
        // Ore sense effect (simplified - just give night vision for now)
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§d§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Resonance Crystal hums with harmonic energy!");
        player.sendMessage("§7You can sense the song within all things.");
        player.sendMessage("§7Right-click for sonic boom, sneak for crystal shield.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Resonance Crystal is no longer with you.");
        player.sendMessage("§7The harmonic frequencies fade to silence.");
        
        // Remove crystal effects
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Reveal ores within radius (simplified implementation)
     */
    private void revealOres(Player player) {
        Location center = player.getLocation();
        int oreCount = 0;
        
        for (int x = -ORE_SENSE_RADIUS; x <= ORE_SENSE_RADIUS; x++) {
            for (int y = -ORE_SENSE_RADIUS; y <= ORE_SENSE_RADIUS; y++) {
                for (int z = -ORE_SENSE_RADIUS; z <= ORE_SENSE_RADIUS; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (isValuableOre(block.getType())) {
                        oreCount++;
                    }
                }
            }
        }
        
        if (oreCount > 0) {
            player.sendMessage("§d§l💎 Crystal Resonance: §r§dDetected §f" + oreCount + "§d valuable ores nearby!");
        }
    }
    
    /**
     * Check if a material is a valuable ore
     */
    private boolean isValuableOre(Material material) {
        return material == Material.DIAMOND_ORE ||
               material == Material.DEEPSLATE_DIAMOND_ORE ||
               material == Material.EMERALD_ORE ||
               material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.GOLD_ORE ||
               material == Material.DEEPSLATE_GOLD_ORE ||
               material == Material.IRON_ORE ||
               material == Material.DEEPSLATE_IRON_ORE ||
               material == Material.ANCIENT_DEBRIS ||
               material == Material.AMETHYST_CLUSTER;
    }
}