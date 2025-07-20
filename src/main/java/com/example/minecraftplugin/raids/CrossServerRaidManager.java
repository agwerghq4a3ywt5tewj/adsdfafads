package com.example.minecraftplugin.raids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.crossserver.CrossServerManager;
import com.example.minecraftplugin.database.DatabaseManager;

/**
 * Manages cross-server raid participation and coordination
 */
public class CrossServerRaidManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final CrossServerManager crossServerManager;
    private final DatabaseManager databaseManager;
    private final RaidManager localRaidManager;
    
    // Cross-server raid tracking
    private final Map<String, CrossServerRaid> activeCrossServerRaids;
    private final Map<UUID, String> playerRaidInvitations;
    private final Map<String, List<String>> raidServerParticipants;
    
    public CrossServerRaidManager(MinecraftPlugin plugin, CrossServerManager crossServerManager,
                                 DatabaseManager databaseManager, RaidManager localRaidManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.crossServerManager = crossServerManager;
        this.databaseManager = databaseManager;
        this.localRaidManager = localRaidManager;
        this.activeCrossServerRaids = new HashMap<>();
        this.playerRaidInvitations = new HashMap<>();
        this.raidServerParticipants = new HashMap<>();
        
        initializeCrossServerRaids();
        
        logger.info("Cross-Server Raid Manager initialized");
    }
    
    /**
     * Initialize cross-server raid system
     */
    private void initializeCrossServerRaids() {
        // Register cross-server raid definitions
        registerCrossServerRaids();
        
        // Start raid coordination task
        startRaidCoordinationTask();
    }
    
    /**
     * Register cross-server specific raids
     */
    private void registerCrossServerRaids() {
        // Multiverse Convergence Raid
        CrossServerRaidDefinition multiverseRaid = new CrossServerRaidDefinition(
            "multiverse_convergence",
            "Multiverse Convergence",
            "Face manifestations of gods across multiple servers",
            RaidManager.RaidTier.CONVERGENCE,
            6, 24, // 6-24 players across servers
            3, 8,   // 3-8 servers
            Arrays.asList(), // All gods
            2400, // 40 minutes
            "Defeat god manifestations across multiple dimensions"
        );
        
        // Cosmic Nexus Raid
        CrossServerRaidDefinition cosmicRaid = new CrossServerRaidDefinition(
            "cosmic_nexus",
            "Cosmic Nexus",
            "Ultimate challenge spanning the entire multiverse",
            RaidManager.RaidTier.CONVERGENCE,
            12, 48, // 12-48 players
            5, 12,  // 5-12 servers
            Arrays.asList(), // All gods
            3600, // 60 minutes
            "Protect the cosmic nexus from multiversal collapse"
        );
        
        // Register raids in database
        registerRaidInDatabase(multiverseRaid);
        registerRaidInDatabase(cosmicRaid);
    }
    
    /**
     * Register raid definition in database
     */
    private void registerRaidInDatabase(CrossServerRaidDefinition raid) {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        String sql = """
            INSERT INTO cross_server_raids (raid_id, name, description, tier, min_players, 
                                          max_players, min_servers, max_servers, time_limit, objective)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                description = VALUES(description),
                tier = VALUES(tier),
                min_players = VALUES(min_players),
                max_players = VALUES(max_players),
                min_servers = VALUES(min_servers),
                max_servers = VALUES(max_servers),
                time_limit = VALUES(time_limit),
                objective = VALUES(objective)
            """;
        
        databaseManager.executeAsync(sql,
            raid.getId(),
            raid.getDisplayName(),
            raid.getDescription(),
            raid.getTier().name(),
            raid.getMinPlayers(),
            raid.getMaxPlayers(),
            raid.getMinServers(),
            raid.getMaxServers(),
            raid.getTimeLimit(),
            raid.getObjective()
        );
    }
    
    /**
     * Start cross-server raid
     */
    public CompletableFuture<Boolean> startCrossServerRaid(String raidId, List<org.bukkit.entity.Player> localPlayers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get raid definition
                CrossServerRaidDefinition raidDef = getCrossServerRaidDefinition(raidId);
                if (raidDef == null) {
                    return false;
                }
                
                // Create cross-server raid instance
                String instanceId = "cross_" + UUID.randomUUID().toString();
                CrossServerRaid raid = new CrossServerRaid(instanceId, raidDef, crossServerManager.getServerId());
                
                // Add local players
                for (org.bukkit.entity.Player player : localPlayers) {
                    raid.addPlayer(crossServerManager.getServerId(), player.getUniqueId(), player.getName());
                }
                
                // Register raid in database
                registerRaidInstance(raid);
                
                // Send invitations to other servers
                sendCrossServerInvitations(raid);
                
                // Store active raid
                activeCrossServerRaids.put(instanceId, raid);
                
                logger.info("Started cross-server raid: " + raidId + " with instance " + instanceId);
                return true;
                
            } catch (Exception e) {
                logger.severe("Failed to start cross-server raid: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Send invitations to other servers
     */
    private void sendCrossServerInvitations(CrossServerRaid raid) {
        Map<String, Object> invitationData = new HashMap<>();
        invitationData.put("raid_instance_id", raid.getInstanceId());
        invitationData.put("raid_id", raid.getDefinition().getId());
        invitationData.put("raid_name", raid.getDefinition().getDisplayName());
        invitationData.put("initiating_server", crossServerManager.getServerId());
        invitationData.put("min_players_needed", raid.getDefinition().getMinPlayers() - raid.getTotalPlayers());
        invitationData.put("max_players_allowed", raid.getDefinition().getMaxPlayers());
        invitationData.put("time_limit", raid.getDefinition().getTimeLimit());
        
        // Broadcast to all servers
        crossServerManager.broadcastMessage("CROSS_SERVER_RAID_INVITATION", invitationData);
        
        logger.info("Sent cross-server raid invitations for: " + raid.getDefinition().getDisplayName());
    }
    
    /**
     * Handle cross-server raid invitation
     */
    public void handleRaidInvitation(Map<String, Object> invitationData) {
        String raidInstanceId = (String) invitationData.get("raid_instance_id");
        String raidId = (String) invitationData.get("raid_id");
        String raidName = (String) invitationData.get("raid_name");
        String initiatingServer = (String) invitationData.get("initiating_server");
        
        // Notify eligible players
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            if (isPlayerEligibleForCrossServerRaid(player, raidId)) {
                player.sendMessage("§6§l✦ CROSS-SERVER RAID INVITATION! ✦");
                player.sendMessage("§7Raid: §f" + raidName);
                player.sendMessage("§7Initiating Server: §f" + initiatingServer);
                player.sendMessage("§7Use §f/raid join-cross " + raidInstanceId + "§7 to participate");
                
                // Store invitation
                playerRaidInvitations.put(player.getUniqueId(), raidInstanceId);
            }
        }
    }
    
    /**
     * Join cross-server raid
     */
    public CompletableFuture<Boolean> joinCrossServerRaid(org.bukkit.entity.Player player, String raidInstanceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if player has invitation
                if (!playerRaidInvitations.containsKey(player.getUniqueId()) ||
                    !playerRaidInvitations.get(player.getUniqueId()).equals(raidInstanceId)) {
                    return false;
                }
                
                // Add player to cross-server raid
                String sql = """
                    INSERT INTO cross_server_raid_participants (raid_instance_id, server_id, player_id, player_name, joined_at)
                    VALUES (?, ?, ?, ?, NOW())
                    """;
                
                databaseManager.executeAsync(sql,
                    raidInstanceId,
                    crossServerManager.getServerId(),
                    player.getUniqueId().toString(),
                    player.getName()
                ).join();
                
                // Notify player
                player.sendMessage("§a§lJoined cross-server raid!");
                player.sendMessage("§7You will be synchronized with players from other servers");
                
                // Notify other servers
                Map<String, Object> joinData = new HashMap<>();
                joinData.put("raid_instance_id", raidInstanceId);
                joinData.put("server_id", crossServerManager.getServerId());
                joinData.put("player_id", player.getUniqueId().toString());
                joinData.put("player_name", player.getName());
                
                crossServerManager.broadcastMessage("CROSS_SERVER_RAID_JOIN", joinData);
                
                // Remove invitation
                playerRaidInvitations.remove(player.getUniqueId());
                
                return true;
                
            } catch (Exception e) {
                logger.severe("Failed to join cross-server raid: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Start raid coordination task
     */
    private void startRaidCoordinationTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            coordinateActiveRaids();
            checkRaidReadiness();
            syncRaidProgress();
        }, 0L, 200L); // Every 10 seconds
    }
    
    /**
     * Coordinate active cross-server raids
     */
    private void coordinateActiveRaids() {
        for (CrossServerRaid raid : activeCrossServerRaids.values()) {
            if (raid.getStatus() == CrossServerRaid.RaidStatus.WAITING_FOR_PLAYERS) {
                checkRaidReadiness(raid);
            } else if (raid.getStatus() == CrossServerRaid.RaidStatus.ACTIVE) {
                syncRaidState(raid);
            }
        }
    }
    
    /**
     * Check if raids are ready to start
     */
    private void checkRaidReadiness() {
        // Implementation for checking raid readiness across servers
    }
    
    /**
     * Check specific raid readiness
     */
    private void checkRaidReadiness(CrossServerRaid raid) {
        if (raid.getTotalPlayers() >= raid.getDefinition().getMinPlayers() &&
            raid.getParticipatingServers() >= raid.getDefinition().getMinServers()) {
            
            // Start the raid
            startRaidExecution(raid);
        }
    }
    
    /**
     * Start raid execution across servers
     */
    private void startRaidExecution(CrossServerRaid raid) {
        raid.setStatus(CrossServerRaid.RaidStatus.ACTIVE);
        raid.setStartTime(System.currentTimeMillis());
        
        // Notify all participating servers
        Map<String, Object> startData = new HashMap<>();
        startData.put("raid_instance_id", raid.getInstanceId());
        startData.put("start_time", raid.getStartTime());
        
        crossServerManager.broadcastMessage("CROSS_SERVER_RAID_START", startData);
        
        // Start local raid components
        startLocalRaidComponents(raid);
        
        logger.info("Started cross-server raid execution: " + raid.getInstanceId());
    }
    
    /**
     * Start local components of cross-server raid
     */
    private void startLocalRaidComponents(CrossServerRaid raid) {
        // Get local players in this raid
        List<org.bukkit.entity.Player> localPlayers = new ArrayList<>();
        
        for (UUID playerId : raid.getPlayersOnServer(crossServerManager.getServerId())) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                localPlayers.add(player);
            }
        }
        
        if (!localPlayers.isEmpty()) {
            // Start local raid instance
            localRaidManager.startRaid(raid.getDefinition().getId(), localPlayers, 
                                     localPlayers.get(0).getLocation());
        }
    }
    
    /**
     * Sync raid progress across servers
     */
    private void syncRaidProgress() {
        // Implementation for syncing raid progress
    }
    
    /**
     * Sync specific raid state
     */
    private void syncRaidState(CrossServerRaid raid) {
        // Update raid state in database
        String sql = """
            UPDATE cross_server_raid_instances 
            SET status = ?, current_phase = ?, objectives_completed = ?, updated_at = NOW()
            WHERE instance_id = ?
            """;
        
        databaseManager.executeAsync(sql,
            raid.getStatus().name(),
            raid.getCurrentPhase(),
            raid.getObjectivesCompleted(),
            raid.getInstanceId()
        );
    }
    
    /**
     * Register raid instance in database
     */
    private void registerRaidInstance(CrossServerRaid raid) {
        if (!databaseManager.isDatabaseEnabled()) {
            return;
        }
        
        String sql = """
            INSERT INTO cross_server_raid_instances (instance_id, raid_id, initiating_server, 
                                                    status, created_at, min_players, max_players)
            VALUES (?, ?, ?, ?, NOW(), ?, ?)
            """;
        
        databaseManager.executeAsync(sql,
            raid.getInstanceId(),
            raid.getDefinition().getId(),
            raid.getInitiatingServer(),
            raid.getStatus().name(),
            raid.getDefinition().getMinPlayers(),
            raid.getDefinition().getMaxPlayers()
        );
    }
    
    /**
     * Get cross-server raid definition
     */
    private CrossServerRaidDefinition getCrossServerRaidDefinition(String raidId) {
        // This would typically load from database or cache
        // For now, return hardcoded definitions
        switch (raidId) {
            case "multiverse_convergence":
                return new CrossServerRaidDefinition(
                    "multiverse_convergence",
                    "Multiverse Convergence",
                    "Face manifestations of gods across multiple servers",
                    RaidManager.RaidTier.CONVERGENCE,
                    6, 24, 3, 8,
                    Arrays.asList(),
                    2400,
                    "Defeat god manifestations across multiple dimensions"
                );
            case "cosmic_nexus":
                return new CrossServerRaidDefinition(
                    "cosmic_nexus",
                    "Cosmic Nexus",
                    "Ultimate challenge spanning the entire multiverse",
                    RaidManager.RaidTier.CONVERGENCE,
                    12, 48, 5, 12,
                    Arrays.asList(),
                    3600,
                    "Protect the cosmic nexus from multiversal collapse"
                );
            default:
                return null;
        }
    }
    
    /**
     * Check if player is eligible for cross-server raid
     */
    private boolean isPlayerEligibleForCrossServerRaid(org.bukkit.entity.Player player, String raidId) {
        // Check convergence status for cross-server raids
        return plugin.getGodManager().getConvergenceManager().hasAchievedConvergence(player);
    }
    
    /**
     * Get cross-server raid statistics
     */
    public CompletableFuture<Map<String, Object>> getCrossServerRaidStats() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("active_cross_server_raids", activeCrossServerRaids.size());
            stats.put("pending_invitations", playerRaidInvitations.size());
            stats.put("participating_servers", crossServerManager.getRegisteredServers());
            
            return stats;
        });
    }
    
    /**
     * Shutdown cross-server raid manager
     */
    public void shutdown() {
        // End all active cross-server raids
        for (CrossServerRaid raid : activeCrossServerRaids.values()) {
            endCrossServerRaid(raid.getInstanceId(), "SERVER_SHUTDOWN");
        }
        
        activeCrossServerRaids.clear();
        playerRaidInvitations.clear();
        raidServerParticipants.clear();
        
        logger.info("Cross-Server Raid Manager shutdown complete");
    }
    
    /**
     * End cross-server raid
     */
    private void endCrossServerRaid(String instanceId, String reason) {
        CrossServerRaid raid = activeCrossServerRaids.remove(instanceId);
        if (raid != null) {
            // Notify all servers
            Map<String, Object> endData = new HashMap<>();
            endData.put("raid_instance_id", instanceId);
            endData.put("reason", reason);
            
            crossServerManager.broadcastMessage("CROSS_SERVER_RAID_END", endData);
            
            logger.info("Ended cross-server raid: " + instanceId + " reason: " + reason);
        }
    }
}