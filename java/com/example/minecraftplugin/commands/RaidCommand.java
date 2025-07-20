package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.raids.ActiveRaid;
import com.example.minecraftplugin.raids.RaidDefinition;
import com.example.minecraftplugin.raids.RaidManager;
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

public class RaidCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final RaidManager raidManager;
    
    public RaidCommand(MinecraftPlugin plugin, RaidManager raidManager) {
        this.plugin = plugin;
        this.raidManager = raidManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.raid")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showRaidHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                showAvailableRaids(player);
                break;
                
            case "info":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /raid info <raid_id>");
                    return true;
                }
                showRaidInfo(player, args[1]);
                break;
                
            case "start":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /raid start <raid_id>");
                    return true;
                }
                startRaid(player, args[1]);
                break;
                
            case "leave":
                leaveRaid(player);
                break;
                
            case "status":
                showRaidStatus(player);
                break;
                
            case "leaderboard":
                if (args.length == 1) {
                    showGlobalLeaderboard(player);
                } else {
                    showSpecificLeaderboard(player, args);
                }
                break;
                
            case "weeklychallenge":
                showWeeklyChallenge(player);
                break;
                
            case "records":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /raid records <player_name>");
                    return true;
                }
                showPlayerRecords(player, args[1]);
                break;
                
            case "stats":
                if (player.hasPermission("minecraftplugin.admin")) {
                    showRaidStatistics(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view raid statistics!");
                }
                break;
                
            default:
                showRaidHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList("list", "info", "start", "leave", "status", 
                "leaderboard", "weeklychallenge", "records", "guild");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("stats");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("start")) {
                // Raid IDs for info and start commands
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    List<RaidDefinition> availableRaids = raidManager.getAvailableRaids(player);
                    for (RaidDefinition raid : availableRaids) {
                        String raidId = raid.getId();
                        if (raidId.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(raidId);
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("leaderboard")) {
                // Leaderboard types
                List<String> leaderboardTypes = Arrays.asList("global", "tier", "raid", "weekly");
                for (String type : leaderboardTypes) {
                    if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(type);
                    }
                }
            } else if (args[0].equalsIgnoreCase("records")) {
                // Player names for records command
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    String playerName = p.getName();
                    if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(playerName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("guild")) {
                // Guild subcommands
                List<String> guildSubs = Arrays.asList("create", "join", "leave", "info");
                for (String sub : guildSubs) {
                    if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("leaderboard")) {
                if (args[1].equalsIgnoreCase("tier")) {
                    // Tier names for tier leaderboard
                    for (RaidManager.RaidTier tier : RaidManager.RaidTier.values()) {
                        String tierName = tier.name().toLowerCase();
                        if (tierName.startsWith(args[2].toLowerCase())) {
                            completions.add(tierName);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("raid")) {
                    // Raid IDs for raid leaderboard
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        List<RaidDefinition> availableRaids = raidManager.getAvailableRaids(player);
                        for (RaidDefinition raid : availableRaids) {
                            String raidId = raid.getId();
                            if (raidId.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(raidId);
                            }
                        }
                    }
                }
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("guild")) {
            if (args[1].equalsIgnoreCase("join")) {
                // Guild names for join command
                for (com.example.minecraftplugin.managers.GuildManager.Guild guild : plugin.getGuildManager().getAllGuilds()) {
                    String guildName = guild.getName();
                    if (guildName.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(guildName);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private void showRaidHelp(Player player) {
        player.sendMessage("§6§l=== RAID COMMANDS ===§r");
        player.sendMessage("§7/raid list - Show available raids");
        player.sendMessage("§7/raid info <raid_id> - Show detailed raid information");
        player.sendMessage("§7/raid start <raid_id> - Start a raid (requires party)");
        player.sendMessage("§7/raid leave - Leave current raid");
        player.sendMessage("§7/raid status - Show current raid status");
        player.sendMessage("§7/raid leaderboard [type] - Show raid leaderboards");
        player.sendMessage("§7/raid weeklychallenge - Show current weekly challenge");
        player.sendMessage("§7/raid records <player> - Show player's raid records");
        player.sendMessage("§7/raid guild <create|join|leave|raids> - Guild management");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/raid stats - Show server raid statistics");
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lRaid Tiers:");
        player.sendMessage("§7• §aNovice§7: 0-2 testaments (1-2 players)");
        player.sendMessage("§7• §eAdept§7: 3-6 testaments (2-3 players)");
        player.sendMessage("§7• §6Master§7: 7-11 testaments (3-4 players)");
        player.sendMessage("§7• §5Convergence§7: 12 testaments (3+ players)");
        
        // Show weekly challenge info
        com.example.minecraftplugin.raids.WeeklyChallenge weeklyChallenge = raidManager.getCurrentWeeklyChallenge();
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            player.sendMessage("");
            player.sendMessage("§e§l⭐ WEEKLY CHALLENGE ACTIVE ⭐");
            player.sendMessage("§7" + weeklyChallenge.getDisplayName() + " - " + weeklyChallenge.getDescription());
            player.sendMessage("§7Time remaining: §f" + weeklyChallenge.getHoursRemaining() + " hours");
        }
    }
    
    private void showAvailableRaids(Player player) {
        List<RaidDefinition> availableRaids = raidManager.getAvailableRaids(player);
        
        if (availableRaids.isEmpty()) {
            player.sendMessage("§c§lNo raids available! §r§cComplete more testaments to unlock raids.");
            return;
        }
        
        player.sendMessage("§6§l=== AVAILABLE RAIDS ===§r");
        player.sendMessage("§7Your Testament Count: §f" + plugin.getGodManager().getTestamentCount(player));
        player.sendMessage("");
        
        for (RaidDefinition raid : availableRaids) {
            String tierColor = getTierColor(raid.getTier());
            player.sendMessage(tierColor + "§l" + raid.getDisplayName() + "§r");
            player.sendMessage("§7• ID: §f" + raid.getId());
            player.sendMessage("§7• Tier: " + tierColor + raid.getTier().getDisplayName());
            player.sendMessage("§7• Players: §f" + raid.getMinPlayers() + "-" + raid.getMaxPlayers());
            player.sendMessage("§7• Time: §f" + (raid.getTimeLimit() / 60) + " minutes");
            player.sendMessage("§7• Scaling: §fDynamic based on party");
            player.sendMessage("§7• " + raid.getDescription());
            player.sendMessage("");
        }
        
        player.sendMessage("§7Use §f/raid info <raid_id>§7 for detailed information");
        player.sendMessage("§7Use §f/raid start <raid_id>§7 to begin a raid");
    }
    
    private void showRaidInfo(Player player, String raidId) {
        List<RaidDefinition> availableRaids = raidManager.getAvailableRaids(player);
        RaidDefinition raid = null;
        
        for (RaidDefinition r : availableRaids) {
            if (r.getId().equalsIgnoreCase(raidId)) {
                raid = r;
                break;
            }
        }
        
        if (raid == null) {
            player.sendMessage("§c§lRaid not found or not available! §r§cUse §f/raid list§c to see available raids.");
            return;
        }
        
        player.sendMessage(raid.getFormattedInfo());
        player.sendMessage("§a§lReady to start? §r§aUse §f/raid start " + raid.getId());
    }
    
    private void startRaid(Player player, String raidId) {
        // Check if player is already in a raid
        ActiveRaid currentRaid = raidManager.getPlayerRaid(player);
        if (currentRaid != null) {
            player.sendMessage("§c§lAlready in raid! §r§cUse §f/raid leave§c to leave your current raid first.");
            return;
        }
        
        // For now, start raid with just the requesting player
        // In a full implementation, this would involve party/group management
        List<Player> raidPlayers = new ArrayList<>();
        raidPlayers.add(player);
        
        boolean success = raidManager.startRaid(raidId, raidPlayers, player.getLocation());
        
        if (!success) {
            player.sendMessage("§c§lFailed to start raid! §r§cCheck requirements and try again.");
            player.sendMessage("§7Possible issues:");
            player.sendMessage("§7• Raid not available for your progression level");
            player.sendMessage("§7• Not enough players (some raids require multiple players)");
            player.sendMessage("§7• Already in a raid");
        }
    }
    
    private void leaveRaid(Player player) {
        ActiveRaid raid = raidManager.getPlayerRaid(player);
        
        if (raid == null) {
            player.sendMessage("§c§lNot in a raid! §r§cUse §f/raid list§c to see available raids.");
            return;
        }
        
        raid.removePlayer(player);
        player.sendMessage("§e§lLeft raid: §r§e" + raid.getDefinition().getDisplayName());
        player.sendMessage("§7You can join another raid anytime.");
    }
    
    private void showRaidStatus(Player player) {
        ActiveRaid raid = raidManager.getPlayerRaid(player);
        
        if (raid == null) {
            player.sendMessage("§c§lNot in a raid! §r§cUse §f/raid list§c to see available raids.");
            return;
        }
        
        long elapsedTime = (System.currentTimeMillis() - raid.getStartTime()) / 1000;
        int actualTimeLimit = (int) (raid.getDefinition().getTimeLimit() * raid.getScaling().getTimeMultiplier());
        long remainingTime = actualTimeLimit - elapsedTime;
        
        player.sendMessage("§6§l=== CURRENT RAID STATUS ===§r");
        player.sendMessage("§7Raid: §f" + raid.getDefinition().getDisplayName());
        player.sendMessage("§7State: §f" + raid.getState().name());
        player.sendMessage("§7Players: §f" + raid.getPlayers().size());
        player.sendMessage("§7Time Remaining: §f" + (remainingTime / 60) + ":" + String.format("%02d", remainingTime % 60));
        player.sendMessage("§7Objectives Completed: §f" + raid.getCompletedObjectives().size());
        player.sendMessage("§7Scaling: §f" + raid.getScaling().getFormattedDisplay());
        
        // Show weekly challenge if active
        if (raid.getWeeklyChallenge() != null && raid.getWeeklyChallenge().isActive()) {
            player.sendMessage("§e§l⭐ Weekly Challenge: §r§e" + raid.getWeeklyChallenge().getDisplayName());
        }
        
        player.sendMessage("");
        player.sendMessage("§7Current Objective: §f" + raid.getDefinition().getObjective());
        
        // Show Eternal Crucible specific status
        if ("eternal_crucible".equals(raid.getDefinition().getId())) {
            player.sendMessage("");
            player.sendMessage("§5§l=== ETERNAL CRUCIBLE STATUS ===§r");
            player.sendMessage("§7Nexus Core Health: §f" + raid.getNexusCoreHealth() + "/1000");
            player.sendMessage("§7Current Wave: §f" + (raid.getCurrentWave() + 1));
            player.sendMessage("§7Gods Defeated: §f" + raid.getGodsDefeated() + "/12");
            player.sendMessage("§7Core Status: " + (raid.isNexusCoreActive() ? "§aActive" : "§cDestroyed"));
        }
        
        if (raid.getPlayers().size() > 1) {
            player.sendMessage("§7Party Members:");
            for (Player p : raid.getPlayers()) {
                if (p != player) {
                    player.sendMessage("§7• §f" + p.getName());
                }
            }
        }
    }
    
    private void showGlobalLeaderboard(Player player) {
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> topRecords = raidManager.getLeaderboardManager().getTopCompletionsByScore(10);
        
        if (topRecords.isEmpty()) {
            player.sendMessage("§e§lNo raid completions recorded yet!");
            player.sendMessage("§7Be the first to complete a raid and set a record!");
            return;
        }
        
        player.sendMessage("§6§l=== GLOBAL RAID LEADERBOARD ===§r");
        player.sendMessage("§7Top 10 raid completions by score:");
        player.sendMessage("");
        
        for (int i = 0; i < topRecords.size(); i++) {
            com.example.minecraftplugin.raids.RaidCompletionRecord record = topRecords.get(i);
            player.sendMessage(record.getLeaderboardDisplay(i + 1));
            player.sendMessage("");
        }
        
        player.sendMessage("§7Use §f/raid leaderboard tier <tier>§7 for tier-specific leaderboards");
        player.sendMessage("§7Use §f/raid leaderboard raid <raid_id>§7 for raid-specific leaderboards");
    }
    
    private void showSpecificLeaderboard(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /raid leaderboard <global|tier|raid|weekly> [name]");
            return;
        }
        
        String type = args[1].toLowerCase();
        
        switch (type) {
            case "tier":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /raid leaderboard tier <tier_name>");
                    return;
                }
                showTierLeaderboard(player, args[2]);
                break;
                
            case "raid":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /raid leaderboard raid <raid_id>");
                    return;
                }
                showRaidLeaderboard(player, args[2]);
                break;
                
            case "weekly":
                showWeeklyChallengeLeaderboard(player);
                break;
                
            case "global":
                showGlobalLeaderboard(player);
                break;
                
            default:
                player.sendMessage("§cInvalid leaderboard type. Use: global, tier, raid, or weekly");
                break;
        }
    }
    
    private void showTierLeaderboard(Player player, String tierName) {
        RaidManager.RaidTier tier;
        try {
            tier = RaidManager.RaidTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid tier: " + tierName);
            player.sendMessage("§7Available tiers: novice, adept, master, convergence");
            return;
        }
        
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> tierRecords = raidManager.getLeaderboardManager().getTopCompletionsByTier(tier, 10);
        
        if (tierRecords.isEmpty()) {
            player.sendMessage("§e§lNo " + tier.getDisplayName() + " raid completions yet!");
            return;
        }
        
        String tierColor = getTierColor(tier);
        player.sendMessage(tierColor + "§l=== " + tier.getDisplayName().toUpperCase() + " RAID LEADERBOARD ===§r");
        player.sendMessage("§7Top 10 completions for " + tier.getDisplayName() + " tier:");
        player.sendMessage("");
        
        for (int i = 0; i < tierRecords.size(); i++) {
            com.example.minecraftplugin.raids.RaidCompletionRecord record = tierRecords.get(i);
            player.sendMessage(record.getLeaderboardDisplay(i + 1));
            player.sendMessage("");
        }
    }
    
    private void showRaidLeaderboard(Player player, String raidId) {
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> raidRecords = raidManager.getLeaderboardManager().getTopCompletions(raidId, 10);
        
        if (raidRecords.isEmpty()) {
            player.sendMessage("§e§lNo completions for raid: " + raidId);
            return;
        }
        
        com.example.minecraftplugin.raids.RaidCompletionRecord firstRecord = raidRecords.get(0);
        player.sendMessage("§6§l=== " + firstRecord.getRaidDisplayName().toUpperCase() + " LEADERBOARD ===§r");
        player.sendMessage("§7Top 10 fastest completions:");
        player.sendMessage("");
        
        for (int i = 0; i < raidRecords.size(); i++) {
            com.example.minecraftplugin.raids.RaidCompletionRecord record = raidRecords.get(i);
            player.sendMessage(record.getLeaderboardDisplay(i + 1));
            player.sendMessage("");
        }
    }
    
    private void showWeeklyChallengeLeaderboard(Player player) {
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> weeklyRecords = raidManager.getLeaderboardManager().getWeeklyChallengeCompletions(10);
        
        if (weeklyRecords.isEmpty()) {
            player.sendMessage("§e§lNo weekly challenge completions yet!");
            return;
        }
        
        player.sendMessage("§e§l=== WEEKLY CHALLENGE LEADERBOARD ===§r");
        player.sendMessage("§7Top 10 weekly challenge completions by score:");
        player.sendMessage("");
        
        for (int i = 0; i < weeklyRecords.size(); i++) {
            com.example.minecraftplugin.raids.RaidCompletionRecord record = weeklyRecords.get(i);
            player.sendMessage(record.getLeaderboardDisplay(i + 1));
            player.sendMessage("");
        }
    }
    
    private void showWeeklyChallenge(Player player) {
        com.example.minecraftplugin.raids.WeeklyChallenge challenge = raidManager.getCurrentWeeklyChallenge();
        
        if (challenge == null) {
            player.sendMessage("§c§lNo weekly challenge active!");
            return;
        }
        
        if (!challenge.isActive()) {
            player.sendMessage("§c§lWeekly challenge has expired!");
            player.sendMessage("§7A new challenge will be available soon.");
            return;
        }
        
        player.sendMessage(challenge.getFormattedDisplay());
        
        // Show current leaderboard for this challenge
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> weeklyRecords = raidManager.getLeaderboardManager().getWeeklyChallengeCompletions(3);
        if (!weeklyRecords.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("§e§lCurrent Weekly Challenge Leaders:");
            for (int i = 0; i < Math.min(3, weeklyRecords.size()); i++) {
                com.example.minecraftplugin.raids.RaidCompletionRecord record = weeklyRecords.get(i);
                String rank = (i == 0) ? "§6#1" : (i == 1) ? "§7#2" : "§c#3";
                player.sendMessage(rank + " §f" + String.join(", ", record.getPlayerNames()) + 
                                 " §7- " + record.getFormattedCompletionTime() + 
                                 " (Score: §f" + record.getScore() + "§7)");
            }
        }
    }
    
    private void showPlayerRecords(Player player, String targetPlayerName) {
        Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage("§cPlayer not found: " + targetPlayerName);
            return;
        }
        
        List<com.example.minecraftplugin.raids.RaidCompletionRecord> playerRecords = raidManager.getLeaderboardManager().getPlayerCompletions(targetPlayer.getUniqueId());
        
        if (playerRecords.isEmpty()) {
            player.sendMessage("§e§l" + targetPlayerName + " has no raid completions yet!");
            return;
        }
        
        player.sendMessage("§6§l=== " + targetPlayerName.toUpperCase() + "'S RAID RECORDS ===§r");
        player.sendMessage("§7Total completions: §f" + playerRecords.size());
        player.sendMessage("");
        player.sendMessage("§7Recent completions:");
        
        // Show last 5 completions
        for (int i = 0; i < Math.min(5, playerRecords.size()); i++) {
            com.example.minecraftplugin.raids.RaidCompletionRecord record = playerRecords.get(i);
            String tierColor = getTierColor(record.getTier());
            
            player.sendMessage("§7• " + tierColor + record.getRaidDisplayName() + "§r");
            player.sendMessage("§7  Time: §f" + record.getFormattedCompletionTime() + 
                             " §7| Score: §f" + record.getScore());
            if (record.isWeeklyChallengeActive()) {
                player.sendMessage("§7  §e⭐ Weekly Challenge");
            }
        }
        
        if (playerRecords.size() > 5) {
            player.sendMessage("§7... and " + (playerRecords.size() - 5) + " more");
        }
    }
    
    private void showRaidStatistics(Player player) {
        Map<String, Object> stats = raidManager.getRaidStatistics();
        
        player.sendMessage("§6§l=== RAID STATISTICS ===§r");
        player.sendMessage("§7Active Raids: §f" + stats.get("active_raids"));
        player.sendMessage("§7Players in Raids: §f" + stats.get("players_in_raids"));
        player.sendMessage("§7Available Raid Types: §f" + stats.get("available_raid_types"));
        
        // Show leaderboard statistics
        @SuppressWarnings("unchecked")
        Map<String, Object> leaderboardStats = (Map<String, Object>) stats.get("leaderboard_stats");
        if (leaderboardStats != null) {
            player.sendMessage("§7Total Completions: §f" + leaderboardStats.get("total_completions"));
            player.sendMessage("§7Weekly Challenge Completions: §f" + leaderboardStats.get("weekly_challenge_completions"));
        }
        
        // Show weekly challenge info
        @SuppressWarnings("unchecked")
        Map<String, Object> challengeInfo = (Map<String, Object>) stats.get("weekly_challenge");
        if (challengeInfo != null) {
            player.sendMessage("§7Weekly Challenge Active: §f" + challengeInfo.get("active"));
            if ((Boolean) challengeInfo.get("active")) {
                player.sendMessage("§7Current Challenge: §f" + challengeInfo.get("name"));
                player.sendMessage("§7Hours Remaining: §f" + challengeInfo.get("hours_remaining"));
            }
        }
        
        @SuppressWarnings("unchecked")
        Map<RaidManager.RaidTier, Integer> tierCounts = (Map<RaidManager.RaidTier, Integer>) stats.get("raids_by_tier");
        
        if (!tierCounts.isEmpty()) {
            player.sendMessage("§7Active Raids by Tier:");
            for (Map.Entry<RaidManager.RaidTier, Integer> entry : tierCounts.entrySet()) {
                String tierColor = getTierColor(entry.getKey());
                player.sendMessage("§7• " + tierColor + entry.getKey().getDisplayName() + "§7: §f" + entry.getValue());
            }
        }
    }
    
    private String getTierColor(RaidManager.RaidTier tier) {
        switch (tier) {
            case NOVICE:
                return "§a";
            case ADEPT:
                return "§e";
            case MASTER:
                return "§6";
            case CONVERGENCE:
                return "§5";
            default:
                return "§7";
        }
    }
}