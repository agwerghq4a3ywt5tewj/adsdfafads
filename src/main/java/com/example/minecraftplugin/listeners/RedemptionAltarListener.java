package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.items.KeyToRedemption;
import com.example.minecraftplugin.items.ShardOfAtonement;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles interactions with the Redemption Altar for combining Shards of Atonement
 */
public class RedemptionAltarListener implements Listener {
    
    private final MinecraftPlugin plugin;
    
    public RedemptionAltarListener(MinecraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click on blocks
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        
        if (clickedBlock == null) {
            return;
        }
        
        // Check if the clicked block is a Redemption Altar
        Material altarMaterial = getAltarMaterial();
        if (clickedBlock.getType() != altarMaterial) {
            return;
        }
        
        // Cancel the event to prevent other interactions (like opening beacon GUI)
        event.setCancelled(true);
        
        // Check if player has all 7 shards
        ItemStack[] inventory = player.getInventory().getContents();
        if (!ShardOfAtonement.hasAllShards(inventory)) {
            // Show what shards the player is missing
            showMissingShards(player, inventory);
            return;
        }
        
        // Remove all shards from inventory
        if (!ShardOfAtonement.removeAllShards(inventory)) {
            player.sendMessage("§c§lError: §r§cCould not remove shards from inventory!");
            return;
        }
        
        // Update the player's inventory
        player.getInventory().setContents(inventory);
        
        // Create and give the Key to Redemption
        ItemStack key = KeyToRedemption.createKey();
        player.getInventory().addItem(key);
        
        // Create dramatic effects
        createForgeEffects(player, clickedBlock);
        
        // Send completion messages
        player.sendMessage("§b§l✦ KEY TO REDEMPTION FORGED! ✦");
        player.sendMessage("§7The 7 Shards of Atonement have been combined!");
        player.sendMessage("§7You now possess the power to free Prisoners of the Void.");
        player.sendMessage("§7Right-click on a prisoner to use the key.");
        
        // Broadcast to server if configured
        if (plugin.getConfig().getBoolean("testament.lives_system.redemption.announce_key_creation", true)) {
            plugin.getServer().broadcastMessage("§b§l" + player.getName() + " §r§bhas forged a Key to Redemption!");
        }
        
        // Log the event
        plugin.getLogger().info(player.getName() + " forged a Key to Redemption at " + 
                               clickedBlock.getWorld().getName() + " " + 
                               clickedBlock.getX() + "," + clickedBlock.getY() + "," + clickedBlock.getZ());
    }
    
    /**
     * Get the material used for the Redemption Altar from config
     */
    private Material getAltarMaterial() {
        String materialName = plugin.getConfig().getString("testament.lives_system.redemption.altar_material", "BEACON");
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid altar material in config: " + materialName + ". Using BEACON as default.");
            return Material.BEACON;
        }
    }
    
    /**
     * Show the player which shards they are missing
     */
    private void showMissingShards(Player player, ItemStack[] inventory) {
        boolean[] foundShards = new boolean[ShardOfAtonement.getTotalShards()];
        
        // Check which shards the player has
        for (ItemStack item : inventory) {
            if (item == null) continue;
            
            int shardNumber = ShardOfAtonement.getShardNumber(item);
            if (shardNumber != -1) {
                foundShards[shardNumber - 1] = true;
            }
        }
        
        // Count found and missing shards
        int foundCount = 0;
        for (boolean found : foundShards) {
            if (found) foundCount++;
        }
        
        player.sendMessage("§c§l⚡ REDEMPTION ALTAR ⚡");
        player.sendMessage("§7You need all 7 Shards of Atonement to forge the Key to Redemption.");
        player.sendMessage("§7Progress: §f" + foundCount + "/7 §7shards collected");
        player.sendMessage("");
        
        // Show missing shards
        player.sendMessage("§c§lMissing Shards:");
        for (int i = 0; i < foundShards.length; i++) {
            if (!foundShards[i]) {
                player.sendMessage("§7• §c✗ " + ShardOfAtonement.getShardName(i + 1));
            }
        }
        
        if (foundCount > 0) {
            player.sendMessage("");
            player.sendMessage("§a§lCollected Shards:");
            for (int i = 0; i < foundShards.length; i++) {
                if (foundShards[i]) {
                    player.sendMessage("§7• §a✓ " + ShardOfAtonement.getShardName(i + 1));
                }
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§7Continue your quest to find the remaining shards!");
    }
    
    /**
     * Create visual and audio effects for key forging
     */
    private void createForgeEffects(Player player, Block altar) {
        // Play dramatic sounds
        player.playSound(altar.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        player.playSound(altar.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        player.playSound(altar.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
        
        // TODO: Add particle effects
        // TODO: Add lightning strike effect
        // TODO: Add temporary beacon beam effect
        
        player.sendMessage("§5§l⚡ Divine energy surges through the altar! ⚡");
    }
}