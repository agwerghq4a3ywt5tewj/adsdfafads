package com.example.minecraftplugin.effects;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages enhanced altar effects for testament completion
 */
public class AltarEffectsManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Track active altar effects to prevent overlap
    private final Map<String, Long> activeAltarEffects;
    
    public AltarEffectsManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.activeAltarEffects = new HashMap<>();
        
        logger.info("Altar Effects Manager initialized");
    }
    
    /**
     * Create enhanced testament completion effects
     */
    public void createTestamentCompletionEffects(Player player, GodType god, Location altarLocation) {
        String altarKey = getLocationKey(altarLocation);
        
        // Prevent effect spam
        if (isEffectActive(altarKey)) {
            return;
        }
        
        markEffectActive(altarKey);
        
        // Create god-specific enhanced effects
        createGodSpecificEffects(player, god, altarLocation);
        
        // Use enhanced altar effects if available
        if (plugin.getEnhancedAltarEffects() != null) {
            plugin.getEnhancedAltarEffects().createEnhancedTestamentEffects(player, god, altarLocation);
        }
        
        // Create dramatic lightning sequence
        createEnhancedLightningSequence(player, god, altarLocation);
        
        // Create permanent altar marking
        createPermanentAltarMarking(altarLocation, god);
        
        // Create enhanced beacon beam
        createEnhancedBeaconBeam(player, god, altarLocation);
        
        // Play god-specific sound sequence
        playGodSpecificSounds(player, god, altarLocation);
        
        logger.info("Created enhanced testament completion effects for " + god.getDisplayName() + 
                   " at " + altarLocation.getWorld().getName() + " " + 
                   altarLocation.getBlockX() + "," + altarLocation.getBlockY() + "," + altarLocation.getBlockZ());
    }
    
    /**
     * Create god-specific visual effects
     */
    private void createGodSpecificEffects(Player player, GodType god, Location location) {
        switch (god) {
            case FALLEN:
                createFallenGodEffects(location);
                break;
            case BANISHMENT:
                createBanishmentGodEffects(location);
                break;
            case ABYSSAL:
                createAbyssalGodEffects(location);
                break;
            case SYLVAN:
                createSylvanGodEffects(location);
                break;
            case TEMPEST:
                createTempestGodEffects(location);
                break;
            case VEIL:
                createVeilGodEffects(location);
                break;
            case FORGE:
                createForgeGodEffects(location);
                break;
            case VOID:
                createVoidGodEffects(location);
                break;
            case TIME:
                createTimeGodEffects(location);
                break;
            case BLOOD:
                createBloodGodEffects(location);
                break;
            case CRYSTAL:
                createCrystalGodEffects(location);
                break;
            case SHADOW:
                createShadowGodEffects(location);
                break;
        }
    }
    
    /**
     * Fallen God specific effects - Dark souls and death energy
     */
    private void createFallenGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 120) { // 6 seconds
                    cancel();
                    return;
                }
                
                // Soul spiral
                double radius = 4.0;
                double height = 0.1 * ticks;
                double angle = ticks * 0.4;
                
                for (int i = 0; i < 3; i++) {
                    double spiralAngle = angle + (i * 120);
                    double x = location.getX() + radius * Math.cos(Math.toRadians(spiralAngle));
                    double y = location.getY() + height + i;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(spiralAngle));
                    
                    Location spiralLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.SOUL, spiralLoc, 5, 0.2, 0.2, 0.2, 0.05);
                    location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spiralLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }
                
                // Central dark energy burst every 30 ticks
                if (ticks % 30 == 0) {
                    location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location.clone().add(0, 2, 0), 20, 1, 1, 1, 0.01);
                    location.getWorld().spawnParticle(Particle.ASH, location.clone().add(0, 2, 0), 30, 2, 2, 2, 0.2);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Banishment God specific effects - Fire and lava eruptions
     */
    private void createBanishmentGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }
                
                // Fire tornado effect
                double radius = 3.0;
                double height = 0.15 * ticks;
                
                for (int i = 0; i < 8; i++) {
                    double angle = (i / 8.0) * 360 + ticks * 5;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location fireLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.FLAME, fireLoc, 8, 0.3, 0.3, 0.3, 0.1);
                    location.getWorld().spawnParticle(Particle.LAVA, fireLoc, 3, 0.2, 0.2, 0.2, 0.05);
                }
                
                // Lava eruption every 25 ticks
                if (ticks % 25 == 0) {
                    for (int i = 0; i < 5; i++) {
                        double offsetX = (Math.random() - 0.5) * 4;
                        double offsetZ = (Math.random() - 0.5) * 4;
                        Location eruptionLoc = location.clone().add(offsetX, 0, offsetZ);
                        
                        location.getWorld().spawnParticle(Particle.LAVA, eruptionLoc, 15, 0.5, 2, 0.5, 0.3);
                        location.getWorld().spawnParticle(Particle.FLAME, eruptionLoc, 20, 0.8, 3, 0.8, 0.2);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Abyssal God specific effects - Water vortex and ocean depths
     */
    private void createAbyssalGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 140) { // 7 seconds
                    cancel();
                    return;
                }
                
                // Water vortex
                double radius = 5.0 - (ticks * 0.02); // Shrinking vortex
                
                for (int i = 0; i < 12; i++) {
                    double angle = (i / 12.0) * 360 + ticks * 8;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 1 + Math.sin(Math.toRadians(ticks * 4)) * 0.5;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location waterLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.DRIPPING_WATER, waterLoc, 5, 0.1, 0.1, 0.1, 0.1);
                    location.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, waterLoc, 3, 0.2, 0.2, 0.2, 0.05);
                }
                
                // Central water spout every 20 ticks
                if (ticks % 20 == 0) {
                    for (int i = 0; i < 10; i++) {
                        Location spoutLoc = location.clone().add(0, i * 0.5, 0);
                        location.getWorld().spawnParticle(Particle.DRIPPING_WATER, spoutLoc, 10, 0.3, 0.1, 0.3, 0.1);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Sylvan God specific effects - Nature growth and forest magic
     */
    private void createSylvanGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 160) { // 8 seconds
                    cancel();
                    return;
                }
                
                // Growing tree effect
                double radius = 2.0 + (ticks * 0.02);
                
                for (int i = 0; i < 6; i++) {
                    double angle = (i / 6.0) * 360 + ticks * 2;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + (ticks * 0.05);
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location natureLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, natureLoc, 3, 0.2, 0.2, 0.2, 0.1);
                    location.getWorld().spawnParticle(Particle.COMPOSTER, natureLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }
                
                // Flower bloom effect every 40 ticks
                if (ticks % 40 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double offsetX = (Math.random() - 0.5) * 6;
                        double offsetZ = (Math.random() - 0.5) * 6;
                        Location bloomLoc = location.clone().add(offsetX, 0, offsetZ);
                        
                        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, bloomLoc, 15, 1, 1, 1, 0.2);
                        location.getWorld().spawnParticle(Particle.HEART, bloomLoc, 5, 0.5, 0.5, 0.5, 0.1);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Tempest God specific effects - Storm and lightning
     */
    private void createTempestGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 120) { // 6 seconds
                    cancel();
                    return;
                }
                
                // Storm cloud formation
                double radius = 4.0;
                double height = 8 + Math.sin(Math.toRadians(ticks * 3)) * 2;
                
                for (int i = 0; i < 16; i++) {
                    double angle = (i / 16.0) * 360 + ticks * 3;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location cloudLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.CLOUD, cloudLoc, 5, 0.5, 0.2, 0.5, 0.02);
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, cloudLoc, 3, 0.3, 0.1, 0.3, 0.05);
                }
                
                // Lightning strikes every 15 ticks
                if (ticks % 15 == 0) {
                    double offsetX = (Math.random() - 0.5) * 8;
                    double offsetZ = (Math.random() - 0.5) * 8;
                    Location strikeLoc = location.clone().add(offsetX, 0, offsetZ);
                    
                    // Visual lightning bolt
                    for (int i = 0; i < 20; i++) {
                        Location boltLoc = strikeLoc.clone().add(0, i, 0);
                        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, boltLoc, 8, 0.2, 0.1, 0.2, 0.1);
                    }
                    
                    location.getWorld().strikeLightningEffect(strikeLoc);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Veil God specific effects - Reality distortion and void magic
     */
    private void createVeilGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 180) { // 9 seconds
                    cancel();
                    return;
                }
                
                // Reality distortion waves
                double waveRadius = (ticks * 0.1) % 6;
                
                for (int i = 0; i < 24; i++) {
                    double angle = (i / 24.0) * 360;
                    double x = location.getX() + waveRadius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 2 + Math.sin(Math.toRadians(ticks * 2)) * 1;
                    double z = location.getZ() + waveRadius * Math.sin(Math.toRadians(angle));
                    
                    Location veilLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.PORTAL, veilLoc, 8, 0.3, 0.3, 0.3, 0.1);
                    location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, veilLoc, 4, 0.2, 0.2, 0.2, 0.05);
                }
                
                // Reality tears every 30 ticks
                if (ticks % 30 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double offsetX = (Math.random() - 0.5) * 10;
                        double offsetY = Math.random() * 5;
                        double offsetZ = (Math.random() - 0.5) * 10;
                        Location tearLoc = location.clone().add(offsetX, offsetY, offsetZ);
                        
                        location.getWorld().spawnParticle(Particle.PORTAL, tearLoc, 30, 0.1, 2, 0.1, 0.3);
                        location.getWorld().spawnParticle(Particle.END_ROD, tearLoc, 10, 0.2, 1, 0.2, 0.1);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Forge God specific effects - Molten metal and creation energy
     */
    private void createForgeGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }
                
                // Molten metal sparks
                for (int i = 0; i < 12; i++) {
                    double angle = (i / 12.0) * 360 + ticks * 6;
                    double radius = 2.5 + Math.sin(Math.toRadians(ticks * 4)) * 0.5;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 1.5;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location sparkLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.CRIT, sparkLoc, 5, 0.2, 0.2, 0.2, 0.1);
                    location.getWorld().spawnParticle(Particle.LAVA, sparkLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }
                
                // Anvil strikes every 20 ticks
                if (ticks % 20 == 0) {
                    location.getWorld().spawnParticle(Particle.CRIT, location.clone().add(0, 2, 0), 25, 1, 1, 1, 0.3);
                    location.getWorld().spawnParticle(Particle.FLAME, location.clone().add(0, 2, 0), 15, 0.8, 0.8, 0.8, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Void God specific effects - Emptiness and phase shifting
     */
    private void createVoidGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 140) { // 7 seconds
                    cancel();
                    return;
                }
                
                // Void collapse effect
                double radius = 5.0 - (ticks * 0.03);
                
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * 360 + ticks * 10;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 2 + Math.sin(Math.toRadians(angle + ticks * 5)) * 1;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location voidLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.SQUID_INK, voidLoc, 3, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.PORTAL, voidLoc, 2, 0.2, 0.2, 0.2, 0.05);
                }
                
                // Void rifts every 35 ticks
                if (ticks % 35 == 0) {
                    location.getWorld().spawnParticle(Particle.SQUID_INK, location.clone().add(0, 3, 0), 40, 2, 2, 2, 0.2);
                    location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location.clone().add(0, 3, 0), 20, 1, 1, 1, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Time God specific effects - Temporal distortion and chronos energy
     */
    private void createTimeGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 200) { // 10 seconds
                    cancel();
                    return;
                }
                
                // Time spiral with varying speeds
                for (int spiral = 0; spiral < 3; spiral++) {
                    double speed = 1.0 + spiral * 0.5;
                    double radius = 3.0 + spiral;
                    double angle = ticks * speed * 3;
                    double height = (ticks * 0.05) + spiral * 2;
                    
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location timeLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.ENCHANT, timeLoc, 4, 0.2, 0.2, 0.2, 0.1);
                    location.getWorld().spawnParticle(Particle.END_ROD, timeLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }
                
                // Temporal pulses every 25 ticks
                if (ticks % 25 == 0) {
                    for (int i = 1; i <= 5; i++) {
                        double pulseRadius = i * 1.5;
                        for (int j = 0; j < 16; j++) {
                            double angle = (j / 16.0) * 360;
                            double x = location.getX() + pulseRadius * Math.cos(Math.toRadians(angle));
                            double y = location.getY() + 1;
                            double z = location.getZ() + pulseRadius * Math.sin(Math.toRadians(angle));
                            
                            Location pulseLoc = new Location(location.getWorld(), x, y, z);
                            location.getWorld().spawnParticle(Particle.ENCHANT, pulseLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Blood God specific effects - Combat energy and berserker rage
     */
    private void createBloodGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 80) { // 4 seconds
                    cancel();
                    return;
                }
                
                // Blood energy waves
                double radius = 3.0;
                
                for (int i = 0; i < 8; i++) {
                    double angle = (i / 8.0) * 360 + ticks * 8;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 1 + Math.sin(Math.toRadians(ticks * 6)) * 0.8;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location bloodLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, bloodLoc, 4, 0.2, 0.2, 0.2, 0.1);
                    location.getWorld().spawnParticle(Particle.CRIT, bloodLoc, 3, 0.1, 0.1, 0.1, 0.05);
                }
                
                // Combat bursts every 20 ticks
                if (ticks % 20 == 0) {
                    location.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, location.clone().add(0, 2, 0), 30, 1.5, 1.5, 1.5, 0.2);
                    location.getWorld().spawnParticle(Particle.CRIT, location.clone().add(0, 2, 0), 20, 1, 1, 1, 0.3);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Crystal God specific effects - Resonance and harmonic energy
     */
    private void createCrystalGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 160) { // 8 seconds
                    cancel();
                    return;
                }
                
                // Harmonic resonance waves
                double waveRadius = (ticks * 0.08) % 5;
                
                for (int i = 0; i < 32; i++) {
                    double angle = (i / 32.0) * 360;
                    double x = location.getX() + waveRadius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 1 + Math.sin(Math.toRadians(ticks * 4 + angle)) * 0.5;
                    double z = location.getZ() + waveRadius * Math.sin(Math.toRadians(angle));
                    
                    Location crystalLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.NOTE, crystalLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    location.getWorld().spawnParticle(Particle.ENCHANT, crystalLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
                
                // Crystal formations every 40 ticks
                if (ticks % 40 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = i * 60;
                        double x = location.getX() + 4 * Math.cos(Math.toRadians(angle));
                        double z = location.getZ() + 4 * Math.sin(Math.toRadians(angle));
                        Location crystalFormLoc = new Location(location.getWorld(), x, location.getY(), z);
                        
                        for (int j = 0; j < 8; j++) {
                            Location crystalPiece = crystalFormLoc.clone().add(0, j * 0.5, 0);
                            location.getWorld().spawnParticle(Particle.NOTE, crystalPiece, 5, 0.2, 0.2, 0.2, 0.1);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Shadow God specific effects - Darkness and stealth energy
     */
    private void createShadowGodEffects(Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 120) { // 6 seconds
                    cancel();
                    return;
                }
                
                // Shadow tendrils
                for (int tendril = 0; tendril < 6; tendril++) {
                    double baseAngle = tendril * 60;
                    double radius = 2.0 + Math.sin(Math.toRadians(ticks * 3 + tendril * 30)) * 1.5;
                    double height = Math.sin(Math.toRadians(ticks * 2 + tendril * 45)) * 3;
                    
                    double x = location.getX() + radius * Math.cos(Math.toRadians(baseAngle + ticks * 2));
                    double y = location.getY() + 1 + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(baseAngle + ticks * 2));
                    
                    Location shadowLoc = new Location(location.getWorld(), x, y, z);
                    location.getWorld().spawnParticle(Particle.LARGE_SMOKE, shadowLoc, 3, 0.2, 0.2, 0.2, 0.02);
                    location.getWorld().spawnParticle(Particle.SQUID_INK, shadowLoc, 2, 0.1, 0.1, 0.1, 0.01);
                }
                
                // Shadow bursts every 30 ticks
                if (ticks % 30 == 0) {
                    location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location.clone().add(0, 2, 0), 25, 2, 2, 2, 0.1);
                    location.getWorld().spawnParticle(Particle.SQUID_INK, location.clone().add(0, 2, 0), 15, 1.5, 1.5, 1.5, 0.05);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create enhanced lightning sequence with proper timing
     */
    private void createEnhancedLightningSequence(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int strikes = 0;
            
            @Override
            public void run() {
                if (strikes >= getGodLightningStrikes(god)) {
                    cancel();
                    return;
                }
                
                // Create god-specific lightning pattern
                Location strikeLocation = getGodLightningLocation(location, god, strikes);
                strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
                
                // Add god-specific lightning effects
                addGodLightningEffects(strikeLocation, god);
                
                strikes++;
            }
        }.runTaskTimer(plugin, 40L, getGodLightningInterval(god)); // Start after 2 seconds
    }
    
    /**
     * Get number of lightning strikes for each god
     */
    private int getGodLightningStrikes(GodType god) {
        switch (god) {
            case TEMPEST:
                return 8; // Most lightning for storm god
            case FALLEN:
            case VEIL:
                return 6;
            case BANISHMENT:
            case FORGE:
            case BLOOD:
                return 5;
            case TIME:
            case VOID:
                return 4;
            default:
                return 3;
        }
    }
    
    /**
     * Get lightning strike interval for each god
     */
    private long getGodLightningInterval(GodType god) {
        switch (god) {
            case TEMPEST:
                return 8L; // Fastest strikes
            case TIME:
                return 15L; // Varied timing
            case BLOOD:
                return 10L; // Aggressive timing
            default:
                return 12L; // Standard timing
        }
    }
    
    /**
     * Get lightning strike location based on god and strike number
     */
    private Location getGodLightningLocation(Location center, GodType god, int strikeNumber) {
        switch (god) {
            case TEMPEST:
                // Circular pattern for storm god
                double angle = strikeNumber * 45;
                double radius = 6.0;
                double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
                double z = center.getZ() + radius * Math.sin(Math.toRadians(angle));
                return new Location(center.getWorld(), x, center.getY(), z);
                
            case FALLEN:
                // Cross pattern for death god
                if (strikeNumber < 4) {
                    double offset = (strikeNumber + 1) * 2;
                    return center.clone().add(strikeNumber % 2 == 0 ? offset : -offset, 0, strikeNumber < 2 ? 0 : (strikeNumber == 2 ? offset : -offset));
                }
                return center.clone().add((Math.random() - 0.5) * 8, 0, (Math.random() - 0.5) * 8);
                
            default:
                // Random pattern for other gods
                double offsetX = (Math.random() - 0.5) * 10;
                double offsetZ = (Math.random() - 0.5) * 10;
                return center.clone().add(offsetX, 0, offsetZ);
        }
    }
    
    /**
     * Add god-specific effects to lightning strikes
     */
    private void addGodLightningEffects(Location location, GodType god) {
        switch (god) {
            case FALLEN:
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 20, 1, 1, 1, 0.1);
                break;
            case BANISHMENT:
                location.getWorld().spawnParticle(Particle.LAVA, location, 15, 1, 1, 1, 0.2);
                break;
            case ABYSSAL:
                location.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, location, 25, 1, 3, 1, 0.1);
                break;
            case TEMPEST:
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 30, 1.5, 1.5, 1.5, 0.2);
                break;
            case VEIL:
                location.getWorld().spawnParticle(Particle.PORTAL, location, 20, 1, 1, 1, 0.3);
                break;
            default:
                location.getWorld().spawnParticle(Particle.FIREWORK, location, 15, 1, 1, 1, 0.2);
                break;
        }
    }
    
    /**
     * Create permanent altar marking with subtle ongoing effects
     */
    private void createPermanentAltarMarking(Location location, GodType god) {
        // Schedule permanent marking task
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if altar still exists
                if (!isAltarStillPresent(location, god)) {
                    cancel();
                    return;
                }
                
                // Create subtle ongoing effects
                createSubtleAltarEffects(location, god);
            }
        }.runTaskTimer(plugin, 200L, 600L); // Start after 10 seconds, repeat every 30 seconds
    }
    
    /**
     * Check if altar is still present at location
     */
    private boolean isAltarStillPresent(Location location, GodType god) {
        Block centerBlock = location.getBlock();
        return centerBlock.getType() == god.getAltarCenterBlock();
    }
    
    /**
     * Create subtle ongoing effects for completed altars
     */
    private void createSubtleAltarEffects(Location location, GodType god) {
        switch (god) {
            case FALLEN:
                location.getWorld().spawnParticle(Particle.SOUL, location.clone().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0.01);
                break;
            case BANISHMENT:
                location.getWorld().spawnParticle(Particle.FLAME, location.clone().add(0, 1, 0), 1, 0.3, 0.3, 0.3, 0.01);
                break;
            case ABYSSAL:
                location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location.clone().add(0, 2, 0), 3, 0.5, 0.5, 0.5, 0.1);
                break;
            case SYLVAN:
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0, 2, 0), 5, 1, 1, 1, 0.1);
                break;
            case TEMPEST:
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 2, 0), 1, 0.2, 0.2, 0.2, 0.02);
                break;
            case VEIL:
                location.getWorld().spawnParticle(Particle.PORTAL, location.clone().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.05);
                break;
            default:
                location.getWorld().spawnParticle(Particle.ENCHANT, location.clone().add(0, 1, 0), 1, 0.3, 0.3, 0.3, 0.02);
                break;
        }
    }
    
    /**
     * Create enhanced beacon beam effect
     */
    private void createEnhancedBeaconBeam(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int duration = 0;
            
            @Override
            public void run() {
                if (duration >= 400) { // 20 seconds
                    cancel();
                    return;
                }
                
                // Create god-specific beam
                createGodBeamEffect(location, god, duration);
                
                duration++;
            }
        }.runTaskTimer(plugin, 60L, 1L); // Start after 3 seconds
    }
    
    /**
     * Create god-specific beam effects
     */
    private void createGodBeamEffect(Location location, GodType god, int duration) {
        // Create upward beam with god-specific particles
        for (int i = 1; i <= 30; i++) {
            Location beamLocation = location.clone().add(0, i, 0);
            
            switch (god) {
                case FALLEN:
                    beamLocation.getWorld().spawnParticle(Particle.SOUL, beamLocation, 2, 0.1, 0.1, 0.1, 0);
                    break;
                case BANISHMENT:
                    beamLocation.getWorld().spawnParticle(Particle.FLAME, beamLocation, 3, 0.1, 0.1, 0.1, 0);
                    break;
                case ABYSSAL:
                    beamLocation.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, beamLocation, 2, 0.1, 0.1, 0.1, 0);
                    break;
                case SYLVAN:
                    beamLocation.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, beamLocation, 2, 0.1, 0.1, 0.1, 0);
                    break;
                case TEMPEST:
                    beamLocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamLocation, 3, 0.1, 0.1, 0.1, 0);
                    break;
                case VEIL:
                    beamLocation.getWorld().spawnParticle(Particle.PORTAL, beamLocation, 4, 0.1, 0.1, 0.1, 0);
                    break;
                default:
                    beamLocation.getWorld().spawnParticle(Particle.END_ROD, beamLocation, 2, 0.1, 0.1, 0.1, 0);
                    break;
            }
        }
    }
    
    /**
     * Play god-specific sound sequences
     */
    private void playGodSpecificSounds(Player player, GodType god, Location location) {
        // Initial dramatic sound
        Sound primarySound = getGodPrimarySound(god);
        player.playSound(location, primarySound, 1.0f, getGodSoundPitch(god));
        
        // Secondary sound after delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Sound secondarySound = getGodSecondarySound(god);
            player.playSound(location, secondarySound, 0.8f, getGodSoundPitch(god) + 0.2f);
        }, 30L);
        
        // Final epic sound
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
            player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }, 60L);
    }
    
    /**
     * Get primary sound for each god
     */
    private Sound getGodPrimarySound(GodType god) {
        switch (god) {
            case FALLEN:
                return Sound.ENTITY_WITHER_DEATH;
            case BANISHMENT:
                return Sound.ENTITY_GHAST_SHOOT;
            case ABYSSAL:
                return Sound.AMBIENT_UNDERWATER_LOOP;
            case SYLVAN:
                return Sound.BLOCK_GRASS_BREAK;
            case TEMPEST:
                return Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case VEIL:
                return Sound.BLOCK_PORTAL_TRIGGER;
            case FORGE:
                return Sound.BLOCK_ANVIL_LAND;
            case VOID:
                return Sound.ENTITY_ENDERMAN_SCREAM;
            case TIME:
                return Sound.BLOCK_BEACON_POWER_SELECT;
            case BLOOD:
                return Sound.ENTITY_RAVAGER_ROAR;
            case CRYSTAL:
                return Sound.BLOCK_AMETHYST_CLUSTER_BREAK;
            case SHADOW:
                return Sound.BLOCK_SCULK_SHRIEKER_SHRIEK;
            default:
                return Sound.ENTITY_ENDER_DRAGON_DEATH;
        }
    }
    
    /**
     * Get secondary sound for each god
     */
    private Sound getGodSecondarySound(GodType god) {
        switch (god) {
            case FALLEN:
                return Sound.PARTICLE_SOUL_ESCAPE;
            case BANISHMENT:
                return Sound.ENTITY_BLAZE_DEATH;
            case ABYSSAL:
                return Sound.ENTITY_DOLPHIN_AMBIENT;
            case SYLVAN:
                return Sound.ENTITY_BEE_POLLINATE;
            case TEMPEST:
                return Sound.ENTITY_ENDER_DRAGON_FLAP;
            case VEIL:
                return Sound.BLOCK_PORTAL_AMBIENT;
            case FORGE:
                return Sound.BLOCK_ANVIL_USE;
            case VOID:
                return Sound.ENTITY_ENDERMAN_TELEPORT;
            case TIME:
                return Sound.BLOCK_BEACON_ACTIVATE;
            case BLOOD:
                return Sound.ENTITY_PLAYER_ATTACK_CRIT;
            case CRYSTAL:
                return Sound.BLOCK_AMETHYST_BLOCK_CHIME;
            case SHADOW:
                return Sound.BLOCK_SCULK_SENSOR_CLICKING;
            default:
                return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
    
    /**
     * Get sound pitch for each god
     */
    private float getGodSoundPitch(GodType god) {
        switch (god) {
            case FALLEN:
                return 0.6f; // Deep, ominous
            case BANISHMENT:
                return 0.8f; // Aggressive
            case ABYSSAL:
                return 0.7f; // Deep, oceanic
            case SYLVAN:
                return 1.2f; // Light, natural
            case TEMPEST:
                return 1.0f; // Powerful, clear
            case VEIL:
                return 1.1f; // Ethereal
            case FORGE:
                return 0.9f; // Strong, metallic
            case VOID:
                return 0.5f; // Very deep, empty
            case TIME:
                return 1.3f; // High, temporal
            case BLOOD:
                return 0.8f; // Aggressive, primal
            case CRYSTAL:
                return 1.4f; // High, crystalline
            case SHADOW:
                return 0.6f; // Dark, whispered
            default:
                return 1.0f;
        }
    }
    
    /**
     * Check if an effect is currently active at a location
     */
    private boolean isEffectActive(String locationKey) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = activeAltarEffects.get(locationKey);
        
        if (lastTime == null) {
            return false;
        }
        
        // Effects are considered active for 30 seconds
        return (currentTime - lastTime) < 30000;
    }
    
    /**
     * Mark an effect as active at a location
     */
    private void markEffectActive(String locationKey) {
        activeAltarEffects.put(locationKey, System.currentTimeMillis());
    }
    
    /**
     * Get a unique key for a location
     */
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + 
               location.getBlockX() + "_" + 
               location.getBlockY() + "_" + 
               location.getBlockZ();
    }
    
    /**
     * Clear all active effects (for shutdown)
     */
    public void clearAllEffects() {
        activeAltarEffects.clear();
    }
}