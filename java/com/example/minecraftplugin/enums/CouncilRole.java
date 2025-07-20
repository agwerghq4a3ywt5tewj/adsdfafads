package com.example.minecraftplugin.enums;

/**
 * Represents roles within the Divine Council
 */
public enum CouncilRole {
    MEMBER("Council Member", "Basic council member with voting rights"),
    ELDER("Council Elder", "Senior member with proposal rights"),
    ARCHON("Divine Archon", "Council leader with executive powers"),
    SUPREME("Supreme Deity", "Ultimate council authority");
    
    private final String displayName;
    private final String description;
    
    CouncilRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get role by name (case insensitive)
     */
    public static CouncilRole fromString(String name) {
        for (CouncilRole role : values()) {
            if (role.name().equalsIgnoreCase(name) || role.getDisplayName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * Check if this role can perform a specific action
     */
    public boolean canPerformAction(CouncilAction action) {
        switch (action) {
            case VOTE:
                return true; // All members can vote
            case PROPOSE:
                return this.ordinal() >= ELDER.ordinal(); // Elder and above
            case EXECUTE:
                return this.ordinal() >= ARCHON.ordinal(); // Archon and above
            case MANAGE_MEMBERS:
                return this == SUPREME; // Only Supreme
            default:
                return false;
        }
    }
    
    /**
     * Council actions that require permission checks
     */
    public enum CouncilAction {
        VOTE,
        PROPOSE,
        EXECUTE,
        MANAGE_MEMBERS
    }
}