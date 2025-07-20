package com.example.minecraftplugin.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.example.minecraftplugin.MinecraftPlugin;

/**
 * Manages performance optimization for the Testament System
 */
public class PerformanceManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Cached configuration values
    private double cachedChestSpawnChance;
    private double cachedMobDropChance;
    private int cachedChestCooldownHours;
    private int cachedMobCooldownHours;
    private int cachedDeathThreshold;
    private boolean cachedEffectsEnabled;
    private boolean cachedConflictsEnabled;
    
    // Performance tracking
    private final Map<String, Long> operationTimes;
    private final Map<String, Integer> operationCounts;
    
    // Async task management
    private final Set<CompletableFuture<Void>> pendingAsyncTasks;
    
    // Player tracking for optimized updates
    private final Set<UUID> playersNeedingEffectUpdates;
    private final Set<UUID> playersWithDivineItems;
    
    public PerformanceManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.operationTimes = new ConcurrentHashMap<>();
        this.operationCounts = new ConcurrentHashMap<>();
        this.pendingAsyncTasks = ConcurrentHashMap.newKeySet();
        this.playersNeedingEffectUpdates = ConcurrentHashMap.newKeySet();
        this.playersWithDivineItems = ConcurrentHashMap.newKeySet();
        
        // Cache configuration values
        cacheConfigurationValues();
        
        // Start performance monitoring
        startPerformanceMonitoring();
        
        logger.info("Performance Manager initialized with cached configuration");
    }
    
    /**
     * Cache frequently accessed configuration values
     */
    public void cacheConfigurationValues() {
        // Use ConfigManager if available, otherwise fall back to main config
        if (plugin.getConfigManager() != null) {
            cachedChestSpawnChance = plugin.getConfigManager().getConfigValue("config", "testament.fragments.chest_spawn_chance", 0.02, Double.class);
            cachedMobDropChance = plugin.getConfigManager().getConfigValue("config", "testament.fragments.mob_drop_chance", 0.001, Double.class);
            cachedChestCooldownHours = plugin.getConfigManager().getConfigValue("config", "testament.fragments.chest_cooldown_hours", 2, Integer.class);
            cachedMobCooldownHours = plugin.getConfigManager().getConfigValue("config", "testament.fragments.mob_cooldown_hours", 1, Integer.class);
            cachedDeathThreshold = plugin.getConfigManager().getConfigValue("config", "testament.lives_system.death_threshold", 3, Integer.class);
            cachedEffectsEnabled = plugin.getConfigManager().getConfigValue("config", "testament.ascension.effects_enabled", true, Boolean.class);
            cachedConflictsEnabled = plugin.getConfigManager().getConfigValue("config", "testament.conflicts.enabled", true, Boolean.class);
        } else {
            // Fallback to main config
            cachedChestSpawnChance = plugin.getConfig().getDouble("testament.fragments.chest_spawn_chance", 0.02);
            cachedMobDropChance = plugin.getConfig().getDouble("testament.fragments.mob_drop_chance", 0.001);
            cachedChestCooldownHours = plugin.getConfig().getInt("testament.fragments.chest_cooldown_hours", 2);
            cachedMobCooldownHours = plugin.getConfig().getInt("testament.fragments.mob_cooldown_hours", 1);
            cachedDeathThreshold = plugin.getConfig().getInt("testament.lives_system.death_threshold", 3);
            cachedEffectsEnabled = plugin.getConfig().getBoolean("testament.ascension.effects_enabled", true);
            cachedConflictsEnabled = plugin.getConfig().getBoolean("testament.conflicts.enabled", true);
        }
        
        logger.info("Configuration values cached for performance optimization");
    }
    
    /**
     * Get cached chest spawn chance
     */
    public double getCachedChestSpawnChance() {
        return cachedChestSpawnChance;
    }
    
    /**
     * Get cached mob drop chance
     */
    public double getCachedMobDropChance() {
        return cachedMobDropChance;
    }
    
    /**
     * Get cached chest cooldown hours
     */
    public int getCachedChestCooldownHours() {
        return cachedChestCooldownHours;
    }
    
    /**
     * Get cached mob cooldown hours
     */
    public int getCachedMobCooldownHours() {
        return cachedMobCooldownHours;
    }
    
    /**
     * Get cached death threshold
     */
    public int getCachedDeathThreshold() {
        return cachedDeathThreshold;
    }
    
    /**
     * Check if effects are enabled (cached)
     */
    public boolean areEffectsEnabled() {
        return cachedEffectsEnabled;
    }
    
    /**
     * Check if conflicts are enabled (cached)
     */
    public boolean areConflictsEnabled() {
        return cachedConflictsEnabled;
    }
    
    /**
     * Execute an operation asynchronously
     */
    public CompletableFuture<Void> executeAsync(Runnable operation, String operationName) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long startTime = System.nanoTime();
            try {
                operation.run();
            } catch (Exception e) {
                logger.severe("Error in async operation " + operationName + ": " + e.getMessage());
            } finally {
                long endTime = System.nanoTime();
                recordOperation(operationName, endTime - startTime);
            }
        });
        future.whenComplete((result, throwable) -> {
            pendingAsyncTasks.remove(future);
            if (throwable != null) {
                logger.severe("Async operation " + operationName + " failed: " + throwable.getMessage());
            }
        });
        pendingAsyncTasks.add(future);
        return future;
    }
    
    /**
     * Record operation timing for performance monitoring
     */
    public void recordOperation(String operationName, long nanoTime) {
        operationTimes.put(operationName, nanoTime);
        operationCounts.merge(operationName, 1, Integer::sum);
    }
    
    /**
     * Start a timed operation
     */
    public long startOperation(String operationName) {
        return System.nanoTime();
    }
    
    /**
     * End a timed operation
     */
    public void endOperation(String operationName, long startTime) {
        long endTime = System.nanoTime();
        recordOperation(operationName, endTime - startTime);
    }
    
    /**
     * Add player to effect update tracking
     */
    public void addPlayerForEffectUpdates(UUID playerId) {
        playersNeedingEffectUpdates.add(playerId);
    }
    
    /**
     * Remove player from effect update tracking
     */
    public void removePlayerFromEffectUpdates(UUID playerId) {
        playersNeedingEffectUpdates.remove(playerId);
    }
    
    /**
     * Get players needing effect updates
     */
    public Set<UUID> getPlayersNeedingEffectUpdates() {
        return new HashSet<>(playersNeedingEffectUpdates);
    }
    
    /**
     * Add player to divine item tracking
     */
    public void addPlayerWithDivineItems(UUID playerId) {
        playersWithDivineItems.add(playerId);
    }
    
    /**
     * Remove player from divine item tracking
     */
    public void removePlayerWithDivineItems(UUID playerId) {
        playersWithDivineItems.remove(playerId);
    }
    
    /**
     * Get players with divine items
     */
    public Set<UUID> getPlayersWithDivineItems() {
        return new HashSet<>(playersWithDivineItems);
    }
    
    /**
     * Batch save player data asynchronously
     */
    public void batchSavePlayerData(PlayerDataManager playerDataManager, Set<UUID> playerIds) {
        if (playerIds.isEmpty()) {
            return;
        }
        
// [PATCHED] Ensure variables used in lambda are effectively final
        executeAsync(() -> {
            for (UUID playerId : playerIds) {
                playerDataManager.savePlayerData(playerId);
            }
        }, "batch_save_player_data");
    }
    
    /**
     * Clean up disconnected players from tracking
     */
    public void cleanupDisconnectedPlayers() {
        Set<UUID> onlinePlayerIds = new HashSet<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            onlinePlayerIds.add(player.getUniqueId());
        }
        
        // Remove offline players from tracking
        playersNeedingEffectUpdates.retainAll(onlinePlayerIds);
        playersWithDivineItems.retainAll(onlinePlayerIds);
    }
    
    /**
     * Start performance monitoring task
     */
    private void startPerformanceMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Clean up disconnected players
                cleanupDisconnectedPlayers();
                
                // Log performance statistics every 5 minutes
                if (plugin.getConfig().getBoolean("performance.logging.enabled", false)) {
                    logPerformanceStatistics();
                }
                
                // Clear old operation data to prevent memory leaks
                if (operationTimes.size() > 1000) {
                    operationTimes.clear();
                    operationCounts.clear();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L); // Every 5 minutes
    }
    
    /**
     * Log performance statistics
     */
    private void logPerformanceStatistics() {
        if (operationTimes.isEmpty()) {
            return;
        }
        
        logger.info("=== TESTAMENT SYSTEM PERFORMANCE STATISTICS ===");
        logger.info("Tracked Players - Effects: " + playersNeedingEffectUpdates.size() + 
                   ", Divine Items: " + playersWithDivineItems.size());
        logger.info("Pending Async Tasks: " + pendingAsyncTasks.size());
        
        // Show top 5 slowest operations
        operationTimes.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
// [PATCHED] Ensure variables used in lambda are effectively final
            .forEach(entry -> {
                String operationName = entry.getKey();
                long nanoTime = entry.getValue();
                int count = operationCounts.getOrDefault(operationName, 1);
                double avgMs = (nanoTime / 1_000_000.0) / count;
                
                logger.info("Operation: " + operationName + 
                           " - Avg: " + String.format("%.2f", avgMs) + "ms" +
                           " - Count: " + count);
            });
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("players_needing_effects", playersNeedingEffectUpdates.size());
        stats.put("players_with_divine_items", playersWithDivineItems.size());
        stats.put("pending_async_tasks", pendingAsyncTasks.size());
        stats.put("tracked_operations", operationTimes.size());
        
        // Calculate average operation times
        Map<String, Double> avgTimes = new HashMap<>();
        for (Map.Entry<String, Long> entry : operationTimes.entrySet()) {
            String operationName = entry.getKey();
            long nanoTime = entry.getValue();
            int count = operationCounts.getOrDefault(operationName, 1);
            double avgMs = (nanoTime / 1_000_000.0) / count;
            avgTimes.put(operationName, avgMs);
        }
        stats.put("average_operation_times_ms", avgTimes);
        
        return stats;
    }
    
    /**
     * Wait for all pending async tasks to complete (for shutdown)
     */
    public void waitForAsyncTasks() {
        if (pendingAsyncTasks.isEmpty()) {
            return;
        }
        
        logger.info("Waiting for " + pendingAsyncTasks.size() + " async tasks to complete...");
        
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
            pendingAsyncTasks.toArray(new CompletableFuture[0])
        );
        
        try {
            allTasks.get(java.util.concurrent.TimeUnit.SECONDS.toMillis(10), 
                        java.util.concurrent.TimeUnit.MILLISECONDS);
            logger.info("All async tasks completed successfully");
        } catch (Exception e) {
            logger.warning("Some async tasks did not complete within timeout: " + e.getMessage());
        }
    }
    
    /**
     * Shutdown the performance manager
     */
    public void shutdown() {
        logger.info("Performance Manager shutting down...");
        
        // Wait for async tasks to complete
        waitForAsyncTasks();
        
        // Log final statistics
        if (plugin.getConfig().getBoolean("performance.logging.enabled", false)) {
            logPerformanceStatistics();
        }
        
        // Clear all tracking data
        operationTimes.clear();
        operationCounts.clear();
        playersNeedingEffectUpdates.clear();
        playersWithDivineItems.clear();
        pendingAsyncTasks.clear();
        
        logger.info("Performance Manager shutdown complete");
    }
}