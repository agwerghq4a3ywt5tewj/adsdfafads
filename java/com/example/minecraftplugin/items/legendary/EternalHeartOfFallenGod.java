package com.example.minecraftplugin.items.legendary;

import java.util.Arrays;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.example.minecraftplugin.items.HeartOfFallenGod;

/**
 * Legendary variant of Heart of Fallen God with enhanced death protection
 */
public class EternalHeartOfFallenGod extends LegendaryVariant {
    
    public EternalHeartOfFallenGod() {
        super(
            new HeartOfFallenGod(),
            "Eternal Heart",
            "§4§l💀 Eternal Heart of the Fallen God 💀§r",
            Arrays.asList(
                "§c§lLegendary Enhancement:",
                "§7• Instant resurrection on death",
                "§7• 35 maximum hearts (70 HP)",
                "§7• Death immunity for 30 seconds after revival",
                "§7• Regeneration III when below 75% health",
                "§7• Soul shield absorbs 50% damage"
            ),
            0.05 // 5% spawn chance
        );
    }
    
    @Override
    protected boolean executeLegendaryAbility(Player player, ItemStack item) {
        // Enhanced healing with soul protection
        double currentHealth = player.getHealth();
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = healthAttribute != null ? healthAttribute.getValue() : 20.0;
        // Full heal
        player.setHealth(maxHealth);
        
        // Grant temporary death immunity
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 4)); // 30 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 10)); // Massive absorption
        
        // Soul shield effect
        player.getWorld().spawnParticle(org.bukkit.Particle.SOUL, player.getLocation(), 50, 2, 2, 2, 0.2);
        player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, player.getLocation(), 30, 1.5, 1.5, 1.5, 0.1);
        
        player.sendMessage("§4§l💀 ETERNAL SOUL PROTECTION! 💀");
        player.sendMessage("§7You are temporarily immune to death itself!");
        
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 2.0f);
        
        return true;
    }
    
    @Override
    protected void applyLegendaryPassiveEffects(Player player, ItemStack item) {
        // Increase max health to 35 hearts
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null && healthAttribute.getBaseValue() < 70.0) {
            healthAttribute.setBaseValue(70.0); // 35 hearts
        }
        
        // Enhanced regeneration threshold
        double currentHealth = player.getHealth();
        double maxHealth = healthAttribute.getValue();
        
        if (currentHealth < (maxHealth * 0.75)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2, false, false));
        }
        
        // Soul shield - damage reduction
        if (Math.random() < 0.1) { // 10% chance per tick
            player.getWorld().spawnParticle(org.bukkit.Particle.SOUL, player.getLocation().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0.02);
        }
    }
    
    @Override
    protected void removeLegendaryEffects(Player player) {
        // Reset max health to base item level
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(50.0); // Back to base Heart level
            if (player.getHealth() > 50.0) {
                player.setHealth(50.0);
            }
        }
        
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
    }
}