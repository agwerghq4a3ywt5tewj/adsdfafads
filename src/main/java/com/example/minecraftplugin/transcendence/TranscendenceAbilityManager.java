package com.example.minecraftplugin.transcendence;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.TranscendenceLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages advanced transcendence abilities for post-convergence players
 */
public class TranscendenceAbilityManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    
    // Track active abilities
    private final Map<UUID, Set<String>> activeAbilities;
    private final Map<UUID, Map<String, Long>> abilityCooldowns;
    
    // Reality manipulation tracking
    private final Map<UUID, List<Location>> manipulatedBlocks;
    private final Map<UUID, Integer> realityPoints;
    
    public TranscendenceAbilityManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.activeAbilities = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.manipulatedBlocks = new HashMap<>();
        this.realityPoints = new HashMap<>();
        
        logger.info("Transcendence Ability Manager initialized");
    }
    
    /**
     * Execute reality manipulation ability
     */
    public boolean executeRealityManipulation(Player player, Location target, String manipulationType) {
        if (!hasAbility(player, "reality_manipulation")) {
            return false;
        }
        
        if (isOnCooldown(player, "reality_manipulation")) {
            long remaining = getRemainingCooldown(player, "reality_manipulation");
            player.sendMessage("§d§lReality Manipulation: §r§cCooldown " + remaining + "s remaining");
            return false;
        }
        
        switch (manipulationType.toLowerCase()) {
            case "transmute":
                return executeTransmutation(player, target);
            case "create":
                return executeCreation(player, target);
            case "destroy":
                return executeDestruction(player, target);
            case "phase":
                return executePhaseShift(player, target);
            default:
                return false;
        }
    }
    
    /**
     * Execute matter transmutation
     */
    private boolean executeTransmutation(Player player, Location target) {
        Block block = target.getBlock();
        Material currentMaterial = block.getType();
        
        // Define transmutation rules
        Material newMaterial = getTransmutationResult(currentMaterial);
        if (newMaterial == null) {
            player.sendMessage("§d§lTransmutation: §r§cThis material cannot be transmuted");
            return false;
        }
        
        // Check reality points cost
        int cost = getTransmutationCost(currentMaterial, newMaterial);
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lTransmutation: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Perform transmutation
        block.setType(newMaterial);
        trackManipulatedBlock(player, target);
        
        // Visual effects
        target.getWorld().spawnParticle(Particle.ENCHANT, target, 20, 0.5, 0.5, 0.5, 0.2);
        target.getWorld().spawnParticle(Particle.REVERSE_PORTAL, target, 10, 0.3, 0.3, 0.3, 0.1);
        
        player.sendMessage("§d§lTransmutation: §r§dTransmuted " + currentMaterial.name() + " → " + newMaterial.name());
        player.playSound(target, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        
        setCooldown(player, "reality_manipulation", 30);
        return true;
    }
    
    /**
     * Execute matter creation
     */
    private boolean executeCreation(Player player, Location target) {
        Block block = target.getBlock();
        
        if (block.getType() != Material.AIR) {
            player.sendMessage("§d§lCreation: §r§cTarget location must be empty");
            return false;
        }
        
        // Create based on player's held item or random valuable material
        Material createMaterial = getCreationMaterial(player);
        int cost = getCreationCost(createMaterial);
        
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lCreation: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Create the block
        block.setType(createMaterial);
        trackManipulatedBlock(player, target);
        
        // Visual effects
        target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target, 30, 0.8, 0.8, 0.8, 0.3);
        target.getWorld().spawnParticle(Particle.FIREWORK, target, 20, 0.5, 0.5, 0.5, 0.2);
        
        player.sendMessage("§d§lCreation: §r§dCreated " + createMaterial.name() + " from pure will");
        player.playSound(target, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        
        setCooldown(player, "reality_manipulation", 45);
        return true;
    }
    
    /**
     * Execute matter destruction
     */
    private boolean executeDestruction(Player player, Location target) {
        Block block = target.getBlock();
        
        if (block.getType() == Material.AIR) {
            player.sendMessage("§d§lDestruction: §r§cNothing to destroy at target location");
            return false;
        }
        
        // Check if block can be destroyed
        if (isProtectedBlock(block.getType())) {
            player.sendMessage("§d§lDestruction: §r§cThis block is protected from reality manipulation");
            return false;
        }
        
        int cost = getDestructionCost(block.getType());
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lDestruction: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Store original material for potential restoration
        Material originalMaterial = block.getType();
        
        // Destroy the block
        block.setType(Material.AIR);
        trackManipulatedBlock(player, target);
        
        // Visual effects
        target.getWorld().spawnParticle(Particle.SQUID_INK, target, 25, 0.6, 0.6, 0.6, 0.2);
        target.getWorld().spawnParticle(Particle.LARGE_SMOKE, target, 15, 0.4, 0.4, 0.4, 0.1);
        
        player.sendMessage("§d§lDestruction: §r§dErased " + originalMaterial.name() + " from existence");
        player.playSound(target, Sound.ENTITY_ENDERMAN_SCREAM, 0.8f, 0.5f);
        
        setCooldown(player, "reality_manipulation", 20);
        return true;
    }
    
    /**
     * Execute phase shift ability
     */
    private boolean executePhaseShift(Player player, Location target) {
        if (target.distance(player.getLocation()) > 50) {
            player.sendMessage("§d§lPhase Shift: §r§cTarget too far away (max 50 blocks)");
            return false;
        }
        
        int cost = 10;
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lPhase Shift: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Phase through solid blocks to reach target
        Location safeTarget = findSafeLocation(target);
        if (safeTarget == null) {
            player.sendMessage("§d§lPhase Shift: §r§cNo safe location found at target");
            return false;
        }
        
        // Teleport with phase effects
        player.teleport(safeTarget);
        
        // Visual effects at both locations
        Location origin = player.getLocation();
        origin.getWorld().spawnParticle(Particle.PORTAL, origin, 30, 1, 1, 1, 0.3);
        safeTarget.getWorld().spawnParticle(Particle.REVERSE_PORTAL, safeTarget, 30, 1, 1, 1, 0.3);
        
        player.sendMessage("§d§lPhase Shift: §r§dPhased through reality to target location");
        player.playSound(safeTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        
        setCooldown(player, "reality_manipulation", 15);
        return true;
    }
    
    /**
     * Execute realm creation ability
     */
    public boolean executeRealmCreation(Player player, String realmName, int size) {
        if (!hasAbility(player, "realm_creation")) {
            return false;
        }
        
        if (isOnCooldown(player, "realm_creation")) {
            long remaining = getRemainingCooldown(player, "realm_creation");
            player.sendMessage("§d§lRealm Creation: §r§cCooldown " + remaining + "s remaining");
            return false;
        }
        
        int cost = size * 100; // Cost scales with realm size
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lRealm Creation: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Create pocket dimension
        Location center = player.getLocation();
        createPocketRealm(player, center, realmName, size);
        
        player.sendMessage("§d§l✦ REALM CREATED! ✦");
        player.sendMessage("§7Realm: §f" + realmName);
        player.sendMessage("§7Size: §f" + size + "x" + size + " blocks");
        player.sendMessage("§7You have shaped reality itself!");
        
        setCooldown(player, "realm_creation", 300); // 5 minute cooldown
        return true;
    }
    
    /**
     * Create a pocket realm
     */
    private void createPocketRealm(Player player, Location center, String realmName, int size) {
        // Create a structured realm with different zones
        int halfSize = size / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                for (int y = -5; y <= 10; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    
                    // Create realm structure
                    if (y == -5) {
                        // Floor
                        block.setType(Material.OBSIDIAN);
                    } else if (y == 10) {
                        // Ceiling
                        block.setType(Material.BARRIER);
                    } else if (Math.abs(x) == halfSize || Math.abs(z) == halfSize) {
                        // Walls
                        if (y <= 5) {
                            block.setType(Material.CRYING_OBSIDIAN);
                        } else {
                            block.setType(Material.AIR);
                        }
                    } else {
                        // Interior
                        if (y <= 0) {
                            block.setType(Material.AIR);
                        } else if (y == 1 && x == 0 && z == 0) {
                            // Central beacon
                            block.setType(Material.BEACON);
                        } else {
                            block.setType(Material.AIR);
                        }
                    }
                    
                    trackManipulatedBlock(player, blockLoc);
                }
            }
        }
        
        // Add portal entrance
        Location portalLoc = center.clone().add(0, 1, halfSize);
        portalLoc.getBlock().setType(Material.NETHER_PORTAL);
        
        // Visual effects
        center.getWorld().spawnParticle(Particle.PORTAL, center, 100, size/2, 5, size/2, 0.5);
        center.getWorld().spawnParticle(Particle.END_ROD, center, 50, size/4, 3, size/4, 0.3);
        
        logger.info(player.getName() + " created realm '" + realmName + "' of size " + size);
    }
    
    /**
     * Execute life creation ability
     */
    public boolean executeLifeCreation(Player player, Location target, String lifeType) {
        if (!hasAbility(player, "life_creation")) {
            return false;
        }
        
        if (isOnCooldown(player, "life_creation")) {
            long remaining = getRemainingCooldown(player, "life_creation");
            player.sendMessage("§d§lLife Creation: §r§cCooldown " + remaining + "s remaining");
            return false;
        }
        
        int cost = getLifeCreationCost(lifeType);
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lLife Creation: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Create life based on type
        boolean success = createLife(player, target, lifeType);
        
        if (success) {
            player.sendMessage("§d§l✦ LIFE CREATED! ✦");
            player.sendMessage("§7You have brought new life into existence!");
            player.sendMessage("§7Type: §f" + lifeType);
            
            setCooldown(player, "life_creation", 600); // 10 minute cooldown
        }
        
        return success;
    }
    
    /**
     * Create life at target location
     */
    private boolean createLife(Player player, Location target, String lifeType) {
        switch (lifeType.toLowerCase()) {
            case "peaceful":
                // Spawn peaceful mobs
                target.getWorld().spawn(target, org.bukkit.entity.Cow.class);
                target.getWorld().spawn(target, org.bukkit.entity.Sheep.class);
                target.getWorld().spawn(target, org.bukkit.entity.Chicken.class);
                break;
                
            case "guardian":
                // Spawn protective entities
                org.bukkit.entity.IronGolem golem = target.getWorld().spawn(target, org.bukkit.entity.IronGolem.class);
                golem.setCustomName("§d§lCreated Guardian");
                golem.setCustomNameVisible(true);
                break;
                
            case "elemental":
                // Spawn elemental beings
                org.bukkit.entity.Blaze blaze = target.getWorld().spawn(target, org.bukkit.entity.Blaze.class);
                blaze.setCustomName("§d§lFire Elemental");
                blaze.setCustomNameVisible(true);
                blaze.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
                break;
                
            case "spirit":
                // Spawn spirit entities
                org.bukkit.entity.Allay allay = target.getWorld().spawn(target, org.bukkit.entity.Allay.class);
                allay.setCustomName("§d§lCreated Spirit");
                allay.setCustomNameVisible(true);
                break;
                
            default:
                return false;
        }
        
        // Visual effects
        target.getWorld().spawnParticle(Particle.HEART, target, 20, 1, 1, 1, 0.2);
        target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target, 30, 1.5, 1.5, 1.5, 0.3);
        player.playSound(target, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        
        return true;
    }
    
    /**
     * Execute dimensional travel ability
     */
    public boolean executeDimensionalTravel(Player player, String dimension) {
        if (!hasAbility(player, "multiverse_travel")) {
            return false;
        }
        
        if (isOnCooldown(player, "dimensional_travel")) {
            long remaining = getRemainingCooldown(player, "dimensional_travel");
            player.sendMessage("§d§lDimensional Travel: §r§cCooldown " + remaining + "s remaining");
            return false;
        }
        
        int cost = 50;
        if (!consumeRealityPoints(player, cost)) {
            player.sendMessage("§d§lDimensional Travel: §r§cInsufficient reality points (" + cost + " needed)");
            return false;
        }
        
        // Get target world
        org.bukkit.World targetWorld = getTargetWorld(dimension);
        if (targetWorld == null) {
            player.sendMessage("§d§lDimensional Travel: §r§cDimension '" + dimension + "' not found");
            return false;
        }
        
        // Find safe spawn location
        Location spawnLoc = targetWorld.getSpawnLocation();
        Location safeLoc = findSafeLocation(spawnLoc);
        
        if (safeLoc == null) {
            player.sendMessage("§d§lDimensional Travel: §r§cNo safe location found in target dimension");
            return false;
        }
        
        // Teleport with dimensional effects
        Location origin = player.getLocation();
        player.teleport(safeLoc);
        
        // Visual effects
        origin.getWorld().spawnParticle(Particle.PORTAL, origin, 50, 2, 2, 2, 0.5);
        safeLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, safeLoc, 50, 2, 2, 2, 0.5);
        
        player.sendMessage("§d§l✦ DIMENSIONAL TRAVEL! ✦");
        player.sendMessage("§7Traveled to dimension: §f" + dimension);
        player.playSound(safeLoc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.5f);
        
        setCooldown(player, "dimensional_travel", 120); // 2 minute cooldown
        return true;
    }
    
    /**
     * Check if player has specific ability
     */
    private boolean hasAbility(Player player, String ability) {
        Set<String> abilities = activeAbilities.get(player.getUniqueId());
        return abilities != null && abilities.contains(ability);
    }
    
    /**
     * Check if ability is on cooldown
     */
    private boolean isOnCooldown(Player player, String ability) {
        Map<String, Long> cooldowns = abilityCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return false;
        
        Long lastUsed = cooldowns.get(ability);
        if (lastUsed == null) return false;
        
        return System.currentTimeMillis() - lastUsed < getCooldownDuration(ability) * 1000L;
    }
    
    /**
     * Get remaining cooldown time
     */
    private long getRemainingCooldown(Player player, String ability) {
        Map<String, Long> cooldowns = abilityCooldowns.get(player.getUniqueId());
        if (cooldowns == null) return 0;
        
        Long lastUsed = cooldowns.get(ability);
        if (lastUsed == null) return 0;
        
        long elapsed = System.currentTimeMillis() - lastUsed;
        long cooldownMs = getCooldownDuration(ability) * 1000L;
        
        return Math.max(0, (cooldownMs - elapsed) / 1000L);
    }
    
    /**
     * Set ability cooldown
     */
    private void setCooldown(Player player, String ability, int seconds) {
        abilityCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                       .put(ability, System.currentTimeMillis());
    }
    
    /**
     * Get cooldown duration for ability
     */
    private int getCooldownDuration(String ability) {
        switch (ability) {
            case "reality_manipulation": return 30;
            case "realm_creation": return 300;
            case "life_creation": return 600;
            case "dimensional_travel": return 120;
            default: return 60;
        }
    }
    
    /**
     * Consume reality points
     */
    private boolean consumeRealityPoints(Player player, int cost) {
        UUID playerId = player.getUniqueId();
        int current = realityPoints.getOrDefault(playerId, 0);
        
        if (current < cost) {
            return false;
        }
        
        realityPoints.put(playerId, current - cost);
        return true;
    }
    
    /**
     * Grant reality points
     */
    public void grantRealityPoints(Player player, int amount, String reason) {
        UUID playerId = player.getUniqueId();
        int current = realityPoints.getOrDefault(playerId, 0);
        realityPoints.put(playerId, current + amount);
        
        player.sendMessage("§d§l✦ REALITY POINTS GAINED! ✦");
        player.sendMessage("§7Amount: §f+" + amount);
        player.sendMessage("§7Reason: §f" + reason);
        player.sendMessage("§7Total: §f" + (current + amount));
    }
    
    /**
     * Get player's reality points
     */
    public int getRealityPoints(Player player) {
        return realityPoints.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Track manipulated blocks for restoration
     */
    private void trackManipulatedBlock(Player player, Location location) {
        manipulatedBlocks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>())
                         .add(location.clone());
    }
    
    /**
     * Get transmutation result
     */
    private Material getTransmutationResult(Material input) {
        Map<Material, Material> transmutations = Map.of(
            Material.STONE, Material.DIAMOND_ORE,
            Material.DIRT, Material.GRASS_BLOCK,
            Material.SAND, Material.GLASS,
            Material.COBBLESTONE, Material.STONE,
            Material.IRON_ORE, Material.GOLD_ORE,
            Material.COAL_ORE, Material.DIAMOND_ORE,
            Material.OAK_WOOD, Material.IRON_BLOCK,
            Material.NETHERRACK, Material.NETHER_GOLD_ORE
        );
        
        return transmutations.get(input);
    }
    
    /**
     * Get transmutation cost
     */
    private int getTransmutationCost(Material from, Material to) {
        // Base cost + value difference
        return 5 + getMaterialValue(to) - getMaterialValue(from);
    }
    
    /**
     * Get material value for cost calculations
     */
    private int getMaterialValue(Material material) {
        switch (material) {
            case DIAMOND_ORE: case DIAMOND_BLOCK: return 20;
            case GOLD_ORE: case GOLD_BLOCK: return 15;
            case IRON_ORE: case IRON_BLOCK: return 10;
            case COAL_ORE: return 5;
            case STONE: return 2;
            case DIRT: case SAND: return 1;
            default: return 3;
        }
    }
    
    /**
     * Get creation material based on player context
     */
    private Material getCreationMaterial(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held != null && held.getType() != Material.AIR) {
            return held.getType();
        }
        
        // Default valuable materials
        Material[] valuableMaterials = {
            Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK,
            Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK
        };
        
        return valuableMaterials[(int) (Math.random() * valuableMaterials.length)];
    }
    
    /**
     * Get creation cost
     */
    private int getCreationCost(Material material) {
        return Math.max(10, getMaterialValue(material) * 2);
    }
    
    /**
     * Get destruction cost
     */
    private int getDestructionCost(Material material) {
        return Math.max(5, getMaterialValue(material));
    }
    
    /**
     * Get life creation cost
     */
    private int getLifeCreationCost(String lifeType) {
        switch (lifeType.toLowerCase()) {
            case "peaceful": return 20;
            case "guardian": return 50;
            case "elemental": return 75;
            case "spirit": return 100;
            default: return 30;
        }
    }
    
    /**
     * Check if block is protected from manipulation
     */
    private boolean isProtectedBlock(Material material) {
        return material == Material.BEDROCK || 
               material == Material.BARRIER ||
               material == Material.COMMAND_BLOCK ||
               material == Material.STRUCTURE_BLOCK;
    }
    
    /**
     * Find safe location near target
     */
    private Location findSafeLocation(Location target) {
        for (int y = -2; y <= 2; y++) {
            Location testLoc = target.clone().add(0, y, 0);
            if (testLoc.getBlock().getType() == Material.AIR &&
                testLoc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                testLoc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
                return testLoc;
            }
        }
        return null;
    }
    
    /**
     * Get target world for dimensional travel
     */
    private org.bukkit.World getTargetWorld(String dimension) {
        switch (dimension.toLowerCase()) {
            case "overworld": case "world":
                return plugin.getServer().getWorld("world");
            case "nether":
                return plugin.getServer().getWorld("world_nether");
            case "end":
                return plugin.getServer().getWorld("world_the_end");
            default:
                return plugin.getServer().getWorld(dimension);
        }
    }
    
    /**
     * Update player abilities based on transcendence level
     */
    public void updatePlayerAbilities(Player player, TranscendenceLevel level) {
        UUID playerId = player.getUniqueId();
        Set<String> abilities = activeAbilities.computeIfAbsent(playerId, k -> new HashSet<>());
        
        abilities.clear();
        
        switch (level) {
            case UNIVERSAL_DEITY:
                abilities.add("omnipotence");
                abilities.add("omniscience");
                abilities.add("omnipresence");
                // Fall through
            case DIMENSIONAL_SOVEREIGN:
                abilities.add("multiverse_travel");
                abilities.add("dimensional_control");
                abilities.add("cosmic_governance");
                // Fall through
            case COSMIC_ARCHITECT:
                abilities.add("realm_creation");
                abilities.add("physics_design");
                abilities.add("life_creation");
                // Fall through
            case REALITY_SHAPER:
                abilities.add("reality_manipulation");
                abilities.add("matter_transmutation");
                abilities.add("space_folding");
                break;
        }
        
        // Grant reality points based on level
        int basePoints = level.ordinal() * 100;
        realityPoints.put(playerId, Math.max(realityPoints.getOrDefault(playerId, 0), basePoints));
    }
    
    /**
     * Get player's active abilities
     */
    public Set<String> getPlayerAbilities(Player player) {
        return new HashSet<>(activeAbilities.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }
    
    /**
     * Cleanup player data
     */
    public void cleanupPlayer(UUID playerId) {
        activeAbilities.remove(playerId);
        abilityCooldowns.remove(playerId);
        manipulatedBlocks.remove(playerId);
        realityPoints.remove(playerId);
    }
}