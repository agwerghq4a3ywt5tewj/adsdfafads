package com.example.minecraftplugin.raids;

/**
 * Represents a weekly raid challenge modifier
 */
public class WeeklyChallenge {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final ChallengeType type;
    private final double modifier;
    private final long startTime;
    private final long endTime;
    
    public WeeklyChallenge(String id, String displayName, String description, 
                          ChallengeType type, double modifier) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.modifier = modifier;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (7 * 24 * 60 * 60 * 1000L); // 7 days
    }
    
    public enum ChallengeType {
        INCREASED_MOB_HEALTH("Increased Mob Health", "Mobs have more health"),
        INCREASED_MOB_SPEED("Increased Mob Speed", "Mobs move faster"),
        REDUCED_PLAYER_HEALING("Reduced Healing", "Player healing is reduced"),
        INCREASED_MOB_DAMAGE("Increased Mob Damage", "Mobs deal more damage"),
        TIME_PRESSURE("Time Pressure", "Reduced time limit"),
        SWARM_MODE("Swarm Mode", "More mobs spawn"),
        ELITE_MOBS("Elite Mobs", "Mobs have special abilities"),
        RESOURCE_SCARCITY("Resource Scarcity", "Limited resources available"),
        DARKNESS("Eternal Darkness", "Reduced visibility"),
        CHAOS_MODE("Chaos Mode", "Random effects throughout raid");
        
        private final String displayName;
        private final String description;
        
        ChallengeType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ChallengeType getType() { return type; }
    public double getModifier() { return modifier; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    
    /**
     * Check if challenge is still active
     */
    public boolean isActive() {
        long currentTime = System.currentTimeMillis();
        return currentTime >= startTime && currentTime <= endTime;
    }
    
    /**
     * Get time remaining in hours
     */
    public long getHoursRemaining() {
        long currentTime = System.currentTimeMillis();
        long remaining = endTime - currentTime;
        return Math.max(0, remaining / (60 * 60 * 1000L));
    }
    
    /**
     * Get formatted display for announcements
     */
    public String getFormattedDisplay() {
        StringBuilder display = new StringBuilder();
        
        display.append("§e§l⭐ WEEKLY CHALLENGE ACTIVE ⭐§r\n");
        display.append("§7Challenge: §f").append(displayName).append("\n");
        display.append("§7Effect: §f").append(description).append("\n");
        display.append("§7Modifier: §f").append(String.format("%.1fx", modifier)).append("\n");
        display.append("§7Time Remaining: §f").append(getHoursRemaining()).append(" hours\n");
        display.append("§7Complete raids during this challenge for bonus rewards!");
        
        return display.toString();
    }
}