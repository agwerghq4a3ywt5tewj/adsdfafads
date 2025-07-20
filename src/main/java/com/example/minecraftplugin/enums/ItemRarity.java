package com.example.minecraftplugin.enums;

/**
 * Represents the rarity levels of divine items
 */
public enum ItemRarity {
    DIVINE("Divine", "§6", "Standard divine item"),
    ENHANCED("Enhanced", "§d", "Upgraded divine item with improved stats"),
    LEGENDARY("Legendary", "§c", "Rare variant with unique properties"),
    MYTHIC("Mythic", "§4", "Ultimate tier with reality-bending powers"),
    TRANSCENDENT("Transcendent", "§5", "Beyond mortal comprehension");
    
    private final String displayName;
    private final String colorCode;
    private final String description;
    
    ItemRarity(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getFormattedName() {
        return colorCode + "§l" + displayName + "§r";
    }
}