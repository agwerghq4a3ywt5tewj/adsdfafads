package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.raids.RaidCompletionRecord;
import com.example.minecraftplugin.raids.RaidManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages raid completion records and leaderboards
 */
public class RaidLeaderboardManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final File leaderboardFile;
    private FileConfiguration leaderboardConfig;
    
    // In-memory cache for performance
    private final List<RaidCompletionRecord> completionRecords;
    
    public RaidLeaderboardManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.leaderboardFile = new File(plugin.getDataFolder(), "raid_leaderboards.yml");
        this.completionRecords = new ArrayList<>();
        
        loadLeaderboardData();
        
        logger.info("Raid Leaderboard Manager initialized with " + completionRecords.size() + " records");
    }
    
    /**
     * Load leaderboard data from file
     */
    private void loadLeaderboardData() {
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.getParentFile().mkdirs();
                leaderboardFile.createNewFile();
                logger.info("Created new raid leaderboard file: " + leaderboardFile.getName());
            } catch (IOException e) {
                logger.severe("Could not create raid leaderboard file: " + e.getMessage());
                return;
            }
        }
        
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);
        
        // Load completion records
        if (leaderboardConfig.contains("completions")) {
            for (String recordId : leaderboardConfig.getConfigurationSection("completions").getKeys(false)) {
                try {
                    RaidCompletionRecord record = loadRecordFromConfig(recordId);
                    if (record != null) {
                        completionRecords.add(record);
                    }
                } catch (Exception e) {
                    logger.warning("Failed to load completion record " + recordId + ": " + e.getMessage());
                }
            }
        }
        
        logger.info("Loaded " + completionRecords.size() + " raid completion records");
    }
    
    /**
     * Load a specific record from config
     */
    private RaidCompletionRecord loadRecordFromConfig(String recordId) {
        String path = "completions." + recordId;
        
        String raidId = leaderboardConfig.getString(path + ".raid_id");
        String raidDisplayName = leaderboardConfig.getString(path + ".raid_display_name");
        List<String> playerIdStrings = leaderboardConfig.getStringList(path + ".player_ids");
        List<String> playerNames = leaderboardConfig.getStringList(path + ".player_names");
        long startTime = leaderboardConfig.getLong(path + ".start_time");
        long endTime = leaderboardConfig.getLong(path + ".end_time");
        int score = leaderboardConfig.getInt(path + ".score");
        String tierString = leaderboardConfig.getString(path + ".tier");
        boolean weeklyChallengeActive = leaderboardConfig.getBoolean(path + ".weekly_challenge_active");
        String weeklyChallengeType = leaderboardConfig.getString(path + ".weekly_challenge_type");
        
        // Convert player ID strings to UUIDs
        List<UUID> playerIds = new ArrayList<>();
        for (String idString : playerIdStrings) {
            try {
                playerIds.add(UUID.fromString(idString));
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid UUID in record " + recordId + ": " + idString);
            }
        }
        
        // Parse tier
        RaidManager.RaidTier tier;
        try {
            tier = RaidManager.RaidTier.valueOf(tierString);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid tier in record " + recordId + ": " + tierString);
            tier = RaidManager.RaidTier.NOVICE;
        }
        
        return new RaidCompletionRecord(raidId, raidDisplayName, playerIds, playerNames, 
                                       startTime, endTime, score, tier, weeklyChallengeActive, 
                                       weeklyChallengeType);
    }
    
    /**
     * Add a new completion record
     */
    public void addCompletionRecord(RaidCompletionRecord record) {
        completionRecords.add(record);
        saveRecordToConfig(record);
        
        logger.info("Added raid completion record: " + record.getRaidDisplayName() + 
                   " completed by " + String.join(", ", record.getPlayerNames()) + 
                   " in " + record.getFormattedCompletionTime());
    }
    
    /**
     * Save a record to config
     */
    private void saveRecordToConfig(RaidCompletionRecord record) {
        String recordId = "record_" + record.getEndTime();
        String path = "completions." + recordId;
        
        leaderboardConfig.set(path + ".raid_id", record.getRaidId());
        leaderboardConfig.set(path + ".raid_display_name", record.getRaidDisplayName());
        
        // Convert UUIDs to strings
        List<String> playerIdStrings = record.getPlayerIds().stream()
            .map(UUID::toString)
            .collect(Collectors.toList());
        leaderboardConfig.set(path + ".player_ids", playerIdStrings);
        leaderboardConfig.set(path + ".player_names", record.getPlayerNames());
        leaderboardConfig.set(path + ".start_time", record.getStartTime());
        leaderboardConfig.set(path + ".end_time", record.getEndTime());
        leaderboardConfig.set(path + ".score", record.getScore());
        leaderboardConfig.set(path + ".tier", record.getTier().name());
        leaderboardConfig.set(path + ".weekly_challenge_active", record.isWeeklyChallengeActive());
        leaderboardConfig.set(path + ".weekly_challenge_type", record.getWeeklyChallengeType());
        
        // Save to file
        try {
            leaderboardConfig.save(leaderboardFile);
        } catch (IOException e) {
            logger.severe("Could not save raid leaderboard: " + e.getMessage());
        }
    }
    
    /**
     * Get top completions for a specific raid
     */
    public List<RaidCompletionRecord> getTopCompletions(String raidId, int limit) {
        return completionRecords.stream()
            .filter(record -> record.getRaidId().equals(raidId))
            .sorted(Comparator.comparingLong(RaidCompletionRecord::getCompletionTime))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get top completions by score
     */
    public List<RaidCompletionRecord> getTopCompletionsByScore(int limit) {
        return completionRecords.stream()
            .sorted(Comparator.comparingInt(RaidCompletionRecord::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get top completions for a specific tier
     */
    public List<RaidCompletionRecord> getTopCompletionsByTier(RaidManager.RaidTier tier, int limit) {
        return completionRecords.stream()
            .filter(record -> record.getTier() == tier)
            .sorted(Comparator.comparingLong(RaidCompletionRecord::getCompletionTime))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get completions for a specific player
     */
    public List<RaidCompletionRecord> getPlayerCompletions(UUID playerId) {
        return completionRecords.stream()
            .filter(record -> record.getPlayerIds().contains(playerId))
            .sorted(Comparator.comparingLong(RaidCompletionRecord::getEndTime).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Get weekly challenge completions
     */
    public List<RaidCompletionRecord> getWeeklyChallengeCompletions(int limit) {
        return completionRecords.stream()
            .filter(RaidCompletionRecord::isWeeklyChallengeActive)
            .sorted(Comparator.comparingInt(RaidCompletionRecord::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get leaderboard statistics
     */
    public Map<String, Object> getLeaderboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_completions", completionRecords.size());
        
        // Count by tier
        Map<RaidManager.RaidTier, Long> tierCounts = completionRecords.stream()
            .collect(Collectors.groupingBy(RaidCompletionRecord::getTier, Collectors.counting()));
        stats.put("completions_by_tier", tierCounts);
        
        // Count by raid
        Map<String, Long> raidCounts = completionRecords.stream()
            .collect(Collectors.groupingBy(RaidCompletionRecord::getRaidId, Collectors.counting()));
        stats.put("completions_by_raid", raidCounts);
        
        // Weekly challenge stats
        long weeklyChallengeCompletions = completionRecords.stream()
            .filter(RaidCompletionRecord::isWeeklyChallengeActive)
            .count();
        stats.put("weekly_challenge_completions", weeklyChallengeCompletions);
        
        // Average completion times by tier
        Map<RaidManager.RaidTier, Double> avgTimes = new HashMap<>();
        for (RaidManager.RaidTier tier : RaidManager.RaidTier.values()) {
            OptionalDouble avgTime = completionRecords.stream()
                .filter(record -> record.getTier() == tier)
                .mapToLong(RaidCompletionRecord::getCompletionTime)
                .average();
            if (avgTime.isPresent()) {
                avgTimes.put(tier, avgTime.getAsDouble() / 1000.0); // Convert to seconds
            }
        }
        stats.put("average_completion_times_seconds", avgTimes);
        
        return stats;
    }
    
    /**
     * Clean up old records (keep only last 1000)
     */
    public void cleanupOldRecords() {
        if (completionRecords.size() <= 1000) {
            return;
        }
        
        // Sort by end time and keep only the most recent 1000
        completionRecords.sort(Comparator.comparingLong(RaidCompletionRecord::getEndTime).reversed());
        List<RaidCompletionRecord> toRemove = completionRecords.subList(1000, completionRecords.size());
        toRemove.clear();
        
        // Rebuild config file
        rebuildConfigFile();
        
        logger.info("Cleaned up old raid records, kept most recent 1000");
    }
    
    /**
     * Rebuild the entire config file
     */
    private void rebuildConfigFile() {
        leaderboardConfig = new YamlConfiguration();
        
        for (RaidCompletionRecord record : completionRecords) {
            saveRecordToConfig(record);
        }
    }
    
    /**
     * Shutdown and save all data
     */
    public void shutdown() {
        try {
            leaderboardConfig.save(leaderboardFile);
            logger.info("Raid Leaderboard Manager shutdown complete");
        } catch (IOException e) {
            logger.severe("Could not save raid leaderboard on shutdown: " + e.getMessage());
        }
    }
}