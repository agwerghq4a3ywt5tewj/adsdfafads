package com.example.minecraftplugin.enums;

/**
 * Represents the tier/category of gods in the Testament system
 */
public enum GodTier {
    CORE("Core Gods", "Fully implemented gods with complete features"),
    EXPANSION("Expansion Gods", "Ready for activation with planned abilities");
    
    private final String displayName;
    private final String description;
    
    GodTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}