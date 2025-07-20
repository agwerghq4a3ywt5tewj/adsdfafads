package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Manages server-wide broadcasting system for important events
 */
public class BroadcastManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    public BroadcastManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        logger.info("Broadcast Manager initialized");
    }
    
    /**
     * Broadcast testament completion
     */
    public void broadcastTestamentCompletion(Player player, GodType god) {
        if (!isBroadcastEnabled("testament_completion")) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.testament_completion.message", 
            "§6§l{player} §r§6has completed the Testament of the {god}!");
        
        message = message.replace("{player}", player.getName())
                        .replace("{god}", god.getDisplayName());
        
        // Broadcast with coordinates if enabled
        if (plugin.getConfig().getBoolean("broadcasts.testament_completion.include_coordinates", true)) {
            Location loc = player.getLocation();
            String coords = " §7at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            message += coords;
        }
        
        Bukkit.broadcastMessage(message);
        
        // Play sound for all players if enabled
        if (plugin.getConfig().getBoolean("broadcasts.testament_completion.play_sound", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
            }
        }
        
        logger.info("Broadcasted testament completion: " + player.getName() + " - " + god.getDisplayName());
    }
    
    /**
     * Broadcast Divine Convergence achievement
     */
    public void broadcastConvergence(Player player) {
        if (!isBroadcastEnabled("convergence")) {
            return;
        }
        
        String[] messages = plugin.getConfig().getStringList("broadcasts.convergence.messages").toArray(new String[0]);
        if (messages.length == 0) {
            // Default messages
            messages = new String[] {
                "§5§l★═══════════════════════════════════════════════════════════════★",
                "§5§l              {player} HAS ACHIEVED DIVINE CONVERGENCE!",
                "§5§l★═══════════════════════════════════════════════════════════════★",
                "§7{player} has mastered all twelve gods and become the",
                "§5§lMaster of All Divinity§r§7! They have transcended mortality itself!",
                "§5§l★═══════════════════════════════════════════════════════════════★"
            };
        }
        
        // Broadcast each message
        for (String message : messages) {
            message = message.replace("{player}", player.getName().toUpperCase());
            Bukkit.broadcastMessage(message);
        }
        
        // Play epic sound for all players
        if (plugin.getConfig().getBoolean("broadcasts.convergence.play_sound", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.5f);
            }
        }
        
        logger.info("Broadcasted Divine Convergence: " + player.getName());
    }
    
    /**
     * Broadcast altar generation
     */
    public void broadcastAltarGeneration(Location location, GodType god, boolean isNatural) {
        String broadcastType = isNatural ? "altar_natural_generation" : "altar_manual_generation";
        
        if (!isBroadcastEnabled(broadcastType)) {
            return;
        }
        
        String message;
        if (isNatural) {
            message = plugin.getConfig().getString("broadcasts.altar_natural_generation.message",
                "§6§lA {god} altar has manifested naturally in the world!");
        } else {
            message = plugin.getConfig().getString("broadcasts.altar_manual_generation.message",
                "§6§lAn altar for the {god} has been constructed!");
        }
        
        message = message.replace("{god}", god.getDisplayName());
        
        // Add coordinates if enabled
        if (plugin.getConfig().getBoolean("broadcasts." + broadcastType + ".include_coordinates", true)) {
            String coords = " §7at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + 
                          " in " + location.getWorld().getName();
            message += coords;
        }
        
        Bukkit.broadcastMessage(message);
        
        logger.info("Broadcasted altar generation: " + god.getDisplayName() + " at " + 
                   location.getWorld().getName() + " " + location.getBlockX() + "," + 
                   location.getBlockY() + "," + location.getBlockZ());
    }
    
    /**
     * Broadcast fragment discovery
     */
    public void broadcastFragmentDiscovery(Player player, GodType god, int fragmentNumber, String source) {
        if (!isBroadcastEnabled("fragment_discovery")) {
            return;
        }
        
        // Only broadcast rare discoveries or milestones
        boolean shouldBroadcast = false;
        
        // Check if this completes the testament
        if (plugin.getGodManager().hasAllFragments(player, god)) {
            shouldBroadcast = true;
        }
        
        // Check if it's a rare source
        if ("mob".equals(source) && Math.random() < 0.1) { // 10% chance for mob drops
            shouldBroadcast = true;
        }
        
        if (!shouldBroadcast) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.fragment_discovery.message",
            "§6§l{player} §r§6has discovered a fragment of the {god}!");
        
        message = message.replace("{player}", player.getName())
                        .replace("{god}", god.getDisplayName())
                        .replace("{fragment}", String.valueOf(fragmentNumber))
                        .replace("{source}", source);
        
        Bukkit.broadcastMessage(message);
        
        logger.info("Broadcasted fragment discovery: " + player.getName() + " - " + god.getDisplayName() + 
                   " fragment " + fragmentNumber + " from " + source);
    }
    
    /**
     * Broadcast bounty events
     */
    public void broadcastBountyPlaced(String targetName, int amount, String currency) {
        if (!isBroadcastEnabled("bounty_placed")) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.bounty_placed.message",
            "§6§l💰 BOUNTY ALERT! 💰 A bounty of {amount} {currency} has been placed on {target}!");
        
        message = message.replace("{target}", targetName)
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{currency}", currency);
        
        Bukkit.broadcastMessage(message);
    }
    
    public void broadcastBountyClaimed(String killerName, String victimName, int amount, String currency) {
        if (!isBroadcastEnabled("bounty_claimed")) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.bounty_claimed.message",
            "§6§l💰 BOUNTY CLAIMED! 💰 {killer} has claimed the bounty on {victim} for {amount} {currency}!");
        
        message = message.replace("{killer}", killerName)
                        .replace("{victim}", victimName)
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{currency}", currency);
        
        Bukkit.broadcastMessage(message);
    }
    
    /**
     * Broadcast boss events
     */
    public void broadcastBossSpawn(String bossName, Location location) {
        if (!isBroadcastEnabled("boss_spawn")) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.boss_spawn.message",
            "§c§l⚔ {boss} has awakened! ⚔");
        
        message = message.replace("{boss}", bossName);
        
        // Add coordinates if enabled
        if (plugin.getConfig().getBoolean("broadcasts.boss_spawn.include_coordinates", true)) {
            String coords = " §7at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
            message += coords;
        }
        
        Bukkit.broadcastMessage(message);
        
        // Play warning sound
        if (plugin.getConfig().getBoolean("broadcasts.boss_spawn.play_sound", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
            }
        }
    }
    
    public void broadcastBossDefeat(String bossName, String killerName) {
        if (!isBroadcastEnabled("boss_defeat")) {
            return;
        }
        
        String message = plugin.getConfig().getString("broadcasts.boss_defeat.message",
            "§a§l⚔ {boss} has been defeated by {killer}! ⚔");
        
        message = message.replace("{boss}", bossName)
                        .replace("{killer}", killerName);
        
        Bukkit.broadcastMessage(message);
        
        // Play victory sound
        if (plugin.getConfig().getBoolean("broadcasts.boss_defeat.play_sound", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            }
        }
    }
    
    /**
     * Check if a specific broadcast type is enabled
     */
    private boolean isBroadcastEnabled(String broadcastType) {
        return plugin.getConfig().getBoolean("broadcasts." + broadcastType + ".enabled", true);
    }
    
    /**
     * Send targeted broadcast to players in range
     */
    public void broadcastInRange(Location center, double range, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(center.getWorld()) && 
                player.getLocation().distance(center) <= range) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Send broadcast with title overlay
     */
    public void broadcastWithTitle(String chatMessage, String title, String subtitle) {
        Bukkit.broadcastMessage(chatMessage);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 20, 60, 20);
        }
    }
}