package com.example.minecraftplugin.config;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validates configuration files and reports errors
 */
public class ConfigValidator {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    public ConfigValidator(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Validate a specific configuration
     */
    public boolean validateConfig(String configName, FileConfiguration config) {
        List<String> errors = new ArrayList<>();
        
        switch (configName) {
            case "config":
                validateMainConfig(config, errors);
                break;
            case "gods":
                validateGodsConfig(config, errors);
                break;
            case "altars":
                validateAltarsConfig(config, errors);
                break;
            case "effects":
                validateEffectsConfig(config, errors);
                break;
            case "performance":
                validatePerformanceConfig(config, errors);
                break;
        }
        
        if (!errors.isEmpty()) {
            logger.warning("Configuration validation errors in " + configName + ".yml:");
            for (String error : errors) {
                logger.warning("  - " + error);
            }
            return false;
        }
        
        logger.info("Configuration " + configName + ".yml validated successfully");
        return true;
    }
    
    /**
     * Validate main configuration
     */
    private void validateMainConfig(FileConfiguration config, List<String> errors) {
        // Plugin settings
        validateBoolean(config, "plugin.enabled", errors);
        validateBoolean(config, "plugin.debug", errors);
        
        // Fragment settings
        validateDouble(config, "testament.fragments.chest_spawn_chance", 0.0, 1.0, errors);
        validateDouble(config, "testament.fragments.mob_drop_chance", 0.0, 1.0, errors);
        validateInteger(config, "testament.fragments.chest_cooldown_hours", 0, 24, errors);
        validateInteger(config, "testament.fragments.mob_cooldown_hours", 0, 24, errors);
        
        // Lives system
        validateInteger(config, "testament.lives_system.death_threshold", 1, 10, errors);
        validateString(config, "testament.lives_system.void_world_name", errors);
        
        // Ascension settings
        validateBoolean(config, "testament.ascension.effects_enabled", errors);
        validateBoolean(config, "testament.ascension.level_announcements", errors);
        
        // Conflict settings
        validateBoolean(config, "testament.conflicts.enabled", errors);
        validateBoolean(config, "testament.conflicts.remove_conflicting_items", errors);
        validateBoolean(config, "testament.conflicts.announce_conflicts", errors);
    }
    
    /**
     * Validate gods configuration
     */
    private void validateGodsConfig(FileConfiguration config, List<String> errors) {
        String[] gods = {"fallen", "banishment", "abyssal", "sylvan", "tempest", "veil", 
                        "forge", "void", "time", "blood", "crystal", "shadow"};
        
        for (String god : gods) {
            String basePath = "gods." + god;
            validateBoolean(config, basePath + ".enabled", errors);
            
            // God-specific validations
            switch (god) {
                case "fallen":
                    validateInteger(config, basePath + ".heart_return_seconds", 10, 300, errors);
                    validateInteger(config, basePath + ".max_hearts", 10, 50, errors);
                    break;
                case "banishment":
                    validateInteger(config, basePath + ".banish_radius", 1, 20, errors);
                    validateInteger(config, basePath + ".fire_duration", 20, 600, errors);
                    break;
                case "tempest":
                    validateDouble(config, basePath + ".launch_power", 0.5, 5.0, errors);
                    break;
                case "veil":
                    validateInteger(config, basePath + ".teleport_range", 5, 100, errors);
                    break;
            }
            
            validateInteger(config, basePath + ".cooldown_seconds", 1, 300, errors);
        }
    }
    
    /**
     * Validate altars configuration
     */
    private void validateAltarsConfig(FileConfiguration config, List<String> errors) {
        // Natural generation
        validateBoolean(config, "altars.natural_generation.enabled", errors);
        validateDouble(config, "altars.natural_generation.base_spawn_chance", 0.0, 0.1, errors);
        validateInteger(config, "altars.natural_generation.min_distance", 100, 2000, errors);
        validateInteger(config, "altars.natural_generation.max_per_chunk", 1, 5, errors);
        
        // Effects
        validateBoolean(config, "altars.effects.enhanced_completion", errors);
        validateBoolean(config, "altars.effects.permanent_marking", errors);
        validateBoolean(config, "altars.effects.god_specific_sounds", errors);
        validateBoolean(config, "altars.effects.lightning_strikes", errors);
        validateBoolean(config, "altars.effects.beacon_beams", errors);
        
        // Validation
        validateBoolean(config, "altars.validation.strict_structure", errors);
        validateBoolean(config, "altars.validation.allow_variations", errors);
        validateBoolean(config, "altars.validation.show_requirements", errors);
    }
    
    /**
     * Validate effects configuration
     */
    private void validateEffectsConfig(FileConfiguration config, List<String> errors) {
        validateBoolean(config, "effects.enabled", errors);
        validateDouble(config, "effects.particle_density", 0.1, 5.0, errors);
        validateBoolean(config, "effects.sound_effects", errors);
        validateBoolean(config, "effects.god_auras", errors);
        validateBoolean(config, "effects.ability_effects", errors);
        validateBoolean(config, "effects.testament_completion_effects", errors);
        validateBoolean(config, "effects.convergence_effects", errors);
        
        // Altar effects
        validateBoolean(config, "effects.altar_effects.enhanced_particles", errors);
        validateBoolean(config, "effects.altar_effects.god_specific_themes", errors);
        validateBoolean(config, "effects.altar_effects.lightning_sequences", errors);
        validateBoolean(config, "effects.altar_effects.beacon_beams", errors);
        validateBoolean(config, "effects.altar_effects.permanent_marking", errors);
        validateBoolean(config, "effects.altar_effects.sound_sequences", errors);
        
        // Performance
        validateInteger(config, "effects.performance.max_particles_per_effect", 10, 500, errors);
        validateInteger(config, "effects.performance.effect_cooldown_ms", 100, 10000, errors);
        validateInteger(config, "effects.performance.global_cooldown_ms", 1000, 30000, errors);
    }
    
    /**
     * Validate performance configuration
     */
    private void validatePerformanceConfig(FileConfiguration config, List<String> errors) {
        // Caching
        validateBoolean(config, "performance.caching.enabled", errors);
        validateInteger(config, "performance.caching.config_cache_duration", 60000, 3600000, errors);
        validateInteger(config, "performance.caching.player_data_cache_size", 100, 10000, errors);
        
        // Async operations
        validateBoolean(config, "performance.async_operations.enabled", errors);
        validateInteger(config, "performance.async_operations.max_concurrent_tasks", 1, 50, errors);
        validateInteger(config, "performance.async_operations.task_timeout_seconds", 5, 300, errors);
        
        // Optimization
        validateInteger(config, "performance.optimization.batch_save_interval", 60, 3600, errors);
        validateInteger(config, "performance.optimization.cleanup_interval", 60, 3600, errors);
        validateInteger(config, "performance.optimization.max_tracked_operations", 100, 10000, errors);
        
        // Monitoring
        validateBoolean(config, "performance.monitoring.enabled", errors);
        validateInteger(config, "performance.monitoring.log_interval", 60, 3600, errors);
        validateBoolean(config, "performance.monitoring.track_slow_operations", errors);
        validateInteger(config, "performance.monitoring.slow_operation_threshold_ms", 10, 1000, errors);
    }
    
    /**
     * Validate boolean configuration value
     */
    private void validateBoolean(FileConfiguration config, String path, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing boolean configuration: " + path);
            return;
        }
        
        if (!config.isBoolean(path)) {
            errors.add("Invalid boolean value for: " + path + " (got: " + config.get(path) + ")");
        }
    }
    
