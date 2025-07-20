package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles the Testament Lives System death tracking and void prisoner mechanics
 */
public class DeathListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    public DeathListener(MinecraftPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Increment death count
        int newDeathCount = playerDataManager.incrementDeathCount(player);
        int deathThreshold = plugin.getConfig().getInt("testament.lives_system.death_threshold", 3);
        
        plugin.getLogger().info(player.getName() + " died. Death count: " + newDeathCount + "/" + deathThreshold);
        
        // Check if player should become prisoner of void
        if (newDeathCount >= deathThreshold && !playerDataManager.isPrisonerOfTheVoid(player)) {
            // Mark as prisoner of void
            playerDataManager.setPrisonerOfTheVoid(player, true);
            
            // Notify player
            player.sendMessage("§4§l⚡ THE VOID CLAIMS YOU! ⚡");
            player.sendMessage("§c§lYou have died too many times and are now a Prisoner of the Void!");
            player.sendMessage("§7Only another player with a Key to Redemption can free you.");
            
            // Broadcast to server
            plugin.getServer().broadcastMessage("§c§l" + player.getName() + " §r§chas become a Prisoner of the Void!");
            
            // Play ominous sound
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.8f);
            
            plugin.getLogger().info(player.getName() + " became a Prisoner of the Void (deaths: " + newDeathCount + ")");
        } else if (newDeathCount < deathThreshold) {
            // Warn player about approaching threshold
            int remaining = deathThreshold - newDeathCount;
            player.sendMessage("§e§lWarning: §r§eYou have §c" + remaining + "§e deaths remaining before the Void claims you!");
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is prisoner of void
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            // Set respawn location to void coordinates
            Location voidLocation = getVoidLocation();
            if (voidLocation != null) {
                event.setRespawnLocation(voidLocation);
            }
            
            // Schedule void effects to be applied after respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                applyVoidPrisonerEffects(player);
            }, 5L); // 5 ticks delay to ensure player has respawned
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if joining player is prisoner of void
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            // Teleport to void location
            Location voidLocation = getVoidLocation();
            if (voidLocation != null) {
                player.teleport(voidLocation);
            }
            
            // Apply void effects
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                applyVoidPrisonerEffects(player);
            }, 10L); // 10 ticks delay to ensure player has fully loaded
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Prevent void prisoners from breaking blocks
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            event.setCancelled(true);
            player.sendMessage("§c§lPrisoner of the Void: §r§cYou cannot break blocks while imprisoned.");
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Prevent void prisoners from placing blocks
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            event.setCancelled(true);
            player.sendMessage("§c§lPrisoner of the Void: §r§cYou cannot place blocks while imprisoned.");
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Prevent void prisoners from most interactions (except Key to Redemption usage by others)
        if (playerDataManager.isPrisonerOfTheVoid(player)) {
            // Allow right-clicking air/blocks for Key to Redemption targeting
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || 
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                return; // Allow this for Key to Redemption mechanics
            }
            
            event.setCancelled(true);
            player.sendMessage("§c§lPrisoner of the Void: §r§cYou cannot interact with the world while imprisoned.");
        }
    }
    
    /**
     * Get the void location from config
     */
    private Location getVoidLocation() {
        try {
            String worldName = plugin.getConfig().getString("testament.lives_system.void_world_name", "world");
            World world = plugin.getServer().getWorld(worldName);
            
            if (world == null) {
                plugin.getLogger().warning("Void world '" + worldName + "' not found! Using default world.");
                world = plugin.getServer().getWorlds().get(0); // Use first available world
            }
            
            int x = plugin.getConfig().getInt("testament.lives_system.void_teleport_coords.x", 0);
            int y = plugin.getConfig().getInt("testament.lives_system.void_teleport_coords.y", -60);
            int z = plugin.getConfig().getInt("testament.lives_system.void_teleport_coords.z", 0);
            
            return new Location(world, x + 0.5, y, z + 0.5); // Center of block
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting void location: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply effects to void prisoners
     */
    private void applyVoidPrisonerEffects(Player player) {
        if (!playerDataManager.isPrisonerOfTheVoid(player)) {
            return;
        }
        
        // Display persistent message
        String prisonerMessage = plugin.getConfig().getString("testament.lives_system.prisoner_message", 
            "§c§lYou are a Prisoner of the Void. Only redemption can free you.");
        player.sendTitle("§4§lPRISONER OF THE VOID", prisonerMessage, 10, 100, 20);
        
        // Apply debuff effects (long duration)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 2, false, false));
        
        // Play ambient void sound
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.3f, 0.5f);
        
        plugin.getLogger().info("Applied void prisoner effects to " + player.getName());
    }
    
    /**
     * Remove void prisoner effects (called when player is redeemed)
     */
    public void removeVoidPrisonerEffects(Player player) {
        // Remove specific debuff effects
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        
        // Clear title
        player.resetTitle();
        
        plugin.getLogger().info("Removed void prisoner effects from " + player.getName());
    }
}