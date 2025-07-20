package com.example.minecraftplugin.database;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Manages database connections and operations for the Testament System
 */
public class DatabaseManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private HikariDataSource dataSource;
    private boolean useDatabase;
    private final DatabaseMigrator migrator;
    
    public DatabaseManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.migrator = new DatabaseMigrator(this);
        
        initializeDatabase();
    }
    
    /**
     * Initialize database connection
     */
    private void initializeDatabase() {
        useDatabase = plugin.getConfig().getBoolean("database.enabled", false);
        
        if (!useDatabase) {
            logger.info("Database disabled, using YAML storage");
            return;
        }
        
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(plugin.getConfig().getString("database.url", "jdbc:mysql://localhost:3306/testament"));
            config.setUsername(plugin.getConfig().getString("database.username", "testament"));
            config.setPassword(plugin.getConfig().getString("database.password", "password"));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection pool settings
            config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool.max_size", 10));
            config.setMinimumIdle(plugin.getConfig().getInt("database.pool.min_idle", 2));
            config.setConnectionTimeout(plugin.getConfig().getLong("database.pool.connection_timeout", 30000));
            config.setIdleTimeout(plugin.getConfig().getLong("database.pool.idle_timeout", 600000));
            config.setMaxLifetime(plugin.getConfig().getLong("database.pool.max_lifetime", 1800000));
            
            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Database connection established successfully");
            }
            
            // Run migrations
            migrator.runMigrations();
            
        } catch (Exception e) {
            logger.severe("Failed to initialize database: " + e.getMessage());
            useDatabase = false;
        }
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (!useDatabase || dataSource == null) {
            throw new SQLException("Database not available");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Execute query asynchronously
     */
    public CompletableFuture<Void> executeAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.severe("Database error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Execute query and return result asynchronously
     */
    public <T> CompletableFuture<T> queryAsync(String sql, ResultSetMapper<T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return mapper.map(rs);
                }
            } catch (SQLException e) {
                logger.severe("Database query error: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Save player data to database
     */
    public CompletableFuture<Void> savePlayerData(UUID playerId, Map<String, Object> data) {
        if (!useDatabase) {
            return CompletableFuture.completedFuture(null);
        }
        
        String sql = """
            INSERT INTO player_data (player_id, death_count, prisoner_of_void, pledged_god, 
                                   completed_testaments, collected_fragments, last_chest_fragment, 
                                   last_mob_fragment, ability_cooldowns, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            ON DUPLICATE KEY UPDATE
                death_count = VALUES(death_count),
                prisoner_of_void = VALUES(prisoner_of_void),
                pledged_god = VALUES(pledged_god),
                completed_testaments = VALUES(completed_testaments),
                collected_fragments = VALUES(collected_fragments),
                last_chest_fragment = VALUES(last_chest_fragment),
                last_mob_fragment = VALUES(last_mob_fragment),
                ability_cooldowns = VALUES(ability_cooldowns),
                updated_at = NOW()
            """;
        
        return executeAsync(sql,
            playerId.toString(),
            data.get("death_count"),
            data.get("prisoner_of_void"),
            data.get("pledged_god"),
            serializeObject(data.get("completed_testaments")),
            serializeObject(data.get("collected_fragments")),
            data.get("last_chest_fragment"),
            data.get("last_mob_fragment"),
            serializeObject(data.get("ability_cooldowns"))
        );
    }
    
    /**
     * Load player data from database
     */
    public CompletableFuture<Map<String, Object>> loadPlayerData(UUID playerId) {
        if (!useDatabase) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        String sql = "SELECT * FROM player_data WHERE player_id = ?";
        
        return queryAsync(sql, rs -> {
            Map<String, Object> data = new HashMap<>();
            if (rs.next()) {
                data.put("death_count", rs.getInt("death_count"));
                data.put("prisoner_of_void", rs.getBoolean("prisoner_of_void"));
                data.put("pledged_god", rs.getString("pledged_god"));
                data.put("completed_testaments", deserializeObject(rs.getString("completed_testaments")));
                data.put("collected_fragments", deserializeObject(rs.getString("collected_fragments")));
                data.put("last_chest_fragment", rs.getLong("last_chest_fragment"));
                data.put("last_mob_fragment", rs.getLong("last_mob_fragment"));
                data.put("ability_cooldowns", deserializeObject(rs.getString("ability_cooldowns")));
            }
            return data;
        }, playerId.toString());
    }
    
    /**
     * Save raid completion record
     */
    public CompletableFuture<Void> saveRaidCompletion(String raidId, List<UUID> playerIds, 
                                                     long startTime, long endTime, int score) {
        if (!useDatabase) {
            return CompletableFuture.completedFuture(null);
        }
        
        String sql = """
            INSERT INTO raid_completions (raid_id, player_ids, start_time, end_time, 
                                        completion_time, score, created_at)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        
        return executeAsync(sql,
            raidId,
            serializeObject(playerIds),
            new Timestamp(startTime),
            new Timestamp(endTime),
            endTime - startTime,
            score
        );
    }
    
    /**
     * Get cross-server statistics
     */
    public CompletableFuture<Map<String, Object>> getCrossServerStats() {
        if (!useDatabase) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        String sql = """
            SELECT 
                COUNT(DISTINCT player_id) as total_players,
                SUM(CASE WHEN JSON_LENGTH(completed_testaments) >= 12 THEN 1 ELSE 0 END) as converged_players,
                AVG(JSON_LENGTH(completed_testaments)) as avg_testaments,
                COUNT(*) as total_records
            FROM player_data
            """;
        
        return queryAsync(sql, rs -> {
            Map<String, Object> stats = new HashMap<>();
            if (rs.next()) {
                stats.put("total_players", rs.getInt("total_players"));
                stats.put("converged_players", rs.getInt("converged_players"));
                stats.put("avg_testaments", rs.getDouble("avg_testaments"));
                stats.put("total_records", rs.getInt("total_records"));
            }
            return stats;
        });
    }
    
    /**
     * Backup database to file
     */
    public CompletableFuture<Boolean> backupDatabase(String backupPath) {
        if (!useDatabase) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String command = String.format("mysqldump -u %s -p%s %s > %s",
                    plugin.getConfig().getString("database.username"),
                    plugin.getConfig().getString("database.password"),
                    getDatabaseName(),
                    backupPath
                );
                
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    logger.info("Database backup completed: " + backupPath);
                    return true;
                } else {
                    logger.warning("Database backup failed with exit code: " + exitCode);
                    return false;
                }
            } catch (Exception e) {
                logger.severe("Database backup error: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Serialize object to JSON string
     */
    private String serializeObject(Object obj) {
        if (obj == null) return null;
        // Simple JSON serialization - in production, use a proper JSON library
        return obj.toString();
    }
    
    /**
     * Deserialize JSON string to object
     */
    private Object deserializeObject(String json) {
        if (json == null) return null;
        // Simple JSON deserialization - in production, use a proper JSON library
        return json;
    }
    
    /**
     * Get database name from URL
     */
    private String getDatabaseName() {
        String url = plugin.getConfig().getString("database.url", "");
        return url.substring(url.lastIndexOf("/") + 1);
    }
    
    /**
     * Check if database is enabled and available
     */
    public boolean isDatabaseEnabled() {
        return useDatabase && dataSource != null;
    }
    
    /**
     * Shutdown database connections
     */
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connections closed");
        }
    }
    
    /**
     * Functional interface for mapping ResultSet to objects
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}