    /**
     * Validate integer configuration value with range
     */
    private void validateInteger(FileConfiguration config, String path, int min, int max, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing integer configuration: " + path);
            return;
        }
        
        if (!config.isInt(path)) {
            errors.add("Invalid integer value for: " + path + " (got: " + config.get(path) + ")");
            return;
        }
        
        int value = config.getInt(path);
        if (value < min || value > max) {
            errors.add("Integer value out of range for: " + path + " (got: " + value + ", expected: " + min + "-" + max + ")");
        }
    }
    
    /**
     * Validate double configuration value with range
     */
    private void validateDouble(FileConfiguration config, String path, double min, double max, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing double configuration: " + path);
            return;
        }
        
        if (!config.isDouble(path) && !config.isInt(path)) {
            errors.add("Invalid double value for: " + path + " (got: " + config.get(path) + ")");
            return;
        }
        
        double value = config.getDouble(path);
        if (value < min || value > max) {
            errors.add("Double value out of range for: " + path + " (got: " + value + ", expected: " + min + "-" + max + ")");
        }
    }
    
    /**
     * Validate string configuration value
     */
    private void validateString(FileConfiguration config, String path, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing string configuration: " + path);
            return;
        }
        
        if (!config.isString(path)) {
            errors.add("Invalid string value for: " + path + " (got: " + config.get(path) + ")");
        }
    }
    
    /**
     * Validate string list configuration value
     */
    private void validateStringList(FileConfiguration config, String path, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing string list configuration: " + path);
            return;
        }
        
        if (!config.isList(path)) {
            errors.add("Invalid string list value for: " + path + " (got: " + config.get(path) + ")");
        }
    }
    
    /**
     * Validate enum configuration value
     */
    private void validateEnum(FileConfiguration config, String path, String[] validValues, List<String> errors) {
        if (!config.contains(path)) {
            errors.add("Missing enum configuration: " + path);
            return;
        }
        
        String value = config.getString(path);
        if (value == null || !Arrays.asList(validValues).contains(value.toUpperCase())) {
            errors.add("Invalid enum value for: " + path + " (got: " + value + ", expected one of: " + Arrays.toString(validValues) + ")");
        }
    }
}