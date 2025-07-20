package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.FragmentItem;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.managers.GodManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles altar interactions for testament completion
 */
public class AltarListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    
    // Define altar patterns for each god
    private final Map<GodType, AltarPattern> altarPatterns;
    
    public AltarListener(MinecraftPlugin plugin, GodManager godManager) {
        this.plugin = plugin;
        this.godManager = godManager;
        this.altarPatterns = new HashMap<>();
        initializeAltarPatterns();
    }
    
    /**
     * Initialize altar patterns for all gods
     */
    private void initializeAltarPatterns() {
        // FALLEN GOD - Dark and ominous altar
        altarPatterns.put(GodType.FALLEN, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                    {Material.OBSIDIAN, Material.SOUL_SAND, Material.OBSIDIAN},
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.WITHER_SKELETON_SKULL, Material.AIR},
                    {Material.WITHER_SKELETON_SKULL, Material.CRYING_OBSIDIAN, Material.WITHER_SKELETON_SKULL},
                    {Material.AIR, Material.WITHER_SKELETON_SKULL, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // BANISHMENT GOD - Fire and lava altar
        altarPatterns.put(GodType.BANISHMENT, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
                    {Material.NETHERRACK, Material.LAVA, Material.NETHERRACK},
                    {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.FIRE, Material.AIR},
                    {Material.FIRE, Material.MAGMA_BLOCK, Material.FIRE},
                    {Material.AIR, Material.FIRE, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // ABYSSAL GOD - Water and prismarine altar
        altarPatterns.put(GodType.ABYSSAL, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE},
                    {Material.PRISMARINE, Material.WATER, Material.PRISMARINE},
                    {Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.PRISMARINE_CRYSTALS, Material.AIR},
                    {Material.PRISMARINE_CRYSTALS, Material.DARK_PRISMARINE, Material.PRISMARINE_CRYSTALS},
                    {Material.AIR, Material.PRISMARINE_CRYSTALS, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // SYLVAN GOD - Nature and wood altar
        altarPatterns.put(GodType.SYLVAN, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.MOSS_BLOCK, Material.MOSS_BLOCK, Material.MOSS_BLOCK},
                    {Material.MOSS_BLOCK, Material.GRASS_BLOCK, Material.MOSS_BLOCK},
                    {Material.MOSS_BLOCK, Material.MOSS_BLOCK, Material.MOSS_BLOCK}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.OAK_LEAVES, Material.AIR},
                    {Material.OAK_LEAVES, Material.OAK_LOG, Material.OAK_LEAVES},
                    {Material.AIR, Material.OAK_LEAVES, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // TEMPEST GOD - Sky and storm altar
        altarPatterns.put(GodType.TEMPEST, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK},
                    {Material.IRON_BLOCK, Material.AIR, Material.IRON_BLOCK},
                    {Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.COPPER_BLOCK, Material.AIR},
                    {Material.COPPER_BLOCK, Material.LIGHTNING_ROD, Material.COPPER_BLOCK},
                    {Material.AIR, Material.COPPER_BLOCK, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // VEIL GOD - End and void altar
        altarPatterns.put(GodType.VEIL, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.END_STONE, Material.END_STONE, Material.END_STONE},
                    {Material.END_STONE, Material.AIR, Material.END_STONE},
                    {Material.END_STONE, Material.END_STONE, Material.END_STONE}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.ENDER_EYE, Material.AIR},
                    {Material.ENDER_EYE, Material.END_PORTAL_FRAME, Material.ENDER_EYE},
                    {Material.AIR, Material.ENDER_EYE, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // FORGE GOD - Metal and crafting altar
        altarPatterns.put(GodType.FORGE, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.DEEPSLATE, Material.DEEPSLATE, Material.DEEPSLATE},
                    {Material.DEEPSLATE, Material.LAVA, Material.DEEPSLATE},
                    {Material.DEEPSLATE, Material.DEEPSLATE, Material.DEEPSLATE}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.IRON_INGOT, Material.AIR},
                    {Material.IRON_INGOT, Material.ANVIL, Material.IRON_INGOT},
                    {Material.AIR, Material.IRON_INGOT, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // VOID GOD - Emptiness and void altar
        altarPatterns.put(GodType.VOID, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                    {Material.OBSIDIAN, Material.AIR, Material.OBSIDIAN},
                    {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.CRYING_OBSIDIAN, Material.AIR},
                    {Material.CRYING_OBSIDIAN, Material.OBSIDIAN, Material.CRYING_OBSIDIAN},
                    {Material.AIR, Material.CRYING_OBSIDIAN, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.ENDER_PEARL, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // TIME GOD - Temporal and crystal altar
        altarPatterns.put(GodType.TIME, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK},
                    {Material.AMETHYST_BLOCK, Material.WATER, Material.AMETHYST_BLOCK},
                    {Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.LARGE_AMETHYST_BUD, Material.AIR},
                    {Material.LARGE_AMETHYST_BUD, Material.AMETHYST_CLUSTER, Material.LARGE_AMETHYST_BUD},
                    {Material.AIR, Material.LARGE_AMETHYST_BUD, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.CLOCK, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // BLOOD GOD - Combat and sacrifice altar
        altarPatterns.put(GodType.BLOOD, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.NETHER_BRICKS, Material.NETHER_BRICKS, Material.NETHER_BRICKS},
                    {Material.NETHER_BRICKS, Material.LAVA, Material.NETHER_BRICKS},
                    {Material.NETHER_BRICKS, Material.NETHER_BRICKS, Material.NETHER_BRICKS}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.NETHERITE_BLOCK, Material.AIR},
                    {Material.NETHERITE_BLOCK, Material.REDSTONE_BLOCK, Material.NETHERITE_BLOCK},
                    {Material.AIR, Material.NETHERITE_BLOCK, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.NETHERITE_SWORD, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // CRYSTAL GOD - Resonance and harmony altar
        altarPatterns.put(GodType.CRYSTAL, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.CALCITE, Material.CALCITE, Material.CALCITE},
                    {Material.CALCITE, Material.AMETHYST_BLOCK, Material.CALCITE},
                    {Material.CALCITE, Material.CALCITE, Material.CALCITE}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.MEDIUM_AMETHYST_BUD, Material.AIR},
                    {Material.MEDIUM_AMETHYST_BUD, Material.LARGE_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD},
                    {Material.AIR, Material.MEDIUM_AMETHYST_BUD, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.AMETHYST_SHARD, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
        
        // SHADOW GOD - Darkness and stealth altar
        altarPatterns.put(GodType.SHADOW, new AltarPattern(
            new Material[][][]{
                // Layer 0 (bottom)
                {
                    {Material.SCULK, Material.SCULK, Material.SCULK},
                    {Material.SCULK, Material.SCULK_SHRIEKER, Material.SCULK},
                    {Material.SCULK, Material.SCULK, Material.SCULK}
                },
                // Layer 1 (middle)
                {
                    {Material.AIR, Material.SCULK_SENSOR, Material.AIR},
                    {Material.SCULK_SENSOR, Material.SCULK_CATALYST, Material.SCULK_SENSOR},
                    {Material.AIR, Material.SCULK_SENSOR, Material.AIR}
                },
                // Layer 2 (top)
                {
                    {Material.AIR, Material.AIR, Material.AIR},
                    {Material.AIR, Material.ECHO_SHARD, Material.AIR},
                    {Material.AIR, Material.AIR, Material.AIR}
                }
            }
        ));
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
        
        // Check if the clicked block is a potential altar center
        GodType altarGod = getAltarGod(clickedBlock);
        if (altarGod == null) {
            return;
        }
        
        // Verify altar structure
        if (!isValidAltar(clickedBlock, altarGod)) {
            showAltarRequirements(player, altarGod);
            return;
        }
        
        // Cancel the event to prevent other interactions
        event.setCancelled(true);
        
        // Check if player already completed this testament
        if (godManager.hasCompletedTestament(player, altarGod)) {
            player.sendMessage("§6§lTestament Already Complete! §r§6You have already completed the testament for the " + altarGod.getDisplayName() + ".");
            player.sendMessage("§7Your divine power from this god remains with you.");
            return;
        }
        
        // Check if player has all fragments
        if (!godManager.hasAllFragments(player, altarGod)) {
            int missingCount = 7 - godManager.getPlayerFragments(player, altarGod).size();
            player.sendMessage("§c§lIncomplete Testament! §r§cYou need §f" + missingCount + " §cmore fragments for the " + altarGod.getDisplayName() + ".");
            player.sendMessage("§7Continue exploring to find the remaining divine fragments.");
            return;
        }
        
        // Remove fragments from inventory
        if (!removeFragmentsFromInventory(player, altarGod)) {
            player.sendMessage("§c§lMissing Fragments! §r§cYou must carry all 7 fragments in your inventory to complete the testament.");
            return;
        }
        
        // Complete the testament
        completeTestament(player, altarGod, clickedBlock.getLocation());
        
        // Trigger tutorial progression if applicable
        if (plugin.getTutorialManager().getCurrentStep(player) == 
            com.example.minecraftplugin.managers.TutorialManager.TutorialStep.TESTAMENT_COMPLETION) {
            plugin.getTutorialManager().showTestamentCompletionTutorial(player);
        }
    }
    
    /**
     * Check if a block is the center of an altar for any god
     */
    private GodType getAltarGod(Block block) {
        Material blockType = block.getType();
        
        for (GodType god : GodType.values()) {
            if (god.getAltarCenterBlock() == blockType) {
                return god;
            }
        }
        
        return null;
    }
    
    /**
     * Verify that the altar structure is valid for the given god
     */
    private boolean isValidAltar(Block centerBlock, GodType god) {
        AltarPattern pattern = altarPatterns.get(god);
        if (pattern == null) {
            plugin.getLogger().warning("No altar pattern defined for " + god.getDisplayName());
            return centerBlock.getType() == god.getAltarCenterBlock();
        }
        
        return pattern.matches(centerBlock);
    }
    
    /**
     * Show altar construction requirements to the player
     */
    private void showAltarRequirements(Player player, GodType god) {
        player.sendMessage("§c§l⚡ INCOMPLETE ALTAR! ⚡");
        player.sendMessage("§7The altar structure for the " + god.getDisplayName() + " is not properly formed.");
        player.sendMessage("");
        
        AltarPattern pattern = altarPatterns.get(god);
        if (pattern != null) {
            player.sendMessage("§e§lRequired Altar Structure:");
            pattern.showRequirements(player, god);
        } else {
            player.sendMessage("§7Expected center block: §f" + god.getAltarCenterBlock().name());
        }
        
        player.sendMessage("");
        player.sendMessage("§7Build the complete altar structure exactly as shown and try again.");
        player.sendMessage("§7§oTip: Start from the bottom layer and work your way up!");
    }
    
    /**
     * Remove all 7 fragments for a god from player's inventory
     */
    private boolean removeFragmentsFromInventory(Player player, GodType god) {
        List<ItemStack> fragmentsToRemove = new ArrayList<>();
        
        // Find all fragments in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            FragmentItem.FragmentInfo fragmentInfo = FragmentItem.getFragmentInfo(item);
            if (fragmentInfo != null && fragmentInfo.getGodType() == god) {
                fragmentsToRemove.add(item);
            }
        }
        
        // Check if we have all 7 fragments
        if (fragmentsToRemove.size() != 7) {
            return false;
        }
        
        // Remove fragments from inventory
        for (ItemStack fragment : fragmentsToRemove) {
            player.getInventory().remove(fragment);
        }
        
        return true;
    }
    
    /**
     * Complete the testament for a player
     */
    private void completeTestament(Player player, GodType god, Location altarLocation) {
        // Mark testament as completed
        godManager.completeTestament(player, god);
        
        // Create enhanced dramatic effects
        plugin.getAltarEffectsManager().createTestamentCompletionEffects(player, god, altarLocation);
        
        // Send completion messages
        player.sendMessage("§6§l✦ TESTAMENT COMPLETED! ✦");
        player.sendMessage("§7You have completed the Testament of the " + god.getDisplayName() + "!");
        player.sendMessage("§7Divine power flows through you...");
        
        // Announce to server if configured
        if (plugin.getConfig().getBoolean("testament.ascension.level_announcements", true)) {
            plugin.getBroadcastManager().broadcastTestamentCompletion(player, god);
        }
        
        // Grant divine item for this god
        DivineItem divineItem = godManager.getDivineItem(god);
        if (divineItem != null) {
            ItemStack divineItemStack = divineItem.createItemStack();
            player.getInventory().addItem(divineItemStack);
            divineItem.onObtained(player, divineItemStack);
        } else {
            player.sendMessage("§e§lDivine Item: §r§eThe divine item for " + god.getDisplayName() + " is not yet available.");
        }
        
        // Log the completion
        plugin.getLogger().info(player.getName() + " completed testament for " + god.getDisplayName() + " at " + 
                               altarLocation.getWorld().getName() + " " + 
                               altarLocation.getBlockX() + "," + altarLocation.getBlockY() + "," + altarLocation.getBlockZ());
    }
    
    /**
     * Inner class to represent an altar pattern
     */
    private static class AltarPattern {
        private final Material[][][] pattern;
        private final int width;
        private final int height;
        private final int depth;
        
        public AltarPattern(Material[][][] pattern) {
            this.pattern = pattern;
            this.height = pattern.length;
            this.depth = pattern[0].length;
            this.width = pattern[0][0].length;
        }
        
        /**
         * Check if the altar pattern matches at the given center block
         */
        public boolean matches(Block centerBlock) {
            // Find the center position in the pattern
            int centerY = 1; // Middle layer
            int centerZ = depth / 2;
            int centerX = width / 2;
            
            // Check each block in the pattern
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    for (int x = 0; x < width; x++) {
                        Material expectedMaterial = pattern[y][z][x];
                        
                        // Calculate world position relative to center block
                        int worldX = centerBlock.getX() + (x - centerX);
                        int worldY = centerBlock.getY() + (y - centerY);
                        int worldZ = centerBlock.getZ() + (z - centerZ);
                        
                        Block worldBlock = centerBlock.getWorld().getBlockAt(worldX, worldY, worldZ);
                        
                        // Special handling for certain materials
                        if (!matchesMaterial(worldBlock, expectedMaterial)) {
                            return false;
                        }
                    }
                }
            }
            
            return true;
        }
        
        /**
         * Check if a block matches the expected material with special cases
         */
        private boolean matchesMaterial(Block block, Material expected) {
            if (expected == Material.AIR) {
                return block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR;
            }
            
            // Special cases for items that can't be placed as blocks
            if (expected == Material.ENDER_PEARL) {
                // Ender pearls can't be placed, so we check for end portal frames with eyes
                return block.getType() == Material.END_PORTAL_FRAME;
            }
            
            if (expected == Material.CLOCK) {
                // Clocks can't be placed, so we accept item frames with clocks or just item frames
                return block.getType() == Material.ITEM_FRAME || block.getType() == Material.GLOW_ITEM_FRAME;
            }
            
            if (expected == Material.NETHERITE_SWORD) {
                // Swords can't be placed, so we accept item frames or armor stands
                return block.getType() == Material.ITEM_FRAME || block.getType() == Material.GLOW_ITEM_FRAME || 
                       block.getType() == Material.ARMOR_STAND;
            }
            
            if (expected == Material.AMETHYST_SHARD) {
                // Shards can't be placed, so we accept item frames
                return block.getType() == Material.ITEM_FRAME || block.getType() == Material.GLOW_ITEM_FRAME;
            }
            
            if (expected == Material.ECHO_SHARD) {
                // Echo shards can't be placed, so we accept item frames
                return block.getType() == Material.ITEM_FRAME || block.getType() == Material.GLOW_ITEM_FRAME;
            }
            
            return block.getType() == expected;
        }
        
        /**
         * Show the altar requirements to a player
         */
        public void showRequirements(Player player, GodType god) {
            player.sendMessage("§e§l" + god.getDisplayName() + " Altar Pattern:");
            player.sendMessage("§7Layer 0 (Bottom - Ground Level):");
            showLayer(player, 0, god);
            player.sendMessage("");
            player.sendMessage("§7Layer 1 (Middle - Center Block Level):");
            showLayer(player, 1, god);
            
            if (height > 2) {
                player.sendMessage("");
                player.sendMessage("§7Layer 2 (Top):");
                showLayer(player, 2, god);
            }
            
            player.sendMessage("");
            player.sendMessage("§7§lSpecial Notes:");
            showSpecialNotes(player, god);
        }
        
        /**
         * Show a specific layer of the altar pattern
         */
        private void showLayer(Player player, int layer, GodType god) {
            if (layer >= height) return;
            
            for (int z = 0; z < depth; z++) {
                StringBuilder row = new StringBuilder("§7  ");
                for (int x = 0; x < width; x++) {
                    Material material = pattern[layer][z][x];
                    String symbol = getMaterialSymbol(material);
                    row.append(symbol).append(" ");
                }
                player.sendMessage(row.toString());
            }
        }
        
        /**
         * Show special construction notes for specific gods
         */
        private void showSpecialNotes(Player player, GodType god) {
            switch (god) {
                case FALLEN:
                    player.sendMessage("§7• Place Wither Skeleton Skulls on the middle layer");
                    player.sendMessage("§7• Soul Sand goes in the center of the bottom layer");
                    break;
                case BANISHMENT:
                    player.sendMessage("§7• Light the Netherrack on fire for the middle layer");
                    player.sendMessage("§7• Lava source block in the center bottom");
                    break;
                case ABYSSAL:
                    player.sendMessage("§7• Water source block in the center bottom");
                    player.sendMessage("§7• Prismarine Crystals on the middle layer");
                    break;
                case SYLVAN:
                    player.sendMessage("§7• Use natural blocks: moss, grass, and wood");
                    player.sendMessage("§7• Oak leaves on the middle layer");
                    break;
                case TEMPEST:
                    player.sendMessage("§7• Lightning Rod in the center, surrounded by copper");
                    player.sendMessage("§7• Iron blocks form the base ring");
                    break;
                case VEIL:
                    player.sendMessage("§7• End Portal Frame with Eyes of Ender");
                    player.sendMessage("§7• Build in The End dimension for best results");
                    break;
                case FORGE:
                    player.sendMessage("§7• Anvil in the center, surrounded by iron blocks");
                    player.sendMessage("§7• Lava source provides the forge fire");
                    break;
                case VOID:
                    player.sendMessage("§7• Place Ender Pearl in item frame on top layer");
                    player.sendMessage("§7• Crying Obsidian forms the void pattern");
                    break;
                case TIME:
                    player.sendMessage("§7• Place Clock in item frame on top layer");
                    player.sendMessage("§7• Amethyst crystals channel temporal energy");
                    break;
                case BLOOD:
                    player.sendMessage("§7• Place Netherite Sword in item frame on top");
                    player.sendMessage("§7• Lava represents the blood sacrifice");
                    break;
                case CRYSTAL:
                    player.sendMessage("§7• Place Amethyst Shard in item frame on top");
                    player.sendMessage("§7• Different amethyst bud sizes create resonance");
                    break;
                case SHADOW:
                    player.sendMessage("§7• Place Echo Shard in item frame on top");
                    player.sendMessage("§7• Sculk blocks absorb light and sound");
                    break;
            }
        }
        
        /**
         * Get a display symbol for a material
         */
        private String getMaterialSymbol(Material material) {
            switch (material) {
                case AIR:
                case CAVE_AIR:
                case VOID_AIR:
                    return "§8·"; // Air
                case OBSIDIAN:
                    return "§0■"; // Black
                case CRYING_OBSIDIAN:
                    return "§5■"; // Purple
                case SOUL_SAND:
                    return "§6■"; // Gold
                case WITHER_SKELETON_SKULL:
                    return "§f☠"; // White skull
                case NETHERRACK:
                    return "§c■"; // Red
                case MAGMA_BLOCK:
                    return "§4■"; // Dark red
                case LAVA:
                    return "§6~"; // Gold wave
                case FIRE:
                    return "§e♦"; // Yellow diamond
                case PRISMARINE:
                    return "§b■"; // Aqua
                case DARK_PRISMARINE:
                    return "§3■"; // Dark aqua
                case PRISMARINE_CRYSTALS:
                    return "§b♦"; // Aqua diamond
                case WATER:
                    return "§9~"; // Blue wave
                case MOSS_BLOCK:
                    return "§a■"; // Green
                case GRASS_BLOCK:
                    return "§2■"; // Dark green
                case OAK_LOG:
                    return "§6|"; // Gold pipe
                case OAK_LEAVES:
                    return "§a♦"; // Green diamond
                case IRON_BLOCK:
                    return "§7■"; // Gray
                case LIGHTNING_ROD:
                    return "§e|"; // Yellow pipe
                case COPPER_BLOCK:
                    return "§6■"; // Gold
                case END_STONE:
                    return "§e■"; // Yellow
                case END_PORTAL_FRAME:
                    return "§5■"; // Purple
                case ENDER_EYE:
                    return "§d♦"; // Light purple diamond
                case DEEPSLATE:
                    return "§8■"; // Dark gray
                case ANVIL:
                    return "§7▲"; // Gray triangle
                case IRON_INGOT:
                    return "§7♦"; // Gray diamond
                case AMETHYST_BLOCK:
                    return "§d■"; // Light purple
                case AMETHYST_CLUSTER:
                case LARGE_AMETHYST_BUD:
                    return "§d♦"; // Light purple diamond
                case REDSTONE_BLOCK:
                    return "§c■"; // Red
                case SCULK:
                    return "§1■"; // Dark blue
                case SCULK_CATALYST:
                    return "§1♦"; // Dark blue diamond
                default:
                    return "§f" + material.name().charAt(0); // First letter of material name
            }
        }
    }
}