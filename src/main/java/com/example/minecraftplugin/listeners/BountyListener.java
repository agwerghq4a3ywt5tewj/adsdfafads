package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.BountyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles bounty-related events
 */
public class BountyListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final BountyManager bountyManager;
    
    public BountyListener(MinecraftPlugin plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        
        // Check if killer is a player
        if (!(victim.getKiller() instanceof Player)) {
            return;
        }
        
        Player killer = victim.getKiller();
        
        // Check if victim had a bounty
        BountyManager.BountyInfo bounty = bountyManager.getBounty(victim);
        if (bounty != null) {
            // Claim the bounty
            bountyManager.claimBounty(killer, victim);
        }
    }
}