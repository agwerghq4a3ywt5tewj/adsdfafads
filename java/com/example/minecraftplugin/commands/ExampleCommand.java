package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExampleCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    
    public ExampleCommand(MinecraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.example")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        player.sendMessage(ChatColor.GREEN + "Hello from the Testament System plugin!");
        player.sendMessage(ChatColor.YELLOW + "This is an example command.");
        
        // Log to console
        plugin.getLogger().info(player.getName() + " used the example command");
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Example subcommands
            List<String> subcommands = Arrays.asList("help", "info", "test");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        }
        
        return completions;
    }
}