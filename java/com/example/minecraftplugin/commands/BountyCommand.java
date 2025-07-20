package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.BountyManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final BountyManager bountyManager;
    
    public BountyCommand(MinecraftPlugin plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.bounty")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showBountyHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "place":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /bounty place <player> <amount> <currency>");
                    return true;
                }
                handlePlaceBounty(player, args[1], args[2], args[3]);
                break;
                
            case "check":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /bounty check <player>");
                    return true;
                }
                handleCheckBounty(player, args[1]);
                break;
                
            case "list":
                handleListBounties(player);
                break;
                
            case "remove":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /bounty remove <player>");
                    return true;
                }
                if (player.hasPermission("minecraftplugin.admin")) {
                    handleRemoveBounty(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to remove bounties!");
                }
                break;
                
            case "stats":
                if (player.hasPermission("minecraftplugin.admin")) {
                    handleBountyStats(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view bounty statistics!");
                }
                break;
                
            default:
                showBountyHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("place", "check", "list");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("remove");
                subcommands.add("stats");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("place") || args[0].equalsIgnoreCase("check") || 
                args[0].equalsIgnoreCase("remove")) {
                // Player names
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    String playerName = p.getName();
                    if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(playerName);
                    }
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("place")) {
            // Currency types
            List<String> currencies = Arrays.asList("diamond", "emerald", "gold_ingot", "iron_ingot");
            for (String currency : currencies) {
                if (currency.toLowerCase().startsWith(args[3].toLowerCase())) {
                    completions.add(currency);
                }
            }
        }
        
        return completions;
    }
    
    private void showBountyHelp(Player player) {
        player.sendMessage("§6§l=== BOUNTY SYSTEM ===§r");
        player.sendMessage("§7/bounty place <player> <amount> <currency> - Place a bounty");
        player.sendMessage("§7/bounty check <player> - Check bounty on a player");
        player.sendMessage("§7/bounty list - List all active bounties");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/bounty remove <player> - Remove a bounty");
            player.sendMessage("§7/bounty stats - Show bounty statistics");
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lSupported Currencies:");
        player.sendMessage("§7• diamond, emerald, gold_ingot, iron_ingot");
        player.sendMessage("");
        player.sendMessage("§e§lHow it works:");
        player.sendMessage("§7• Place bounties on other players using your items");
        player.sendMessage("§7• Kill a player with a bounty to claim the reward");
        player.sendMessage("§7• Bounties expire after 7 days and are refunded");
    }
    
    private void handlePlaceBounty(Player player, String targetName, String amountStr, String currencyStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount: " + amountStr);
            return;
        }
        
        Material currency;
        try {
            currency = Material.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid currency: " + currencyStr);
            player.sendMessage("§7Supported: diamond, emerald, gold_ingot, iron_ingot");
            return;
        }
        
        bountyManager.placeBounty(player, target, amount, currency);
    }
    
    private void handleCheckBounty(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        BountyManager.BountyInfo bounty = bountyManager.getBounty(target);
        if (bounty == null) {
            player.sendMessage("§7No bounty on " + target.getName());
            return;
        }
        
        player.sendMessage("§6§l=== BOUNTY INFO ===§r");
        player.sendMessage("§7Target: §f" + bounty.getTargetName());
        player.sendMessage("§7Amount: §6" + bounty.getTotalAmount() + " " + bounty.getCurrency().name().toLowerCase());
        player.sendMessage("§7Contributors: §f" + bounty.getContributors().size());
        
        long timeRemaining = (7 * 24 * 60 * 60 * 1000L) - (System.currentTimeMillis() - bounty.getCreatedTime());
        long hoursRemaining = timeRemaining / (60 * 60 * 1000L);
        player.sendMessage("§7Expires in: §f" + hoursRemaining + " hours");
    }
    
    private void handleListBounties(Player player) {
        Map<UUID, BountyManager.BountyInfo> bounties = bountyManager.getActiveBounties();
        
        if (bounties.isEmpty()) {
            player.sendMessage("§7No active bounties.");
            return;
        }
        
        player.sendMessage("§6§l=== ACTIVE BOUNTIES ===§r");
        player.sendMessage("§7Total: §f" + bounties.size() + " active bounties");
        player.sendMessage("");
        
        // Sort by bounty amount (highest first)
        bounties.values().stream()
            .sorted((b1, b2) -> Integer.compare(b2.getTotalAmount(), b1.getTotalAmount()))
            .limit(10) // Show top 10
            .forEach(bounty -> {
                player.sendMessage("§7• §c" + bounty.getTargetName() + "§7: §6" + 
                                 bounty.getTotalAmount() + " " + bounty.getCurrency().name().toLowerCase());
            });
        
        if (bounties.size() > 10) {
            player.sendMessage("§7... and " + (bounties.size() - 10) + " more");
        }
    }
    
    private void handleRemoveBounty(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        boolean removed = bountyManager.removeBounty(target.getUniqueId());
        if (removed) {
            admin.sendMessage("§aRemoved bounty on " + target.getName());
        } else {
            admin.sendMessage("§cNo bounty found on " + target.getName());
        }
    }
    
    private void handleBountyStats(Player admin) {
        Map<String, Object> stats = bountyManager.getBountyStatistics();
        
        admin.sendMessage("§6§l=== BOUNTY STATISTICS ===§r");
        admin.sendMessage("§7Active Bounties: §f" + stats.get("active_bounties"));
        admin.sendMessage("§7Total Claimed: §f" + stats.get("total_claimed"));
        admin.sendMessage("§7Total Active Value: §f" + stats.get("total_active_value"));
        
        if (stats.containsKey("most_wanted")) {
            admin.sendMessage("§7Most Wanted: §c" + stats.get("most_wanted") + 
                            " §7(§6" + stats.get("highest_bounty") + "§7)");
        }
    }
}