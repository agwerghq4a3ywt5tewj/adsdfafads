package com.example.minecraftplugin.world;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the natural generation of altars in the world
 */
public class AltarGenerator implements Listener {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final Random random;
    
    // Configuration values
    private double baseSpawnChance;
    private int minDistanceBetweenAltars;
    private int maxAltarsPerChunk;
    private boolean enableNaturalGeneration;
    
    // Track generated altars to prevent overlap
    private final Set<String> generatedAltarLocations;
    private final Map<String, Long> chunkProcessingQueue;
    
    public AltarGenerator(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.random = new Random();
        this.generatedAltarLocations = new HashSet<>();
        this.chunkProcessingQueue = new HashMap<>();
        
        loadConfiguration();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Start background processing task
        startProcessingTask();
        
        logger.info("Natural Altar Generator initialized");
    }
    
    /**
     * Load configuration values
     */
    private void loadConfiguration() {
        enableNaturalGeneration = plugin.getConfig().getBoolean("testament.altars.natural_generation.enabled", true);
        baseSpawnChance = plugin.getConfig().getDouble("testament.altars.natural_generation.base_spawn_chance", 0.001); // 0.1%
        minDistanceBetweenAltars = plugin.getConfig().getInt("testament.altars.natural_generation.min_distance", 500);
        maxAltarsPerChunk = plugin.getConfig().getInt("testament.altars.natural_generation.max_per_chunk", 1);
        
        logger.info("Altar Generation Config - Enabled: " + enableNaturalGeneration + 
                   ", Spawn Chance: " + (baseSpawnChance * 100) + "%, Min Distance: " + minDistanceBetweenAltars);
    }
    
