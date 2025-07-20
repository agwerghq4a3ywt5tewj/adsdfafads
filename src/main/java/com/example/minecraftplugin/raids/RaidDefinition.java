package com.example.minecraftplugin.raids;

import com.example.minecraftplugin.enums.GodType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Defines the properties and requirements of a raid
 */
public class RaidDefinition {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final RaidManager.RaidTier tier;
    private final int minPlayers;
    private final int maxPlayers;
    private final List<GodType> associatedGods;
    private final int timeLimit; // in seconds
    private final String objective;
    
    // Dynamic scaling base values
    private final double baseMobHealthMultiplier;
    private final double baseMobDamageMultiplier;
    private final double baseMobCountMultiplier;
    private final Map<String, Object> scalingParameters;
    
    public RaidDefinition(String id, String displayName, String description, 
                         RaidManager.RaidTier tier, int minPlayers, int maxPlayers,
                         List<GodType> associatedGods, int timeLimit, String objective) {
        this(id, displayName, description, tier, minPlayers, maxPlayers, 
             associatedGods, timeLimit, objective, 1.0, 1.0, 1.0, new HashMap<>());
    }
    
    public RaidDefinition(String id, String displayName, String description, 
                         RaidManager.RaidTier tier, int minPlayers, int maxPlayers,
                         List<GodType> associatedGods, int timeLimit, String objective,
                         double baseMobHealthMultiplier, double baseMobDamageMultiplier,
                         double baseMobCountMultiplier, Map<String, Object> scalingParameters) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.tier = tier;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.associatedGods = associatedGods;
        this.timeLimit = timeLimit;
        this.objective = objective;
        this.baseMobHealthMultiplier = baseMobHealthMultiplier;
        this.baseMobDamageMultiplier = baseMobDamageMultiplier;
        this.baseMobCountMultiplier = baseMobCountMultiplier;
        this.scalingParameters = new HashMap<>(scalingParameters);
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public RaidManager.RaidTier getTier() {
        return tier;
    }
    
    public int getMinPlayers() {
        return minPlayers;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public List<GodType> getAssociatedGods() {
        return associatedGods;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public String getObjective() {
        return objective;
    }
    
    public double getBaseMobHealthMultiplier() {
        return baseMobHealthMultiplier;
    }
    
    public double getBaseMobDamageMultiplier() {
        return baseMobDamageMultiplier;
    }
    
    public double getBaseMobCountMultiplier() {
        return baseMobCountMultiplier;
    }
    
    public Map<String, Object> getScalingParameters() {
        return new HashMap<>(scalingParameters);
    }
    
    /**
     * Get formatted information about this raid
     */
    public String getFormattedInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("§6§l").append(displayName).append("§r\n");
        info.append("§7").append(description).append("\n");
        info.append("\n");
        info.append("§e§lRaid Details:§r\n");
        info.append("§7Tier: §f").append(tier.getDisplayName()).append("\n");
        info.append("§7Players: §f").append(minPlayers);
        if (maxPlayers != minPlayers) {
            info.append("-").append(maxPlayers);
        }
        info.append("\n");
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
        
        // Add special information for Eternal Crucible
        if ("eternal_crucible".equals(id)) {
            info.append("\n");
            info.append("§5§l=== ETERNAL CRUCIBLE SPECIAL FEATURES ===§r\n");
            info.append("§7• Face manifestations of all 12 gods\n");
            info.append("§7• Protect the Nexus Core (1000 HP)\n");
            info.append("§7• Survive multiple waves of divine enemies\n");
            info.append("§7• Unique rewards: Crucible Crown, Divine Essence\n");
            info.append("§7• Permanent +2 hearts bonus upon completion\n");
            info.append("§c§lWarning: §r§cThis is the ultimate convergence challenge!\n");
        }
        
        // Add scaling information
        info.append("\n");
        info.append("§e§lDynamic Scaling:§r\n");
        info.append("§7This raid scales based on player count and power level\n");
        info.append("§7Base scaling: Health ").append(String.format("%.1fx", baseMobHealthMultiplier));
        info.append(", Damage ").append(String.format("%.1fx", baseMobDamageMultiplier));
        info.append(", Mobs ").append(String.format("%.1fx", baseMobCountMultiplier)).append("\n");
        
        return info.toString();
    }
}