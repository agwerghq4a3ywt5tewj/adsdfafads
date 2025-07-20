package com.example.minecraftplugin.world;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles the generation of natural altar structures in the world
 */
public class AltarStructure {
    
    private final GodType godType;
    private final Material[][][] pattern;
    private final int width;
    private final int height;
    private final int depth;
    
    // Define altar patterns for each god (same as AltarListener but for generation)
    private static final Map<GodType, Material[][][]> ALTAR_PATTERNS = new HashMap<>();
    
    static {
        initializeAltarPatterns();
    }
    
    public AltarStructure(GodType godType) {
        this.godType = godType;
        this.pattern = ALTAR_PATTERNS.get(godType);
        if (pattern != null) {
            this.height = pattern.length;
            this.depth = pattern[0].length;
            this.width = pattern[0][0].length;
        } else {
            this.height = 0;
            this.depth = 0;
            this.width = 0;
        }
    }
    
    /**
     * Initialize altar patterns for all gods
     */
    private static void initializeAltarPatterns() {
        // FALLEN GOD - Dark and ominous altar
        ALTAR_PATTERNS.put(GodType.FALLEN, new Material[][][]{
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
        });
        
        // BANISHMENT GOD - Fire and lava altar
        ALTAR_PATTERNS.put(GodType.BANISHMENT, new Material[][][]{
            // Layer 0 (bottom)
            {
                {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
                {Material.NETHERRACK, Material.LAVA, Material.NETHERRACK},
                {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
            },
            // Layer 1 (middle)
            {
                {Material.AIR, Material.NETHERRACK, Material.AIR},
                {Material.NETHERRACK, Material.MAGMA_BLOCK, Material.NETHERRACK},
                {Material.AIR, Material.NETHERRACK, Material.AIR}
            },
            // Layer 2 (top)
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // ABYSSAL GOD - Water and prismarine altar
        ALTAR_PATTERNS.put(GodType.ABYSSAL, new Material[][][]{
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
        });
        
        // SYLVAN GOD - Nature and wood altar
        ALTAR_PATTERNS.put(GodType.SYLVAN, new Material[][][]{
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
        });
        
        // TEMPEST GOD - Sky and storm altar
        ALTAR_PATTERNS.put(GodType.TEMPEST, new Material[][][]{
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
        });
        
        // VEIL GOD - End and void altar
        ALTAR_PATTERNS.put(GodType.VEIL, new Material[][][]{
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
        });
        
        // Add patterns for expansion gods
        initializeExpansionGodPatterns();
    }
    
    /**
     * Initialize patterns for expansion gods
     */
    private static void initializeExpansionGodPatterns() {
        // FORGE GOD - Metal and crafting altar
        ALTAR_PATTERNS.put(GodType.FORGE, new Material[][][]{
            {
                {Material.DEEPSLATE, Material.DEEPSLATE, Material.DEEPSLATE},
                {Material.DEEPSLATE, Material.LAVA, Material.DEEPSLATE},
                {Material.DEEPSLATE, Material.DEEPSLATE, Material.DEEPSLATE}
            },
            {
                {Material.AIR, Material.IRON_BLOCK, Material.AIR},
                {Material.IRON_BLOCK, Material.ANVIL, Material.IRON_BLOCK},
                {Material.AIR, Material.IRON_BLOCK, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // VOID GOD - Emptiness and void altar
        ALTAR_PATTERNS.put(GodType.VOID, new Material[][][]{
            {
                {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.AIR, Material.OBSIDIAN},
                {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
            },
            {
                {Material.AIR, Material.CRYING_OBSIDIAN, Material.AIR},
                {Material.CRYING_OBSIDIAN, Material.OBSIDIAN, Material.CRYING_OBSIDIAN},
                {Material.AIR, Material.CRYING_OBSIDIAN, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // TIME GOD - Temporal and crystal altar
        ALTAR_PATTERNS.put(GodType.TIME, new Material[][][]{
            {
                {Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK},
                {Material.AMETHYST_BLOCK, Material.WATER, Material.AMETHYST_BLOCK},
                {Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK, Material.AMETHYST_BLOCK}
            },
            {
                {Material.AIR, Material.LARGE_AMETHYST_BUD, Material.AIR},
                {Material.LARGE_AMETHYST_BUD, Material.AMETHYST_CLUSTER, Material.LARGE_AMETHYST_BUD},
                {Material.AIR, Material.LARGE_AMETHYST_BUD, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // BLOOD GOD - Combat and sacrifice altar
        ALTAR_PATTERNS.put(GodType.BLOOD, new Material[][][]{
            {
                {Material.NETHER_BRICKS, Material.NETHER_BRICKS, Material.NETHER_BRICKS},
                {Material.NETHER_BRICKS, Material.LAVA, Material.NETHER_BRICKS},
                {Material.NETHER_BRICKS, Material.NETHER_BRICKS, Material.NETHER_BRICKS}
            },
            {
                {Material.AIR, Material.NETHERITE_BLOCK, Material.AIR},
                {Material.NETHERITE_BLOCK, Material.REDSTONE_BLOCK, Material.NETHERITE_BLOCK},
                {Material.AIR, Material.NETHERITE_BLOCK, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // CRYSTAL GOD - Resonance and harmony altar
        ALTAR_PATTERNS.put(GodType.CRYSTAL, new Material[][][]{
            {
                {Material.CALCITE, Material.CALCITE, Material.CALCITE},
                {Material.CALCITE, Material.AMETHYST_BLOCK, Material.CALCITE},
                {Material.CALCITE, Material.CALCITE, Material.CALCITE}
            },
            {
                {Material.AIR, Material.MEDIUM_AMETHYST_BUD, Material.AIR},
                {Material.MEDIUM_AMETHYST_BUD, Material.LARGE_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD},
                {Material.AIR, Material.MEDIUM_AMETHYST_BUD, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
        
        // SHADOW GOD - Darkness and stealth altar
        ALTAR_PATTERNS.put(GodType.SHADOW, new Material[][][]{
            {
                {Material.SCULK, Material.SCULK, Material.SCULK},
                {Material.SCULK, Material.SCULK_SHRIEKER, Material.SCULK},
                {Material.SCULK, Material.SCULK, Material.SCULK}
            },
            {
                {Material.AIR, Material.SCULK_SENSOR, Material.AIR},
                {Material.SCULK_SENSOR, Material.SCULK_CATALYST, Material.SCULK_SENSOR},
                {Material.AIR, Material.SCULK_SENSOR, Material.AIR}
            },
            {
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR},
                {Material.AIR, Material.AIR, Material.AIR}
            }
        });
    }
    
    /**
     * Generate the altar structure at the given location
     */
    public boolean generateAt(Location centerLocation) {
        if (pattern == null) {
            return false;
        }
        
        World world = centerLocation.getWorld();
        if (world == null) {
            return false;
        }
        
        // Find the center position in the pattern
        int centerY = 1; // Middle layer
        int centerZ = depth / 2;
        int centerX = width / 2;
        
        // Generate each block in the pattern
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    Material material = pattern[y][z][x];
                    
                    // Calculate world position relative to center
                    int worldX = centerLocation.getBlockX() + (x - centerX);
                    int worldY = centerLocation.getBlockY() + (y - centerY);
                    int worldZ = centerLocation.getBlockZ() + (z - centerZ);
                    
                    Block block = world.getBlockAt(worldX, worldY, worldZ);
                    
                    // Place the block
                    if (material != Material.AIR) {
                        block.setType(material);
                        
                        // Special handling for certain blocks
                        handleSpecialBlocks(block, material);
                    }
                }
            }
        }
        
        // Add fire to netherrack blocks for Banishment God
        if (godType == GodType.BANISHMENT) {
            addFireToNetherrack(centerLocation);
        }
        
        return true;
    }
    
    /**
     * Handle special block placement requirements
     */
    private void handleSpecialBlocks(Block block, Material material) {
        switch (material) {
            case WITHER_SKELETON_SKULL:
                // Set skull direction
                if (block.getBlockData() instanceof org.bukkit.block.data.Directional) {
                    org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) block.getBlockData();
                    directional.setFacing(BlockFace.UP);
                    block.setBlockData(directional);
                }
                break;
            case END_PORTAL_FRAME:
                // Set portal frame with eye
                if (block.getBlockData() instanceof org.bukkit.block.data.type.EndPortalFrame) {
                    org.bukkit.block.data.type.EndPortalFrame frame = (org.bukkit.block.data.type.EndPortalFrame) block.getBlockData();
                    frame.setEye(true);
                    block.setBlockData(frame);
                }
                break;
            case LIGHTNING_ROD:
                // Set lightning rod direction
                if (block.getBlockData() instanceof org.bukkit.block.data.type.LightningRod) {
                    org.bukkit.block.data.type.LightningRod rod = (org.bukkit.block.data.type.LightningRod) block.getBlockData();
                    rod.setFacing(BlockFace.UP);
                    block.setBlockData(rod);
                }
                break;
            case ANVIL:
                // Set anvil direction
                if (block.getBlockData() instanceof org.bukkit.block.data.Directional) {
                    org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) block.getBlockData();
                    directional.setFacing(BlockFace.NORTH);
                    block.setBlockData(directional);
                }
                break;
        }
    }
    
    /**
     * Add fire to netherrack blocks for Banishment God altar
     */
    private void addFireToNetherrack(Location centerLocation) {
        // Add fire on top of netherrack blocks in the middle layer
        Block fireBlock1 = centerLocation.getWorld().getBlockAt(centerLocation.getBlockX() - 1, centerLocation.getBlockY() + 1, centerLocation.getBlockZ());
        Block fireBlock2 = centerLocation.getWorld().getBlockAt(centerLocation.getBlockX() + 1, centerLocation.getBlockY() + 1, centerLocation.getBlockZ());
        Block fireBlock3 = centerLocation.getWorld().getBlockAt(centerLocation.getBlockX(), centerLocation.getBlockY() + 1, centerLocation.getBlockZ() - 1);
        Block fireBlock4 = centerLocation.getWorld().getBlockAt(centerLocation.getBlockX(), centerLocation.getBlockY() + 1, centerLocation.getBlockZ() + 1);
        
        if (fireBlock1.getType() == Material.AIR) fireBlock1.setType(Material.FIRE);
        if (fireBlock2.getType() == Material.AIR) fireBlock2.setType(Material.FIRE);
        if (fireBlock3.getType() == Material.AIR) fireBlock3.setType(Material.FIRE);
        if (fireBlock4.getType() == Material.AIR) fireBlock4.setType(Material.FIRE);
    }
    
    /**
     * Check if the location is suitable for this god's altar
     */
    public boolean isSuitableLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        
        // Check biome compatibility
        String biomeName = location.getBlock().getBiome().name();
        boolean biomeMatch = false;
        
        for (String validBiome : godType.getBiomes()) {
            if (biomeName.contains(validBiome.toUpperCase())) {
                biomeMatch = true;
                break;
            }
        }
        
        if (!biomeMatch) {
            return false;
        }
        
        // Check if area is clear and suitable
        return isAreaClear(location) && hasGoodFoundation(location);
    }
    
    /**
     * Check if the area around the location is clear for altar generation
     */
    private boolean isAreaClear(Location location) {
        World world = location.getWorld();
        
        // Check a 5x5x3 area around the center
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = world.getBlockAt(
                        location.getBlockX() + x,
                        location.getBlockY() + y,
                        location.getBlockZ() + z
                    );
                    
                    // Allow air, grass, flowers, and other replaceable blocks
                    if (!isReplaceableBlock(block.getType())) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if the block can be replaced during altar generation
     */
    private boolean isReplaceableBlock(Material material) {
        return material == Material.AIR ||
               material == Material.SHORT_GRASS ||
               material == Material.TALL_GRASS ||
               material == Material.FERN ||
               material == Material.LARGE_FERN ||
               material == Material.DANDELION ||
               material == Material.POPPY ||
               material == Material.BLUE_ORCHID ||
               material == Material.ALLIUM ||
               material == Material.AZURE_BLUET ||
               material == Material.RED_TULIP ||
               material == Material.ORANGE_TULIP ||
               material == Material.WHITE_TULIP ||
               material == Material.PINK_TULIP ||
               material == Material.OXEYE_DAISY ||
               material == Material.SNOW ||
               material == Material.WATER ||
               material.name().contains("SAPLING");
    }
    
    /**
     * Check if the location has a good foundation for the altar
     */
    private boolean hasGoodFoundation(Location location) {
        World world = location.getWorld();
        
        // Check the block below the center
        Block foundationBlock = world.getBlockAt(
            location.getBlockX(),
            location.getBlockY() - 1,
            location.getBlockZ()
        );
        
        // Must be solid ground
        return foundationBlock.getType().isSolid() && 
               foundationBlock.getType() != Material.SAND &&
               foundationBlock.getType() != Material.GRAVEL;
    }
    
    public GodType getGodType() {
        return godType;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getDepth() {
        return depth;
    }
}