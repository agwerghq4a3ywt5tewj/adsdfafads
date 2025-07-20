package com.example.minecraftplugin.raids;

import java.util.List;
import java.util.UUID;

/**
 * Represents a completed raid record for leaderboards
 */
public class RaidCompletionRecord {
    
    private final String raidId;
    private final String raidDisplayName;
    private final List<UUID> playerIds;
    private final List<String> playerNames;
    private final long completionTime;
    private final long startTime;
    private final long endTime;
    private final int score;
    private final RaidManager.RaidTier tier;
    private final boolean weeklyChallengeActive;
    private final String weeklyChallengeType;
    
    public RaidCompletionRecord(String raidId, String raidDisplayName, List<UUID> playerIds, 
                               List<String> playerNames, long startTime, long endTime, 
                               int score, RaidManager.RaidTier tier, boolean weeklyChallengeActive, 
                               String weeklyChallengeType) {
        this.raidId = raidId;
        this.raidDisplayName = raidDisplayName;
        this.playerIds = playerIds;
        this.playerNames = playerNames;
        this.startTime = startTime;
        this.endTime = endTime;
        this.completionTime = endTime - startTime;
        this.score = score;
        this.tier = tier;
        this.weeklyChallengeActive = weeklyChallengeActive;
        this.weeklyChallengeType = weeklyChallengeType;
    }
    
    // Getters
    public String getRaidId() { return raidId; }
    public String getRaidDisplayName() { return raidDisplayName; }
    public List<UUID> getPlayerIds() { return playerIds; }
    public List<String> getPlayerNames() { return playerNames; }
    public long getCompletionTime() { return completionTime; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getScore() { return score; }
    public RaidManager.RaidTier getTier() { return tier; }
    public boolean isWeeklyChallengeActive() { return weeklyChallengeActive; }
    public String getWeeklyChallengeType() { return weeklyChallengeType; }
    
    /**
     * Get formatted completion time
     */
    public String getFormattedCompletionTime() {
        long seconds = completionTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
    
    /**
     * Get formatted display for leaderboards
     */
    public String getLeaderboardDisplay(int rank) {
        StringBuilder display = new StringBuilder();
        
        // Rank with medal emojis for top 3
        String rankDisplay;
        switch (rank) {
            case 1:
                rankDisplay = "§6§l🥇 #1";
                break;
            case 2:
                rankDisplay = "§7§l🥈 #2";
                break;
            case 3:
                rankDisplay = "§c§l🥉 #3";
                break;
            default:
                rankDisplay = "§f#" + rank;
                break;
        }
        
        display.append(rankDisplay).append(" §r");
        display.append("§f").append(raidDisplayName).append(" ");
        display.append("§7- ").append(getFormattedCompletionTime()).append(" ");
        display.append("§7(Score: §f").append(score).append("§7)");
        
        if (weeklyChallengeActive) {
            display.append(" §e⭐");
        }
        
        display.append("\n");
        display.append("§7   Players: §f").append(String.join(", ", playerNames));
        
        return display.toString();
    }
}