package com.example.minecraftplugin.enums;

/**
 * Represents transcendence levels beyond Divine Convergence
 */
public enum TranscendenceLevel {
    NONE(0, "Not Transcendent", "Standard convergence state"),
    REALITY_SHAPER(1, "Reality Shaper", "Can manipulate the fabric of existence"),
    COSMIC_ARCHITECT(3, "Cosmic Architect", "Designs and creates new realms"),
    DIMENSIONAL_SOVEREIGN(5, "Dimensional Sovereign", "Rules over multiple dimensions"),
    UNIVERSAL_DEITY(10, "Universal Deity", "Commands the forces of creation itself");
    
    private final int requiredChallenges;
    private final String title;
    private final String description;
    
    TranscendenceLevel(int requiredChallenges, String title, String description) {
        this.requiredChallenges = requiredChallenges;
        this.title = title;
        this.description = description;
    }
    
    public int getRequiredChallenges() {
        return requiredChallenges;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get transcendence level based on completed challenges
     */
    public static TranscendenceLevel fromChallengeCount(int challenges) {
        TranscendenceLevel result = NONE;
        for (TranscendenceLevel level : values()) {
            if (challenges >= level.getRequiredChallenges()) {
                result = level;
            }
        }
        return result;
    }
}