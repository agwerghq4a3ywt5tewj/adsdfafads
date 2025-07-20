package com.example.minecraftplugin.raids;

import java.util.*;

/**
 * Represents an active cross-server raid instance
 */
public class CrossServerRaid {
    
    private final String instanceId;
    private final CrossServerRaidDefinition definition;
    private final String initiatingServer;
    
    private RaidStatus status;
    private long startTime;
    private int currentPhase;
    private int objectivesCompleted;
    
    // Player tracking across servers
    private final Map<String, Set<UUID>> serverPlayers; // server_id -> player_ids
    private final Map<UUID, String> playerNames;
    private final Map<String, Integer> serverContributions;
    
    public CrossServerRaid(String instanceId, CrossServerRaidDefinition definition, String initiatingServer) {
        this.instanceId = instanceId;
        this.definition = definition;
        this.initiatingServer = initiatingServer;
        this.status = RaidStatus.WAITING_FOR_PLAYERS;
        this.startTime = 0;
        this.currentPhase = 0;
        this.objectivesCompleted = 0;
        this.serverPlayers = new HashMap<>();
        this.playerNames = new HashMap<>();
        this.serverContributions = new HashMap<>();
    }
    
    /**
     * Add player to the raid
     */
    public void addPlayer(String serverId, UUID playerId, String playerName) {
        serverPlayers.computeIfAbsent(serverId, k -> new HashSet<>()).add(playerId);
        playerNames.put(playerId, playerName);
        serverContributions.putIfAbsent(serverId, 0);
    }
    
    /**
     * Remove player from the raid
     */
    public void removePlayer(String serverId, UUID playerId) {
        Set<UUID> players = serverPlayers.get(serverId);
        if (players != null) {
            players.remove(playerId);
            if (players.isEmpty()) {
                serverPlayers.remove(serverId);
                serverContributions.remove(serverId);
            }
        }
        playerNames.remove(playerId);
    }
    
    /**
     * Get total number of players across all servers
     */
    public int getTotalPlayers() {
        return serverPlayers.values().stream()
                           .mapToInt(Set::size)
                           .sum();
    }
    
    /**
     * Get number of participating servers
     */
    public int getParticipatingServers() {
        return serverPlayers.size();
    }
    
    /**
     * Get players on specific server
     */
    public Set<UUID> getPlayersOnServer(String serverId) {
        return new HashSet<>(serverPlayers.getOrDefault(serverId, new HashSet<>()));
    }
    
    /**
     * Get all participating server IDs
     */
    public Set<String> getParticipatingServerIds() {
        return new HashSet<>(serverPlayers.keySet());
    }
    
    /**
     * Add contribution for server
     */
    public void addServerContribution(String serverId, int contribution) {
        serverContributions.merge(serverId, contribution, Integer::sum);
    }
    
    /**
     * Get server contribution
     */
    public int getServerContribution(String serverId) {
        return serverContributions.getOrDefault(serverId, 0);
    }
    
    /**
     * Get raid summary for display
     */
    public String getRaidSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("§6§l=== CROSS-SERVER RAID ===§r\n");
        summary.append("§7Raid: §f").append(definition.getDisplayName()).append("\n");
        summary.append("§7Instance: §f").append(instanceId).append("\n");
        summary.append("§7Status: §f").append(status.name()).append("\n");
        summary.append("§7Players: §f").append(getTotalPlayers()).append("/").append(definition.getMaxPlayers()).append("\n");
        summary.append("§7Servers: §f").append(getParticipatingServers()).append("/").append(definition.getMaxServers()).append("\n");
        summary.append("§7Phase: §f").append(currentPhase + 1).append("\n");
        summary.append("§7Objectives: §f").append(objectivesCompleted).append("\n");
        
        if (startTime > 0) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = definition.getTimeLimit() - elapsed;
            summary.append("§7Time Remaining: §f").append(remaining / 60).append(":").append(String.format("%02d", remaining % 60)).append("\n");
        }
        
        summary.append("\n§e§lParticipating Servers:§r\n");
        for (Map.Entry<String, Set<UUID>> entry : serverPlayers.entrySet()) {
            String serverId = entry.getKey();
            int playerCount = entry.getValue().size();
            int contribution = serverContributions.getOrDefault(serverId, 0);
            
            summary.append("§7• §f").append(serverId).append(" §7(").append(playerCount).append(" players, ").append(contribution).append(" contribution)\n");
        }
        
        return summary.toString();
    }
    
    // Getters and setters
    public String getInstanceId() { return instanceId; }
    public CrossServerRaidDefinition getDefinition() { return definition; }
    public String getInitiatingServer() { return initiatingServer; }
    public RaidStatus getStatus() { return status; }
    public void setStatus(RaidStatus status) { this.status = status; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public int getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(int currentPhase) { this.currentPhase = currentPhase; }
    public int getObjectivesCompleted() { return objectivesCompleted; }
    public void setObjectivesCompleted(int objectivesCompleted) { this.objectivesCompleted = objectivesCompleted; }
    
    /**
     * Cross-server raid status
     */
    public enum RaidStatus {
        WAITING_FOR_PLAYERS,
        READY_TO_START,
        ACTIVE,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}