package com.example.minecraftplugin.enums;

import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the divine ascension levels based on testament completion
 */
public enum AscensionLevel {
    MORTAL(0, "Mortal", "Base gameplay", 
           Arrays.asList()),
    BLESSED(1, "Blessed", "Luck I, minor divine favor", 
            Arrays.asList(new EffectData(PotionEffectType.LUCK, 0, Integer.MAX_VALUE))),
    CHOSEN(3, "Chosen", "Luck II, Hero of Village, significant abilities", 
           Arrays.asList(
               new EffectData(PotionEffectType.LUCK, 1, Integer.MAX_VALUE),
               new EffectData(PotionEffectType.HERO_OF_THE_VILLAGE, 0, Integer.MAX_VALUE)
           )),
    DIVINE(5, "Divine", "Luck III, enhanced effects, reality manipulation", 
           Arrays.asList(
               new EffectData(PotionEffectType.LUCK, 2, Integer.MAX_VALUE),
               new EffectData(PotionEffectType.HERO_OF_THE_VILLAGE, 1, Integer.MAX_VALUE),
               new EffectData(PotionEffectType.REGENERATION, 0, Integer.MAX_VALUE)
           )),
    GODLIKE(7, "Godlike", "Maximum effects, ultimate cosmic powers", 
            Arrays.asList(
                new EffectData(PotionEffectType.LUCK, 2, Integer.MAX_VALUE),
                new EffectData(PotionEffectType.HERO_OF_THE_VILLAGE, 2, Integer.MAX_VALUE),
                new EffectData(PotionEffectType.REGENERATION, 1, Integer.MAX_VALUE),
                new EffectData(PotionEffectType.RESISTANCE, 0, Integer.MAX_VALUE),
                new EffectData(PotionEffectType.SATURATION, 0, Integer.MAX_VALUE)
            )),
    CONVERGENCE(12, "Master of All Divinity", "Transcends all limitations", 
                Arrays.asList(
                    new EffectData(PotionEffectType.LUCK, 3, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.HERO_OF_THE_VILLAGE, 3, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.REGENERATION, 2, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.RESISTANCE, 1, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.SATURATION, 1, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.ABSORPTION, 2, Integer.MAX_VALUE),
                    new EffectData(PotionEffectType.CONDUIT_POWER, 0, Integer.MAX_VALUE)
                ));
    
    private final int requiredTestaments;
    private final String title;
    private final String description;
    private final List<EffectData> effects;
    
    AscensionLevel(int requiredTestaments, String title, String description, List<EffectData> effects) {
        this.requiredTestaments = requiredTestaments;
        this.title = title;
        this.description = description;
        this.effects = effects;
    }
    
    public int getRequiredTestaments() {
        return requiredTestaments;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<EffectData> getEffects() {
        return effects;
    }
    
    public boolean hasEffects() {
        return !effects.isEmpty();
    }
    
    /**
     * Get ascension level based on number of completed testaments
     */
    public static AscensionLevel fromTestamentCount(int testaments) {
        AscensionLevel result = MORTAL;
        for (AscensionLevel level : values()) {
            if (testaments >= level.getRequiredTestaments()) {
                result = level;
            }
        }
        return result;
    }
    
    /**
     * Inner class to hold effect data
     */
    public static class EffectData {
        private final PotionEffectType type;
        private final int amplifier;
        private final int duration;
        
        public EffectData(PotionEffectType type, int amplifier, int duration) {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
        }
        
        public PotionEffectType getType() {
            return type;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public int getDuration() {
            return duration;
        }
    }
}