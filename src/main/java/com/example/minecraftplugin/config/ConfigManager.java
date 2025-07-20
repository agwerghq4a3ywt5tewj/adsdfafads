package com.example.minecraftplugin.config;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Advanced configuration management system with hot reloading and validation
 */
public class ConfigManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Configuration files
    private final Map<String, FileConfiguration> configFiles;
    private final Map<String, File> configFileObjects;
    
    // Configuration validation
    private final ConfigValidator validator;
    
    // Hot reload tracking
    private final Map<String, Long> lastModified;
    
    public ConfigManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configFiles = new HashMap<>();
        this.configFileObjects = new HashMap<>();
        this.lastModified = new HashMap<>();
        this.validator = new ConfigValidator(plugin);
        
        initializeConfigurations();
        startHotReloadTask();
        
        logger.info("Advanced Configuration Manager initialized");
    }
    
    /**
     * Initialize all configuration files
     */
    private void initializeConfigurations() {
        // Main config
        loadConfigFile("config", "config.yml");
        
        // God-specific configurations
        loadConfigFile("gods", "gods.yml");
        
        // Altar configurations
        loadConfigFile("altars", "altars.yml");
        
        // Effects configurations
        loadConfigFile("effects", "effects.yml");
        
        // Performance configurations
        loadConfigFile("performance", "performance.yml");
        
        // Validate all configurations
        validateAllConfigurations();
    }
    
    /**
     * Load a specific configuration file
     */
    private void loadConfigFile(String configName, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        // Create default file if it doesn't exist
        if (!configFile.exists()) {
            createDefaultConfig(configName, configFile);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configFiles.put(configName, config);
        configFileObjects.put(configName, configFile);
        lastModified.put(configName, configFile.lastModified());
        
        logger.info("Loaded configuration: " + fileName);
    }
    
    /**
     * Create default configuration files
     */
    private void createDefaultConfig(String configName, File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            
            switch (configName) {
                case "gods":
                    createDefaultGodsConfig(config);
                    break;
                case "altars":
                    createDefaultAltarsConfig(config);
                    break;
                case "effects":
                    createDefaultEffectsConfig(config);
                    break;
                case "performance":
                    createDefaultPerformanceConfig(config);
                    break;
            }
            
            config.save(configFile);
            logger.info("Created default configuration: " + configFile.getName());
            
        } catch (IOException e) {
            logger.severe("Could not create default config " + configFile.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Create default gods configuration
     */
    private void createDefaultGodsConfig(FileConfiguration config) {
        config.set("gods.fallen.enabled", true);
        config.set("gods.fallen.heart_return_seconds", 45);
        config.set("gods.fallen.max_hearts", 25);
        config.set("gods.fallen.regeneration_threshold", 0.5);
        
        config.set("gods.banishment.enabled", true);
        config.set("gods.banishment.banish_radius", 8);
        config.set("gods.banishment.fire_duration", 100);
        config.set("gods.banishment.cooldown_seconds", 45);
        
        config.set("gods.abyssal.enabled", true);
        config.set("gods.abyssal.aura_radius", 10);
        config.set("gods.abyssal.aura_duration", 1200);
        config.set("gods.abyssal.cooldown_seconds", 60);
        
        config.set("gods.sylvan.enabled", true);
        config.set("gods.sylvan.growth_radius", 7);
        config.set("gods.sylvan.cooldown_seconds", 30);
        
        config.set("gods.tempest.enabled", true);
        config.set("gods.tempest.launch_power", 2.0);
        config.set("gods.tempest.cooldown_seconds", 20);
        
        config.set("gods.veil.enabled", true);
        config.set("gods.veil.teleport_range", 32);
        config.set("gods.veil.cooldown_seconds", 15);
        
        // Add expansion gods
        config.set("gods.forge.enabled", true);
        config.set("gods.forge.repair_amount", 500);
        config.set("gods.forge.cooldown_seconds", 30);
        
        config.set("gods.void.enabled", true);
        config.set("gods.void.teleport_distance", 10);
        config.set("gods.void.cooldown_seconds", 5);
        
        config.set("gods.time.enabled", true);
        config.set("gods.time.dilation_radius", 12);
        config.set("gods.time.effect_duration", 400);
        config.set("gods.time.cooldown_seconds", 25);
        
        config.set("gods.blood.enabled", true);
        config.set("gods.blood.rage_duration", 200);
        config.set("gods.blood.cooldown_seconds", 45);
        
        config.set("gods.crystal.enabled", true);
        config.set("gods.crystal.sonic_radius", 8);
        config.set("gods.crystal.cooldown_seconds", 20);
        
        config.set("gods.shadow.enabled", true);
        config.set("gods.shadow.shadow_form_duration", 200);
        config.set("gods.shadow.cooldown_seconds", 30);
    }
    
    /**
     * Create default altars configuration
     */
    private void createDefaultAltarsConfig(FileConfiguration config) {
        config.set("altars.natural_generation.enabled", true);
        config.set("altars.natural_generation.base_spawn_chance", 0.001);
        config.set("altars.natural_generation.min_distance", 500);
        config.set("altars.natural_generation.max_per_chunk", 1);
        
        config.set("altars.effects.enhanced_completion", true);
        config.set("altars.effects.permanent_marking", true);
        config.set("altars.effects.god_specific_sounds", true);
        config.set("altars.effects.lightning_strikes", true);
        config.set("altars.effects.beacon_beams", true);
        
        config.set("altars.validation.strict_structure", true);
        config.set("altars.validation.allow_variations", false);
        config.set("altars.validation.show_requirements", true);
    }
    
    /**
     * Create default effects configuration
     */
    private void createDefaultEffectsConfig(FileConfiguration config) {
        config.set("effects.enabled", true);
        config.set("effects.particle_density", 1.0);
        config.set("effects.sound_effects", true);
        config.set("effects.god_auras", true);
        config.set("effects.ability_effects", true);
        config.set("effects.testament_completion_effects", true);
        config.set("effects.convergence_effects", true);
        
        config.set("effects.altar_effects.enhanced_particles", true);
        config.set("effects.altar_effects.god_specific_themes", true);
        config.set("effects.altar_effects.lightning_sequences", true);
        config.set("effects.altar_effects.beacon_beams", true);
        config.set("effects.altar_effects.permanent_marking", true);
        config.set("effects.altar_effects.sound_sequences", true);
        
        config.set("effects.performance.max_particles_per_effect", 100);
        config.set("effects.performance.effect_cooldown_ms", 1000);
        config.set("effects.performance.global_cooldown_ms", 5000);
    }
    
    /**
     * Create default performance configuration
     */
    private void createDefaultPerformanceConfig(FileConfiguration config) {
        config.set("performance.caching.enabled", true);
        config.set("performance.caching.config_cache_duration", 300000); // 5 minutes
        config.set("performance.caching.player_data_cache_size", 1000);
        
        config.set("performance.async_operations.enabled", true);
        config.set("performance.async_operations.max_concurrent_tasks", 10);
        config.set("performance.async_operations.task_timeout_seconds", 30);
        
        config.set("performance.optimization.batch_save_interval", 300); // 5 minutes
        config.set("performance.optimization.cleanup_interval", 600); // 10 minutes
        config.set("performance.optimization.max_tracked_operations", 1000);
        
        config.set("performance.monitoring.enabled", false);
        config.set("performance.monitoring.log_interval", 300); // 5 minutes
        config.set("performance.monitoring.track_slow_operations", true);
        config.set("performance.monitoring.slow_operation_threshold_ms", 100);
    }
    
    /**
     * Get configuration value with type safety
     */
    public <T> T getConfigValue(String configName, String path, T defaultValue, Class<T> type) {
        FileConfiguration config = configFiles.get(configName);
        if (config == null) {
            logger.warning("Configuration not found: " + configName);
            return defaultValue;
        }
        
        if (!config.contains(path)) {
            logger.warning("Configuration path not found: " + configName + "." + path);
            return defaultValue;
        }
        
        Object value = config.get(path);
        
        try {
            if (type.isInstance(value)) {
                return type.cast(value);
            } else {
                logger.warning("Configuration value type mismatch for " + configName + "." + path + 
                              ". Expected: " + type.getSimpleName() + ", Got: " + value.getClass().getSimpleName());
                return defaultValue;
            }
        } catch (ClassCastException e) {
            logger.warning("Could not cast configuration value for " + configName + "." + path + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Set configuration value
     */
    public void setConfigValue(String configName, String path, Object value) {
        FileConfiguration config = configFiles.get(configName);
        if (config == null) {
            logger.warning("Configuration not found: " + configName);
            return;
        }
        
        config.set(path, value);
        saveConfig(configName);
    }
    
    /**
     * Save a specific configuration file
     */
    public void saveConfig(String configName) {
        FileConfiguration config = configFiles.get(configName);
        File configFile = configFileObjects.get(configName);
        
        if (config == null || configFile == null) {
            logger.warning("Cannot save configuration: " + configName);
            return;
        }
        
        try {
            config.save(configFile);
            lastModified.put(configName, configFile.lastModified());
            logger.info("Saved configuration: " + configName);
        } catch (IOException e) {
            logger.severe("Could not save configuration " + configName + ": " + e.getMessage());
        }
    }
    
    /**
     * Reload a specific configuration file
     */
    public void reloadConfig(String configName) {
        File configFile = configFileObjects.get(configName);
        if (configFile == null) {
            logger.warning("Configuration file not found: " + configName);
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configFiles.put(configName, config);
        lastModified.put(configName, configFile.lastModified());
        
        // Validate the reloaded configuration
        validator.validateConfig(configName, config);
        
        logger.info("Reloaded configuration: " + configName);
    }
    
    /**
     * Reload all configuration files
     */
    public void reloadAllConfigs() {
        for (String configName : configFiles.keySet()) {
            reloadConfig(configName);
        }
        
        // Update cached values in PerformanceManager
        plugin.getPerformanceManager().cacheConfigurationValues();
        
        logger.info("Reloaded all configurations");
    }
    
    /**
     * Start hot reload monitoring task
     */
    private void startHotReloadTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            checkForConfigChanges();
        }, 100L, 100L); // Check every 5 seconds
    }
    
    /**
     * Check for configuration file changes and hot reload
     */
    private void checkForConfigChanges() {
        for (Map.Entry<String, File> entry : configFileObjects.entrySet()) {
            String configName = entry.getKey();
            File configFile = entry.getValue();
            
            if (!configFile.exists()) {
                continue;
            }
            
            long currentModified = configFile.lastModified();
            long lastKnownModified = lastModified.getOrDefault(configName, 0L);
            
            if (currentModified > lastKnownModified) {
                // File has been modified, reload it
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    logger.info("Detected changes in " + configName + ".yml, hot reloading...");
                    reloadConfig(configName);
                    
                    // Notify online admins
                    notifyAdminsOfReload(configName);
                });
            }
        }
    }
    
    /**
     * Notify online administrators of configuration reload
     */
    private void notifyAdminsOfReload(String configName) {
        String message = "§a§lConfiguration Reloaded: §r§a" + configName + ".yml has been automatically reloaded.";
        
        plugin.getServer().getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("minecraftplugin.admin"))
            .forEach(admin -> admin.sendMessage(message));
    }
    
    /**
     * Validate all configurations
     */
    private void validateAllConfigurations() {
        for (Map.Entry<String, FileConfiguration> entry : configFiles.entrySet()) {
            validator.validateConfig(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Get configuration template for a specific server type
     */
    public void applyTemplate(String templateName) {
        switch (templateName.toLowerCase()) {
            case "survival":
                applySurvivalTemplate();
                break;
            case "creative":
                applyCreativeTemplate();
                break;
            case "hardcore":
                applyHardcoreTemplate();
                break;
            case "pvp":
                applyPvPTemplate();
                break;
            default:
                logger.warning("Unknown template: " + templateName);
                break;
        }
    }
    
    /**
     * Apply survival server template
     */
    private void applySurvivalTemplate() {
        // Balanced settings for survival gameplay
        setConfigValue("config", "testament.fragments.chest_spawn_chance", 0.02);
        setConfigValue("config", "testament.fragments.mob_drop_chance", 0.001);
        setConfigValue("config", "testament.lives_system.death_threshold", 3);
        setConfigValue("effects", "effects.particle_density", 1.0);
        setConfigValue("performance", "performance.caching.enabled", true);
        
        logger.info("Applied survival server template");
    }
    
    /**
     * Apply creative server template
     */
    private void applyCreativeTemplate() {
        // More forgiving settings for creative/testing
        setConfigValue("config", "testament.fragments.chest_spawn_chance", 0.1);
        setConfigValue("config", "testament.fragments.mob_drop_chance", 0.05);
        setConfigValue("config", "testament.lives_system.death_threshold", 10);
        setConfigValue("effects", "effects.particle_density", 1.5);
        
        logger.info("Applied creative server template");
    }
    
    /**
     * Apply hardcore server template
     */
    private void applyHardcoreTemplate() {
        // Challenging settings for hardcore gameplay
        setConfigValue("config", "testament.fragments.chest_spawn_chance", 0.005);
        setConfigValue("config", "testament.fragments.mob_drop_chance", 0.0005);
        setConfigValue("config", "testament.lives_system.death_threshold", 1);
        setConfigValue("effects", "effects.particle_density", 0.8);
        
        logger.info("Applied hardcore server template");
    }
    
    /**
     * Apply PvP server template
     */
    private void applyPvPTemplate() {
        // Competitive settings for PvP gameplay
        setConfigValue("config", "testament.fragments.chest_spawn_chance", 0.03);
        setConfigValue("config", "testament.fragments.mob_drop_chance", 0.002);
        setConfigValue("config", "testament.lives_system.death_threshold", 2);
        setConfigValue("config", "testament.conflicts.enabled", true);
        setConfigValue("effects", "effects.particle_density", 1.2);
        
        logger.info("Applied PvP server template");
    }
    
    /**
     * Export current configuration to a backup file
     */
    public void exportConfiguration(String backupName) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        backupDir.mkdirs();
        
        for (Map.Entry<String, File> entry : configFileObjects.entrySet()) {
            String configName = entry.getKey();
            File sourceFile = entry.getValue();
            File backupFile = new File(backupDir, backupName + "_" + configName + ".yml");
            
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(sourceFile);
                config.save(backupFile);
                logger.info("Exported " + configName + " to backup: " + backupFile.getName());
            } catch (IOException e) {
                logger.severe("Could not export " + configName + " configuration: " + e.getMessage());
            }
        }
    }
    
    /**
     * Import configuration from a backup file
     */
    public void importConfiguration(String backupName) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        
        for (String configName : configFiles.keySet()) {
            File backupFile = new File(backupDir, backupName + "_" + configName + ".yml");
            
            if (backupFile.exists()) {
                try {
                    FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);
                    File targetFile = configFileObjects.get(configName);
                    backupConfig.save(targetFile);
                    
                    reloadConfig(configName);
                    logger.info("Imported " + configName + " from backup: " + backupFile.getName());
                } catch (IOException e) {
                    logger.severe("Could not import " + configName + " configuration: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_configs", configFiles.size());
        stats.put("hot_reload_enabled", true);
        
        Map<String, Long> fileSizes = new HashMap<>();
        for (Map.Entry<String, File> entry : configFileObjects.entrySet()) {
            fileSizes.put(entry.getKey(), entry.getValue().length());
        }
        stats.put("file_sizes", fileSizes);
        
        Map<String, Long> lastModifiedTimes = new HashMap<>(lastModified);
        stats.put("last_modified", lastModifiedTimes);
        
        return stats;
    }
    
    /**
     * Shutdown the configuration manager
     */
    public void shutdown() {
        logger.info("Configuration Manager shutting down...");
        
        // Save all configurations
        for (String configName : configFiles.keySet()) {
            saveConfig(configName);
        }
        
        // Clear all data
        configFiles.clear();
        configFileObjects.clear();
        lastModified.clear();
        
        logger.info("Configuration Manager shutdown complete");
    }
}