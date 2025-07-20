package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.enums.AscensionLevel;
import com.example.minecraftplugin.enums.PlayerTitle;
import com.example.minecraftplugin.managers.GodManager;
import com.example.minecraftplugin.managers.ConvergenceManager;
import com.example.minecraftplugin.managers.PlayerDataManager;
import com.example.minecraftplugin.managers.PlayerTitleManager;
import com.example.minecraftplugin.items.ShardOfAtonement;
import com.example.minecraftplugin.items.KeyToRedemption;
import com.example.minecraftplugin.items.FragmentItem;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class TestamentCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    private final PlayerDataManager playerDataManager;
    private final PlayerTitleManager playerTitleManager;
    
    public TestamentCommand(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.godManager = plugin.getGodManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.playerTitleManager = plugin.getPlayerTitleManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.testament")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Handle subcommands
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showTestamentStatus(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("fragments")) {
            if (args.length == 1) {
                showAllFragments(player);
            } else {
                showGodFragments(player, args[1]);
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("lives")) {
            showLivesStatus(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("conflicts")) {
            showConflicts(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("ascension")) {
            showAscensionStatus(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("title")) {
            showTitleStatus(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("convergence")) {
            showConvergenceStatus(player);
            return true;
        }
        
        // Admin commands
        if (args[0].equalsIgnoreCase("admin") && player.hasPermission("minecraftplugin.admin")) {
            handleAdminCommand(player, args);
            return true;
        }
        
        // Invalid subcommand
        player.sendMessage(ChatColor.RED + "Usage: /testament [status|fragments [god]|lives|conflicts|ascension|title|convergence|admin]");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subcommands = Arrays.asList("status", "fragments", "lives", "conflicts", "ascension", "title", "convergence");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("admin");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("fragments")) {
                // God names for fragments command
                for (GodType god : GodType.values()) {
                    String godName = god.name().toLowerCase();
                    if (godName.startsWith(args[1].toLowerCase())) {
                        completions.add(godName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("minecraftplugin.admin")) {
                // Admin subcommands
                List<String> adminCommands = Arrays.asList("setdeaths", "setvoid", "resetplayer", "give-fragment", 
                    "complete", "reset", "cooldown", "debug", "convergence", "give-shard", "give-key", "title");
                for (String adminCmd : adminCommands) {
                    if (adminCmd.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(adminCmd);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("minecraftplugin.admin")) {
                // Player names for admin commands
                if (Arrays.asList("setdeaths", "setvoid", "resetplayer", "give-fragment", "complete", 
                    "reset", "cooldown", "debug", "convergence", "give-shard", "give-key", "title").contains(args[1].toLowerCase())) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        String playerName = player.getName();
                        if (playerName.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(playerName);
                        }
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("minecraftplugin.admin")) {
                if (args[1].equalsIgnoreCase("give-fragment") || args[1].equalsIgnoreCase("complete")) {
                    // God names for give-fragment and complete commands
                    for (GodType god : GodType.values()) {
                        String godName = god.name().toLowerCase();
                        if (godName.startsWith(args[3].toLowerCase())) {
                            completions.add(godName);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("setvoid")) {
                    // Boolean values for setvoid
                    if ("true".startsWith(args[3].toLowerCase())) completions.add("true");
                    if ("false".startsWith(args[3].toLowerCase())) completions.add("false");
                } else if (args[1].equalsIgnoreCase("convergence")) {
                    // Convergence actions
                    if ("grant".startsWith(args[3].toLowerCase())) completions.add("grant");
                    if ("remove".startsWith(args[3].toLowerCase())) completions.add("remove");
                } else if (args[1].equalsIgnoreCase("give-shard")) {
                    // Shard numbers 1-7
                    for (int i = 1; i <= 7; i++) {
                        String shardNum = String.valueOf(i);
                        if (shardNum.startsWith(args[3])) {
                            completions.add(shardNum);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("title")) {
                    // Player titles
                    List<String> titles = Arrays.asList("clear", "none", "fallen", "toxic", "cursed", "blessed", "champion", "legend");
                    for (String title : titles) {
                        if (title.toLowerCase().startsWith(args[3].toLowerCase())) {
                            completions.add(title);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("cooldown")) {
                    // Cooldown subcommand
                    if ("clear".startsWith(args[3].toLowerCase())) {
                        completions.add("clear");
                    }
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("minecraftplugin.admin")) {
                if (args[1].equalsIgnoreCase("give-fragment")) {
                    // Fragment numbers 1-7
                    for (int i = 1; i <= 7; i++) {
                        String fragNum = String.valueOf(i);
                        if (fragNum.startsWith(args[4])) {
                            completions.add(fragNum);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("reset")) {
                    // God names for reset command (optional)
                    for (GodType god : GodType.values()) {
                        String godName = god.name().toLowerCase();
                        if (godName.startsWith(args[4].toLowerCase())) {
                            completions.add(godName);
                        }
                    }
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Show the player's overall testament status
     */
    private void showTestamentStatus(Player player) {
        String status = godManager.getPlayerStatus(player);
        player.sendMessage(status);
    }
    
    /**
     * Show fragment progress for all gods
     */
    private void showAllFragments(Player player) {
        player.sendMessage("§6§l=== FRAGMENT COLLECTION STATUS ===§r");
        player.sendMessage("§7Player: §f" + player.getName());
        player.sendMessage("");
        
        for (GodType god : GodType.values()) {
            Set<Integer> fragments = godManager.getPlayerFragments(player, god);
            int fragmentCount = fragments.size();
            boolean completed = godManager.hasCompletedTestament(player, god);
            
            String status = completed ? "§a§lCOMPLETED§r" : "§7" + fragmentCount + "/7";
            String godName = god.getDisplayName();
            
            player.sendMessage("§7• " + godName + ": §f" + status);
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §f/testament fragments <god>§7 for detailed fragment info");
    }
    
    /**
     * Show fragment progress for a specific god
     */
    private void showGodFragments(Player player, String godName) {
        GodType god = GodType.fromString(godName);
        
        if (god == null) {
            player.sendMessage("§c§lInvalid God! §r§cAvailable gods:");
            StringBuilder gods = new StringBuilder();
            for (GodType g : GodType.values()) {
                gods.append(g.name().toLowerCase()).append(", ");
            }
            player.sendMessage("§7" + gods.toString().replaceAll(", $", ""));
            return;
        }
        
        Set<Integer> fragments = godManager.getPlayerFragments(player, god);
        boolean completed = godManager.hasCompletedTestament(player, god);
        
        player.sendMessage("§6§l=== " + god.getDisplayName().toUpperCase() + " FRAGMENTS ===§r");
        player.sendMessage("§7Theme: §f" + god.getTheme());
        player.sendMessage("§7Tier: §f" + god.getTier().getDisplayName());
        player.sendMessage("");
        
        if (completed) {
            player.sendMessage("§a§l✓ TESTAMENT COMPLETED! ✓§r");
            player.sendMessage("§7You have completed this testament and received divine power.");
        } else {
            player.sendMessage("§7Fragment Progress: §f" + fragments.size() + "/7");
            player.sendMessage("");
            
            // Show which fragments are collected
            for (int i = 1; i <= 7; i++) {
                String status = fragments.contains(i) ? "§a✓" : "§c✗";
                player.sendMessage("§7Fragment " + i + ": " + status);
            }
            
            if (fragments.size() == 7) {
                player.sendMessage("");
                player.sendMessage("§e§lAll fragments collected! Find an altar to complete your testament!");
                player.sendMessage("§7Altar center block: §f" + god.getAltarCenterBlock().name());
            } else {
                int missing = 7 - fragments.size();
                player.sendMessage("");
                player.sendMessage("§7Missing §f" + missing + "§7 more fragments. Keep exploring!");
            }
        }
    }
    
    /**
     * Show the player's lives system status
     */
    private void showLivesStatus(Player player) {
        String summary = playerDataManager.getPlayerDataSummary(player);
        player.sendMessage(summary);
        
        // Additional context
        int deathThreshold = plugin.getConfig().getInt("testament.lives_system.death_threshold", 3);
        int currentDeaths = playerDataManager.getDeathCount(player);
        
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            player.sendMessage("");
            player.sendMessage("§c§lYou are imprisoned in the Void!");
            player.sendMessage("§7Another player must use a Key to Redemption to free you.");
        } else if (currentDeaths > 0) {
            int remaining = deathThreshold - currentDeaths;
            player.sendMessage("");
            player.sendMessage("§e§lWarning: §r§eYou have §c" + remaining + "§e deaths remaining!");
            player.sendMessage("§7Death threshold: §f" + deathThreshold);
        } else {
            player.sendMessage("");
            player.sendMessage("§a§lYou are safe from the Void.");
            player.sendMessage("§7Death threshold: §f" + deathThreshold);
        }
    }
    
    /**
     * Show the player's divine conflicts
     */
    private void showConflicts(Player player) {
        Set<GodType> completed = godManager.getCompletedTestaments(player);
        
        player.sendMessage("§6§l=== DIVINE CONFLICTS ===§r");
        player.sendMessage("§7Player: §f" + player.getName());
        player.sendMessage("");
        
        boolean hasConflicts = false;
        List<String> conflictPairs = new ArrayList<>();
        
        for (GodType god1 : completed) {
            for (GodType god2 : completed) {
                if (god1 != god2 && god1.conflictsWith(god2)) {
                    String pair = god1.getDisplayName() + " vs " + god2.getDisplayName();
                    String reversePair = god2.getDisplayName() + " vs " + god1.getDisplayName();
                    
                    // Avoid duplicate pairs
                    if (!conflictPairs.contains(reversePair)) {
                        conflictPairs.add(pair);
                        hasConflicts = true;
                    }
                }
            }
        }
        
        if (hasConflicts) {
            player.sendMessage("§c§lActive Conflicts:");
            for (String conflict : conflictPairs) {
                player.sendMessage("§7• §c⚡ " + conflict);
            }
            player.sendMessage("");
            player.sendMessage("§e§lWarning: §r§eConflicting divine powers may cause instability!");
            player.sendMessage("§7Divine items from opposing gods cannot coexist.");
        } else {
            player.sendMessage("§a§lNo Active Conflicts");
            player.sendMessage("§7Your divine powers are in harmony.");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Potential Conflicts:");
        player.sendMessage("§7• Fallen vs Veil (Death vs Reality)");
        player.sendMessage("§7• Banishment vs Abyssal (Fire vs Water)");
        player.sendMessage("§7• Sylvan vs Tempest (Nature vs Storm)");
        player.sendMessage("§7• Forge vs Void (Creation vs Destruction)");
        player.sendMessage("§7• Time vs Shadow (Light vs Dark)");
        player.sendMessage("§7• Blood vs Crystal (Chaos vs Order)");
    }
    
    /**
     * Show the player's ascension status and effects
     */
    private void showAscensionStatus(Player player) {
        AscensionLevel level = godManager.getAscensionLevel(player);
        int testamentCount = godManager.getTestamentCount(player);
        
        player.sendMessage("§6§l=== DIVINE ASCENSION STATUS ===§r");
        player.sendMessage("§7Player: §f" + player.getName());
        player.sendMessage("");
        
        player.sendMessage("§e§lCurrent Level: §f" + level.getTitle());
        player.sendMessage("§7Description: §f" + level.getDescription());
        player.sendMessage("§7Testaments: §f" + testamentCount + "/12");
        player.sendMessage("");
        
        // Show progression to next level
        AscensionLevel nextLevel = getNextAscensionLevel(level);
        if (nextLevel != null) {
            int needed = nextLevel.getRequiredTestaments() - testamentCount;
            player.sendMessage("§e§lNext Level: §f" + nextLevel.getTitle());
            player.sendMessage("§7Testaments needed: §f" + needed);
        } else {
            player.sendMessage("§5§l★ MAXIMUM ASCENSION ACHIEVED! ★");
            if (testamentCount >= 12) {
                player.sendMessage("§5§l✦ DIVINE CONVERGENCE COMPLETE! ✦");
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lAscension Benefits:");
        
        switch (level) {
            case MORTAL:
                player.sendMessage("§7• Base gameplay experience");
                break;
            case BLESSED:
                player.sendMessage("§7• Luck I effect");
                player.sendMessage("§7• Minor divine favor");
                break;
            case CHOSEN:
                player.sendMessage("§7• Luck II effect");
                player.sendMessage("§7• Hero of the Village effect");
                player.sendMessage("§7• Significant divine abilities");
                break;
            case DIVINE:
                player.sendMessage("§7• Luck III effect");
                player.sendMessage("§7• Enhanced divine item effects");
                player.sendMessage("§7• Reality manipulation abilities");
                break;
            case GODLIKE:
                player.sendMessage("§7• Maximum effect potency");
                player.sendMessage("§7• Ultimate cosmic powers");
                player.sendMessage("§7• Transcendent abilities");
                break;
            case CONVERGENCE:
                player.sendMessage("§5§l• Master of All Divinity");
                player.sendMessage("§5§l• Transcends all limitations");
                player.sendMessage("§5§l• Supreme deity status");
                break;
        }
    }
    
    /**
     * Show the player's title status
     */
    private void showTitleStatus(Player player) {
        PlayerTitle currentTitle = playerTitleManager.getPlayerTitle(player);
        
        player.sendMessage("§6§l=== PLAYER TITLE STATUS ===§r");
        player.sendMessage("§7Player: §f" + player.getName());
        player.sendMessage("");
        
        if (currentTitle == PlayerTitle.NONE) {
            player.sendMessage("§7Current Title: §fNone");
            player.sendMessage("§7You have no special title assigned.");
        } else {
            player.sendMessage("§7Current Title: " + currentTitle.getDisplayName());
            player.sendMessage("§7Description: §f" + currentTitle.getDescription());
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lTitle Information:");
        player.sendMessage("§7Titles are earned through achievements and behavior:");
        player.sendMessage("§7• §a§lBlessed§r§7: Complete 2+ testaments");
        player.sendMessage("§7• §6§lChampion§r§7: Complete 5+ testaments");
        player.sendMessage("§7• §b§lLegend§r§7: Achieve Divine Convergence");
        player.sendMessage("§7• §c§lToxic§r§7: Negative behavior (temporary)");
        player.sendMessage("§7• §4§lFallen§r§7: Toxic + excessive deaths (permanent)");
        
        if (currentTitle == PlayerTitle.FALLEN) {
            player.sendMessage("");
            player.sendMessage("§4§lWarning: §r§cFallen status is permanent and affects divine powers!");
        }
    }
    
    /**
     * Show the player's convergence status
     */
    private void showConvergenceStatus(Player player) {
        ConvergenceManager convergenceManager = plugin.getGodManager().getConvergenceManager();
        boolean hasConvergence = convergenceManager.hasAchievedConvergence(player);
        int testamentCount = godManager.getTestamentCount(player);
        
        player.sendMessage("§5§l=== DIVINE CONVERGENCE STATUS ===§r");
        player.sendMessage("§7Player: §f" + player.getName());
        player.sendMessage("");
        
        if (hasConvergence) {
            player.sendMessage("§5§l★ CONVERGENCE ACHIEVED! ★");
            player.sendMessage("§7You are the §5§lMaster of All Divinity§r§7!");
            player.sendMessage("§7You have transcended mortality and mastered all divine realms.");
            player.sendMessage("");
            player.sendMessage("§e§lConvergence Benefits:");
            player.sendMessage("§7• §5Convergence Nexus §7- Ultimate divine item");
            player.sendMessage("§7• §530 Hearts §7maximum health");
            player.sendMessage("§7• §5All divine powers §7combined");
            player.sendMessage("§7• §5Reality manipulation §7abilities");
            player.sendMessage("§7• §5Transcendent aura §7effects");
        } else {
            player.sendMessage("§7Convergence Status: §cNot Achieved");
            player.sendMessage("§7Testaments Completed: §f" + testamentCount + "/12");
            
            if (testamentCount >= 10) {
                player.sendMessage("§e§lSo close! §r§eYou are nearly ready for Divine Convergence!");
                int remaining = 12 - testamentCount;
                player.sendMessage("§7Complete §f" + remaining + "§7 more testament" + (remaining == 1 ? "" : "s") + " to achieve convergence.");
            } else if (testamentCount >= 6) {
                player.sendMessage("§6§lWell on your way! §r§6You have mastered half the gods!");
                player.sendMessage("§7Continue your divine journey to reach convergence.");
            } else if (testamentCount >= 3) {
                player.sendMessage("§a§lGood progress! §r§aYou have begun your ascension.");
                player.sendMessage("§7Keep collecting fragments and completing testaments.");
            } else {
                player.sendMessage("§7§lJust beginning! §r§7Start by collecting divine fragments.");
                player.sendMessage("§7Each god requires 7 fragments to complete their testament.");
            }
            
            player.sendMessage("");
            player.sendMessage("§e§lWhat is Divine Convergence?");
            player.sendMessage("§7Divine Convergence is achieved by completing all 12 testaments.");
            player.sendMessage("§7It grants ultimate power and transcendent status.");
            player.sendMessage("§7Only the most dedicated can achieve this legendary feat!");
        }
        
        // Show server convergence statistics
        ConvergenceManager.ConvergenceStats stats = plugin.getGodManager().getConvergenceManager().getConvergenceStats();
        player.sendMessage("");
        player.sendMessage("§e§lServer Statistics:");
        player.sendMessage("§7Total Converged Players: §f" + stats.getTotalConverged());
        player.sendMessage("§7Currently Online: §f" + stats.getOnlineConverged());
        
        if (stats.getTotalConverged() == 0) {
            player.sendMessage("§7Be the first to achieve Divine Convergence on this server!");
        } else if (!hasConvergence) {
            player.sendMessage("§7Join the ranks of the transcendent!");
        }
    }
    
    /**
     * Get the next ascension level
     */
    private AscensionLevel getNextAscensionLevel(AscensionLevel current) {
        AscensionLevel[] levels = AscensionLevel.values();
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == current) {
                return levels[i + 1];
            }
        }
        return null; // Already at max level
    }
    
    /**
     * Handle admin subcommands
     */
    private void handleAdminCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/testament admin setdeaths <player> <count>");
            player.sendMessage("§7/testament admin setvoid <player> <true/false>");
            player.sendMessage("§7/testament admin resetplayer <player>");
            player.sendMessage("§7/testament admin give-fragment <player> <god> <number>");
            player.sendMessage("§7/testament admin complete <player> <god>");
            player.sendMessage("§7/testament admin reset <player> [god]");
            player.sendMessage("§7/testament admin cooldown clear <player>");
            player.sendMessage("§7/testament admin debug <player>");
            player.sendMessage("§7/testament admin give-shard <player> <shard_number>");
            player.sendMessage("§7/testament admin give-key <player>");
            player.sendMessage("§7/testament admin title <player> <title|clear>");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "setdeaths":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /testament admin setdeaths <player> <count>");
                    return;
                }
                handleSetDeaths(player, args[2], args[3]);
                break;
                
            case "setvoid":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /testament admin setvoid <player> <true/false>");
                    return;
                }
                handleSetVoid(player, args[2], args[3]);
                break;
                
            case "resetplayer":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /testament admin resetplayer <player>");
                    return;
                }
                handleResetPlayer(player, args[2]);
                break;
                
            case "give-shard":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /testament admin give-shard <player> <shard_number>");
                    return;
                }
                handleGiveShard(player, args[2], args[3]);
                break;
                
            case "give-key":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /testament admin give-key <player>");
                    return;
                }
                handleGiveKey(player, args[2]);
                break;
                
            case "give-fragment":
                if (args.length != 5) {
                    player.sendMessage("§cUsage: /testament admin give-fragment <player> <god> <number>");
                    return;
                }
                handleGiveFragment(player, args[2], args[3], args[4]);
                break;
                
            case "complete":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /testament admin complete <player> <god>");
                    return;
                }
                handleCompleteTestament(player, args[2], args[3]);
                break;
                
            case "reset":
                if (args.length < 3 || args.length > 4) {
                    player.sendMessage("§cUsage: /testament admin reset <player> [god]");
                    return;
                }
                String godName = args.length == 4 ? args[3] : null;
                handleResetTestament(player, args[2], godName);
                break;
                
            case "cooldown":
                if (args.length != 4 || !args[2].equalsIgnoreCase("clear")) {
                    player.sendMessage("§cUsage: /testament admin cooldown clear <player>");
                    return;
                }
                handleClearCooldowns(player, args[3]);
                break;
                
            case "debug":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /testament admin debug <player>");
                    return;
                }
                handleDebugPlayer(player, args[2]);
                break;
                
            case "convergence":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /testament admin convergence <grant|remove> <player>");
                    return;
                }
                handleConvergenceAdmin(player, args[2], args.length > 3 ? args[3] : null);
                break;
                
            case "title":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /testament admin title <player> <title|clear>");
                    return;
                }
                handleSetTitle(player, args[2], args[3]);
                break;
                
            default:
                player.sendMessage("§cUnknown admin command: " + subCommand);
                break;
        }
    }
    
    private void handleSetDeaths(Player admin, String targetName, String countStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        try {
            int count = Integer.parseInt(countStr);
            playerDataManager.setDeathCount(target, count);
            admin.sendMessage("§aSet death count for " + target.getName() + " to " + count);
            target.sendMessage("§eYour death count has been set to " + count + " by an administrator.");
        } catch (NumberFormatException e) {
            admin.sendMessage("§cInvalid number: " + countStr);
        }
    }
    
    private void handleSetVoid(Player admin, String targetName, String statusStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        boolean status = Boolean.parseBoolean(statusStr);
        playerDataManager.setPrisonerOfTheVoid(target, status);
        
        if (status) {
            admin.sendMessage("§aSet " + target.getName() + " as Prisoner of the Void");
            target.sendMessage("§cYou have been marked as a Prisoner of the Void by an administrator.");
        } else {
            admin.sendMessage("§aFreed " + target.getName() + " from the Void");
            target.sendMessage("§aYou have been freed from the Void by an administrator.");
        }
    }
    
    private void handleResetPlayer(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        playerDataManager.resetPlayerData(target);
        admin.sendMessage("§aReset all lives system data for " + target.getName());
        target.sendMessage("§eYour lives system data has been reset by an administrator.");
    }
    
    private void handleGiveShard(Player admin, String targetName, String shardNumberStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        try {
            int shardNumber = Integer.parseInt(shardNumberStr);
            if (shardNumber < 1 || shardNumber > ShardOfAtonement.getTotalShards()) {
                admin.sendMessage("§cShard number must be between 1 and " + ShardOfAtonement.getTotalShards());
                return;
            }
            
            ItemStack shard = ShardOfAtonement.createShard(shardNumber);
            target.getInventory().addItem(shard);
            
            admin.sendMessage("§aGave " + ShardOfAtonement.getShardName(shardNumber) + " to " + target.getName());
            target.sendMessage("§dYou have received a " + ShardOfAtonement.getShardName(shardNumber) + " from an administrator.");
        } catch (NumberFormatException e) {
            admin.sendMessage("§cInvalid shard number: " + shardNumberStr);
        }
    }
    
    private void handleGiveKey(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        ItemStack key = KeyToRedemption.createKey();
        target.getInventory().addItem(key);
        
        admin.sendMessage("§aGave Key to Redemption to " + target.getName());
        target.sendMessage("§bYou have received a Key to Redemption from an administrator.");
    }
    
    private void handleGiveFragment(Player admin, String targetName, String godName, String fragmentNumberStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        GodType god = GodType.fromString(godName);
        if (god == null) {
            admin.sendMessage("§cInvalid god: " + godName);
            return;
        }
        
        try {
            int fragmentNumber = Integer.parseInt(fragmentNumberStr);
            if (fragmentNumber < 1 || fragmentNumber > 7) {
                admin.sendMessage("§cFragment number must be between 1 and 7");
                return;
            }
            
            FragmentItem fragment = new FragmentItem(god, fragmentNumber);
            ItemStack fragmentItem = fragment.createItemStack();
            target.getInventory().addItem(fragmentItem);
            
            godManager.addFragment(target, god, fragmentNumber);
            
            admin.sendMessage("§aGave " + god.getDisplayName() + " fragment " + fragmentNumber + " to " + target.getName());
            target.sendMessage("§6You have received a divine fragment from an administrator.");
        } catch (NumberFormatException e) {
            admin.sendMessage("§cInvalid fragment number: " + fragmentNumberStr);
        }
    }
    
    private void handleCompleteTestament(Player admin, String targetName, String godName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        GodType god = GodType.fromString(godName);
        if (god == null) {
            admin.sendMessage("§cInvalid god: " + godName);
            return;
        }
        
        if (godManager.hasCompletedTestament(target, god)) {
            admin.sendMessage("§c" + target.getName() + " has already completed the testament for " + god.getDisplayName());
            return;
        }
        
        // Complete the testament
        godManager.completeTestament(target, god);
        
        // Grant divine item
        DivineItem divineItem = godManager.getDivineItem(god);
        if (divineItem != null) {
            ItemStack divineItemStack = divineItem.createItemStack();
            target.getInventory().addItem(divineItemStack);
            divineItem.onObtained(target, divineItemStack);
        }
        
        admin.sendMessage("§aCompleted testament for " + god.getDisplayName() + " for " + target.getName());
        target.sendMessage("§6Your testament for " + god.getDisplayName() + " has been completed by an administrator!");
    }
    
    private void handleResetTestament(Player admin, String targetName, String godName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        if (godName == null) {
            // Reset all testaments
            playerDataManager.resetPlayerData(target);
            admin.sendMessage("§aReset all testament data for " + target.getName());
            target.sendMessage("§eAll your testament data has been reset by an administrator.");
        } else {
            GodType god = GodType.fromString(godName);
            if (god == null) {
                admin.sendMessage("§cInvalid god: " + godName);
                return;
            }
            
            // Reset specific god's testament (this would need new methods in PlayerDataManager)
            admin.sendMessage("§cSpecific god reset not yet implemented. Use full reset for now.");
        }
    }
    
    private void handleClearCooldowns(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        plugin.getCooldownManager().clearPlayerCooldowns(target);
        admin.sendMessage("§aCleared all cooldowns for " + target.getName());
        target.sendMessage("§eYour cooldowns have been cleared by an administrator.");
    }
    
    private void handleDebugPlayer(Player admin, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        admin.sendMessage("§6§l=== DEBUG INFO FOR " + target.getName().toUpperCase() + " ===");
        admin.sendMessage("§7UUID: §f" + target.getUniqueId());
        admin.sendMessage("§7Online: §f" + target.isOnline());
        admin.sendMessage("§7World: §f" + target.getWorld().getName());
        admin.sendMessage("§7Location: §f" + target.getLocation().getBlockX() + ", " + 
                         target.getLocation().getBlockY() + ", " + target.getLocation().getBlockZ());
        
        // Testament data
        admin.sendMessage("§7Testaments: §f" + godManager.getTestamentCount(target) + "/12");
        admin.sendMessage("§7Ascension: §f" + godManager.getAscensionLevel(target).getTitle());
        admin.sendMessage("§7Death Count: §f" + playerDataManager.getDeathCount(target));
        admin.sendMessage("§7Void Prisoner: §f" + playerDataManager.isPrisonerOfTheVoid(target));
        
        // Fragment counts
        int totalFragments = 0;
        for (GodType god : GodType.values()) {
            totalFragments += godManager.getPlayerFragments(target, god).size();
        }
        admin.sendMessage("§7Total Fragments: §f" + totalFragments);
        
        // Divine items in inventory
        int divineItemCount = 0;
        for (ItemStack item : target.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                if (lore != null) {
                    for (String line : lore) {
                        if (line.contains("Divine Item of the")) {
                            divineItemCount++;
                            break;
                        }
                    }
                }
            }
        }
        admin.sendMessage("§7Divine Items: §f" + divineItemCount);
    }
    
    private void handleConvergenceAdmin(Player admin, String action, String targetName) {
        if (targetName == null) {
            admin.sendMessage("§cUsage: /testament admin convergence <grant|remove> <player>");
            return;
        }
        
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        ConvergenceManager convergenceManager = plugin.getGodManager().getConvergenceManager();
        
        if (action.equalsIgnoreCase("grant")) {
            if (convergenceManager.hasAchievedConvergence(target)) {
                admin.sendMessage("§c" + target.getName() + " has already achieved Divine Convergence!");
                return;
            }
            
            convergenceManager.triggerDivineConvergence(target);
            admin.sendMessage("§aGranted Divine Convergence to " + target.getName());
            
        } else if (action.equalsIgnoreCase("remove")) {
            if (!convergenceManager.hasAchievedConvergence(target)) {
                admin.sendMessage("§c" + target.getName() + " has not achieved Divine Convergence!");
                return;
            }
            
            convergenceManager.removeConvergence(target);
            admin.sendMessage("§aRemoved Divine Convergence from " + target.getName());
            target.sendMessage("§cYour Divine Convergence status has been removed by an administrator.");
            
        } else {
            admin.sendMessage("§cUsage: /testament admin convergence <grant|remove> <player>");
        }
    }
    
    private void handleSetTitle(Player admin, String targetName, String titleName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        if (titleName.equalsIgnoreCase("clear")) {
            playerTitleManager.clearPlayerTitle(target);
            admin.sendMessage("§aCleared title for " + target.getName());
            target.sendMessage("§eYour title has been cleared by an administrator.");
            return;
        }
        
        PlayerTitle title = PlayerTitle.fromString(titleName);
        if (title == null) {
            admin.sendMessage("§cInvalid title: " + titleName);
            admin.sendMessage("§7Available titles: NONE, FALLEN, TOXIC, CURSED, BLESSED, CHAMPION, LEGEND");
            return;
        }
        
        playerTitleManager.setPlayerTitle(target, title);
        admin.sendMessage("§aSet title for " + target.getName() + " to " + title.getDisplayName());
        target.sendMessage("§eYour title has been set to " + title.getDisplayName() + " §eby an administrator.");
    }
}