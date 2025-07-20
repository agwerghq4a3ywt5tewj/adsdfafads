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
import java.util.logging.Logger;

/**
 * Enhanced altar effects system with complex particle systems and god-specific themes
 */
public class EnhancedAltarEffects {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Track active enhanced effects
    private final Map<String, Long> activeEnhancedEffects;
    
    public EnhancedAltarEffects(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.activeEnhancedEffects = new HashMap<>();
        
        logger.info("Enhanced Altar Effects system initialized");
    }
    
    /**
     * Create enhanced testament completion effects with complex particle systems
     */
    public void createEnhancedTestamentEffects(Player player, GodType god, Location altarLocation) {
        String effectKey = getLocationKey(altarLocation) + "_" + god.name();
        
        // Prevent effect spam
        if (isEnhancedEffectActive(effectKey)) {
            return;
        }
        
        markEnhancedEffectActive(effectKey);
        
        // Create pre-completion buildup
        createPreCompletionBuildup(player, god, altarLocation);
        
        // Create main completion sequence
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            createMainCompletionSequence(player, god, altarLocation);
        }, 60L); // 3 second delay
        
        // Create post-completion effects
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            createPostCompletionEffects(player, god, altarLocation);
        }, 200L); // 10 second delay
        
        logger.info("Created enhanced testament effects for " + god.getDisplayName() + 
                   " at " + altarLocation.getWorld().getName());
    }
    
    /**
     * Create pre-completion buildup effects
     */
    private void createPreCompletionBuildup(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                // Create energy gathering effect
                double intensity = ticks / 60.0; // 0 to 1
                createEnergyGatheringEffect(location, god, intensity);
                
                // Play buildup sounds
                if (ticks % 20 == 0) {
                    playBuildupSound(player, god, intensity);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create energy gathering effect
     */
    private void createEnergyGatheringEffect(Location location, GodType god, double intensity) {
        int particleCount = (int) (5 + intensity * 15);
        double radius = 8.0 - (intensity * 3.0); // Shrinking circle
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (i / (double) particleCount) * 360;
            double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
            double y = location.getY() + 1 + Math.sin(Math.toRadians(angle * 2)) * intensity;
            double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
            
            Location particleLocation = new Location(location.getWorld(), x, y, z);
            
            // Create god-specific energy particles
            createGodEnergyParticles(particleLocation, god, intensity);
        }
    }
    
    /**
     * Create god-specific energy particles
     */
    private void createGodEnergyParticles(Location location, GodType god, double intensity) {
        switch (god) {
            case FALLEN:
                location.getWorld().spawnParticle(Particle.SOUL, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.02);
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 
                    (int)(1 + intensity * 2), 0.05, 0.05, 0.05, 0.01);
                break;
                
            case BANISHMENT:
                location.getWorld().spawnParticle(Particle.FLAME, location, 
                    (int)(3 + intensity * 5), 0.2, 0.2, 0.2, 0.05);
                location.getWorld().spawnParticle(Particle.LAVA, location, 
                    (int)(1 + intensity * 2), 0.1, 0.1, 0.1, 0.02);
                break;
                
            case ABYSSAL:
                location.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, location, 
                    (int)(4 + intensity * 6), 0.3, 0.3, 0.3, 0.1);
                location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location, 
                    (int)(2 + intensity * 3), 0.2, 0.2, 0.2, 0.05);
                break;
                
            case SYLVAN:
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 
                    (int)(3 + intensity * 4), 0.2, 0.2, 0.2, 0.1);
                location.getWorld().spawnParticle(Particle.COMPOSTER, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.05);
                break;
                
            case TEMPEST:
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 
                    (int)(4 + intensity * 6), 0.3, 0.3, 0.3, 0.1);
                location.getWorld().spawnParticle(Particle.CLOUD, location, 
                    (int)(2 + intensity * 3), 0.2, 0.2, 0.2, 0.02);
                break;
                
            case VEIL:
                location.getWorld().spawnParticle(Particle.PORTAL, location, 
                    (int)(5 + intensity * 8), 0.4, 0.4, 0.4, 0.2);
                location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 
                    (int)(3 + intensity * 4), 0.2, 0.2, 0.2, 0.1);
                break;
                
            case FORGE:
                location.getWorld().spawnParticle(Particle.CRIT, location, 
                    (int)(4 + intensity * 6), 0.3, 0.3, 0.3, 0.1);
                location.getWorld().spawnParticle(Particle.LAVA, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.02);
                break;
                
            case VOID:
                location.getWorld().spawnParticle(Particle.SQUID_INK, location, 
                    (int)(3 + intensity * 4), 0.2, 0.2, 0.2, 0.02);
                location.getWorld().spawnParticle(Particle.PORTAL, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.05);
                break;
                
            case TIME:
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 
                    (int)(5 + intensity * 7), 0.4, 0.4, 0.4, 0.15);
                location.getWorld().spawnParticle(Particle.END_ROD, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.02);
                break;
                
            case BLOOD:
                location.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, location, 
                    (int)(3 + intensity * 4), 0.2, 0.2, 0.2, 0.05);
                location.getWorld().spawnParticle(Particle.CRIT, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.1);
                break;
                
            case CRYSTAL:
                location.getWorld().spawnParticle(Particle.NOTE, location, 
                    (int)(4 + intensity * 5), 0.3, 0.3, 0.3, 0.1);
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.05);
                break;
                
            case SHADOW:
                location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 
                    (int)(3 + intensity * 4), 0.2, 0.2, 0.2, 0.02);
                location.getWorld().spawnParticle(Particle.SQUID_INK, location, 
                    (int)(2 + intensity * 3), 0.1, 0.1, 0.1, 0.01);
                break;
        }
    }
    
    /**
     * Play buildup sound based on god and intensity
     */
    private void playBuildupSound(Player player, GodType god, double intensity) {
        float volume = (float) (0.3 + intensity * 0.5);
        float pitch = (float) (0.8 + intensity * 0.4);
        
        Sound sound = getGodBuildupSound(god);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * Get buildup sound for each god
     */
    private Sound getGodBuildupSound(GodType god) {
        switch (god) {
            case FALLEN:
                return Sound.ENTITY_WITHER_AMBIENT;
            case BANISHMENT:
                return Sound.ENTITY_BLAZE_AMBIENT;
            case ABYSSAL:
                return Sound.AMBIENT_UNDERWATER_LOOP;
            case SYLVAN:
                return Sound.ENTITY_BEE_POLLINATE;
            case TEMPEST:
                return Sound.ENTITY_LIGHTNING_BOLT_IMPACT;
            case VEIL:
                return Sound.BLOCK_PORTAL_AMBIENT;
            case FORGE:
                return Sound.BLOCK_FIRE_AMBIENT;
            case VOID:
                return Sound.ENTITY_ENDERMAN_AMBIENT;
            case TIME:
                return Sound.BLOCK_BEACON_AMBIENT;
            case BLOOD:
                return Sound.ENTITY_RAVAGER_AMBIENT;
            case CRYSTAL:
                return Sound.BLOCK_AMETHYST_BLOCK_RESONATE;
            case SHADOW:
                return Sound.BLOCK_SCULK_SENSOR_CLICKING;
            default:
                return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
    
    /**
     * Create main completion sequence with dramatic timing
     */
    private void createMainCompletionSequence(Player player, GodType god, Location location) {
        // Phase 1: Energy convergence (2 seconds)
        createEnergyConvergence(player, god, location);
        
        // Phase 2: Divine manifestation (3 seconds)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            createDivineManifestationEffect(player, god, location);
        }, 40L);
        
        // Phase 3: Power transfer (2 seconds)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            createPowerTransferEffect(player, god, location);
        }, 100L);
        
        // Phase 4: Completion burst (1 second)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            createCompletionBurst(player, god, location);
        }, 140L);
    }
    
    /**
     * Create energy convergence effect
     */
    private void createEnergyConvergence(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) { // 2 seconds
                    cancel();
                    return;
                }
                
                // Create converging energy beams
                for (int beam = 0; beam < 8; beam++) {
                    double angle = beam * 45;
                    double distance = 10.0 - (ticks * 0.2); // Converging
                    
                    double x = location.getX() + distance * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + 5;
                    double z = location.getZ() + distance * Math.sin(Math.toRadians(angle));
                    
                    Location beamStart = new Location(location.getWorld(), x, y, z);
                    
                    // Create beam towards center
                    createEnergyBeam(beamStart, location, god);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Play convergence sound
        player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
    }
    
    /**
     * Create energy beam effect
     */
    private void createEnergyBeam(Location start, Location end, GodType god) {
        double distance = start.distance(end);
        int steps = (int) (distance * 2);
        
        for (int i = 0; i <= steps; i++) {
            double ratio = i / (double) steps;
            double x = start.getX() + (end.getX() - start.getX()) * ratio;
            double y = start.getY() + (end.getY() - start.getY()) * ratio;
            double z = start.getZ() + (end.getZ() - start.getZ()) * ratio;
            
            Location beamPoint = new Location(start.getWorld(), x, y, z);
            createGodEnergyParticles(beamPoint, god, 1.0);
        }
    }
    
    /**
     * Create divine manifestation effect
     */
    private void createDivineManifestationEffect(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                // Create god avatar manifestation
                double height = 3.0 + Math.sin(Math.toRadians(ticks * 6)) * 1.5;
                Location manifestLocation = location.clone().add(0, height, 0);
                
                // Create god-specific manifestation
                createGodManifestation(manifestLocation, god, ticks);
                
                // Lightning strikes for dramatic effect
                if (ticks % 15 == 0) {
                    location.getWorld().strikeLightningEffect(location.clone().add(
                        (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6));
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Play manifestation sound
        player.playSound(location, getGodManifestationSound(god), 1.0f, 0.8f);
    }
    
    /**
     * Create god-specific manifestation
     */
    private void createGodManifestation(Location location, GodType god, int ticks) {
        switch (god) {
            case FALLEN:
                // Death avatar with soul energy
                location.getWorld().spawnParticle(Particle.SOUL, location, 20, 1, 1, 1, 0.1);
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 0.5, 0.5, 0.5, 0.05);
                break;
                
            case BANISHMENT:
                // Fire avatar with molten core
                location.getWorld().spawnParticle(Particle.FLAME, location, 25, 1.2, 1.2, 1.2, 0.2);
                location.getWorld().spawnParticle(Particle.LAVA, location, 15, 0.8, 0.8, 0.8, 0.1);
                break;
                
            case TEMPEST:
                // Storm avatar with lightning
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 30, 1.5, 1.5, 1.5, 0.3);
                location.getWorld().spawnParticle(Particle.CLOUD, location, 20, 1, 1, 1, 0.05);
                break;
                
            case VEIL:
                // Reality avatar with dimensional tears
                location.getWorld().spawnParticle(Particle.PORTAL, location, 40, 2, 2, 2, 0.5);
                location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 20, 1, 1, 1, 0.2);
                break;
                
            default:
                // Generic divine manifestation
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 25, 1, 1, 1, 0.2);
                location.getWorld().spawnParticle(Particle.END_ROD, location, 15, 0.8, 0.8, 0.8, 0.1);
                break;
        }
    }
    
    /**
     * Get manifestation sound for each god
     */
    private Sound getGodManifestationSound(GodType god) {
        switch (god) {
            case FALLEN:
                return Sound.ENTITY_WITHER_DEATH;
            case BANISHMENT:
                return Sound.ENTITY_GHAST_SHOOT;
            case ABYSSAL:
                return Sound.ENTITY_ELDER_GUARDIAN_AMBIENT;
            case SYLVAN:
                return Sound.ENTITY_ENDER_DRAGON_GROWL;
            case TEMPEST:
                return Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case VEIL:
                return Sound.BLOCK_PORTAL_TRIGGER;
            default:
                return Sound.ENTITY_ENDER_DRAGON_DEATH;
        }
    }
    
    /**
     * Create power transfer effect
     */
    private void createPowerTransferEffect(Player player, GodType god, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) { // 2 seconds
                    cancel();
                    return;
                }
                
                // Create power stream from manifestation to player
                Location playerLocation = player.getLocation().add(0, 1, 0);
                Location manifestLocation = location.clone().add(0, 4, 0);
                
                createPowerStream(manifestLocation, playerLocation, god, ticks);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Play power transfer sound
        player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.6f);
    }
    
    /**
     * Create power stream effect
     */
    private void createPowerStream(Location start, Location end, GodType god, int ticks) {
        double distance = start.distance(end);
        int steps = (int) (distance * 3);
        
        for (int i = 0; i <= steps; i++) {
            double ratio = i / (double) steps;
            double x = start.getX() + (end.getX() - start.getX()) * ratio;
            double y = start.getY() + (end.getY() - start.getY()) * ratio;
            double z = start.getZ() + (end.getZ() - start.getZ()) * ratio;
            
            // Add spiral motion
            double spiralRadius = 0.3 * Math.sin(Math.toRadians(i * 20 + ticks * 10));
            double spiralAngle = i * 30 + ticks * 15;
            x += spiralRadius * Math.cos(Math.toRadians(spiralAngle));
            z += spiralRadius * Math.sin(Math.toRadians(spiralAngle));
            
            Location streamPoint = new Location(start.getWorld(), x, y, z);
            createGodEnergyParticles(streamPoint, god, 0.8);
        }
    }
    
    /**
     * Create completion burst effect
     */
    private void createCompletionBurst(Player player, GodType god, Location location) {
        // Massive particle explosion
        for (int burst = 0; burst < 5; burst++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location.clone().add(0, 2, 0), 5);
                createGodEnergyParticles(location.clone().add(0, 2, 0), god, 2.0);
                
                // Shockwave
                for (int i = 1; i <= 15; i++) {
                    double radius = i * 1.5;
                    for (int j = 0; j < 24; j++) {
                        double angle = (j / 24.0) * 360;
                        double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                        double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                        Location shockwavePoint = new Location(location.getWorld(), x, location.getY(), z);
                        
                        createGodEnergyParticles(shockwavePoint, god, 0.5);
                    }
                }
            }, burst * 4L);
        }
        
        // Play completion sound sequence
        player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.5f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }, 10L);
    }
    
    /**
     * Create post-completion effects
     */
    private void createPostCompletionEffects(Player player, GodType god, Location location) {
        // Create permanent altar enhancement
        enhanceAltarPermanently(location, god);
        
        // Create lingering divine presence
        createLingeringDivinePresence(location, god);
    }
    
    /**
     * Enhance altar permanently with god-specific blocks
     */
    private void enhanceAltarPermanently(Location location, GodType god) {
        // Add god-specific enhancement blocks around the altar
        switch (god) {
            case FALLEN:
                // Add soul fire around the altar
                addEnhancementBlocks(location, Material.SOUL_FIRE, 2);
                break;
            case BANISHMENT:
                // Add fire around the altar
                addEnhancementBlocks(location, Material.FIRE, 2);
                break;
            case TEMPEST:
                // Add lightning rods
                addEnhancementBlocks(location, Material.LIGHTNING_ROD, 1);
                break;
            case VEIL:
                // Add end rods for mystical effect
                addEnhancementBlocks(location, Material.END_ROD, 1);
                break;
            default:
                // Add torches for general enhancement
                addEnhancementBlocks(location, Material.TORCH, 1);
                break;
        }
    }
    
    /**
     * Add enhancement blocks around the altar
     */
    private void addEnhancementBlocks(Location center, Material material, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue; // Skip center
                
                Location blockLocation = center.clone().add(x, 1, z);
                Block block = blockLocation.getBlock();
                
                if (block.getType() == Material.AIR) {
                    block.setType(material);
                }
            }
        }
    }
    
    /**
     * Create lingering divine presence effect
     */
    private void createLingeringDivinePresence(Location location, GodType god) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if altar still exists
                if (location.getBlock().getType() != god.getAltarCenterBlock()) {
                    cancel();
                    return;
                }
                
                // Create subtle divine presence particles
                createGodEnergyParticles(location.clone().add(0, 1, 0), god, 0.3);
            }
        }.runTaskTimer(plugin, 0L, 100L); // Every 5 seconds
    }
    
    /**
     * Check if enhanced effect is active
     */
    private boolean isEnhancedEffectActive(String effectKey) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = activeEnhancedEffects.get(effectKey);
        
        if (lastTime == null) {
            return false;
        }
        
        // Enhanced effects are considered active for 60 seconds
        return (currentTime - lastTime) < 60000;
    }
    
    /**
     * Mark enhanced effect as active
     */
    private void markEnhancedEffectActive(String effectKey) {
        activeEnhancedEffects.put(effectKey, System.currentTimeMillis());
    }
    
    /**
     * Get location key for tracking
     */
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + 
               location.getBlockX() + "_" + 
               location.getBlockY() + "_" + 
               location.getBlockZ();
    }
    
    /**
     * Clear all enhanced effects
     */
    public void clearAllEnhancedEffects() {
        activeEnhancedEffects.clear();
    }
}