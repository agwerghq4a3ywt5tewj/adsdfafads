package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages persistent player data for the Testament System
 * Handles death counts, void prisoner status, pledged gods, completed testaments, and collected fragments
 */
public class PlayerDataManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    // In-memory cache for performance
    private final Map<UUID, PlayerData> playerDataCache;
    
    public PlayerDataManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerDataCache = new HashMap<>();
        
        loadPlayerData();
    }
    
    /**
     * Load player data from file
     */
    private void loadPlayerData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                logger.info("Created new player data file: " + dataFile.getName());
            } catch (IOException e) {
                logger.severe("Could not create player data file: " + e.getMessage());
                return;
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load all player data into cache
        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidString);
                    PlayerData data = loadPlayerDataFromConfig(playerId);
                    playerDataCache.put(playerId, data);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid UUID in player data: " + uuidString);
                }
            }
        }
        
        logger.info("Loaded player data for " + playerDataCache.size() + " players");
    }
    
    /**
     * Load a specific player's data from config
     */
    private PlayerData loadPlayerDataFromConfig(UUID playerId) {
        String path = "players." + playerId.toString();
        
        int deathCount = dataConfig.getInt(path + ".death_count", 0);
        boolean isPrisonerOfVoid = dataConfig.getBoolean(path + ".prisoner_of_void", false);
        String pledgedGodString = dataConfig.getString(path + ".pledged_god", null);
        
        GodType pledgedGod = null;
        if (pledgedGodString != null) {
            pledgedGod = GodType.fromString(pledgedGodString);
        }
        
        // Load completed testaments
        Set<GodType> completedTestaments = new HashSet<>();
        if (dataConfig.contains(path + ".completed_testaments")) {
            List<String> testamentStrings = dataConfig.getStringList(path + ".completed_testaments");
            for (String testamentString : testamentStrings) {
                GodType god = GodType.fromString(testamentString);
                if (god != null) {
                    completedTestaments.add(god);
                }
            }
        }
        
        // Load collected fragments
        Map<GodType, Set<Integer>> collectedFragments = new HashMap<>();
        if (dataConfig.contains(path + ".fragments")) {
            for (String godString : dataConfig.getConfigurationSection(path + ".fragments").getKeys(false)) {
                GodType god = GodType.fromString(godString);
                if (god != null) {
                    List<Integer> fragmentList = dataConfig.getIntegerList(path + ".fragments." + godString);
                    collectedFragments.put(god, new HashSet<>(fragmentList));
                }
            }
        }
        
        // Load cooldown data
        long lastChestFragmentTime = dataConfig.getLong(path + ".cooldowns.last_chest_fragment", 0);
        long lastMobFragmentTime = dataConfig.getLong(path + ".cooldowns.last_mob_fragment", 0);
        
        Map<String, Long> abilityCooldowns = new HashMap<>();
        if (dataConfig.contains(path + ".cooldowns.abilities")) {
            for (String abilityName : dataConfig.getConfigurationSection(path + ".cooldowns.abilities").getKeys(false)) {
                long cooldownTime = dataConfig.getLong(path + ".cooldowns.abilities." + abilityName, 0);
                abilityCooldowns.put(abilityName, cooldownTime);
            }
        }
        
        return new PlayerData(deathCount, isPrisonerOfVoid, pledgedGod, completedTestaments, 
                             collectedFragments, lastChestFragmentTime, lastMobFragmentTime, abilityCooldowns);
    }
    
    /**
     * Save all player data to file
     */
    public void saveAllPlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            savePlayerDataToConfig(entry.getKey(), entry.getValue());
        }
        
        try {
            dataConfig.save(dataFile);
            logger.info("Saved player data for " + playerDataCache.size() + " players");
        } catch (IOException e) {
            logger.severe("Could not save player data: " + e.getMessage());
        }
    }
    
    /**
     * Save a specific player's data to config
     */
    private void savePlayerDataToConfig(UUID playerId, PlayerData data) {
        String path = "players." + playerId.toString();
        
        dataConfig.set(path + ".death_count", data.deathCount);
        dataConfig.set(path + ".prisoner_of_void", data.isPrisonerOfVoid);
        dataConfig.set(path + ".pledged_god", data.pledgedGod != null ? data.pledgedGod.name() : null);
        
        // Save completed testaments
        List<String> testamentStrings = new ArrayList<>();
        for (GodType god : data.completedTestaments) {
            testamentStrings.add(god.name());
        }
        dataConfig.set(path + ".completed_testaments", testamentStrings);
        
        // Save collected fragments
        dataConfig.set(path + ".fragments", null); // Clear existing fragments
        for (Map.Entry<GodType, Set<Integer>> entry : data.collectedFragments.entrySet()) {
            String godName = entry.getKey().name();
            List<Integer> fragmentList = new ArrayList<>(entry.getValue());
            Collections.sort(fragmentList); // Sort for consistency
            dataConfig.set(path + ".fragments." + godName, fragmentList);
        }
        
        // Save cooldown data
        dataConfig.set(path + ".cooldowns.last_chest_fragment", data.lastChestFragmentTime);
        dataConfig.set(path + ".cooldowns.last_mob_fragment", data.lastMobFragmentTime);
        
        // Save ability cooldowns
        dataConfig.set(path + ".cooldowns.abilities", null); // Clear existing abilities
        for (Map.Entry<String, Long> entry : data.abilityCooldowns.entrySet()) {
            dataConfig.set(path + ".cooldowns.abilities." + entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Save a specific player's data immediately
     */
    public void savePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data != null) {
            savePlayerDataToConfig(playerId, data);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                logger.severe("Could not save player data for " + playerId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Get or create player data
     */
    private PlayerData getOrCreatePlayerData(UUID playerId) {
        return playerDataCache.computeIfAbsent(playerId, k -> new PlayerData(0, false, null, new HashSet<>(), new HashMap<>(), 0, 0, new HashMap<>()));
    }
    
    /**
     * Get a player's death count
     */
    public int getDeathCount(Player player) {
        return getDeathCount(player.getUniqueId());
    }
    
    public int getDeathCount(UUID playerId) {
        return getOrCreatePlayerData(playerId).deathCount;
    }
    
    /**
     * Set a player's death count
     */
    public void setDeathCount(Player player, int count) {
        setDeathCount(player.getUniqueId(), count);
    }
    
    public void setDeathCount(UUID playerId, int count) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.deathCount = Math.max(0, count); // Ensure non-negative
        savePlayerData(playerId);
    }
    
    /**
     * Increment a player's death count
     */
    public int incrementDeathCount(Player player) {
        return incrementDeathCount(player.getUniqueId());
    }
    
    public int incrementDeathCount(UUID playerId) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.deathCount++;
        savePlayerData(playerId);
        return data.deathCount;
    }
    
    /**
     * Check if a player is a prisoner of the void
     */
    public boolean isPrisonerOfTheVoid(Player player) {
        return isPrisonerOfTheVoid(player.getUniqueId());
    }
    
    public boolean isPrisonerOfTheVoid(UUID playerId) {
        return getOrCreatePlayerData(playerId).isPrisonerOfVoid;
    }
    
    /**
     * Set a player's prisoner of void status
     */
    public void setPrisonerOfTheVoid(Player player, boolean status) {
        setPrisonerOfTheVoid(player.getUniqueId(), status);
    }
    
    public void setPrisonerOfTheVoid(UUID playerId, boolean status) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.isPrisonerOfVoid = status;
        savePlayerData(playerId);
    }
    
    /**
     * Get a player's pledged god
     */
    public GodType getPledgedGod(Player player) {
        return getPledgedGod(player.getUniqueId());
    }
    
    public GodType getPledgedGod(UUID playerId) {
        return getOrCreatePlayerData(playerId).pledgedGod;
    }
    
    /**
     * Set a player's pledged god
     */
    public void setPledgedGod(Player player, GodType god) {
        setPledgedGod(player.getUniqueId(), god);
    }
    
    public void setPledgedGod(UUID playerId, GodType god) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.pledgedGod = god;
        savePlayerData(playerId);
    }
    
    /**
     * Check if a player has pledged to any god
     */
    public boolean hasPledged(Player player) {
        return hasPledged(player.getUniqueId());
    }
    
    public boolean hasPledged(UUID playerId) {
        return getPledgedGod(playerId) != null;
    }
    
    /**
     * Get completed testaments for a player
     */
    public Set<GodType> getCompletedTestaments(Player player) {
        return getCompletedTestaments(player.getUniqueId());
    }
    
    public Set<GodType> getCompletedTestaments(UUID playerId) {
        return new HashSet<>(getOrCreatePlayerData(playerId).completedTestaments);
    }
    
    /**
     * Add a completed testament for a player
     */
    public void addCompletedTestament(Player player, GodType god) {
        addCompletedTestament(player.getUniqueId(), god);
    }
    
    public void addCompletedTestament(UUID playerId, GodType god) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.completedTestaments.add(god);
        savePlayerData(playerId);
    }
    
    /**
     * Check if a player has completed a testament
     */
    public boolean hasCompletedTestament(Player player, GodType god) {
        return hasCompletedTestament(player.getUniqueId(), god);
    }
    
    public boolean hasCompletedTestament(UUID playerId, GodType god) {
        return getOrCreatePlayerData(playerId).completedTestaments.contains(god);
    }
    
    /**
     * Get collected fragments for a player and god
     */
    public Set<Integer> getPlayerFragments(Player player, GodType god) {
        return getPlayerFragments(player.getUniqueId(), god);
    }
    
    public Set<Integer> getPlayerFragments(UUID playerId, GodType god) {
        PlayerData data = getOrCreatePlayerData(playerId);
        return new HashSet<>(data.collectedFragments.getOrDefault(god, new HashSet<>()));
    }
    
    /**
     * Get last chest fragment time for a player
     */
    public long getLastChestFragmentTime(Player player) {
        return getLastChestFragmentTime(player.getUniqueId());
    }
    
    public long getLastChestFragmentTime(UUID playerId) {
        return getOrCreatePlayerData(playerId).lastChestFragmentTime;
    }
    
    /**
     * Set last chest fragment time for a player
     */
    public void setLastChestFragmentTime(Player player, long time) {
        setLastChestFragmentTime(player.getUniqueId(), time);
    }
    
    public void setLastChestFragmentTime(UUID playerId, long time) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.lastChestFragmentTime = time;
        savePlayerData(playerId);
    }
    
    /**
     * Get last mob fragment time for a player
     */
    public long getLastMobFragmentTime(Player player) {
        return getLastMobFragmentTime(player.getUniqueId());
    }
    
    public long getLastMobFragmentTime(UUID playerId) {
        return getOrCreatePlayerData(playerId).lastMobFragmentTime;
    }
    
    /**
     * Set last mob fragment time for a player
     */
    public void setLastMobFragmentTime(Player player, long time) {
        setLastMobFragmentTime(player.getUniqueId(), time);
    }
    
    public void setLastMobFragmentTime(UUID playerId, long time) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.lastMobFragmentTime = time;
        savePlayerData(playerId);
    }
    
    /**
     * Get ability cooldown time for a player
     */
    public long getAbilityCooldownTime(Player player, String abilityName) {
        return getAbilityCooldownTime(player.getUniqueId(), abilityName);
    }
    
    public long getAbilityCooldownTime(UUID playerId, String abilityName) {
        PlayerData data = getOrCreatePlayerData(playerId);
        return data.abilityCooldowns.getOrDefault(abilityName, 0L);
    }
    
    /**
     * Set ability cooldown time for a player
     */
    public void setAbilityCooldownTime(Player player, String abilityName, long time) {
        setAbilityCooldownTime(player.getUniqueId(), abilityName, time);
    }
    
    public void setAbilityCooldownTime(UUID playerId, String abilityName, long time) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.abilityCooldowns.put(abilityName, time);
        savePlayerData(playerId);
    }
    
    /**
     * Clear all cooldowns for a player
     */
    public void clearAllCooldowns(Player player) {
        clearAllCooldowns(player.getUniqueId());
    }
    
    public void clearAllCooldowns(UUID playerId) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.lastChestFragmentTime = 0;
        data.lastMobFragmentTime = 0;
        data.abilityCooldowns.clear();
        savePlayerData(playerId);
    }
    
    /**
     * Add a fragment for a player
     */
    public void addFragment(Player player, GodType god, int fragmentNumber) {
        addFragment(player.getUniqueId(), god, fragmentNumber);
    }
    
    public void addFragment(UUID playerId, GodType god, int fragmentNumber) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.collectedFragments.computeIfAbsent(god, k -> new HashSet<>()).add(fragmentNumber);
        savePlayerData(playerId);
    }
    
    /**
     * Check if a player has a specific fragment
     */
    public boolean hasFragment(Player player, GodType god, int fragmentNumber) {
        return hasFragment(player.getUniqueId(), god, fragmentNumber);
    }
    
    public boolean hasFragment(UUID playerId, GodType god, int fragmentNumber) {
        return getPlayerFragments(playerId, god).contains(fragmentNumber);
    }
    
    /**
     * Check if a player has all fragments for a god
     */
    public boolean hasAllFragments(Player player, GodType god) {
        return hasAllFragments(player.getUniqueId(), god);
    }
    
    public boolean hasAllFragments(UUID playerId, GodType god) {
        Set<Integer> fragments = getPlayerFragments(playerId, god);
        return fragments.size() == 7 && fragments.containsAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
    }
    
    /**
     * Get a missing fragment number for a god (returns -1 if all fragments are collected)
     */
    public int getMissingFragment(Player player, GodType god) {
        return getMissingFragment(player.getUniqueId(), god);
    }
    
    public int getMissingFragment(UUID playerId, GodType god) {
        Set<Integer> fragments = getPlayerFragments(playerId, god);
        
        for (int i = 1; i <= 7; i++) {
            if (!fragments.contains(i)) {
                return i;
            }
        }
        
        return -1; // All fragments collected
    }
    
    /**
     * Get the number of completed testaments for a player
     */
    public int getTestamentCount(Player player) {
        return getTestamentCount(player.getUniqueId());
    }
    
    public int getTestamentCount(UUID playerId) {
        return getOrCreatePlayerData(playerId).completedTestaments.size();
    }
    
    /**
     * Reset all data for a player (useful for admin commands)
     */
    public void resetPlayerData(Player player) {
        resetPlayerData(player.getUniqueId());
    }
    
    public void resetPlayerData(UUID playerId) {
        PlayerData data = getOrCreatePlayerData(playerId);
        data.deathCount = 0;
        data.isPrisonerOfVoid = false;
        data.pledgedGod = null;
        data.completedTestaments.clear();
        data.collectedFragments.clear();
        data.lastChestFragmentTime = 0;
        data.lastMobFragmentTime = 0;
        data.abilityCooldowns.clear();
        savePlayerData(playerId);
        
        logger.info("Reset all data for player: " + playerId);
    }
    
    /**
     * Get player data summary for display
     */
    public String getPlayerDataSummary(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData data = getOrCreatePlayerData(playerId);
        
        StringBuilder summary = new StringBuilder();
        summary.append("§6§l=== PLAYER DATA SUMMARY ===§r\n");
        summary.append("§7Player: §f").append(player.getName()).append("\n");
        summary.append("§7Death Count: §f").append(data.deathCount).append("\n");
        summary.append("§7Prisoner of Void: §f").append(data.isPrisonerOfVoid ? "§cYes" : "§aNo").append("\n");
        summary.append("§7Pledged God: §f").append(data.pledgedGod != null ? data.pledgedGod.getDisplayName() : "§7None");
        summary.append("\n§7Completed Testaments: §f").append(data.completedTestaments.size()).append("/12");
        
        // Show fragment progress summary
        int totalFragments = 0;
        for (Set<Integer> fragments : data.collectedFragments.values()) {
            totalFragments += fragments.size();
        }
        summary.append("\n§7Total Fragments: §f").append(totalFragments);
        
        return summary.toString();
    }
    
    /**
     * Shutdown method to save all data
     */
    public void shutdown() {
        saveAllPlayerData();
        logger.info("PlayerDataManager shutdown complete");
    }
    
    /**
     * Inner class to hold player data
     */
    private static class PlayerData {
        int deathCount;
        boolean isPrisonerOfVoid;
        GodType pledgedGod;
        Set<GodType> completedTestaments;
        Map<GodType, Set<Integer>> collectedFragments;
        long lastChestFragmentTime;
        long lastMobFragmentTime;
        Map<String, Long> abilityCooldowns;
        
        PlayerData(int deathCount, boolean isPrisonerOfVoid, GodType pledgedGod, 
                  Set<GodType> completedTestaments, Map<GodType, Set<Integer>> collectedFragments,
                  long lastChestFragmentTime, long lastMobFragmentTime, Map<String, Long> abilityCooldowns) {
            this.deathCount = deathCount;
            this.isPrisonerOfVoid = isPrisonerOfVoid;
            this.pledgedGod = pledgedGod;
            this.completedTestaments = completedTestaments;
            this.collectedFragments = collectedFragments;
            this.lastChestFragmentTime = lastChestFragmentTime;
            this.lastMobFragmentTime = lastMobFragmentTime;
            this.abilityCooldowns = abilityCooldowns;
        }
    }
}