package com.example.minecraftplugin.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles database schema migrations
 */
public class DatabaseMigrator {
    
    private final DatabaseManager databaseManager;
    private final Logger logger;
    
    public DatabaseMigrator(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.logger = Logger.getLogger(DatabaseMigrator.class.getName());
    }
    
    /**
     * Run all pending migrations
     */
    public void runMigrations() {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        try {
            createMigrationsTable();
            
            List<Migration> migrations = getMigrations();
            List<String> appliedMigrations = getAppliedMigrations();
            
            for (Migration migration : migrations) {
                if (!appliedMigrations.contains(migration.getName())) {
                    logger.info("Running migration: " + migration.getName());
                    runMigration(migration);
                    recordMigration(migration.getName());
                    logger.info("Migration completed: " + migration.getName());
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Migration error: " + e.getMessage());
        }
    }
    
    /**
     * Create migrations tracking table
     */
    private void createMigrationsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS migrations (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get list of all migrations
     */
    private List<Migration> getMigrations() {
        List<Migration> migrations = new ArrayList<>();
        
        // Player data table
        migrations.add(new Migration("001_create_player_data", """
            CREATE TABLE IF NOT EXISTS player_data (
                player_id VARCHAR(36) PRIMARY KEY,
                death_count INT DEFAULT 0,
                prisoner_of_void BOOLEAN DEFAULT FALSE,
                pledged_god VARCHAR(50),
                completed_testaments JSON,
                collected_fragments JSON,
                last_chest_fragment BIGINT DEFAULT 0,
                last_mob_fragment BIGINT DEFAULT 0,
                ability_cooldowns JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_player_id (player_id),
                INDEX idx_updated_at (updated_at)
            )
            """));
        
        // Raid completions table
        migrations.add(new Migration("002_create_raid_completions", """
            CREATE TABLE IF NOT EXISTS raid_completions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                raid_id VARCHAR(100) NOT NULL,
                player_ids JSON NOT NULL,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                completion_time BIGINT NOT NULL,
                score INT NOT NULL,
                tier VARCHAR(20),
                weekly_challenge BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_raid_id (raid_id),
                INDEX idx_completion_time (completion_time),
                INDEX idx_score (score),
                INDEX idx_created_at (created_at)
            )
            """));
        
        // Guild data table
        migrations.add(new Migration("003_create_guild_data", """
            CREATE TABLE IF NOT EXISTS guild_data (
                guild_id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                leader_id VARCHAR(36) NOT NULL,
                members JSON NOT NULL,
                raids_completed INT DEFAULT 0,
                total_score INT DEFAULT 0,
                best_time BIGINT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_name (name),
                INDEX idx_leader_id (leader_id),
                INDEX idx_raids_completed (raids_completed)
            )
            """));
        
        // Divine Council table
        migrations.add(new Migration("004_create_divine_council", """
            CREATE TABLE IF NOT EXISTS divine_council (
                player_id VARCHAR(36) PRIMARY KEY,
                role VARCHAR(20) NOT NULL,
                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                proposals_created INT DEFAULT 0,
                votes_cast INT DEFAULT 0,
                INDEX idx_role (role),
                INDEX idx_joined_at (joined_at)
            )
            """));
        
        // Council proposals table
        migrations.add(new Migration("005_create_council_proposals", """
            CREATE TABLE IF NOT EXISTS council_proposals (
                id VARCHAR(50) PRIMARY KEY,
                proposer_id VARCHAR(36) NOT NULL,
                title VARCHAR(200) NOT NULL,
                description TEXT NOT NULL,
                type VARCHAR(30) NOT NULL,
                status VARCHAR(20) NOT NULL,
                votes JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NOT NULL,
                executed_at TIMESTAMP NULL,
                INDEX idx_proposer_id (proposer_id),
                INDEX idx_status (status),
                INDEX idx_created_at (created_at),
                INDEX idx_expires_at (expires_at)
            )
            """));
        
        // Transcendence progress table
        migrations.add(new Migration("006_create_transcendence_progress", """
            CREATE TABLE IF NOT EXISTS transcendence_progress (
                player_id VARCHAR(36) PRIMARY KEY,
                completed_challenges INT DEFAULT 0,
                unlocked_abilities JSON,
                last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                level VARCHAR(30) DEFAULT 'NONE',
                INDEX idx_level (level),
                INDEX idx_completed_challenges (completed_challenges)
            )
            """));
        
        // Cross-server sync table
        migrations.add(new Migration("007_create_cross_server_sync", """
            CREATE TABLE IF NOT EXISTS cross_server_sync (
                id INT AUTO_INCREMENT PRIMARY KEY,
                server_id VARCHAR(50) NOT NULL,
                player_id VARCHAR(36) NOT NULL,
                data_type VARCHAR(30) NOT NULL,
                data JSON NOT NULL,
                sync_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                processed BOOLEAN DEFAULT FALSE,
                INDEX idx_server_id (server_id),
                INDEX idx_player_id (player_id),
                INDEX idx_data_type (data_type),
                INDEX idx_sync_time (sync_time),
                INDEX idx_processed (processed)
            )
            """));
        
        return migrations;
    }
    
    /**
     * Get list of applied migrations
     */
    private List<String> getAppliedMigrations() throws SQLException {
        List<String> applied = new ArrayList<>();
        
        String sql = "SELECT name FROM migrations ORDER BY applied_at";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                applied.add(rs.getString("name"));
            }
        }
        
        return applied;
    }
    
    /**
     * Run a specific migration
     */
    private void runMigration(Migration migration) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(migration.getSql())) {
            stmt.executeUpdate();
        }
    }
    
    /**
     * Record that a migration was applied
     */
    private void recordMigration(String migrationName) throws SQLException {
        String sql = "INSERT INTO migrations (name) VALUES (?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, migrationName);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Inner class for migration definition
     */
    private static class Migration {
        private final String name;
        private final String sql;
        
        public Migration(String name, String sql) {
            this.name = name;
            this.sql = sql;
        }
        
        public String getName() { return name; }
        public String getSql() { return sql; }
    }
}