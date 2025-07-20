package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.ConvergenceNexus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages the Divine Convergence system for players who master all 12 gods
 */
public class ConvergenceManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final GodManager godManager;
    private final PlayerDataManager playerDataManager;
    
    // Track players who have achieved convergence
    private final Set<UUID> convergedPlayers;
    
    public ConvergenceManager(MinecraftPlugin plugin, GodManager godManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.godManager = godManager;
        this.playerDataManager = playerDataManager;
        this.convergedPlayers = new HashSet<>();
        
        // Start convergence effects task
        startConvergenceEffectsTask();
        
        logger.info("Convergence Manager initialized");
    }
    
    /**
     * Check if a player has achieved Divine Convergence
     */
    public boolean hasAchievedConvergence(Player player) {
        return hasAchievedConvergence(player.getUniqueId());
    }
    
    public boolean hasAchievedConvergence(UUID playerId) {
        return convergedPlayers.contains(playerId) || getTestamentCountByUUID(playerId) >= 12;
    }
    
    /**
     * Get testament count by UUID
     */
    private int getTestamentCountByUUID(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            return godManager.getTestamentCount(player);
        }
        return 0;
    }
    
    /**
     * Check if a player should achieve convergence and trigger it
     */
    public void checkForConvergence(Player player) {
        if (hasAchievedConvergence(player)) {
            return; // Already achieved
        }
        
        // Check if player has completed all 12 testaments
        Set<GodType> completedTestaments = godManager.getCompletedTestaments(player);
        if (completedTestaments.size() >= 12) {
            triggerDivineConvergence(player);
        }
    }
    
    /**
     * Trigger Divine Convergence for a player
     */
    public void triggerDivineConvergence(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (convergedPlayers.contains(playerId)) {
            return; // Already converged
        }
        
        // Mark player as converged
        convergedPlayers.add(playerId);
        
        // Create the Convergence Nexus
        ConvergenceNexus nexus = new ConvergenceNexus();
        ItemStack nexusItem = nexus.createItemStack();
        
        // Give the nexus to the player
        player.getInventory().addItem(nexusItem);
        nexus.onObtained(player, nexusItem);
        
        // Create dramatic convergence effects
        createConvergenceEffects(player);
        
        // Create enhanced visual effects
        plugin.getVisualEffectsManager().createConvergenceEffect(player);
        
        // Send convergence messages
        sendConvergenceMessages(player);
        
        // Server-wide announcement
        announceConvergence(player);
        
        // Log the achievement
        logger.info(player.getName() + " has achieved Divine Convergence!");
    }
    
    /**
     * Create dramatic visual and audio effects for convergence
     */
    private void createConvergenceEffects(Player player) {
        Location location = player.getLocation();
        
        // Play epic sounds
        player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.3f);
        player.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.5f);
        player.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
        player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.6f);
        
        // Create convergence particle effects
        createConvergenceParticles(player, location);
        
        // Create lightning strikes
        createConvergenceLightning(player, location);
        
        // Create beacon beam effect
        createConvergenceBeacon(player, location);
    }
    
    /**
     * Create convergence particle effects
     */
    private void createConvergenceParticles(Player player, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 200) { // Run for 10 seconds
                    cancel();
                    return;
                }
                
                // Create multiple particle spirals
                for (int spiral = 0; spiral < 3; spiral++) {
                    double radius = 2.0 + spiral;
                    double height = 0.05 * ticks + spiral * 2;
                    double angle = ticks * 0.2 + spiral * 120;
                    
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location particleLocation = new Location(location.getWorld(), x, y, z);
                    
                    // Different particles for each spiral
                    switch (spiral) {
                        case 0:
                            location.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 3, 0.1, 0.1, 0.1, 0.05);
                            break;
                        case 1:
                            location.getWorld().spawnParticle(Particle.HEART, particleLocation, 5, 0.2, 0.2, 0.2, 0.1);
                            break;
                        case 2:
                            location.getWorld().spawnParticle(Particle.ENCHANT, particleLocation, 8, 0.3, 0.3, 0.3, 0.2);
                            break;
                    }
                }
                
                // Central explosion every 40 ticks
                if (ticks % 40 == 0) {
                    location.getWorld().spawnParticle(Particle.EXPLOSION, location.clone().add(0, 2, 0), 3);
                    location.getWorld().spawnParticle(Particle.FIREWORK, location.clone().add(0, 2, 0), 50, 2, 2, 2, 0.5);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create convergence lightning effects
     */
    private void createConvergenceLightning(Player player, Location location) {
        new BukkitRunnable() {
            int strikes = 0;
            
            @Override
            public void run() {
                if (strikes >= 12) { // One strike for each god
                    cancel();
                    return;
                }
                
                // Strike lightning in a circle around the player
                double angle = strikes * 30; // 30 degrees apart
                double radius = 5.0;
                
                double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                
                Location strikeLocation = new Location(location.getWorld(), x, location.getY(), z);
                strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
                
                strikes++;
            }
        }.runTaskTimer(plugin, 20L, 10L); // Start after 1 second, then every 0.5 seconds
    }
    
    /**
     * Create convergence beacon effect
     */
    private void createConvergenceBeacon(Player player, Location location) {
        new BukkitRunnable() {
            int duration = 0;
            
            @Override
            public void run() {
                if (duration >= 300) { // Run for 15 seconds
                    cancel();
                    return;
                }
                
                // Create upward particle beam
                for (int i = 1; i <= 50; i++) {
                    Location beamLocation = location.clone().add(0, i, 0);
                    if (duration % 5 == 0) {
                        beamLocation.getWorld().spawnParticle(Particle.END_ROD, beamLocation, 5, 0.2, 0.2, 0.2, 0);
                        beamLocation.getWorld().spawnParticle(Particle.HEART, beamLocation, 3, 0.1, 0.1, 0.1, 0);
                    }
                }
                
                duration++;
            }
        }.runTaskTimer(plugin, 60L, 1L); // Start after 3 seconds
    }
    
    /**
     * Send convergence messages to the player
     */
    private void sendConvergenceMessages(Player player) {
        player.sendMessage("");
        player.sendMessage("§5§l★═══════════════════════════════════════════════════════════════★");
        player.sendMessage("§5§l                    DIVINE CONVERGENCE ACHIEVED!");
        player.sendMessage("§5§l★═══════════════════════════════════════════════════════════════★");
        player.sendMessage("");
        player.sendMessage("§7You have mastered all twelve gods and transcended mortality!");
        player.sendMessage("§7The power of every divine realm flows through your being.");
        player.sendMessage("§7You are now the §5§lMaster of All Divinity§r§7!");
        player.sendMessage("");
        player.sendMessage("§e§lConvergence Rewards:");
        player.sendMessage("§7• §5Convergence Nexus §7- Ultimate divine item");
        player.sendMessage("§7• §530 Hearts §7maximum health");
        player.sendMessage("§7• §5All divine powers §7combined");
        player.sendMessage("§7• §5Reality manipulation §7abilities");
        player.sendMessage("§7• §5Transcendent status §7among players");
        player.sendMessage("");
        player.sendMessage("§d§l\"I am become divine, transcendent of all limitations.\"");
        player.sendMessage("§5§l★═══════════════════════════════════════════════════════════════★");
        player.sendMessage("");
        
        // Show title
        player.sendTitle("§5§l★ DIVINE CONVERGENCE ★", "§7Master of All Divinity", 20, 100, 20);
    }
    
    /**
     * Announce convergence to the entire server
     */
    private void announceConvergence(Player player) {
        // Use broadcast manager for convergence announcement
        plugin.getBroadcastManager().broadcastConvergence(player);
    }
    
    /**
     * Start a task that applies special effects to converged players
     */
    private void startConvergenceEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerId : convergedPlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        applyConvergenceAura(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Run every 5 seconds
    }
    
    /**
     * Apply special aura effects to converged players
     */
    private void applyConvergenceAura(Player player) {
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
     * Get all converged players
     */
    public Set<UUID> getConvergedPlayers() {
        return new HashSet<>(convergedPlayers);
    }
    
    /**
     * Remove a player from convergence (admin command)
     */
    public void removeConvergence(Player player) {
        removeConvergence(player.getUniqueId());
    }
    
    public void removeConvergence(UUID playerId) {
        convergedPlayers.remove(playerId);
        logger.info("Removed convergence status from " + playerId);
    }
    
    /**
     * Get convergence statistics
     */
    public ConvergenceStats getConvergenceStats() {
        int totalConverged = convergedPlayers.size();
        int onlineConverged = 0;
        
        for (UUID playerId : convergedPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                onlineConverged++;
                
                // Create ambient convergence effects
                plugin.getVisualEffectsManager().createConvergenceAmbientEffect(player);
            }
        }
        
        return new ConvergenceStats(totalConverged, onlineConverged);
    }
    
    /**
     * Inner class for convergence statistics
     */
    public static class ConvergenceStats {
        private final int totalConverged;
        private final int onlineConverged;
        
        public ConvergenceStats(int totalConverged, int onlineConverged) {
            this.totalConverged = totalConverged;
            this.onlineConverged = onlineConverged;
        }
        
        public int getTotalConverged() {
            return totalConverged;
        }
        
        public int getOnlineConverged() {
            return onlineConverged;
        }
    }
}