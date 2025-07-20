package com.example.minecraftplugin.raids;

import com.example.minecraftplugin.enums.GodType;

import java.util.List;

/**
 * Defines cross-server raid properties and requirements
 */
public class CrossServerRaidDefinition {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final RaidManager.RaidTier tier;
    private final int minPlayers;
    private final int maxPlayers;
    private final int minServers;
    private final int maxServers;
    private final List<GodType> associatedGods;
    private final int timeLimit; // in seconds
    private final String objective;
    
    public CrossServerRaidDefinition(String id, String displayName, String description,
                                   RaidManager.RaidTier tier, int minPlayers, int maxPlayers,
                                   int minServers, int maxServers, List<GodType> associatedGods,
                                   int timeLimit, String objective) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.tier = tier;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.minServers = minServers;
        this.maxServers = maxServers;
        this.associatedGods = associatedGods;
        this.timeLimit = timeLimit;
        this.objective = objective;
    }
    
    /**
     * Get formatted information about this cross-server raid
     */
    public String getFormattedInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("§6§l").append(displayName).append(" §7(Cross-Server)§r\n");
        info.append("§7").append(description).append("\n");
        info.append("\n");
        info.append("§e§lCross-Server Raid Details:§r\n");
        info.append("§7Tier: §f").append(tier.getDisplayName()).append("\n");
        info.append("§7Players: §f").append(minPlayers).append("-").append(maxPlayers).append(" across all servers\n");
        info.append("§7Servers: §f").append(minServers).append("-").append(maxServers).append(" participating\n");
        info.append("§7Time Limit: §f").append(timeLimit / 60).append(" minutes\n");
        info.append("§7Objective: §f").append(objective).append("\n");
        
        if (!associatedGods.isEmpty()) {
            info.append("§7Associated Gods: §f");
            for (int i = 0; i < associatedGods.size(); i++) {
                info.append(associatedGods.get(i).getDisplayName());
                if (i < associatedGods.size() - 1) {
                    info.append(", ");
                }
            }
            info.append("\n");
        }
        
        info.append("\n");
        info.append("§e§lCross-Server Features:§r\n");
        info.append("§7• Synchronized objectives across all servers\n");
        info.append("§7• Shared progress and leaderboards\n");
        info.append("§7• Cross-server communication during raid\n");
        info.append("§7• Combined rewards based on server contribution\n");
        info.append("§7• Global announcements for major achievements\n");
        
        return info.toString();
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public RaidManager.RaidTier getTier() { return tier; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getMinServers() { return minServers; }
    public int getMaxServers() { return maxServers; }
    public List<GodType> getAssociatedGods() { return associatedGods; }
    public int getTimeLimit() { return timeLimit; }
    public String getObjective() { return objective; }
}