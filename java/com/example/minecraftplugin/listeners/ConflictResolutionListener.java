package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.items.ConvergenceNexus;
import com.example.minecraftplugin.managers.GodManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles divine conflict resolution when players obtain conflicting divine items
 */
public class ConflictResolutionListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    
    public ConflictResolutionListener(MinecraftPlugin plugin, GodManager godManager) {
        this.plugin = plugin;
        this.godManager = godManager;
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack pickedUpItem = event.getItem().getItemStack();
        
        // Check if the picked up item is a divine item
        DivineItem pickedUpDivineItem = getDivineItemFromStack(pickedUpItem);
        if (pickedUpDivineItem == null) {
            return;
        }
        
        // Check for conflicts with existing inventory items
        checkAndResolveConflicts(player, pickedUpDivineItem, pickedUpItem);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null) {
            return;
        }
        
        // Check if the clicked item is a divine item
        DivineItem clickedDivineItem = getDivineItemFromStack(clickedItem);
        if (clickedDivineItem == null) {
            return;
        }
        
        // Delay the conflict check to ensure inventory state is updated
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            checkAndResolveConflicts(player, clickedDivineItem, clickedItem);
        }, 1L);
    }
    
    /**
     * Check for divine conflicts and resolve them
     */
    private void checkAndResolveConflicts(Player player, DivineItem newDivineItem, ItemStack newItem) {
        if (!plugin.getConfig().getBoolean("testament.conflicts.enabled", true)) {
            return;
        }
        
        GodType newGod = newDivineItem.getGodType();
        List<ItemStack> conflictingItems = new ArrayList<>();
        
        // Check all items in player's inventory for conflicts
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.equals(newItem)) {
                continue;
            }
            
            DivineItem existingDivineItem = getDivineItemFromStack(item);
            if (existingDivineItem == null) {
                continue;
            }
            
            GodType existingGod = existingDivineItem.getGodType();
            
            // Check if the gods conflict
            if (newGod.conflictsWith(existingGod)) {
                conflictingItems.add(item);
            }
        }
        
        // Resolve conflicts
        if (!conflictingItems.isEmpty()) {
            resolveConflicts(player, newGod, newItem, conflictingItems);
        }
    }
    
    /**
     * Resolve divine conflicts by removing conflicting items
     */
    private void resolveConflicts(Player player, GodType newGod, ItemStack newItem, List<ItemStack> conflictingItems) {
        boolean removeConflictingItems = plugin.getConfig().getBoolean("testament.conflicts.remove_conflicting_items", true);
        boolean announceConflicts = plugin.getConfig().getBoolean("testament.conflicts.announce_conflicts", true);
        
        // Notify player of the conflict
        player.sendMessage("§c§l⚡ DIVINE CONFLICT DETECTED! ⚡");
        player.sendMessage("§7The " + newGod.getDisplayName() + " conflicts with other divine powers!");
        
        for (ItemStack conflictingItem : conflictingItems) {
            DivineItem conflictingDivineItem = getDivineItemFromStack(conflictingItem);
            if (conflictingDivineItem == null) {
                continue;
            }
            
            GodType conflictingGod = conflictingDivineItem.getGodType();
            
            player.sendMessage("§c§l⚠ Conflict: §r§c" + newGod.getDisplayName() + " vs " + conflictingGod.getDisplayName());
            
            if (removeConflictingItems) {
                // Remove the conflicting item
                player.getInventory().remove(conflictingItem);
                
                // Call the onLost method for the conflicting item
                conflictingDivineItem.onLost(player, conflictingItem);
                
                player.sendMessage("§c§lRemoved: §r§c" + conflictingDivineItem.getDisplayName());
                
                // Drop the item at player's location
                player.getWorld().dropItemNaturally(player.getLocation(), conflictingItem);
                player.sendMessage("§7The conflicting divine item has been dropped at your location.");
            } else {
                player.sendMessage("§e§lWarning: §r§eConflicting divine powers detected but not removed.");
                player.sendMessage("§7Consider choosing which god to follow.");
            }
        }
        
        // Play dramatic conflict sound
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.5f);
        
        // Server announcement if enabled
        if (announceConflicts) {
            String conflictMessage = "§c§l" + player.getName() + " §r§chas triggered a divine conflict with " + newGod.getDisplayName() + "!";
            plugin.getServer().broadcastMessage(conflictMessage);
        }
        
        // Log the conflict
        plugin.getLogger().info("Divine conflict resolved for " + player.getName() + 
                               ": " + newGod.getDisplayName() + " conflicted with " + conflictingItems.size() + " items");
    }
    
    /**
     * Get the DivineItem instance from an ItemStack
     */
    private DivineItem getDivineItemFromStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        // Check all registered divine items
        for (GodType god : GodType.values()) {
            DivineItem divineItem = godManager.getDivineItem(god);
            if (divineItem != null && divineItem.isDivineItem(item)) {
                return divineItem;
            }
        }
        
        // Check for Convergence Nexus (not tied to a specific god)
        ConvergenceNexus nexus = new ConvergenceNexus();
        if (nexus.isDivineItem(item)) {
            return nexus;
        }
        
        return null;
    }
}