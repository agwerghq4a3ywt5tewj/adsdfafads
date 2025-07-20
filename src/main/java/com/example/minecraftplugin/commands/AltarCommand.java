package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.world.AltarGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AltarCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final AltarGenerator altarGenerator;
    
    public AltarCommand(MinecraftPlugin plugin, AltarGenerator altarGenerator) {
        this.plugin = plugin;
        this.altarGenerator = altarGenerator;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minecraftplugin.altar")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "generate":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                handleGenerate((Player) sender, args);
                break;
                
            case "stats":
                handleStats(sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("minecraftplugin.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload configuration!");
                    return true;
                }
                handleReload(sender);
                break;
                
            case "clear":
                if (!sender.hasPermission("minecraftplugin.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to clear altar data!");
                    return true;
                }
                handleClear(sender);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList("generate", "stats");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("reload");
                subcommands.add("clear");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("generate")) {
            // God names for generate command
            for (GodType god : GodType.values()) {
                String godName = god.name().toLowerCase();
                if (godName.startsWith(args[1].toLowerCase())) {
                    completions.add(godName);
                }
            }
        }
        
        return completions;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== ALTAR COMMANDS ===§r");
        sender.sendMessage("§7/altar generate <god> - Generate an altar at your location");
        sender.sendMessage("§7/altar stats - Show altar generation statistics");
        
        if (sender.hasPermission("minecraftplugin.admin")) {
            sender.sendMessage("§c§lAdmin Commands:");
            sender.sendMessage("§7/altar reload - Reload altar generation configuration");
            sender.sendMessage("§7/altar clear - Clear all generated altar records");
        }
        
        sender.sendMessage("");
        sender.sendMessage("§7Available gods:");
        StringBuilder gods = new StringBuilder();
        for (GodType god : GodType.values()) {
            gods.append(god.name().toLowerCase()).append(", ");
        }
        sender.sendMessage("§f" + gods.toString().replaceAll(", $", ""));
    }
    
    private void handleGenerate(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUsage: /altar generate <god>");
            return;
        }
        
        GodType god = GodType.fromString(args[1]);
        if (god == null) {
            player.sendMessage("§cInvalid god: " + args[1]);
            player.sendMessage("§7Available gods: " + getGodList());
            return;
        }
        
        Location location = player.getLocation();
        
        // Try to generate the altar
        boolean success = altarGenerator.generateAltarAt(location, god);
        
        if (success) {
            player.sendMessage("§a§lAltar Generated! §r§aSuccessfully created " + god.getDisplayName() + " altar at your location.");
            player.sendMessage("§7The altar has been integrated into the world generation system.");
            
            // Broadcast to server
            plugin.getServer().broadcastMessage("§6§l" + player.getName() + " §r§6has generated an altar for the " + god.getDisplayName() + "!");
        } else {
            player.sendMessage("§c§lGeneration Failed! §r§cCould not generate altar at this location.");
            player.sendMessage("§7Possible reasons:");
            player.sendMessage("§7• Location too close to another altar");
            player.sendMessage("§7• Unsuitable biome for " + god.getDisplayName());
            player.sendMessage("§7• Area not clear enough for altar structure");
            player.sendMessage("§7• Poor foundation or terrain");
        }
    }
    
    private void handleStats(CommandSender sender) {
        Map<String, Object> stats = altarGenerator.getGenerationStatistics();
        
        sender.sendMessage("§6§l=== ALTAR GENERATION STATISTICS ===§r");
        sender.sendMessage("§7Natural Generation: §f" + (Boolean) stats.get("enabled"));
        sender.sendMessage("§7Total Generated Altars: §f" + stats.get("total_altars"));
        sender.sendMessage("§7Spawn Chance: §f" + String.format("%.3f%%", (Double) stats.get("spawn_chance") * 100));
        sender.sendMessage("§7Minimum Distance: §f" + stats.get("min_distance") + " blocks");
        sender.sendMessage("");
        
        // Show god distribution if available
        @SuppressWarnings("unchecked")
        Map<GodType, Integer> godCounts = (Map<GodType, Integer>) stats.get("god_distribution");
        if (!godCounts.isEmpty()) {
            sender.sendMessage("§e§lGod Distribution:");
            for (Map.Entry<GodType, Integer> entry : godCounts.entrySet()) {
                sender.sendMessage("§7• " + entry.getKey().getDisplayName() + ": §f" + entry.getValue());
            }
        }
        
        sender.sendMessage("");
        sender.sendMessage("§7Configuration can be modified in config.yml under 'testament.altars.natural_generation'");
    }
    
    private void handleReload(CommandSender sender) {
        altarGenerator.reloadConfiguration();
        sender.sendMessage("§a§lConfiguration Reloaded! §r§aAltar generation settings have been updated.");
    }
    
    private void handleClear(CommandSender sender) {
        altarGenerator.clearGeneratedAltars();
        sender.sendMessage("§a§lData Cleared! §r§aAll generated altar records have been removed.");
        sender.sendMessage("§7Note: This only clears the tracking data, not the actual altar structures.");
    }
    
    private String getGodList() {
        StringBuilder gods = new StringBuilder();
        for (GodType god : GodType.values()) {
            gods.append(god.name().toLowerCase()).append(", ");
        }
        return gods.toString().replaceAll(", $", "");
    }
}