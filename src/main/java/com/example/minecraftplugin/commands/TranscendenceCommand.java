package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.TranscendenceManager;
import com.example.minecraftplugin.enums.TranscendenceLevel;
import com.example.minecraftplugin.transcendence.TranscendenceAbilityManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TranscendenceCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final TranscendenceManager transcendenceManager;
    private final TranscendenceAbilityManager abilityManager;
    
    public TranscendenceCommand(MinecraftPlugin plugin, TranscendenceManager transcendenceManager) {
        this.plugin = plugin;
        this.transcendenceManager = transcendenceManager;
        this.abilityManager = plugin.getTranscendenceAbilityManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.transcendence")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showTranscendenceStatus(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                showTranscendenceStatus(player);
                break;
                
            case "challenges":
                showAvailableChallenges(player);
                break;
                
            case "abilities":
                showUnlockedAbilities(player);
                break;
                
            case "reality":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /transcendence reality <transmute|create|destroy|phase> <target>");
                    return true;
                }
                handleRealityManipulation(player, args[1], args[2]);
                break;
                
            case "realm":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /transcendence realm <name> <size>");
                    return true;
                }
                handleRealmCreation(player, args[1], args[2]);
                break;
                
            case "life":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /transcendence life <peaceful|guardian|elemental|spirit>");
                    return true;
                }
                handleLifeCreation(player, args[1]);
                break;
                
            case "travel":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /transcendence travel <overworld|nether|end>");
                    return true;
                }
                handleDimensionalTravel(player, args[1]);
                break;
                
            case "points":
                showRealityPoints(player);
                break;
                
            case "complete":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /transcendence complete <challenge_id>");
                    return true;
                }
                if (player.hasPermission("minecraftplugin.admin")) {
                    completeChallenge(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to complete challenges manually!");
                }
                break;
                
            case "help":
                showTranscendenceHelp(player);
                break;
                
            default:
                showTranscendenceHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("status", "challenges", "abilities", 
                "reality", "realm", "life", "travel", "points", "help");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("complete");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("complete")) {
            // Challenge IDs for complete command
            if (sender.hasPermission("minecraftplugin.admin")) {
                List<String> challengeIds = Arrays.asList("reality_forge", "time_mastery", "void_walking", 
                    "realm_creation", "star_forging", "life_genesis", "multiverse_nexus", "entropy_reversal", 
                    "creation_mastery", "omnipresence");
                for (String challengeId : challengeIds) {
                    if (challengeId.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(challengeId);
                    }
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reality")) {
                List<String> realityTypes = Arrays.asList("transmute", "create", "destroy", "phase");
                for (String type : realityTypes) {
                    if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(type);
                    }
                }
            } else if (args[0].equalsIgnoreCase("life")) {
                List<String> lifeTypes = Arrays.asList("peaceful", "guardian", "elemental", "spirit");
                for (String type : lifeTypes) {
                    if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(type);
                    }
                }
            } else if (args[0].equalsIgnoreCase("travel")) {
                List<String> dimensions = Arrays.asList("overworld", "nether", "end");
                for (String dim : dimensions) {
                    if (dim.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(dim);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private void showTranscendenceStatus(Player player) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§l=== TRANSCENDENCE LOCKED ===§r");
            player.sendMessage("§7Transcendence content is only available to players who have");
            player.sendMessage("§7achieved §5§lDivine Convergence§r§7 (completed all 12 testaments).");
            player.sendMessage("");
            player.sendMessage("§7Current progress: §f" + plugin.getGodManager().getTestamentCount(player) + "/12 §7testaments");
            player.sendMessage("§7Complete your divine journey first, then return for transcendence!");
            return;
        }
        
        String status = transcendenceManager.getTranscendenceStatus(player);
        player.sendMessage(status);
        
        // Show next level information
        TranscendenceLevel currentLevel = transcendenceManager.getTranscendenceLevel(player);
        TranscendenceLevel nextLevel = getNextTranscendenceLevel(currentLevel);
        
        if (nextLevel != null) {
            player.sendMessage("");
            player.sendMessage("§d§lNext Level: §f" + nextLevel.getTitle());
            player.sendMessage("§7" + nextLevel.getDescription());
            player.sendMessage("§7Challenges needed: §f" + (nextLevel.getRequiredChallenges() - currentLevel.getRequiredChallenges()));
        } else {
            player.sendMessage("");
            player.sendMessage("§d§l★ MAXIMUM TRANSCENDENCE ACHIEVED! ★");
            player.sendMessage("§7You have reached the pinnacle of existence itself!");
        }
    }
    
    private void showAvailableChallenges(Player player) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence challenges are locked! Achieve Divine Convergence first.");
            return;
        }
        
        List<TranscendenceManager.TranscendenceChallenge> challenges = transcendenceManager.getAvailableChallenges(player);
        
        if (challenges.isEmpty()) {
            player.sendMessage("§e§lNo transcendence challenges available at your current level.");
            return;
        }
        
        player.sendMessage("§d§l=== TRANSCENDENCE CHALLENGES ===§r");
        player.sendMessage("§7Available challenges for your transcendence level:");
        player.sendMessage("");
        
        for (TranscendenceManager.TranscendenceChallenge challenge : challenges) {
            String levelColor = getTranscendenceLevelColor(challenge.getRequiredLevel());
            
            player.sendMessage(levelColor + "§l" + challenge.getDisplayName() + "§r");
            player.sendMessage("§7• ID: §f" + challenge.getId());
            player.sendMessage("§7• Level: " + levelColor + challenge.getRequiredLevel().getTitle());
            player.sendMessage("§7• " + challenge.getDescription());
            
            player.sendMessage("§7• Objectives:");
            for (String objective : challenge.getObjectives()) {
                player.sendMessage("§7  - " + objective);
            }
            player.sendMessage("");
        }
        
        player.sendMessage("§7§lNote: §r§7Transcendence challenges are completed through gameplay,");
        player.sendMessage("§7not commands. Demonstrate mastery to progress!");
    }
    
    private void showUnlockedAbilities(Player player) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence abilities are locked! Achieve Divine Convergence first.");
            return;
        }
        
        TranscendenceLevel level = transcendenceManager.getTranscendenceLevel(player);
        
        player.sendMessage("§d§l=== TRANSCENDENCE ABILITIES ===§r");
        player.sendMessage("§7Current Level: §f" + level.getTitle());
        player.sendMessage("§7" + level.getDescription());
        player.sendMessage("");
        
        if (level == TranscendenceLevel.NONE) {
            player.sendMessage("§7Complete transcendence challenges to unlock abilities!");
            return;
        }
        
        player.sendMessage("§d§lUnlocked Abilities:");
        
        switch (level) {
            case REALITY_SHAPER:
                player.sendMessage("§7• §dReality Manipulation §7- Enhanced block manipulation");
                player.sendMessage("§7• §dMatter Transmutation §7- Transform materials at will");
                player.sendMessage("§7• §dSpace Folding §7- Enhanced movement and teleportation");
                break;
                
            case COSMIC_ARCHITECT:
                player.sendMessage("§7• §dAll Reality Shaper abilities");
                player.sendMessage("§7• §dRealm Creation §7- Design and build new dimensions");
                player.sendMessage("§7• §dPhysics Design §7- Alter natural laws");
                player.sendMessage("§7• §dLife Creation §7- Bring new beings into existence");
                break;
                
            case DIMENSIONAL_SOVEREIGN:
                player.sendMessage("§7• §dAll previous abilities");
                player.sendMessage("§7• §dMultiverse Travel §7- Journey between universes");
                player.sendMessage("§7• §dDimensional Control §7- Rule over multiple realities");
                player.sendMessage("§7• §dCosmic Governance §7- Establish universal laws");
                break;
                
            case UNIVERSAL_DEITY:
                player.sendMessage("§7• §dAll previous abilities");
                player.sendMessage("§7• §dOmnipotence §7- Unlimited power over all existence");
                player.sendMessage("§7• §dOmniscience §7- Knowledge of all things");
                player.sendMessage("§7• §dOmnipresence §7- Exist everywhere simultaneously");
                break;
        }
        
        player.sendMessage("");
        player.sendMessage("§7These abilities are automatically applied while you're online.");
        player.sendMessage("§7Progress through challenges to unlock higher levels!");
    }
    
    private void handleRealityManipulation(Player player, String manipulationType, String targetStr) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence locked! Achieve Divine Convergence first.");
            return;
        }
        
        // Get target location (player's looking direction)
        org.bukkit.Location target = player.getTargetBlock(null, 50).getLocation();
        
        boolean success = abilityManager.executeRealityManipulation(player, target, manipulationType);
        if (!success) {
            player.sendMessage("§c§lReality manipulation failed! Check requirements and try again.");
        }
    }
    
    private void handleRealmCreation(Player player, String realmName, String sizeStr) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence locked! Achieve Divine Convergence first.");
            return;
        }
        
        try {
            int size = Integer.parseInt(sizeStr);
            if (size < 10 || size > 100) {
                player.sendMessage("§c§lInvalid size! Must be between 10 and 100.");
                return;
            }
            
            boolean success = abilityManager.executeRealmCreation(player, realmName, size);
            if (!success) {
                player.sendMessage("§c§lRealm creation failed! Check requirements and try again.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c§lInvalid size! Must be a number.");
        }
    }
    
    private void handleLifeCreation(Player player, String lifeType) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence locked! Achieve Divine Convergence first.");
            return;
        }
        
        org.bukkit.Location target = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
        
        boolean success = abilityManager.executeLifeCreation(player, target, lifeType);
        if (!success) {
            player.sendMessage("§c§lLife creation failed! Check requirements and try again.");
        }
    }
    
    private void handleDimensionalTravel(Player player, String dimension) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence locked! Achieve Divine Convergence first.");
            return;
        }
        
        boolean success = abilityManager.executeDimensionalTravel(player, dimension);
        if (!success) {
            player.sendMessage("§c§lDimensional travel failed! Check requirements and try again.");
        }
    }
    
    private void showRealityPoints(Player player) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lTranscendence locked! Achieve Divine Convergence first.");
            return;
        }
        
        int points = abilityManager.getRealityPoints(player);
        Set<String> abilities = abilityManager.getPlayerAbilities(player);
        
        player.sendMessage("§d§l=== REALITY POINTS ===§r");
        player.sendMessage("§7Current Points: §f" + points);
        player.sendMessage("§7Active Abilities: §f" + abilities.size());
        player.sendMessage("");
        player.sendMessage("§d§lActive Abilities:§r");
        for (String ability : abilities) {
            player.sendMessage("§7• §d" + ability.replace("_", " "));
        }
        player.sendMessage("");
        player.sendMessage("§7Reality points are consumed when using transcendence abilities.");
        player.sendMessage("§7Points regenerate over time and can be earned through achievements.");
    }
    
    private void completeChallenge(Player player, String challengeId) {
        if (!transcendenceManager.canAccessTranscendence(player)) {
            player.sendMessage("§c§lPlayer must achieve Divine Convergence first!");
            return;
        }
        
        transcendenceManager.completeChallenge(player, challengeId);
        player.sendMessage("§a§lAdmin: §r§aCompleted transcendence challenge: " + challengeId);
    }
    
    private void showTranscendenceHelp(Player player) {
        player.sendMessage("§d§l=== TRANSCENDENCE SYSTEM ===§r");
        player.sendMessage("§7The ultimate progression beyond Divine Convergence");
        player.sendMessage("");
        player.sendMessage("§e§lCommands:");
        player.sendMessage("§7/transcendence status - Show your transcendence progress");
        player.sendMessage("§7/transcendence challenges - View available challenges");
        player.sendMessage("§7/transcendence abilities - See unlocked abilities");
        player.sendMessage("§7/transcendence reality <type> <target> - Manipulate reality");
        player.sendMessage("§7/transcendence realm <name> <size> - Create pocket realm");
        player.sendMessage("§7/transcendence life <type> - Create life");
        player.sendMessage("§7/transcendence travel <dimension> - Travel dimensions");
        player.sendMessage("§7/transcendence points - Show reality points");
        player.sendMessage("§7/transcendence help - Show this help");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/transcendence complete <challenge_id> - Complete a challenge");
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lTranscendence Levels:");
        player.sendMessage("§7• §dReality Shaper §7- Manipulate the fabric of existence");
        player.sendMessage("§7• §dCosmic Architect §7- Design and create new realms");
        player.sendMessage("§7• §dDimensional Sovereign §7- Rule over multiple dimensions");
        player.sendMessage("§7• §dUniversal Deity §7- Command the forces of creation");
        player.sendMessage("");
        player.sendMessage("§7§lRequirement: §r§7Must have achieved §5§lDivine Convergence§r§7 first!");
    }
    
    private TranscendenceLevel getNextTranscendenceLevel(TranscendenceLevel current) {
        TranscendenceLevel[] levels = TranscendenceLevel.values();
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == current) {
                return levels[i + 1];
            }
        }
        return null;
    }
    
    private String getTranscendenceLevelColor(TranscendenceLevel level) {
        switch (level) {
            case REALITY_SHAPER:
                return "§d";
            case COSMIC_ARCHITECT:
                return "§5";
            case DIMENSIONAL_SOVEREIGN:
                return "§9";
            case UNIVERSAL_DEITY:
                return "§b";
            default:
                return "§7";
        }
    }
}