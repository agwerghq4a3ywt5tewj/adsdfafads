package com.example.minecraftplugin.crossserver;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.database.DatabaseManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages cross-server communication and data synchronization
 */
public class CrossServerManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final DatabaseManager databaseManager;
    
    // Server registration
    private final String serverId;
    private final Map<String, ServerInfo> registeredServers;
    private final Map<String, Long> lastHeartbeat;
    
    // Cross-server messaging
    private final Map<String, MessageHandler> messageHandlers;
    
    public CrossServerManager(MinecraftPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.databaseManager = databaseManager;
        this.serverId = generateServerId();
        this.registeredServers = new ConcurrentHashMap<>();
        this.lastHeartbeat = new ConcurrentHashMap<>();
        this.messageHandlers = new HashMap<>();
        
        initializeCrossServer();
        startHeartbeatTask();
        startSyncTask();
        
        logger.info("Cross-Server Manager initialized with ID: " + serverId);
    }
    
    /**
     * Initialize cross-server system
     */
    private void initializeCrossServer() {
        // Register this server
        registerServer();
        
        // Register message handlers
        registerMessageHandlers();
        
        // Discover other servers
        discoverServers();
    }
    
    /**
     * Generate unique server ID
     */
    private String generateServerId() {
        String serverName = plugin.getConfig().getString("server.name", "unknown");
        String serverPort = plugin.getConfig().getString("server.port", "25565");
        return serverName + "_" + serverPort + "_" + System.currentTimeMillis();
    }
    
    /**
     * Register this server in the database
     */
    private void registerServer() {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        String sql = """
            INSERT INTO server_registry (server_id, name, address, port, max_players, 
                                       online_players, last_heartbeat, status)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), 'ONLINE')
            ON DUPLICATE KEY UPDATE
                last_heartbeat = NOW(),
                status = 'ONLINE',
                online_players = VALUES(online_players)
            """;
        
        databaseManager.executeAsync(sql,
            serverId,
            plugin.getConfig().getString("server.name", "Testament Server"),
            plugin.getConfig().getString("server.address", "localhost"),
            plugin.getConfig().getInt("server.port", 25565),
            plugin.getServer().getMaxPlayers(),
            plugin.getServer().getOnlinePlayers().size()
        );
    }
    
    /**
     * Discover other registered servers
     */
    private void discoverServers() {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        String sql = """
            SELECT server_id, name, address, port, max_players, online_players, status
            FROM server_registry 
            WHERE server_id != ? AND status = 'ONLINE' 
            AND last_heartbeat > DATE_SUB(NOW(), INTERVAL 5 MINUTE)
            """;
        
        databaseManager.queryAsync(sql, rs -> {
            registeredServers.clear();
            while (rs.next()) {
                ServerInfo server = new ServerInfo(
                    rs.getString("server_id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getInt("port"),
                    rs.getInt("max_players"),
                    rs.getInt("online_players"),
                    rs.getString("status")
                );
                registeredServers.put(server.getServerId(), server);
            }
            return null;
        }, serverId);
    }
    
    /**
     * Start heartbeat task to maintain server registration
     */
    private void startHeartbeatTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            registerServer();
            discoverServers();
            cleanupOfflineServers();
        }, 0L, 1200L); // Every minute
    }
    
    /**
     * Start data synchronization task
     */
    private void startSyncTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            syncPlayerData();
            syncRaidData();
            syncGuildData();
        }, 0L, 6000L); // Every 5 minutes
    }
    
    /**
     * Sync player data across servers
     */
    private void syncPlayerData() {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        // Get players who need syncing
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Check if player data needs syncing
            String sql = """
                SELECT COUNT(*) as count FROM cross_server_sync 
                WHERE player_id = ? AND data_type = 'PLAYER_DATA' AND processed = FALSE
                """;
            
            databaseManager.queryAsync(sql, rs -> {
                if (rs.next() && rs.getInt("count") > 0) {
                    // Process sync data for this player
                    processSyncData(playerId, "PLAYER_DATA");
                }
                return null;
            }, playerId.toString());
        }
    }
    
    /**
     * Process synchronization data for a player
     */
    private void processSyncData(UUID playerId, String dataType) {
        String sql = """
            SELECT data FROM cross_server_sync 
            WHERE player_id = ? AND data_type = ? AND processed = FALSE
            ORDER BY sync_time DESC LIMIT 1
            """;
        
        databaseManager.queryAsync(sql, rs -> {
            if (rs.next()) {
                String syncData = rs.getString("data");
                // Apply sync data to local player
                applySyncData(playerId, dataType, syncData);
                
                // Mark as processed
                markSyncProcessed(playerId, dataType);
            }
            return null;
        }, playerId.toString(), dataType);
    }
    
    /**
     * Apply synchronized data to local player
     */
    private void applySyncData(UUID playerId, String dataType, String syncData) {
        // This would parse the sync data and apply it to the local player
        // Implementation depends on the specific data format
        logger.info("Applied sync data for player " + playerId + " type " + dataType);
    }
    
    /**
     * Mark sync data as processed
     */
    private void markSyncProcessed(UUID playerId, String dataType) {
        String sql = """
            UPDATE cross_server_sync 
            SET processed = TRUE 
            WHERE player_id = ? AND data_type = ? AND processed = FALSE
            """;
        
        databaseManager.executeAsync(sql, playerId.toString(), dataType);
    }
    
    /**
     * Sync raid data across servers
     */
    private void syncRaidData() {
        // Implementation for syncing raid completions and leaderboards
    }
    
    /**
     * Sync guild data across servers
     */
    private void syncGuildData() {
        // Implementation for syncing guild information
    }
    
    /**
     * Send cross-server message
     */
    public CompletableFuture<Void> sendMessage(String targetServerId, String messageType, Map<String, Object> data) {
        if (!databaseManager.isDatabaseEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        
        String sql = """
            INSERT INTO cross_server_messages (from_server, to_server, message_type, data, created_at)
            VALUES (?, ?, ?, ?, NOW())
            """;
        
        return databaseManager.executeAsync(sql,
            serverId,
            targetServerId,
            messageType,
            serializeData(data)
        );
    }
    
    /**
     * Broadcast message to all servers
     */
    public CompletableFuture<Void> broadcastMessage(String messageType, Map<String, Object> data) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String targetServerId : registeredServers.keySet()) {
            futures.add(sendMessage(targetServerId, messageType, data));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Register message handlers
     */
    private void registerMessageHandlers() {
        messageHandlers.put("RAID_INVITATION", this::handleRaidInvitation);
        messageHandlers.put("PLAYER_TRANSFER", this::handlePlayerTransfer);
        messageHandlers.put("GUILD_UPDATE", this::handleGuildUpdate);
        messageHandlers.put("CONVERGENCE_ANNOUNCEMENT", this::handleConvergenceAnnouncement);
    }
    
    /**
     * Handle raid invitation message
     */
    private void handleRaidInvitation(Map<String, Object> data) {
        String raidId = (String) data.get("raid_id");
        String invitingServer = (String) data.get("server_id");
        List<String> invitedPlayers = (List<String>) data.get("players");
        
        logger.info("Received cross-server raid invitation: " + raidId + " from " + invitingServer);
        
        // Notify invited players
        for (String playerName : invitedPlayers) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerName);
            if (player != null) {
                player.sendMessage("§6§lCross-Server Raid Invitation!");
                player.sendMessage("§7Server: §f" + invitingServer);
                player.sendMessage("§7Raid: §f" + raidId);
                player.sendMessage("§7Use §f/raid join " + raidId + "§7 to participate");
            }
        }
    }
    
    /**
     * Handle player transfer message
     */
    private void handlePlayerTransfer(Map<String, Object> data) {
        String playerId = (String) data.get("player_id");
        String playerData = (String) data.get("data");
        
        logger.info("Received player transfer data for: " + playerId);
        
        // Store transfer data for when player joins
        // Implementation would cache this data
    }
    
    /**
     * Handle guild update message
     */
    private void handleGuildUpdate(Map<String, Object> data) {
        String guildId = (String) data.get("guild_id");
        String updateType = (String) data.get("update_type");
        
        logger.info("Received guild update: " + updateType + " for guild " + guildId);
        
        // Update local guild data
        // Implementation would sync guild changes
    }
    
    /**
     * Handle convergence announcement
     */
    private void handleConvergenceAnnouncement(Map<String, Object> data) {
        String playerName = (String) data.get("player_name");
        String serverName = (String) data.get("server_name");
        
        // Broadcast convergence across all servers
        plugin.getServer().broadcastMessage("§5§l★ CROSS-SERVER CONVERGENCE! ★");
        plugin.getServer().broadcastMessage("§5§l" + playerName + " §r§5on server §5§l" + serverName + 
                                          " §r§5has achieved Divine Convergence!");
    }
    
    /**
     * Clean up offline servers
     */
    private void cleanupOfflineServers() {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        String sql = """
            UPDATE server_registry 
            SET status = 'OFFLINE' 
            WHERE last_heartbeat < DATE_SUB(NOW(), INTERVAL 5 MINUTE) 
            AND status = 'ONLINE'
            """;
        
        databaseManager.executeAsync(sql);
    }
    
    /**
     * Get cross-server statistics
     */
    public CompletableFuture<Map<String, Object>> getCrossServerStats() {
        return databaseManager.getCrossServerStats();
    }
    
    /**
     * Serialize data for cross-server transmission
     */
    private String serializeData(Map<String, Object> data) {
        // Simple serialization - in production, use proper JSON library
        return data.toString();
    }
    
    /**
     * Get registered servers
     */
    public Map<String, ServerInfo> getRegisteredServers() {
        return new HashMap<>(registeredServers);
    }
    
    /**
     * Get this server's ID
     */
    public String getServerId() {
        return serverId;
    }
    
    /**
     * Shutdown cross-server manager
     */
    public void shutdown() {
        // Mark server as offline
        if (databaseManager.isDatabaseEnabled()) {
            String sql = "UPDATE server_registry SET status = 'OFFLINE' WHERE server_id = ?";
            databaseManager.executeAsync(sql, serverId);
        }
        
        logger.info("Cross-Server Manager shutdown complete");
    }
    
    /**
     * Functional interface for message handlers
     */
    @FunctionalInterface
    public interface MessageHandler {
        void handle(Map<String, Object> data);
    }
    
    /**
     * Server information class
     */
    public static class ServerInfo {
        private final String serverId;
        private final String name;
        private final String address;
        private final int port;
        private final int maxPlayers;
        private final int onlinePlayers;
        private final String status;
        
        public ServerInfo(String serverId, String name, String address, int port, 
                         int maxPlayers, int onlinePlayers, String status) {
            this.serverId = serverId;
            this.name = name;
            this.address = address;
            this.port = port;
            this.maxPlayers = maxPlayers;
            this.onlinePlayers = onlinePlayers;
            this.status = status;
        }
        
        // Getters
        public String getServerId() { return serverId; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public int getPort() { return port; }
        public int getMaxPlayers() { return maxPlayers; }
        public int getOnlinePlayers() { return onlinePlayers; }
        public String getStatus() { return status; }
    }
}