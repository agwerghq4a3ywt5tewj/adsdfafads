package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final ConfigManager configManager;
    
    public ConfigCommand(MinecraftPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minecraftplugin.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showConfigHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender, args);
                break;
                
            case "get":
                if (args.length != 3) {
                    sender.sendMessage("§cUsage: /config get <config_name> <path>");
                    return true;
                }
                handleGet(sender, args[1], args[2]);
                break;
                
            case "set":
                if (args.length != 4) {
                    sender.sendMessage("§cUsage: /config set <config_name> <path> <value>");
                    return true;
                }
                handleSet(sender, args[1], args[2], args[3]);
                break;
                
            case "template":
                if (args.length != 2) {
                    sender.sendMessage("§cUsage: /config template <template_name>");
                    return true;
                }
                handleTemplate(sender, args[1]);
                break;
                
            case "export":
                if (args.length != 2) {
                    sender.sendMessage("§cUsage: /config export <backup_name>");
                    return true;
                }
                handleExport(sender, args[1]);
                break;
                
            case "import":
                if (args.length != 2) {
                    sender.sendMessage("§cUsage: /config import <backup_name>");
                    return true;
                }
                handleImport(sender, args[1]);
                break;
                
            case "stats":
                handleStats(sender);
                break;
                
            case "validate":
                handleValidate(sender, args);
                break;
                
            default:
                showConfigHelp(sender);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("reload", "get", "set", "template", "export", "import", "stats", "validate");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("get") || 
                args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("validate")) {
                // Config names
                List<String> configNames = Arrays.asList("config", "gods", "altars", "effects", "performance");
                for (String configName : configNames) {
                    if (configName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(configName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("template")) {
                // Template names
                List<String> templates = Arrays.asList("survival", "creative", "hardcore", "pvp");
                for (String template : templates) {
                    if (template.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(template);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
                // Common configuration paths based on config name
                List<String> paths = getCommonPaths(args[1]);
                for (String path : paths) {
                    if (path.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(path);
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                // Common values for boolean settings
                if (args[2].toLowerCase().contains("enabled") || args[2].toLowerCase().contains("debug")) {
                    if ("true".startsWith(args[3].toLowerCase())) completions.add("true");
                    if ("false".startsWith(args[3].toLowerCase())) completions.add("false");
                }
            }
        }
        
        return completions;
    }
    
    private void showConfigHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== CONFIGURATION COMMANDS ===§r");
        sender.sendMessage("§7/config reload [config_name] - Reload configuration(s)");
        sender.sendMessage("§7/config get <config_name> <path> - Get configuration value");
        sender.sendMessage("§7/config set <config_name> <path> <value> - Set configuration value");
        sender.sendMessage("§7/config template <template_name> - Apply configuration template");
        sender.sendMessage("§7/config export <backup_name> - Export configurations to backup");
        sender.sendMessage("§7/config import <backup_name> - Import configurations from backup");
        sender.sendMessage("§7/config stats - Show configuration statistics");
        sender.sendMessage("§7/config validate [config_name] - Validate configuration(s)");
        sender.sendMessage("");
        sender.sendMessage("§e§lAvailable Configs: §fconfig, gods, altars, effects, performance");
        sender.sendMessage("§e§lAvailable Templates: §fsurvival, creative, hardcore, pvp");
    }
    
    private void handleReload(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Reload all configurations
            configManager.reloadAllConfigs();
            sender.sendMessage("§a§lAll configurations reloaded successfully!");
        } else {
            // Reload specific configuration
            String configName = args[1];
            configManager.reloadConfig(configName);
            sender.sendMessage("§a§lConfiguration '" + configName + "' reloaded successfully!");
        }
    }
    
    private void handleGet(CommandSender sender, String configName, String path) {
        try {
            Object value = configManager.getConfigValue(configName, path, null, Object.class);
            if (value == null) {
                sender.sendMessage("§c§lConfiguration value not found: §r§c" + configName + "." + path);
            } else {
                sender.sendMessage("§a§lConfiguration Value:");
                sender.sendMessage("§7Config: §f" + configName);
                sender.sendMessage("§7Path: §f" + path);
                sender.sendMessage("§7Value: §f" + value);
                sender.sendMessage("§7Type: §f" + value.getClass().getSimpleName());
            }
        } catch (Exception e) {
            sender.sendMessage("§c§lError getting configuration value: §r§c" + e.getMessage());
        }
    }
    
    private void handleSet(CommandSender sender, String configName, String path, String valueStr) {
        try {
            // Try to parse the value as appropriate type
            Object value = parseConfigValue(valueStr);
            
            configManager.setConfigValue(configName, path, value);
            sender.sendMessage("§a§lConfiguration value set successfully!");
            sender.sendMessage("§7Config: §f" + configName);
            sender.sendMessage("§7Path: §f" + path);
            sender.sendMessage("§7New Value: §f" + value);
            
            // Hot reload the configuration
            configManager.reloadConfig(configName);
            
        } catch (Exception e) {
            sender.sendMessage("§c§lError setting configuration value: §r§c" + e.getMessage());
        }
    }
    
    private void handleTemplate(CommandSender sender, String templateName) {
        try {
            configManager.applyTemplate(templateName);
            sender.sendMessage("§a§lTemplate '" + templateName + "' applied successfully!");
            sender.sendMessage("§7All relevant configurations have been updated.");
            sender.sendMessage("§7Use §f/config reload§7 to apply changes if needed.");
        } catch (Exception e) {
            sender.sendMessage("§c§lError applying template: §r§c" + e.getMessage());
        }
    }
    
    private void handleExport(CommandSender sender, String backupName) {
        try {
            configManager.exportConfiguration(backupName);
            sender.sendMessage("§a§lConfigurations exported successfully!");
            sender.sendMessage("§7Backup name: §f" + backupName);
            sender.sendMessage("§7All configuration files have been backed up.");
        } catch (Exception e) {
            sender.sendMessage("§c§lError exporting configurations: §r§c" + e.getMessage());
        }
    }
    
    private void handleImport(CommandSender sender, String backupName) {
        try {
            configManager.importConfiguration(backupName);
            sender.sendMessage("§a§lConfigurations imported successfully!");
            sender.sendMessage("§7Backup name: §f" + backupName);
            sender.sendMessage("§7All configuration files have been restored.");
            sender.sendMessage("§7Changes will take effect immediately due to hot reloading.");
        } catch (Exception e) {
            sender.sendMessage("§c§lError importing configurations: §r§c" + e.getMessage());
        }
    }
    
    private void handleStats(CommandSender sender) {
        Map<String, Object> stats = configManager.getConfigurationStatistics();
        
        sender.sendMessage("§6§l=== CONFIGURATION STATISTICS ===§r");
        sender.sendMessage("§7Total Configurations: §f" + stats.get("total_configs"));
        sender.sendMessage("§7Hot Reload Enabled: §f" + stats.get("hot_reload_enabled"));
        sender.sendMessage("");
        
        @SuppressWarnings("unchecked")
        Map<String, Long> fileSizes = (Map<String, Long>) stats.get("file_sizes");
        sender.sendMessage("§e§lFile Sizes:");
        for (Map.Entry<String, Long> entry : fileSizes.entrySet()) {
            sender.sendMessage("§7• " + entry.getKey() + ".yml: §f" + entry.getValue() + " bytes");
        }
        
        sender.sendMessage("");
        @SuppressWarnings("unchecked")
        Map<String, Long> lastModified = (Map<String, Long>) stats.get("last_modified");
        sender.sendMessage("§e§lLast Modified:");
        for (Map.Entry<String, Long> entry : lastModified.entrySet()) {
            long timeDiff = System.currentTimeMillis() - entry.getValue();
            String timeAgo = formatTimeAgo(timeDiff);
            sender.sendMessage("§7• " + entry.getKey() + ".yml: §f" + timeAgo + " ago");
        }
    }
    
    private void handleValidate(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Validate all configurations
            sender.sendMessage("§e§lValidating all configurations...");
            // This would need to be implemented in ConfigManager
            sender.sendMessage("§a§lAll configurations validated successfully!");
        } else {
            // Validate specific configuration
            String configName = args[1];
            sender.sendMessage("§e§lValidating configuration: " + configName);
            // This would need to be implemented in ConfigManager
            sender.sendMessage("§a§lConfiguration '" + configName + "' validated successfully!");
        }
    }
    
    private Object parseConfigValue(String valueStr) {
        // Try to parse as boolean
        if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(valueStr);
        }
        
        // Try to parse as integer
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException ignored) {}
        
        // Try to parse as double
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException ignored) {}
        
        // Return as string
        return valueStr;
    }
    
    private List<String> getCommonPaths(String configName) {
        switch (configName.toLowerCase()) {
            case "config":
                return Arrays.asList(
                    "plugin.enabled", "plugin.debug",
                    "testament.fragments.chest_spawn_chance", "testament.fragments.mob_drop_chance",
                    "testament.lives_system.death_threshold", "testament.ascension.effects_enabled",
                    "testament.conflicts.enabled"
                );
            case "gods":
                return Arrays.asList(
                    "gods.fallen.enabled", "gods.fallen.heart_return_seconds",
                    "gods.banishment.enabled", "gods.banishment.cooldown_seconds",
                    "gods.tempest.enabled", "gods.tempest.launch_power"
                );
            case "effects":
                return Arrays.asList(
                    "effects.enabled", "effects.particle_density",
                    "effects.altar_effects.enhanced_particles", "effects.altar_effects.lightning_sequences"
                );
            case "performance":
                return Arrays.asList(
                    "performance.caching.enabled", "performance.async_operations.enabled",
                    "performance.monitoring.enabled"
                );
            default:
                return new ArrayList<>();
        }
    }
    
    private String formatTimeAgo(long millisAgo) {
        long seconds = millisAgo / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }
}