    /**
     * Handle chunk loading for altar generation
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!enableNaturalGeneration) {
            return;
        }
        
        Chunk chunk = event.getChunk();
        
        // Skip if chunk was already processed
        String chunkKey = getChunkKey(chunk);
        if (generatedAltarLocations.contains(chunkKey + "_processed")) {
            return;
        }
        
        // Add to processing queue with delay to avoid lag
        chunkProcessingQueue.put(chunkKey, System.currentTimeMillis());
    }
    
    /**
     * Start background task to process chunk generation queue
     */
    private void startProcessingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                processChunkQueue();
            }
        }.runTaskTimer(plugin, 100L, 40L); // Start after 5 seconds, run every 2 seconds
    }
    
    /**
     * Process the chunk generation queue
     */
    private void processChunkQueue() {
        if (chunkProcessingQueue.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = chunkProcessingQueue.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String chunkKey = entry.getKey();
            long queueTime = entry.getValue();
            
            // Process chunks that have been in queue for at least 5 seconds
            if (currentTime - queueTime >= 5000) {
                processChunkForAltars(chunkKey);
                iterator.remove();
            }
        }
    }
    
    /**
     * Process a specific chunk for altar generation
     */
    private void processChunkForAltars(String chunkKey) {
        try {
            String[] parts = chunkKey.split("_");
            if (parts.length != 3) {
                return;
            }
            
            String worldName = parts[0];
            int chunkX = Integer.parseInt(parts[1]);
            int chunkZ = Integer.parseInt(parts[2]);
            
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return;
            }
            
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            attemptAltarGeneration(chunk);
            
            // Mark chunk as processed
            generatedAltarLocations.add(chunkKey + "_processed");
            
        } catch (Exception e) {
            logger.warning("Error processing chunk for altar generation: " + e.getMessage());
        }
    }
    
    /**
     * Attempt to generate altars in a chunk
     */
    private void attemptAltarGeneration(Chunk chunk) {
        World world = chunk.getWorld();
        int altarsGenerated = 0;
        
        // Try multiple times to find suitable locations
        for (int attempt = 0; attempt < 10 && altarsGenerated < maxAltarsPerChunk; attempt++) {
            // Random location within chunk
            int x = (chunk.getX() << 4) + random.nextInt(16);
            int z = (chunk.getZ() << 4) + random.nextInt(16);
            
            // Find suitable Y coordinate
            int y = findSuitableY(world, x, z);
            if (y == -1) {
                continue;
            }
            
            Location location = new Location(world, x, y, z);
            
            // Check spawn chance
            if (random.nextDouble() > baseSpawnChance) {
                continue;
            }
            
            // Check distance from other altars
            if (!isValidDistance(location)) {
                continue;
            }
            
            // Select appropriate god for this biome
            GodType selectedGod = selectGodForLocation(location);
            if (selectedGod == null) {
                continue;
            }
            
            // Generate the altar
            if (generateAltar(location, selectedGod)) {
                altarsGenerated++;
                logger.info("Generated " + selectedGod.getDisplayName() + " altar at " + 
                           world.getName() + " " + x + "," + y + "," + z);
            }
        }
    }
    
    /**
     * Find a suitable Y coordinate for altar placement
     */
    private int findSuitableY(World world, int x, int z) {
        // Start from a reasonable height and work down
        for (int y = world.getMaxHeight() - 20; y >= world.getMinHeight() + 10; y--) {
            Location testLocation = new Location(world, x, y, z);
            
            // Check if this is a good surface level
            if (world.getBlockAt(x, y - 1, z).getType().isSolid() &&
                world.getBlockAt(x, y, z).getType().isAir() &&
                world.getBlockAt(x, y + 1, z).getType().isAir() &&
                world.getBlockAt(x, y + 2, z).getType().isAir()) {
                
                // Additional checks for specific environments
                if (isGoodAltarHeight(testLocation)) {
                    return y;
                }
            }
        }
        
        return -1; // No suitable location found
    }
    
    /**
     * Check if the height is good for altar placement
     */
    private boolean isGoodAltarHeight(Location location) {
        World world = location.getWorld();
        int y = location.getBlockY();
        
        // Avoid placing too high or too low
        if (y < 60 || y > 120) {
            return false;
        }
        
        // Check for water surface (for Abyssal God)
        if (world.getBlockAt(location.getBlockX(), y - 1, location.getBlockZ()).getType().name().contains("WATER")) {
            return location.getBlock().getBiome().name().contains("OCEAN");
        }
        
        // Avoid placing in caves
        if (!world.getBlockAt(location.getBlockX(), y + 10, location.getBlockZ()).getType().isAir()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the location is a valid distance from other altars
     */
    private boolean isValidDistance(Location location) {
        String locationKey = getLocationKey(location);
        
        for (String existingLocation : generatedAltarLocations) {
            if (existingLocation.endsWith("_processed")) {
                continue;
            }
            
            try {
                String[] parts = existingLocation.split("_");
                if (parts.length >= 4) {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    
                    if (worldName.equals(location.getWorld().getName())) {
                        double distance = location.distance(new Location(location.getWorld(), x, y, z));
                        if (distance < minDistanceBetweenAltars) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore malformed location keys
            }
        }
        
        return true;
    }
    
    /**
     * Select an appropriate god for the given location based on biome
     */
    private GodType selectGodForLocation(Location location) {
        String biomeName = location.getBlock().getBiome().name().toUpperCase();
        List<GodType> suitableGods = new ArrayList<>();
        
        // Find gods that match this biome
        for (GodType god : GodType.values()) {
            for (String validBiome : god.getBiomes()) {
                if (biomeName.contains(validBiome.toUpperCase())) {
                    suitableGods.add(god);
                    break;
                }
            }
        }
        
        if (suitableGods.isEmpty()) {
            return null;
        }
        
        // Apply rarity weights (Core gods are more common)
        List<GodType> weightedGods = new ArrayList<>();
        for (GodType god : suitableGods) {
            int weight = god.getTier().name().equals("CORE") ? 3 : 1;
            for (int i = 0; i < weight; i++) {
                weightedGods.add(god);
            }
        }
        
        return weightedGods.get(random.nextInt(weightedGods.size()));
    }
    
    /**
     * Generate an altar at the specified location
     */
    private boolean generateAltar(Location location, GodType godType) {
        AltarStructure altar = new AltarStructure(godType);
        
        // Check if location is suitable for this specific god
        if (!altar.isSuitableLocation(location)) {
            return false;
        }
        
        // Generate the altar structure
        boolean success = altar.generateAt(location);
        
        if (success) {
            // Record the altar location
            String locationKey = getLocationKey(location);
            generatedAltarLocations.add(locationKey);
            
            // Broadcast altar generation
            plugin.getBroadcastManager().broadcastAltarGeneration(location, godType, true);
            
            // Schedule post-generation effects
            schedulePostGenerationEffects(location, godType);
        }
        
        return success;
    }
    
    /**
     * Schedule effects to run after altar generation
     */
    private void schedulePostGenerationEffects(Location location, GodType godType) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Add subtle particle effects around the altar
                createAltarAmbientEffects(location, godType);
            }
        }.runTaskLater(plugin, 20L); // Run after 1 second
    }
    
    /**
     * Create ambient effects around generated altars
     */
    private void createAltarAmbientEffects(Location location, GodType godType) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        
        // Add god-specific ambient effects
        switch (godType) {
            case FALLEN:
                // Dark particles
                world.spawnParticle(org.bukkit.Particle.SMOKE, location.clone().add(0, 2, 0), 5, 1, 1, 1, 0.01);
                break;
            case BANISHMENT:
                // Fire particles
                world.spawnParticle(org.bukkit.Particle.FLAME, location.clone().add(0, 2, 0), 3, 0.5, 0.5, 0.5, 0.01);
                break;
            case ABYSSAL:
                // Water particles
                world.spawnParticle(org.bukkit.Particle.FALLING_WATER, location.clone().add(0, 3, 0), 8, 1, 1, 1, 0.1);
                break;
            case SYLVAN:
                // Nature particles
                world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, location.clone().add(0, 2, 0), 5, 1, 1, 1, 0.1);
                break;
            case TEMPEST:
                // Lightning particles
                world.spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, location.clone().add(0, 3, 0), 3, 0.5, 0.5, 0.5, 0.1);
                break;
            case VEIL:
                // Portal particles
                world.spawnParticle(org.bukkit.Particle.PORTAL, location.clone().add(0, 2, 0), 10, 1, 1, 1, 0.1);
                break;
        }
    }
    
    /**
     * Get a unique key for a chunk
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "_" + chunk.getX() + "_" + chunk.getZ();
    }
    
    /**
     * Get a unique key for a location
     */
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + 
               location.getBlockX() + "_" + 
               location.getBlockY() + "_" + 
               location.getBlockZ();
    }
    
    /**
     * Manually generate an altar at a specific location (admin command)
     */
    public boolean generateAltarAt(Location location, GodType godType) {
        if (!isValidDistance(location)) {
            return false;
        }
        
        boolean success = generateAltar(location, godType);
        if (success) {
            logger.info("Manually generated " + godType.getDisplayName() + " altar at " + 
                       location.getWorld().getName() + " " + 
                       location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            
            // Broadcast manual altar generation
            plugin.getBroadcastManager().broadcastAltarGeneration(location, godType, false);
        }
        
        return success;
    }
    
    /**
     * Get statistics about generated altars
     */
    public Map<String, Object> getGenerationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalAltars = 0;
        Map<GodType, Integer> godCounts = new HashMap<>();
        
        for (String location : generatedAltarLocations) {
            if (!location.endsWith("_processed")) {
                totalAltars++;
                // Could parse god type from location if we stored it
            }
        }
        
        stats.put("total_altars", totalAltars);
        stats.put("god_distribution", godCounts);
        stats.put("enabled", enableNaturalGeneration);
        stats.put("spawn_chance", baseSpawnChance);
        stats.put("min_distance", minDistanceBetweenAltars);
        
        return stats;
    }
    
    /**
     * Reload configuration
     */
    public void reloadConfiguration() {
        loadConfiguration();
        logger.info("Altar Generator configuration reloaded");
    }
    
    /**
     * Clear all generated altar records (for testing)
     */
    public void clearGeneratedAltars() {
        generatedAltarLocations.clear();
        chunkProcessingQueue.clear();
        logger.info("Cleared all generated altar records");
    }
}