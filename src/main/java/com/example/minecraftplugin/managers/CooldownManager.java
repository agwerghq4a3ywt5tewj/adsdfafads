package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Manages cooldowns for various plugin features
 */
public class CooldownManager {
    
    private final MinecraftPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    public CooldownManager(MinecraftPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }
    
    /**
     * Check if a player is on cooldown for chest fragments
     */
    public boolean isOnChestFragmentCooldown(Player player, int cooldownHours) {
        long lastTime = playerDataManager.getLastChestFragmentTime(player);
        
        if (lastTime == 0) {
            return false;
        }
        
        long cooldownMillis = cooldownHours * 60 * 60 * 1000L;
        return (System.currentTimeMillis() - lastTime) < cooldownMillis;
    }
    
    /**
     * Set chest fragment cooldown for a player
     */
    public void setChestFragmentCooldown(Player player) {
        playerDataManager.setLastChestFragmentTime(player, System.currentTimeMillis());
    }
    
    /**
     * Check if a player is on cooldown for mob fragments
     */
    public boolean isOnMobFragmentCooldown(Player player, int cooldownHours) {
        long lastTime = playerDataManager.getLastMobFragmentTime(player);
        
        if (lastTime == 0) {
            return false;
        }
        
        long cooldownMillis = cooldownHours * 60 * 60 * 1000L;
        return (System.currentTimeMillis() - lastTime) < cooldownMillis;
    }
    
    /**
     * Set mob fragment cooldown for a player
     */
    public void setMobFragmentCooldown(Player player) {
        playerDataManager.setLastMobFragmentTime(player, System.currentTimeMillis());
    }
    
    /**
     * Check if a player is on cooldown for a specific ability
     */
    public boolean isOnAbilityCooldown(Player player, String abilityName, int cooldownSeconds) {
        long lastTime = playerDataManager.getAbilityCooldownTime(player, abilityName);
        
        if (lastTime == 0) {
            return false;
        }
        
        long cooldownMillis = cooldownSeconds * 1000L;
        return (System.currentTimeMillis() - lastTime) < cooldownMillis;
    }
    
    /**
     * Set ability cooldown for a player
     */
    public void setAbilityCooldown(Player player, String abilityName) {
        playerDataManager.setAbilityCooldownTime(player, abilityName, System.currentTimeMillis());
    }
    
    /**
     * Get remaining cooldown time in seconds
     */
    public long getRemainingCooldown(Player player, String abilityName, int cooldownSeconds) {
        long lastTime = playerDataManager.getAbilityCooldownTime(player, abilityName);
        
        if (lastTime == 0) {
            return 0;
        }
        
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsed = System.currentTimeMillis() - lastTime;
        long remaining = cooldownMillis - elapsed;
        
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * Clear all cooldowns for a player (useful for testing or admin commands)
     */
    public void clearPlayerCooldowns(Player player) {
        playerDataManager.clearAllCooldowns(player);
    }
}