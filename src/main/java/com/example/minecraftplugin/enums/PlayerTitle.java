package com.example.minecraftplugin.enums;

/**
 * Represents player titles in the Testament system
 */
public enum PlayerTitle {
    NONE("", "Default state"),
    FALLEN("§4§lFallen§r", "Toxic behavior + excessive deaths (permanent consequences)"),
    TOXIC("§c§lToxic§r", "Detected toxic chat/behavior"),
    CURSED("§5§lCursed§r", "Divine punishment"),
    BLESSED("§e§lBlessed§r", "Divine favor from gods"),
    CHAMPION("§6§lChampion§r", "Skilled warrior status"),
    LEGEND("§b§lLegend§r", "Legendary achievements");
    
    private final String displayName;
    private final String description;
    
    PlayerTitle(String displayName, String description) {
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
     * Get title by name (case insensitive)
     */
    public static PlayerTitle fromString(String name) {
        for (PlayerTitle title : values()) {
            if (title.name().equalsIgnoreCase(name)) {
                return title;
            }
        }
        return NONE;
    }
}