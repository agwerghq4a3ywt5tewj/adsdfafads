package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.TutorialManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final TutorialManager tutorialManager;
    
    public TutorialCommand(MinecraftPlugin plugin, TutorialManager tutorialManager) {
        this.plugin = plugin;
        this.tutorialManager = tutorialManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.tutorial")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showTutorialHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                tutorialManager.startTutorial(player);
                player.sendMessage("§a§lTutorial started! §r§aFollow the instructions to learn the Testament System.");
                break;
                
            case "skip":
                tutorialManager.skipTutorial(player);
                break;
                
            case "reset":
                tutorialManager.resetTutorial(player);
                break;
                
            case "progress":
                String progress = tutorialManager.getTutorialProgress(player);
                player.sendMessage(progress);
                break;
                
            case "status":
                TutorialManager.TutorialStep currentStep = tutorialManager.getCurrentStep(player);
                boolean completed = tutorialManager.isTutorialCompleted(player);
                
                player.sendMessage("§6§l=== TUTORIAL STATUS ===§r");
                player.sendMessage("§7Current Step: §f" + currentStep.name());
                player.sendMessage("§7Completed: §f" + (completed ? "§aYes" : "§cNo"));
                
                if (!completed) {
                    player.sendMessage("§7Use §f/tutorial progress§7 for detailed progress.");
                }
                break;
                
            default:
                showTutorialHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tutorial subcommands
            List<String> subcommands = Arrays.asList("start", "skip", "reset", "progress", "status");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        }
        
        return completions;
    }
    
    private void showTutorialHelp(Player player) {
        player.sendMessage("§6§l=== TUTORIAL COMMANDS ===§r");
        player.sendMessage("§7/tutorial start - Start the tutorial");
        player.sendMessage("§7/tutorial skip - Skip the current tutorial");
        player.sendMessage("§7/tutorial reset - Reset tutorial progress");
        player.sendMessage("§7/tutorial progress - Show detailed progress");
        player.sendMessage("§7/tutorial status - Show current status");
        player.sendMessage("");
        player.sendMessage("§7The tutorial will guide you through:");
        player.sendMessage("§7• Finding divine fragments");
        player.sendMessage("§7• Using the Godlex system");
        player.sendMessage("§7• Building altars");
        player.sendMessage("§7• Completing testaments");
        player.sendMessage("§7• Understanding divine powers");
    }
}