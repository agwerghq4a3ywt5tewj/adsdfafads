package com.example.minecraftplugin.items.legendary;

import com.example.minecraftplugin.items.ScepterOfBanishment;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Legendary variant of Scepter of Banishment with inferno powers
 */
public class InfernoScepterOfBanishment extends LegendaryVariant {
    
    public InfernoScepterOfBanishment() {
        super(
            new ScepterOfBanishment(),
            "Inferno Scepter",
            "§c§l🔥 Inferno Scepter of Banishment 🔥§r",
            Arrays.asList(
                "§c§lLegendary Enhancement:",
                "§7• Creates permanent fire zones",
                "§7• Summons fire elementals",
                "§7• Lava immunity and walking",
                "§7• Fire aura damages nearby enemies",
                "§7• Molten armor when health is low"
            ),
            0.04 // 4% spawn chance
        );
    }
    
    @Override
    protected boolean executeLegendaryAbility(Player player, ItemStack item) {
        // Create inferno zone with fire elementals
        org.bukkit.Location center = player.getLocation();
        
        // Create permanent fire blocks in a pattern
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * 360;
            double radius = 5.0;
            double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
            double z = center.getZ() + radius * Math.sin(Math.toRadians(angle));
            
            org.bukkit.Location fireLoc = new org.bukkit.Location(center.getWorld(), x, center.getY(), z);
            if (fireLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                fireLoc.getBlock().setType(org.bukkit.Material.FIRE);
            }
        }
        
        // Summon fire elementals (blazes)
        for (int i = 0; i < 3; i++) {
            org.bukkit.Location spawnLoc = center.clone().add(
                (Math.random() - 0.5) * 8, 1, (Math.random() - 0.5) * 8);
            org.bukkit.entity.Blaze blaze = center.getWorld().spawn(spawnLoc, org.bukkit.entity.Blaze.class);
            blaze.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 1));
            blaze.setCustomName("§c§lFire Elemental");
            blaze.setCustomNameVisible(true);
        }
        
        // Massive fire damage to enemies
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)) {
                org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) entity;
                target.damage(20.0, player);
                target.setFireTicks(400); // 20 seconds of fire
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2));
            }
        }
        
        // Visual effects
        center.getWorld().spawnParticle(org.bukkit.Particle.FLAME, center, 100, 4, 4, 4, 0.3);
        center.getWorld().spawnParticle(org.bukkit.Particle.LAVA, center, 50, 3, 3, 3, 0.2);
        center.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, center, 5);
        
        player.sendMessage("§c§l🔥 INFERNO SUMMONING! 🔥");
        player.sendMessage("§7Fire elementals rise to serve you!");
        
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.8f);
        
        return true;
    }
    
    @Override
    protected void applyLegendaryPassiveEffects(Player player, ItemStack item) {
        // Lava immunity and walking
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
        
        // Fire aura damages nearby enemies
        if (Math.random() < 0.2) { // 20% chance per second
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(4, 4, 4)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)) {
                    org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) entity;
                    target.damage(2.0, player);
                    target.setFireTicks(60);
                }
            }
            player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation().add(0, 1, 0), 8, 1, 1, 1, 0.1);
        }
        
        // Molten armor when health is low
        double healthPercent = player.getHealth() / player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        if (healthPercent < 0.3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 2, false, false));
            
            if (Math.random() < 0.3) {
                player.getWorld().spawnParticle(org.bukkit.Particle.LAVA, player.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }
    
    @Override
    protected void removeLegendaryEffects(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
    }
}