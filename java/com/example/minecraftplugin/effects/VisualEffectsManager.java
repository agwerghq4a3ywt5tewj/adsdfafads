package com.example.minecraftplugin.effects;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages enhanced visual effects for the Testament System
 */
public class VisualEffectsManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Track active effects to prevent spam
    private final Map<UUID, Long> lastEffectTime;
    private final Map<String, Long> globalEffectCooldowns;
    
    public VisualEffectsManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.lastEffectTime = new HashMap<>();
        this.globalEffectCooldowns = new HashMap<>();
        
        logger.info("Visual Effects Manager initialized");
    }
    
    /**
     * Check if effects are enabled in config
     */
    private boolean areEffectsEnabled() {
        return plugin.getConfig().getBoolean("effects.enabled", true);
    }
    
    /**
     * Check if player can receive effects (cooldown check)
     */
    private boolean canPlayerReceiveEffect(Player player, long cooldownMs) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastEffectTime.getOrDefault(playerId, 0L);
        
        if (currentTime - lastTime >= cooldownMs) {
            lastEffectTime.put(playerId, currentTime);
            return true;
        }
        return false;
    }
    
    /**
     * Check global effect cooldown
     */
    private boolean checkGlobalCooldown(String effectName, long cooldownMs) {
        long currentTime = System.currentTimeMillis();
        long lastTime = globalEffectCooldowns.getOrDefault(effectName, 0L);
        
        if (currentTime - lastTime >= cooldownMs) {
            globalEffectCooldowns.put(effectName, currentTime);
            return true;
        }
        return false;
    }
    
    /**
     * Create god-specific aura effects around a player
     */
    public void createGodAura(Player player, GodType god) {
        if (!areEffectsEnabled() || !canPlayerReceiveEffect(player, 1000)) { // 1 second cooldown
            return;
        }
        
        Location location = player.getLocation().add(0, 1, 0);
        
        switch (god) {
            case FALLEN:
                createFallenAura(location);
                break;
            case BANISHMENT:
                createBanishmentAura(location);
                break;
            case ABYSSAL:
                createAbyssalAura(location);
                break;
            case SYLVAN:
                createSylvanAura(location);
                break;
            case TEMPEST:
                createTempestAura(location);
                break;
            case VEIL:
                createVeilAura(location);
                break;
            case FORGE:
                createForgeAura(location);
                break;
            case VOID:
                createVoidAura(location);
                break;
            case TIME:
                createTimeAura(location);
                break;
            case BLOOD:
                createBloodAura(location);
                break;
            case CRYSTAL:
                createCrystalAura(location);
                break;
            case SHADOW:
                createShadowAura(location);
                break;
        }
    }
    
    /**
     * Create Fallen God aura (dark, soul-like particles)
     */
    private void createFallenAura(Location location) {
        location.getWorld().spawnParticle(Particle.SOUL, location, 3, 0.5, 0.5, 0.5, 0.02);
        location.getWorld().spawnParticle(Particle.SMOKE, location, 2, 0.3, 0.3, 0.3, 0.01);
    }
    
    /**
     * Create Banishment God aura (fire and embers)
     */
    private void createBanishmentAura(Location location) {
        location.getWorld().spawnParticle(Particle.FLAME, location, 4, 0.4, 0.4, 0.4, 0.02);
        location.getWorld().spawnParticle(Particle.LAVA, location, 1, 0.2, 0.2, 0.2, 0.01);
    }
    
    /**
     * Create Abyssal God aura (water and bubbles)
     */
    private void createAbyssalAura(Location location) {
        location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location, 5, 0.6, 0.6, 0.6, 0.1);
        location.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, location, 2, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Create Sylvan God aura (nature and growth)
     */
    private void createSylvanAura(Location location) {
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 4, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.COMPOSTER, location, 2, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Create Tempest God aura (wind and lightning)
     */
    private void createTempestAura(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 3, 0.4, 0.4, 0.4, 0.05);
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 2, 0.3, 0.3, 0.3, 0.02);
    }
    
    /**
     * Create Veil God aura (reality distortion)
     */
    private void createVeilAura(Location location) {
        location.getWorld().spawnParticle(Particle.PORTAL, location, 6, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 2, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Create Forge God aura (sparks and heat)
     */
    private void createForgeAura(Location location) {
        location.getWorld().spawnParticle(Particle.CRIT, location, 4, 0.4, 0.4, 0.4, 0.1);
        location.getWorld().spawnParticle(Particle.LAVA, location, 1, 0.2, 0.2, 0.2, 0.01);
    }
    
    /**
     * Create Void God aura (void and darkness)
     */
    private void createVoidAura(Location location) {
        location.getWorld().spawnParticle(Particle.SQUID_INK, location, 3, 0.4, 0.4, 0.4, 0.02);
        location.getWorld().spawnParticle(Particle.PORTAL, location, 2, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Create Time God aura (temporal effects)
     */
    private void createTimeAura(Location location) {
        location.getWorld().spawnParticle(Particle.ENCHANT, location, 5, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 2, 0.3, 0.3, 0.3, 0.02);
    }
    
    /**
     * Create Blood God aura (blood and combat)
     */
    private void createBloodAura(Location location) {
        location.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, location, 3, 0.4, 0.4, 0.4, 0.05);
        location.getWorld().spawnParticle(Particle.CRIT, location, 2, 0.3, 0.3, 0.3, 0.1);
    }
    
    /**
     * Create Crystal God aura (resonance and harmony)
     */
    private void createCrystalAura(Location location) {
        location.getWorld().spawnParticle(Particle.NOTE, location, 4, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.ENCHANT, location, 2, 0.3, 0.3, 0.3, 0.05);
    }
    
    /**
     * Create Shadow God aura (darkness and stealth)
     */
    private void createShadowAura(Location location) {
        location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 3, 0.4, 0.4, 0.4, 0.02);
        location.getWorld().spawnParticle(Particle.SQUID_INK, location, 2, 0.3, 0.3, 0.3, 0.01);
    }
    
    /**
     * Create ability activation effect
     */
    public void createAbilityActivationEffect(Player player, GodType god, String abilityName) {
        if (!areEffectsEnabled()) {
            return;
        }
        
        Location location = player.getLocation();
        
        // Play god-specific sound
        Sound sound = getGodSound(god);
        if (sound != null) {
            player.playSound(location, sound, 0.8f, 1.0f);
        }
        
        // Create ability-specific particles
        createAbilityParticles(location, god, abilityName);
    }
    
    /**
     * Get god-specific sound
     */
    private Sound getGodSound(GodType god) {
        switch (god) {
            case FALLEN:
                return Sound.ENTITY_WITHER_AMBIENT;
            case BANISHMENT:
                return Sound.ENTITY_BLAZE_SHOOT;
            case ABYSSAL:
                return Sound.AMBIENT_UNDERWATER_ENTER;
            case SYLVAN:
                return Sound.BLOCK_GRASS_BREAK;
            case TEMPEST:
                return Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case VEIL:
                return Sound.BLOCK_PORTAL_AMBIENT;
            case FORGE:
                return Sound.BLOCK_ANVIL_USE;
            case VOID:
                return Sound.ENTITY_ENDERMAN_TELEPORT;
            case TIME:
                return Sound.BLOCK_BEACON_ACTIVATE;
            case BLOOD:
                return Sound.ENTITY_RAVAGER_ROAR;
            case CRYSTAL:
                return Sound.BLOCK_AMETHYST_BLOCK_CHIME;
            case SHADOW:
                return Sound.BLOCK_SCULK_SENSOR_CLICKING;
            default:
                return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
    
    /**
     * Create ability-specific particles
     */
    private void createAbilityParticles(Location location, GodType god, String abilityName) {
        // Create a burst of god-specific particles
        for (int i = 0; i < 10; i++) {
            double angle = (i / 10.0) * 2 * Math.PI;
            double radius = 1.5;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(location.getWorld(), x, location.getY() + 1, z);
            
            switch (god) {
                case FALLEN:
                    location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLocation, 2);
                    break;
                case BANISHMENT:
                    location.getWorld().spawnParticle(Particle.FLAME, particleLocation, 3);
                    break;
                case ABYSSAL:
                    location.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, particleLocation, 2);
                    break;
                case SYLVAN:
                    location.getWorld().spawnParticle(Particle.COMPOSTER, particleLocation, 3);
                    break;
                case TEMPEST:
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 2);
                    break;
                case VEIL:
                    location.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 4);
                    break;
                default:
                    location.getWorld().spawnParticle(Particle.ENCHANT, particleLocation, 2);
                    break;
            }
        }
    }
    
    /**
     * Create testament completion effect
     */
    public void createTestamentCompletionEffect(Player player, GodType god) {
        if (!areEffectsEnabled() || !checkGlobalCooldown("testament_completion", 5000)) {
            return;
        }
        
        Location location = player.getLocation();
        
        // Create epic completion effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }
                
                // Create spiral effect
                double radius = 3.0;
                double height = 0.1 * ticks;
                double angle = ticks * 0.3;
                
                double x = location.getX() + radius * Math.cos(angle);
                double y = location.getY() + height;
                double z = location.getZ() + radius * Math.sin(angle);
                
                Location spiralLocation = new Location(location.getWorld(), x, y, z);
                
                // God-specific particles in spiral
                createGodParticleSpiral(spiralLocation, god);
                
                // Central explosion every 20 ticks
                if (ticks % 20 == 0) {
                    location.getWorld().spawnParticle(Particle.EXPLOSION, 
                        location.clone().add(0, 2, 0), 1);
                    location.getWorld().spawnParticle(Particle.FIREWORK, 
                        location.clone().add(0, 2, 0), 20, 1, 1, 1, 0.3);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Play epic sound sequence
        player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.5f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        }, 20L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        }, 40L);
    }
    
    /**
     * Create god-specific particle spiral
     */
    private void createGodParticleSpiral(Location location, GodType god) {
        switch (god) {
            case FALLEN:
                location.getWorld().spawnParticle(Particle.SOUL, location, 3, 0.1, 0.1, 0.1, 0.05);
                break;
            case BANISHMENT:
                location.getWorld().spawnParticle(Particle.FLAME, location, 3, 0.1, 0.1, 0.1, 0.05);
                break;
            case ABYSSAL:
                location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location, 3, 0.1, 0.1, 0.1, 0.1);
                break;
            case SYLVAN:
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 3, 0.1, 0.1, 0.1, 0.1);
                break;
            case TEMPEST:
                location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 3, 0.1, 0.1, 0.1, 0.05);
                break;
            case VEIL:
                location.getWorld().spawnParticle(Particle.PORTAL, location, 5, 0.2, 0.2, 0.2, 0.1);
                break;
            default:
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 3, 0.1, 0.1, 0.1, 0.1);
                break;
        }
    }
    
    /**
     * Create convergence effect
     */
    public void createConvergenceEffect(Player player) {
        if (!areEffectsEnabled() || !checkGlobalCooldown("convergence", 10000)) {
            return;
        }
        
        Location location = player.getLocation();
        
        // Create ultimate convergence effect
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 200) { // 10 seconds
                    cancel();
                    return;
                }
                
                // Create multiple spirals for all gods
                for (int spiral = 0; spiral < 3; spiral++) {
                    double radius = 2.0 + spiral;
                    double height = 0.05 * ticks + spiral * 2;
                    double angle = ticks * 0.2 + spiral * 120;
                    
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location spiralLocation = new Location(location.getWorld(), x, y, z);
                    
                    // Different particles for each spiral
                    switch (spiral) {
                        case 0:
                            location.getWorld().spawnParticle(Particle.END_ROD, spiralLocation, 3, 0.1, 0.1, 0.1, 0.05);
                            break;
                        case 1:
                            location.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, spiralLocation, 5, 0.2, 0.2, 0.2, 0.1);
                            break;
                        case 2:
                            location.getWorld().spawnParticle(Particle.ENCHANT, spiralLocation, 8, 0.3, 0.3, 0.3, 0.2);
                            break;
                    }
                }
                
                // Central explosion every 40 ticks
                if (ticks % 40 == 0) {
                    location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location.clone().add(0, 2, 0), 3);
                    location.getWorld().spawnParticle(Particle.FIREWORK, location.clone().add(0, 2, 0), 50, 2, 2, 2, 0.5);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create subtle ambient effect for converged players
     */
    public void createConvergenceAmbientEffect(Player player) {
        if (!areEffectsEnabled() || !canPlayerReceiveEffect(player, 5000)) { // 5 second cooldown
            return;
        }
        
        Location location = player.getLocation();
        
        // Create subtle particle aura
        for (int i = 0; i < 5; i++) {
            double angle = Math.random() * 360;
            double radius = 1.5 + Math.random() * 0.5;
            double height = Math.random() * 2;
            
            double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
            double y = location.getY() + height;
            double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
            
            Location particleLocation = new Location(location.getWorld(), x, y, z);
            location.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Clear all tracked effects (for shutdown)
     */
    public void clearAllEffects() {
        lastEffectTime.clear();
        globalEffectCooldowns.clear();
    }
}