package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.DivineForgeManager;
import com.example.minecraftplugin.items.upgrades.UpgradeMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Handles events related to the Divine Forge system
 */
public class DivineForgeListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final DivineForgeManager forgeManager;
    
    public DivineForgeListener(MinecraftPlugin plugin, DivineForgeManager forgeManager) {
        this.plugin = plugin;
        this.forgeManager = forgeManager;
    }
    
    /**
     * Handle upgrade material drops from special mobs
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        
        // Check for special mob types that drop upgrade materials
        switch (event.getEntity().getType()) {
            case ENDER_DRAGON:
                // Guaranteed transcendent core
                forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.TRANSCENDENT_CORE, 1, 
                    "Ender Dragon defeat");
                // Also give cosmic fragments
                forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.COSMIC_FRAGMENT, 3, 
                    "Ender Dragon defeat");
                break;
                
            case WITHER:
                // Reality shards from wither
                forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.REALITY_SHARD, 2, 
                    "Wither defeat");
                break;
                
            case ELDER_GUARDIAN:
                // Cosmic fragments from elder guardian
                if (Math.random() < 0.7) { // 70% chance
                    forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.COSMIC_FRAGMENT, 1, 
                        "Elder Guardian defeat");
                }
                break;
                
            case WARDEN:
                // Reality shards from warden
                if (Math.random() < 0.8) { // 80% chance
                    forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.REALITY_SHARD, 1, 
                        "Warden defeat");
                }
                break;
                
            case WITHER_SKELETON:
                // Divine essence from wither skeletons
                if (Math.random() < 0.1) { // 10% chance
                    forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.DIVINE_ESSENCE, 1, 
                        "Wither Skeleton defeat");
                }
                break;
                
            case BLAZE:
                // Divine essence from blazes
                if (Math.random() < 0.08) { // 8% chance
                    forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.DIVINE_ESSENCE, 1, 
                        "Blaze defeat");
                }
                break;
                
            case ENDERMAN:
                // Fusion catalyst from endermen
                if (Math.random() < 0.05) { // 5% chance
                    forgeManager.grantUpgradeMaterials(killer, UpgradeMaterial.UpgradeType.FUSION_CATALYST, 1, 
                        "Enderman defeat");
                }
                break;
        }
    }
    
    /**
     * Handle upgrade material drops from special blocks
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Special blocks that can drop upgrade materials
        switch (blockType) {
            case ANCIENT_DEBRIS:
                // Cosmic fragments from ancient debris
                if (Math.random() < 0.15) { // 15% chance
                    forgeManager.grantUpgradeMaterials(player, UpgradeMaterial.UpgradeType.COSMIC_FRAGMENT, 1, 
                        "Ancient Debris mining");
                }
                break;
                
            case AMETHYST_CLUSTER:
                // Divine essence from amethyst clusters
                if (Math.random() < 0.12) { // 12% chance
                    forgeManager.grantUpgradeMaterials(player, UpgradeMaterial.UpgradeType.DIVINE_ESSENCE, 1, 
                        "Amethyst Cluster mining");
                }
                break;
                
            case SCULK_CATALYST:
                // Reality shards from sculk catalysts
                if (Math.random() < 0.2) { // 20% chance
                    forgeManager.grantUpgradeMaterials(player, UpgradeMaterial.UpgradeType.REALITY_SHARD, 1, 
                        "Sculk Catalyst mining");
                }
                break;
                
            case END_PORTAL_FRAME:
                // Transcendent cores from end portal frames (very rare)
                if (Math.random() < 0.01) { // 1% chance
                    forgeManager.grantUpgradeMaterials(player, UpgradeMaterial.UpgradeType.TRANSCENDENT_CORE, 1, 
                        "End Portal Frame mining");
                }
                break;
        }
    }
    
    /**
     * Handle divine forge interactions
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        // Check if player is interacting with a divine forge (anvil for now)
        if (event.getClickedBlock().getType() == Material.ANVIL) {
            Player player = event.getPlayer();
            
            // Check if player is holding upgrade materials or divine items
            org.bukkit.inventory.ItemStack heldItem = player.getInventory().getItemInMainHand();
            
            if (UpgradeMaterial.getUpgradeType(heldItem) != null) {
                event.setCancelled(true);
                player.sendMessage("§6§l⚒ DIVINE FORGE ⚒");
                player.sendMessage("§7Use §f/forge upgrade§7 to upgrade divine items");
                player.sendMessage("§7Use §f/forge combine§7 to combine divine items");
                player.sendMessage("§7Use §f/forge help§7 for more information");
            }
        }
    }
}