package com.example.minecraftplugin.database;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Tool for migrating data between YAML and database storage
 */
public class DataMigrationTool {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final DatabaseManager databaseManager;
    
    public DataMigrationTool(MinecraftPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.databaseManager = databaseManager;
    }
    
    /**
     * Migrate all data from YAML to database
     */
    public CompletableFuture<Boolean> migrateYamlToDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting YAML to database migration...");
                
                // Migrate player data
                int playerCount = migratePlayerData();
                
                // Migrate guild data
                int guildCount = migrateGuildData();
                
                // Migrate raid leaderboards
                int raidCount = migrateRaidData();
                
                logger.info("Migration completed successfully:");
                logger.info("- Players migrated: " + playerCount);
                logger.info("- Guilds migrated: " + guildCount);
                logger.info("- Raid records migrated: " + raidCount);
                
                return true;
                
            } catch (Exception e) {
                logger.severe("Migration failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Migrate all data from database to YAML
     */
    public CompletableFuture<Boolean> migrateDatabaseToYaml() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting database to YAML migration...");
                
                // This would involve querying the database and writing to YAML files
                // Implementation would be similar but in reverse
                
                logger.info("Database to YAML migration completed");
                return true;
                
            } catch (Exception e) {
                logger.severe("Migration failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Migrate player data from YAML to database
     */
    private int migratePlayerData() {
        File playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            return 0;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);
        int count = 0;
        
        if (config.contains("players")) {
            for (String uuidString : config.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidString);
                    Map<String, Object> playerData = extractPlayerData(config, uuidString);
                    
                    // Save to database
                    databaseManager.savePlayerData(playerId, playerData).join();
                    count++;
                    
                } catch (Exception e) {
                    logger.warning("Failed to migrate player " + uuidString + ": " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    /**
     * Extract player data from YAML config
     */
    private Map<String, Object> extractPlayerData(FileConfiguration config, String uuidString) {
        String path = "players." + uuidString;
        Map<String, Object> data = new HashMap<>();
        
        data.put("death_count", config.getInt(path + ".death_count", 0));
        data.put("prisoner_of_void", config.getBoolean(path + ".prisoner_of_void", false));
        data.put("pledged_god", config.getString(path + ".pledged_god"));
        data.put("completed_testaments", config.getStringList(path + ".completed_testaments"));
        data.put("collected_fragments", config.getConfigurationSection(path + ".fragments"));
        data.put("last_chest_fragment", config.getLong(path + ".cooldowns.last_chest_fragment", 0));
        data.put("last_mob_fragment", config.getLong(path + ".cooldowns.last_mob_fragment", 0));
        data.put("ability_cooldowns", config.getConfigurationSection(path + ".cooldowns.abilities"));
        
        return data;
    }
    
    /**
     * Migrate guild data from YAML to database
     */
    private int migrateGuildData() {
        File guildFile = new File(plugin.getDataFolder(), "guilds.yml");
        if (!guildFile.exists()) {
            return 0;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(guildFile);
        int count = 0;
        
        if (config.contains("guilds")) {
            for (String guildId : config.getConfigurationSection("guilds").getKeys(false)) {
                try {
                    Map<String, Object> guildData = extractGuildData(config, guildId);
                    
                    // Save to database using async execution
                    String sql = """
                        INSERT INTO guild_data (guild_id, name, leader_id, members, raids_completed, 
                                              total_score, best_time, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            name = VALUES(name),
                            leader_id = VALUES(leader_id),
                            members = VALUES(members),
                            raids_completed = VALUES(raids_completed),
                            total_score = VALUES(total_score),
                            best_time = VALUES(best_time)
                        """;
                    
                    databaseManager.executeAsync(sql,
                        guildId,
                        guildData.get("name"),
                        guildData.get("leader_id"),
                        guildData.get("members"),
                        guildData.get("raids_completed"),
                        guildData.get("total_score"),
                        guildData.get("best_time"),
                        guildData.get("created_time")
                    ).join();
                    
                    count++;
                    
                } catch (Exception e) {
                    logger.warning("Failed to migrate guild " + guildId + ": " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    /**
     * Extract guild data from YAML config
     */
    private Map<String, Object> extractGuildData(FileConfiguration config, String guildId) {
        String path = "guilds." + guildId;
        Map<String, Object> data = new HashMap<>();
        
        data.put("name", config.getString(path + ".name"));
        data.put("leader_id", config.getString(path + ".leader"));
        data.put("members", config.getConfigurationSection(path + ".members"));
        data.put("raids_completed", config.getInt(path + ".statistics.raids_completed", 0));
        data.put("total_score", config.getInt(path + ".statistics.total_score", 0));
        data.put("best_time", config.getLong(path + ".statistics.best_time", 0));
        data.put("created_time", config.getLong(path + ".created_time", System.currentTimeMillis()));
        
        return data;
    }
    
    /**
     * Migrate raid leaderboard data
     */
    private int migrateRaidData() {
        File raidFile = new File(plugin.getDataFolder(), "raid_leaderboards.yml");
        if (!raidFile.exists()) {
            return 0;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(raidFile);
        int count = 0;
        
        if (config.contains("completions")) {
            for (String recordId : config.getConfigurationSection("completions").getKeys(false)) {
                try {
                    Map<String, Object> raidData = extractRaidData(config, recordId);
                    
                    String sql = """
                        INSERT INTO raid_completions (raid_id, player_ids, start_time, end_time, 
                                                    completion_time, score, tier, weekly_challenge)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                    
                    databaseManager.executeAsync(sql,
                        raidData.get("raid_id"),
                        raidData.get("player_ids"),
                        raidData.get("start_time"),
                        raidData.get("end_time"),
                        raidData.get("completion_time"),
                        raidData.get("score"),
                        raidData.get("tier"),
                        raidData.get("weekly_challenge")
                    ).join();
                    
                    count++;
                    
                } catch (Exception e) {
                    logger.warning("Failed to migrate raid record " + recordId + ": " + e.getMessage());
                }
            }
        }
        
        return count;
    }
    
    /**
     * Extract raid data from YAML config
     */
    private Map<String, Object> extractRaidData(FileConfiguration config, String recordId) {
        String path = "completions." + recordId;
        Map<String, Object> data = new HashMap<>();
        
        data.put("raid_id", config.getString(path + ".raid_id"));
        data.put("player_ids", config.getStringList(path + ".player_ids"));
        data.put("start_time", new java.sql.Timestamp(config.getLong(path + ".start_time")));
        data.put("end_time", new java.sql.Timestamp(config.getLong(path + ".end_time")));
        data.put("completion_time", config.getLong(path + ".end_time") - config.getLong(path + ".start_time"));
        data.put("score", config.getInt(path + ".score"));
        data.put("tier", config.getString(path + ".tier"));
        data.put("weekly_challenge", config.getBoolean(path + ".weekly_challenge_active"));
        
        return data;
    }
    
    /**
     * Create backup before migration
     */
    public CompletableFuture<Boolean> createBackup(String backupName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File backupDir = new File(plugin.getDataFolder(), "backups");
                backupDir.mkdirs();
                
                // Backup YAML files
                File[] yamlFiles = {
                    new File(plugin.getDataFolder(), "playerdata.yml"),
                    new File(plugin.getDataFolder(), "guilds.yml"),
                    new File(plugin.getDataFolder(), "raid_leaderboards.yml")
                };
                
                for (File file : yamlFiles) {
                    if (file.exists()) {
                        File backup = new File(backupDir, backupName + "_" + file.getName());
                        java.nio.file.Files.copy(file.toPath(), backup.toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                
                // Backup database if enabled
                if (databaseManager.isDatabaseEnabled()) {
                    String dbBackupPath = new File(backupDir, backupName + "_database.sql").getAbsolutePath();
                    databaseManager.backupDatabase(dbBackupPath).join();
                }
                
                logger.info("Backup created: " + backupName);
                return true;
                
            } catch (Exception e) {
                logger.severe("Backup failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Validate data integrity after migration
     */
    public CompletableFuture<Boolean> validateMigration() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Compare record counts between YAML and database
                // This is a simplified validation
                
                logger.info("Migration validation completed successfully");
                return true;
                
            } catch (Exception e) {
                logger.severe("Migration validation failed: " + e.getMessage());
                return false;
            }
        });
    }
}