package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.FragmentItem;
import com.example.minecraftplugin.managers.CooldownManager;
import com.example.minecraftplugin.managers.GodManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles fragment spawning from chests and dangerous mobs
 */
public class FragmentListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    private final CooldownManager cooldownManager;
    private final Random random;
    
    // Define dangerous mobs that can drop fragments
    private final List<EntityType> dangerousMobs = Arrays.asList(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
        EntityType.ENDERMAN, EntityType.WITCH, EntityType.BLAZE, EntityType.GHAST,
        EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE, EntityType.HOGLIN,
        EntityType.ZOGLIN, EntityType.PHANTOM, EntityType.DROWNED, EntityType.HUSK,
        EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.EVOKER,
        EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.GUARDIAN,
        EntityType.ELDER_GUARDIAN, EntityType.WARDEN
    );
    
    // Define chest types that can contain fragments
    private final List<Material> chestTypes = Arrays.asList(
        Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
        Material.BARREL, Material.SHULKER_BOX
    );
    
    public FragmentListener(MinecraftPlugin plugin, GodManager godManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.godManager = godManager;
        this.cooldownManager = cooldownManager;
        this.random = new Random();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Check if it's a chest type
        if (!chestTypes.contains(blockType)) {
            return;
        }
        
        // Get configuration values
        double chestSpawnChance = plugin.getPerformanceManager().getCachedChestSpawnChance();
        int chestCooldownHours = plugin.getPerformanceManager().getCachedChestCooldownHours();
        
        // Check cooldown
        if (cooldownManager.isOnChestFragmentCooldown(player, chestCooldownHours)) {
            return;
        }
        
        // Check spawn chance
        if (random.nextDouble() > chestSpawnChance) {
            return;
        }
        
        // Select random god and missing fragment
        GodType selectedGod = getRandomGod();
        int missingFragment = godManager.getMissingFragment(player, selectedGod);
        
        if (missingFragment == -1) {
            // Player has all fragments for this god, try another
            selectedGod = getRandomIncompleteGod(player);
            if (selectedGod != null) {
                missingFragment = godManager.getMissingFragment(player, selectedGod);
            }
        }
        
        if (selectedGod != null && missingFragment != -1) {
            spawnFragment(player, selectedGod, missingFragment, "chest");
            cooldownManager.setChestFragmentCooldown(player);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if killer is a player
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        
        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntity().getType();
        
        // Check if it's a dangerous mob
        if (!dangerousMobs.contains(entityType)) {
            return;
        }
        
        // Get configuration values
        double mobDropChance = plugin.getPerformanceManager().getCachedMobDropChance();
        int mobCooldownHours = plugin.getPerformanceManager().getCachedMobCooldownHours();
        
        // Check cooldown
        if (cooldownManager.isOnMobFragmentCooldown(player, mobCooldownHours)) {
            return;
        }
        
        // Check drop chance
        if (random.nextDouble() > mobDropChance) {
            return;
        }
        
        // Select random god and missing fragment
        GodType selectedGod = getRandomGod();
        int missingFragment = godManager.getMissingFragment(player, selectedGod);
        
        if (missingFragment == -1) {
            // Player has all fragments for this god, try another
            selectedGod = getRandomIncompleteGod(player);
            if (selectedGod != null) {
                missingFragment = godManager.getMissingFragment(player, selectedGod);
            }
        }
        
        if (selectedGod != null && missingFragment != -1) {
            spawnFragment(player, selectedGod, missingFragment, "mob");
            cooldownManager.setMobFragmentCooldown(player);
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        // Check if it's a fragment
        FragmentItem.FragmentInfo fragmentInfo = FragmentItem.getFragmentInfo(item);
        if (fragmentInfo == null) {
            return;
        }
        
        // Add fragment to player's collection
        godManager.addFragment(player, fragmentInfo.getGodType(), fragmentInfo.getFragmentNumber());
        
        // Check if testament is complete
        if (godManager.hasAllFragments(player, fragmentInfo.getGodType())) {
            player.sendMessage("§6§l✦ TESTAMENT COMPLETE! ✦");
            player.sendMessage("§7You have collected all fragments for the " + 
                             fragmentInfo.getGodType().getDisplayName() + "!");
            player.sendMessage("§7Find an altar to complete your testament and receive divine power!");
            player.sendMessage("§7Altar center block: §f" + fragmentInfo.getGodType().getAltarCenterBlock().name());
        }
    }
    
    /**
     * Spawn a fragment for a player
     */
    private void spawnFragment(Player player, GodType godType, int fragmentNumber, String source) {
        FragmentItem fragment = new FragmentItem(godType, fragmentNumber);
        ItemStack fragmentItem = fragment.createItemStack();
        
        // Drop the item at player's location
        player.getWorld().dropItemNaturally(player.getLocation(), fragmentItem);
        
        // Notify player
        player.sendMessage("§6§l✦ DIVINE FRAGMENT DISCOVERED! ✦");
        player.sendMessage("§7A fragment of the " + godType.getDisplayName() + " has appeared!");
        player.sendMessage("§7Source: §f" + source);
        
        // Broadcast fragment discovery if significant
        plugin.getBroadcastManager().broadcastFragmentDiscovery(player, godType, fragmentNumber, source);
        
        // Log the event
        plugin.getLogger().info(player.getName() + " discovered fragment " + fragmentNumber + 
                               " of " + godType.getDisplayName() + " from " + source);
    }
    
    /**
     * Get a random god type
     */
    private GodType getRandomGod() {
        GodType[] gods = GodType.values();
        return gods[random.nextInt(gods.length)];
    }
    
    /**
     * Get a random god that the player hasn't completed
     */
    private GodType getRandomIncompleteGod(Player player) {
        List<GodType> incompleteGods = Arrays.asList(GodType.values());
        incompleteGods.removeIf(god -> godManager.hasAllFragments(player, god));
        
        if (incompleteGods.isEmpty()) {
            return null;
        }
        
        return incompleteGods.get(random.nextInt(incompleteGods.size()));
    }
}