package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.GuildManager;
import org.bukkit.Bukkit;
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
import java.util.UUID;

public class GuildCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final GuildManager guildManager;
    
    public GuildCommand(MinecraftPlugin plugin, GuildManager guildManager) {
        this.plugin = plugin;
        this.guildManager = guildManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.guild")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showGuildStatus(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /guild create <name>");
                    return true;
                }
                handleCreateGuild(player, args[1]);
                break;
                
            case "invite":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /guild invite <player>");
                    return true;
                }
                handleInvitePlayer(player, args[1]);
                break;
                
            case "join":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /guild join <guild_name>");
                    return true;
                }
                handleJoinGuild(player, args[1]);
                break;
                
            case "leave":
                handleLeaveGuild(player);
                break;
                
            case "info":
                if (args.length == 1) {
                    showGuildInfo(player, null);
                } else {
                    showGuildInfo(player, args[1]);
                }
                break;
                
            case "list":
                showGuildList(player);
                break;
                
            case "stats":
                if (player.hasPermission("minecraftplugin.admin")) {
                    showGuildStatistics(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view guild statistics!");
                }
                break;
                
            case "help":
                showGuildHelp(player);
                break;
                
            default:
                showGuildHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("create", "invite", "join", "leave", "info", "list", "help");
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
            if (args[0].equalsIgnoreCase("invite")) {
                // Player names for invite
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    String playerName = p.getName();
                    if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(playerName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")) {
                // Guild names
                for (GuildManager.Guild guild : guildManager.getAllGuilds()) {
                    String guildName = guild.getName();
                    if (guildName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(guildName);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private void showGuildStatus(Player player) {
        GuildManager.Guild guild = guildManager.getPlayerGuild(player);
        
        if (guild == null) {
            player.sendMessage("§7You are not in a guild.");
            player.sendMessage("§7Use §f/guild create <name>§7 to create a guild");
            player.sendMessage("§7or §f/guild list§7 to see available guilds");
            return;
        }
        
        showGuildInfo(player, null);
    }
    
    private void handleCreateGuild(Player player, String guildName) {
        if (guildManager.isInGuild(player)) {
            player.sendMessage("§c§lAlready in guild! §r§cYou must leave your current guild first.");
            return;
        }
        
        if (guildName.length() < 3 || guildName.length() > 20) {
            player.sendMessage("§c§lInvalid name! §r§cGuild name must be 3-20 characters long.");
            return;
        }
        
        boolean success = guildManager.createGuild(player, guildName);
        if (!success) {
            player.sendMessage("§c§lFailed to create guild! §r§cName may already be taken.");
        }
    }
    
    private void handleInvitePlayer(Player player, String targetName) {
        GuildManager.Guild guild = guildManager.getPlayerGuild(player);
        if (guild == null) {
            player.sendMessage("§c§lNot in guild! §r§cYou must be in a guild to invite players.");
            return;
        }
        
        GuildManager.GuildRole role = guild.getMemberRole(player.getUniqueId());
        if (role == null || !role.canInvite()) {
            player.sendMessage("§c§lInsufficient permissions! §r§cOnly officers and leaders can invite players.");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c§lPlayer not found: §r§c" + targetName);
            return;
        }
        
        boolean success = guildManager.invitePlayer(player, target, guild.getName());
        if (!success) {
            player.sendMessage("§c§lFailed to invite! §r§cPlayer may already be in a guild or already invited.");
        }
    }
    
    private void handleJoinGuild(Player player, String guildName) {
        if (guildManager.isInGuild(player)) {
            player.sendMessage("§c§lAlready in guild! §r§cYou must leave your current guild first.");
            return;
        }
        
        boolean success = guildManager.joinGuild(player, guildName);
        if (!success) {
            player.sendMessage("§c§lFailed to join guild! §r§cYou may not have an invitation or the guild doesn't exist.");
        }
    }
    
    private void handleLeaveGuild(Player player) {
        if (!guildManager.isInGuild(player)) {
            player.sendMessage("§c§lNot in guild! §r§cYou are not currently in a guild.");
            return;
        }
        
        boolean success = guildManager.leaveGuild(player);
        if (!success) {
            player.sendMessage("§c§lFailed to leave guild! §r§cPlease try again.");
        }
    }
    
    private void showGuildInfo(Player player, String guildName) {
        GuildManager.Guild guild;
        
        if (guildName == null) {
            guild = guildManager.getPlayerGuild(player);
            if (guild == null) {
                player.sendMessage("§c§lNot in guild! §r§cSpecify a guild name or join a guild first.");
                return;
            }
        } else {
            guild = guildManager.getAllGuilds().stream()
                .filter(g -> g.getName().equalsIgnoreCase(guildName))
                .findFirst()
                .orElse(null);
            
            if (guild == null) {
                player.sendMessage("§c§lGuild not found: §r§c" + guildName);
                return;
            }
        }
        
        player.sendMessage("§6§l=== " + guild.getName().toUpperCase() + " ===§r");
        
        // Basic info
        Player leader = Bukkit.getPlayer(guild.getLeader());
        String leaderName = leader != null ? leader.getName() : "Unknown";
        player.sendMessage("§7Leader: §f" + leaderName);
        player.sendMessage("§7Members: §f" + guild.getAllMembers().size());
        
        // Creation date
        long daysSinceCreation = (System.currentTimeMillis() - guild.getCreatedTime()) / (24 * 60 * 60 * 1000);
        player.sendMessage("§7Created: §f" + daysSinceCreation + " days ago");
        
        // Statistics
        player.sendMessage("§7Raids Completed: §f" + guild.getRaidsCompleted());
        player.sendMessage("§7Total Score: §f" + guild.getTotalScore());
        if (guild.getBestTime() != Long.MAX_VALUE) {
            long bestTimeSeconds = guild.getBestTime() / 1000;
            player.sendMessage("§7Best Time: §f" + formatTime(bestTimeSeconds));
        }
        
        // Members list
        player.sendMessage("§e§lMembers:");
        for (Map.Entry<UUID, GuildManager.GuildRole> entry : guild.getAllMembers().entrySet()) {
            Player member = Bukkit.getPlayer(entry.getKey());
            String memberName = member != null ? member.getName() : "Unknown";
            String onlineStatus = (member != null && member.isOnline()) ? "§a●" : "§7●";
            String roleColor = getRoleColor(entry.getValue());
            
            player.sendMessage("§7• " + roleColor + entry.getValue().getDisplayName() + "§7: " + 
                             onlineStatus + " §f" + memberName);
        }
    }
    
    private void showGuildList(Player player) {
        if (guildManager.getAllGuilds().isEmpty()) {
            player.sendMessage("§7No guilds exist yet. Create one with §f/guild create <name>");
            return;
        }
        
        player.sendMessage("§6§l=== GUILD LIST ===§r");
        player.sendMessage("§7Total guilds: §f" + guildManager.getAllGuilds().size());
        player.sendMessage("");
        
        // Sort by member count
        List<GuildManager.Guild> sortedGuilds = new ArrayList<>(guildManager.getAllGuilds());
        sortedGuilds.sort((g1, g2) -> Integer.compare(g2.getAllMembers().size(), g1.getAllMembers().size()));
        
        for (GuildManager.Guild guild : sortedGuilds) {
            Player leader = Bukkit.getPlayer(guild.getLeader());
            String leaderName = leader != null ? leader.getName() : "Unknown";
            
            player.sendMessage("§7• §f" + guild.getName() + " §7(" + guild.getAllMembers().size() + " members)");
            player.sendMessage("§7  Leader: §f" + leaderName + " §7| Raids: §f" + guild.getRaidsCompleted());
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §f/guild info <name>§7 for detailed information");
    }
    
    private void showGuildStatistics(Player admin) {
        Map<String, Object> stats = guildManager.getGuildStatistics();
        
        admin.sendMessage("§6§l=== GUILD STATISTICS ===§r");
        admin.sendMessage("§7Total Guilds: §f" + stats.get("total_guilds"));
        admin.sendMessage("§7Total Members: §f" + stats.get("total_members"));
        admin.sendMessage("§7Average Guild Size: §f" + String.format("%.1f", (Double) stats.get("average_guild_size")));
        
        if (stats.containsKey("most_active_guild")) {
            admin.sendMessage("§7Most Active Guild: §f" + stats.get("most_active_guild") + 
                            " §7(§f" + stats.get("most_raids_completed") + " §7raids)");
        }
    }
    
    private void showGuildHelp(Player player) {
        player.sendMessage("§6§l=== GUILD COMMANDS ===§r");
        player.sendMessage("§7/guild - Show your guild status");
        player.sendMessage("§7/guild create <name> - Create a new guild");
        player.sendMessage("§7/guild invite <player> - Invite a player to your guild");
        player.sendMessage("§7/guild join <guild_name> - Join a guild (requires invitation)");
        player.sendMessage("§7/guild leave - Leave your current guild");
        player.sendMessage("§7/guild info [guild_name] - Show guild information");
        player.sendMessage("§7/guild list - List all guilds");
        player.sendMessage("§7/guild help - Show this help");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/guild stats - Show guild statistics");
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lGuild Benefits:");
        player.sendMessage("§7• Team up for raids with guild members");
        player.sendMessage("§7• Track guild raid statistics and achievements");
        player.sendMessage("§7• Compete with other guilds on leaderboards");
        player.sendMessage("§7• Build lasting friendships and alliances");
    }
    
    private String getRoleColor(GuildManager.GuildRole role) {
        switch (role) {
            case LEADER:
                return "§6";
            case OFFICER:
                return "§e";
            case MEMBER:
                return "§a";
            default:
                return "§7";
        }
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return seconds + " seconds";
        }
    }
}