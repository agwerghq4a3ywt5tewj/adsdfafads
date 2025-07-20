package com.example.minecraftplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.ConvergenceNexus;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.managers.CooldownManager;
import com.example.minecraftplugin.managers.GodManager;

/**
 * Handles divine item interactions and passive effects
 */
public class DivineItemListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    private final CooldownManager cooldownManager;
    
    public DivineItemListener(MinecraftPlugin plugin, GodManager godManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.godManager = godManager;
        this.cooldownManager = cooldownManager;
        
        // Start passive effects task
        startPassiveEffectsTask();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Check if it's a divine item
        DivineItem divineItem = getDivineItemFromStack(item);
        if (divineItem == null) {
            return;
        }

        // Check cooldown
        String abilityName = divineItem.getGodType().name() + "_active";
        if (cooldownManager.isOnAbilityCooldown(player, abilityName, divineItem.getCooldownSeconds())) {
            long remaining = cooldownManager.getRemainingCooldown(player, abilityName, divineItem.getCooldownSeconds());
            player.sendMessage("§c§lAbility on cooldown! §r§cWait §f" + remaining + "§c more seconds.");
            return;
        }

        // Try to use the ability
        boolean abilityUsed = divineItem.onRightClick(player, item);

        if (abilityUsed) {
            // Set cooldown
            cooldownManager.setAbilityCooldown(player, abilityName);

            // Create visual effects
            plugin.getVisualEffectsManager().createAbilityActivationEffect(player, divineItem.getGodType(), abilityName);

            // Cancel the event to prevent other interactions
            event.setCancelled(true);
        }
    }
    
    /**
     * Start a task that applies passive effects for divine items
     */
    private void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    applyPassiveEffectsForPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }
    
    /**
     * Apply passive effects for all divine items a player has
     */
    private void applyPassiveEffectsForPlayer(Player player) {
        // Check main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        DivineItem mainHandItem = getDivineItemFromStack(mainHand);
        if (mainHandItem != null) {
            mainHandItem.applyPassiveEffects(player, mainHand);
        }
        
        // Check off hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        DivineItem offHandItem = getDivineItemFromStack(offHand);
        if (offHandItem != null) {
            offHandItem.applyPassiveEffects(player, offHand);
        }
        
        // Check inventory for divine items (passive effects when carried)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            DivineItem divineItem = getDivineItemFromStack(item);
            if (divineItem != null && divineItem != mainHandItem && divineItem != offHandItem) {
                // Apply weaker passive effects when in inventory (not held)
                divineItem.applyPassiveEffects(player, item);
                
                // Create god aura effects
                if (divineItem.getGodType() != null) {
                    plugin.getVisualEffectsManager().createGodAura(player, divineItem.getGodType());
                }
            }
        }
    }
    
    /**
     * Get the DivineItem instance from an ItemStack
     */
    private DivineItem getDivineItemFromStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        java.util.List<String> lore = meta.hasLore() ? meta.getLore() : null;

        // Check all registered divine items
        for (GodType god : GodType.values()) {
            DivineItem divineItem = godManager.getDivineItem(god);
            if (divineItem != null) {
                // Match by display name and lore
                boolean nameMatch = displayName != null && displayName.equals(divineItem.getDisplayName());
                boolean loreMatch = lore != null && divineItem.getLore() != null && lore.containsAll(divineItem.getLore());
                if (nameMatch && loreMatch) {
                    return divineItem;
                }
            }
        }
        // Check for Convergence Nexus (not tied to a specific god)
        ConvergenceNexus nexus = new ConvergenceNexus();
        if (displayName != null && displayName.equals(nexus.getDisplayName()) && lore != null && nexus.getLore() != null && lore.containsAll(nexus.getLore())) {
            return nexus;
        }
        return null;
    }
}