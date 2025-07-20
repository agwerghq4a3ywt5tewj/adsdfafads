package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.items.KeyToRedemption;
import com.example.minecraftplugin.managers.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the usage of the Key to Redemption to free Prisoners of the Void
 */
public class RedemptionListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    public RedemptionListener(MinecraftPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player redeemer = event.getPlayer();
        
        // Check if the target entity is a player
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        
        Player prisoner = (Player) event.getRightClicked();
        
        // Check if the redeemer is holding a Key to Redemption
        ItemStack heldItem = redeemer.getInventory().getItemInMainHand();
        if (!KeyToRedemption.isKey(heldItem)) {
            return;
        }
        
        // Check if the target player is actually a Prisoner of the Void
        if (!playerDataManager.isPrisonerOfTheVoid(prisoner)) {
            redeemer.sendMessage("§c§lKey to Redemption: §r§c" + prisoner.getName() + " is not a Prisoner of the Void!");
            redeemer.sendMessage("§7This key can only be used on players imprisoned by the Void.");
            return;
        }
        
        // Check if the redeemer is trying to free themselves (shouldn't be possible, but safety check)
        if (redeemer.equals(prisoner)) {
            redeemer.sendMessage("§c§lKey to Redemption: §r§cYou cannot use this key on yourself!");
            return;
        }
        
        // Cancel the event to prevent other interactions
        event.setCancelled(true);
        
        // Perform the redemption
        performRedemption(redeemer, prisoner, heldItem);
    }
    
    /**
     * Perform the actual redemption process
     */
    private void performRedemption(Player redeemer, Player prisoner, ItemStack key) {
        // Remove the key from the redeemer's inventory
        key.setAmount(key.getAmount() - 1);
        if (key.getAmount() <= 0) {
            redeemer.getInventory().setItemInMainHand(null);
        }
        
        // Free the prisoner from the void
        playerDataManager.setPrisonerOfTheVoid(prisoner, false);
        
        // Remove void prisoner effects
        if (plugin.getServer().getPluginManager().getPlugin("MinecraftPlugin") != null) {
            // Get the DeathListener to remove effects
            // This is a bit of a hack, but it works for now
            prisoner.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS);
            prisoner.removePotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS);
            prisoner.removePotionEffect(org.bukkit.potion.PotionEffectType.MINING_FATIGUE);
            prisoner.resetTitle();
        }
        
        // Apply redemption costs to the freed player
        applyRedemptionCosts(prisoner);
        
        // Teleport the freed player to safety
        Location safeLocation = getRedemptionLocation();
        if (safeLocation != null) {
            prisoner.teleport(safeLocation);
        }
        
        // Create dramatic effects
        createRedemptionEffects(redeemer, prisoner);
        
        // Send messages
        sendRedemptionMessages(redeemer, prisoner);
        
        // Broadcast to server if configured
        if (plugin.getConfig().getBoolean("testament.lives_system.redemption.broadcast_redemption", true)) {
            String broadcastMessage = plugin.getConfig().getString("testament.lives_system.redemption.broadcast_message", 
                "§b§l{redeemer} §r§bhas freed §b§l{prisoner} §r§bfrom the Void!");
            broadcastMessage = broadcastMessage.replace("{redeemer}", redeemer.getName())
                                             .replace("{prisoner}", prisoner.getName());
            plugin.getServer().broadcastMessage(broadcastMessage);
        }
        
        // Log the redemption
        plugin.getLogger().info(redeemer.getName() + " used Key to Redemption to free " + prisoner.getName() + " from the Void");
    }
    
    /**
     * Apply costs to the freed player (XP loss, item clearing, etc.)
     */
    private void applyRedemptionCosts(Player prisoner) {
        // XP Loss
        double xpLossPercentage = plugin.getConfig().getDouble("testament.lives_system.redemption.xp_loss_percentage", 0.5);
        if (xpLossPercentage > 0) {
            int currentXP = prisoner.getTotalExperience();
            int xpToRemove = (int) (currentXP * xpLossPercentage);
            prisoner.setTotalExperience(Math.max(0, currentXP - xpToRemove));
            
            if (xpToRemove > 0) {
                prisoner.sendMessage("§c§lRedemption Cost: §r§cLost " + xpToRemove + " experience points.");
            }
        }
        
        // Item Clearing
        boolean clearNonDivineItems = plugin.getConfig().getBoolean("testament.lives_system.redemption.clear_non_divine_items", false);
        if (clearNonDivineItems) {
            clearNonDivineItems(prisoner);
            prisoner.sendMessage("§c§lRedemption Cost: §r§cAll non-divine items have been cleared from your inventory.");
        }
        
        // Reset death count
        playerDataManager.setDeathCount(prisoner, 0);
        
        // Reduce maximum health
        applyReducedMaxHealth(prisoner);
    }
    
    /**
     * Clear all non-divine items from a player's inventory
     */
    private void clearNonDivineItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;
            
            // Check if it's a divine item (has "Divine Item of the" in lore)
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                boolean isDivineItem = false;
                for (String loreLine : item.getItemMeta().getLore()) {
                    if (loreLine.contains("Divine Item of the")) {
                        isDivineItem = true;
                        break;
                    }
                }
                
                // Don't clear divine items, shards, or keys
                if (isDivineItem || KeyToRedemption.isKey(item) || 
                    com.example.minecraftplugin.items.ShardOfAtonement.isShard(item)) {
                    continue;
                }
            }
            
            // Clear the item
            contents[i] = null;
        }
        
        player.getInventory().setContents(contents);
    }
    
    /**
     * Apply reduced maximum health to the freed player
     */
    private void applyReducedMaxHealth(Player player) {
        double reducedMaxHearts = plugin.getConfig().getDouble("testament.lives_system.redemption.reduced_max_hearts", 10.0);
        
        org.bukkit.attribute.AttributeInstance healthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            // Set the new maximum health
            healthAttribute.setBaseValue(reducedMaxHearts);
            
            // If current health exceeds new maximum, reduce it
            if (player.getHealth() > reducedMaxHearts) {
                player.setHealth(reducedMaxHearts);
            }
            
            // Notify the player
            int hearts = (int) (reducedMaxHearts / 2);
            player.sendMessage("§c§lRedemption Cost: §r§cYour maximum health has been reduced to " + hearts + " hearts.");
            player.sendMessage("§7Your body bears the scars of the Void's imprisonment.");
            
            plugin.getLogger().info("Reduced " + player.getName() + "'s max health to " + reducedMaxHearts + " HP (" + hearts + " hearts)");
        }
    }
    
    /**
     * Get the location where freed players should be teleported
     */
    private Location getRedemptionLocation() {
        try {
            String worldName = plugin.getConfig().getString("testament.lives_system.redemption.teleport_world", "world");
            World world = plugin.getServer().getWorld(worldName);
            
            if (world == null) {
                plugin.getLogger().warning("Redemption world '" + worldName + "' not found! Using default world.");
                world = plugin.getServer().getWorlds().get(0);
            }
            
            int x = plugin.getConfig().getInt("testament.lives_system.redemption.teleport_coords.x", 0);
            int y = plugin.getConfig().getInt("testament.lives_system.redemption.teleport_coords.y", 100);
            int z = plugin.getConfig().getInt("testament.lives_system.redemption.teleport_coords.z", 0);
            
            return new Location(world, x + 0.5, y, z + 0.5);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting redemption location: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create visual and audio effects for redemption
     */
    private void createRedemptionEffects(Player redeemer, Player prisoner) {
        // Play sounds for both players
        redeemer.playSound(redeemer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        redeemer.playSound(redeemer.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.0f);
        
        prisoner.playSound(prisoner.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.5f);
        prisoner.playSound(prisoner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        
        // TODO: Add particle effects
        // TODO: Add light beam effect
    }
    
    /**
     * Send redemption messages to both players
     */
    private void sendRedemptionMessages(Player redeemer, Player prisoner) {
        // Messages for the redeemer
        redeemer.sendMessage("§b§l✦ REDEMPTION SUCCESSFUL! ✦");
        redeemer.sendMessage("§7You have freed " + prisoner.getName() + " from the Void!");
        redeemer.sendMessage("§7Your act of compassion has been noted by the divine.");
        
        // Messages for the freed prisoner
        prisoner.sendMessage("§a§l✦ YOU HAVE BEEN FREED! ✦");
        prisoner.sendMessage("§7" + redeemer.getName() + " has used a Key to Redemption to free you!");
        prisoner.sendMessage("§7You are no longer a Prisoner of the Void.");
        prisoner.sendMessage("§7Use this second chance wisely...");
        
        // Show redemption costs
        String costMessage = plugin.getConfig().getString("testament.lives_system.redemption.cost_message", 
            "§e§lRedemption comes with a price. Learn from this experience.");
        prisoner.sendMessage(costMessage);
    }
}