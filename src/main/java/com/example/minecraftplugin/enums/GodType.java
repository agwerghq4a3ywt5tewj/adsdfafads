package com.example.minecraftplugin.enums;

import org.bukkit.Material;
import java.util.Arrays;
import java.util.List;

/**
 * Enum representing the 12 Fallen Gods in the Testament system.
 * Each god has unique properties including theme, biomes, altar center block, and tier.
 */
public enum GodType {
    
    // CORE SIX GODS (Fully Implemented)
    FALLEN("Fallen God", "Death, undeath, ultimate protection", 
           Arrays.asList("SWAMP", "DARK_FOREST", "DEEP_DARK"), 
           Material.CRYING_OBSIDIAN, GodTier.CORE),
           
    BANISHMENT("Banishment God", "Fire, exile, destruction", 
               Arrays.asList("DESERT", "BADLANDS", "SAVANNA"), 
               Material.MAGMA_BLOCK, GodTier.CORE),
               
    ABYSSAL("Abyssal God", "Ocean depths, water mastery", 
            Arrays.asList("OCEAN", "DEEP_OCEAN", "COLD_OCEAN", "LUKEWARM_OCEAN", "WARM_OCEAN"), 
            Material.DARK_PRISMARINE, GodTier.CORE),
            
    SYLVAN("Sylvan God", "Forests, nature, growth", 
           Arrays.asList("FOREST", "BIRCH_FOREST", "DARK_FOREST", "JUNGLE", "TAIGA"), 
           Material.OAK_LOG, GodTier.CORE),
           
    TEMPEST("Tempest God", "Sky, storms, lightning, flight", 
            Arrays.asList("MOUNTAINS", "WINDSWEPT_HILLS", "JAGGED_PEAKS", "FROZEN_PEAKS"), 
            Material.LIGHTNING_ROD, GodTier.CORE),
            
    VEIL("Veil God", "Reality manipulation, void magic", 
         Arrays.asList("THE_END"), 
         Material.END_PORTAL_FRAME, GodTier.CORE),
    
    // EXPANSION SIX GODS (Ready for Activation)
    FORGE("Forge God", "Smithing, crafting, creation, molten metal", 
          Arrays.asList("MOUNTAINS", "DRIPSTONE_CAVES", "BASALT_DELTAS"), 
          Material.ANVIL, GodTier.EXPANSION),
          
    VOID("Void God", "Emptiness, teleportation, phase shifting", 
         Arrays.asList("THE_END", "DEEP_DARK", "DRIPSTONE_CAVES"), 
         Material.OBSIDIAN, GodTier.EXPANSION),
         
    TIME("Time God", "Time manipulation, aging, temporal magic", 
         Arrays.asList("DEEP_DARK", "DRIPSTONE_CAVES", "LUSH_CAVES"), 
         Material.AMETHYST_CLUSTER, GodTier.EXPANSION),
         
    BLOOD("Blood God", "Combat, sacrifice, berserker rage", 
          Arrays.asList("NETHER_WASTES", "CRIMSON_FOREST", "WARPED_FOREST"), 
          Material.REDSTONE_BLOCK, GodTier.EXPANSION),
          
    CRYSTAL("Crystal God", "Crystals, sound, vibration, harmony", 
            Arrays.asList("DRIPSTONE_CAVES", "LUSH_CAVES"), 
            Material.LARGE_AMETHYST_BUD, GodTier.EXPANSION),
            
    SHADOW("Shadow God", "Stealth, darkness, assassination", 
           Arrays.asList("DEEP_DARK", "DARK_FOREST", "SCULK"), 
           Material.SCULK_CATALYST, GodTier.EXPANSION);
    
    private final String displayName;
    private final String theme;
    private final List<String> biomes;
    private final Material altarCenterBlock;
    private final GodTier tier;
    
    GodType(String displayName, String theme, List<String> biomes, Material altarCenterBlock, GodTier tier) {
        this.displayName = displayName;
        this.theme = theme;
        this.biomes = biomes;
        this.altarCenterBlock = altarCenterBlock;
        this.tier = tier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public List<String> getBiomes() {
        return biomes;
    }
    
    public Material getAltarCenterBlock() {
        return altarCenterBlock;
    }
    
    public GodTier getTier() {
        return tier;
    }
    
    /**
     * Get god by name (case insensitive)
     */
    public static GodType fromString(String name) {
        for (GodType god : values()) {
            if (god.name().equalsIgnoreCase(name) || god.getDisplayName().equalsIgnoreCase(name)) {
                return god;
            }
        }
        return null;
    }
    
    /**
     * Check if this god conflicts with another god
     */
    public boolean conflictsWith(GodType other) {
        // Define god conflicts as per design
        switch (this) {
            case FALLEN:
                return other == VEIL;
            case VEIL:
                return other == FALLEN;
            case BANISHMENT:
                return other == ABYSSAL;
            case ABYSSAL:
                return other == BANISHMENT;
            case SYLVAN:
                return other == TEMPEST;
            case TEMPEST:
                return other == SYLVAN;
            case FORGE:
                return other == VOID;
            case VOID:
                return other == FORGE;
            case TIME:
                return other == SHADOW;
            case SHADOW:
                return other == TIME;
            case BLOOD:
                return other == CRYSTAL;
            case CRYSTAL:
                return other == BLOOD;
            default:
                return false;
        }
    }
    
    /**
     * Get all core gods (first 6)
     */
    public static GodType[] getCoreGods() {
        return new GodType[]{FALLEN, BANISHMENT, ABYSSAL, SYLVAN, TEMPEST, VEIL};
    }
    
    /**
     * Get all expansion gods (last 6)
     */
    public static GodType[] getExpansionGods() {
        return new GodType[]{FORGE, VOID, TIME, BLOOD, CRYSTAL, SHADOW};
    }
}