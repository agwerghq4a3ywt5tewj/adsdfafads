package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.AscensionLevel;
import com.example.minecraftplugin.managers.GodManager;
import com.example.minecraftplugin.managers.PlayerTitleManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    private final PlayerTitleManager playerTitleManager;
    
    public PlayerListener(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.godManager = plugin.getGodManager();
        this.playerTitleManager = plugin.getPlayerTitleManager();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join event
        String playerName = event.getPlayer().getName();
        
        // Custom join message
        event.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.YELLOW + playerName + ChatColor.GREEN + " joined the server!");
        
        // Welcome message to the player
        event.getPlayer().sendMessage(ChatColor.GOLD + "Welcome to the server, " + playerName + "!");
        event.getPlayer().sendMessage(ChatColor.GRAY + "Type " + ChatColor.WHITE + "/example" + ChatColor.GRAY + " to test the plugin!");
        
        // Log to console
        plugin.getLogger().info(playerName + " joined the server");
        
        // Apply ascension effects if player has any
        AscensionLevel level = godManager.getAscensionLevel(event.getPlayer());
        if (level.hasEffects()) {
            godManager.applyAscensionEffects(event.getPlayer(), level);
        }
        
        // Update player title based on current status
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            playerTitleManager.updatePlayerTitle(event.getPlayer());
        }, 20L); // Delay to ensure data is loaded
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle player quit event
        String playerName = event.getPlayer().getName();
        
        // Custom quit message
        event.setQuitMessage(ChatColor.RED + "- " + ChatColor.YELLOW + playerName + ChatColor.RED + " left the server!");
        
        // Log to console
        plugin.getLogger().info(playerName + " left the server");
        
        // Remove player from ascension effects tracking
        godManager.removePlayerFromAscensionTracking(event.getPlayer().getUniqueId());
    }
}