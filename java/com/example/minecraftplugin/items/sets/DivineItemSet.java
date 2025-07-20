package com.example.minecraftplugin.items.sets;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Represents sets of divine items that provide bonus effects when worn together
 */
public class DivineItemSet {
    
    private final String setName;
    private final List<GodType> requiredGods;
    private final Map<Integer, SetBonus> setBonuses;
    private final String description;
    
    // Predefined item sets
    private static final Map<String, DivineItemSet> ITEM_SETS = new HashMap<>();
    
    static {
        initializeItemSets();
    }
    
    public DivineItemSet(String setName, List<GodType> requiredGods, String description) {
        this.setName = setName;
        this.requiredGods = requiredGods;
        this.setBonuses = new HashMap<>();
        this.description = description;
    }
    
    /**
     * Initialize predefined item sets
     */
    private static void initializeItemSets() {
        // Elemental Mastery Set
        DivineItemSet elementalSet = new DivineItemSet(
            "Elemental Mastery",
            Arrays.asList(GodType.BANISHMENT, GodType.ABYSSAL, GodType.SYLVAN, GodType.TEMPEST),
            "Master of all elemental forces"
        );
        elementalSet.addSetBonus(2, new SetBonus("Elemental Resistance", 
            "Immunity to environmental damage", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0),
                new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0)
            )));
        elementalSet.addSetBonus(4, new SetBonus("Elemental Supremacy", 
            "Control over all elements", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1),
                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1)
            )));
        ITEM_SETS.put("elemental_mastery", elementalSet);
        
        // Death and Void Set
        DivineItemSet deathVoidSet = new DivineItemSet(
            "Death and Void",
            Arrays.asList(GodType.FALLEN, GodType.VOID, GodType.SHADOW),
            "Master of death, void, and shadow"
        );
        deathVoidSet.addSetBonus(2, new SetBonus("Undying Will", 
            "Resistance to death effects", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1),
                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0)
            )));
        deathVoidSet.addSetBonus(3, new SetBonus("Void Mastery", 
            "Command over death and void", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0),
                new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0),
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1)
            )));
        ITEM_SETS.put("death_void", deathVoidSet);
        
        // Creation and Order Set
        DivineItemSet creationSet = new DivineItemSet(
            "Creation and Order",
            Arrays.asList(GodType.FORGE, GodType.CRYSTAL, GodType.TIME),
            "Master of creation, harmony, and time"
        );
        creationSet.addSetBonus(2, new SetBonus("Harmonic Creation", 
            "Enhanced crafting and building", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 2)
            )));
        creationSet.addSetBonus(3, new SetBonus("Temporal Mastery", 
            "Control over time and creation", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 3),
                new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 2)
            )));
        ITEM_SETS.put("creation_order", creationSet);
        
        // Reality Manipulation Set
        DivineItemSet realitySet = new DivineItemSet(
            "Reality Manipulation",
            Arrays.asList(GodType.VEIL, GodType.TIME, GodType.VOID),
            "Master of reality, time, and void"
        );
        realitySet.addSetBonus(2, new SetBonus("Reality Sight", 
            "See through illusions and time", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0),
                new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0)
            )));
        realitySet.addSetBonus(3, new SetBonus("Reality Dominion", 
            "Command over space and time", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0),
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 3),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2)
            )));
        ITEM_SETS.put("reality_manipulation", realitySet);
        
        // Combat Mastery Set
        DivineItemSet combatSet = new DivineItemSet(
            "Combat Mastery",
            Arrays.asList(GodType.BLOOD, GodType.BANISHMENT, GodType.FORGE),
            "Master of combat, fire, and forging"
        );
        combatSet.addSetBonus(2, new SetBonus("Warrior's Might", 
            "Enhanced combat abilities", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1),
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)
            )));
        combatSet.addSetBonus(3, new SetBonus("Berserker's Fury", 
            "Ultimate combat prowess", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1),
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)
            )));
        ITEM_SETS.put("combat_mastery", combatSet);
        
        // Ultimate Convergence Set (all 12 gods)
        DivineItemSet convergenceSet = new DivineItemSet(
            "Divine Convergence",
            Arrays.asList(GodType.values()),
            "Master of all divine powers"
        );
        convergenceSet.addSetBonus(6, new SetBonus("Partial Convergence", 
            "Half the gods mastered", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 2)
            )));
        convergenceSet.addSetBonus(9, new SetBonus("Near Convergence", 
            "Three-quarters mastered", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 4),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2)
            )));
        convergenceSet.addSetBonus(12, new SetBonus("True Convergence", 
            "All gods mastered - transcendent power", 
            Arrays.asList(
                new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 9),
                new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 4),
                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 4),
                new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 4),
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2),
                new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 4)
            )));
        ITEM_SETS.put("divine_convergence", convergenceSet);
    }
    
    /**
     * Add set bonus for specific item count
     */
    public void addSetBonus(int itemCount, SetBonus bonus) {
        setBonuses.put(itemCount, bonus);
    }
    
    /**
     * Check if player has items for this set and apply bonuses
     */
    public void checkAndApplySetBonuses(Player player, List<DivineItem> playerDivineItems) {
        // Count matching items
        int matchingItems = 0;
        Set<GodType> playerGods = new HashSet<>();
        
        for (DivineItem item : playerDivineItems) {
            if (item.getGodType() != null && requiredGods.contains(item.getGodType())) {
                playerGods.add(item.getGodType());
            }
        }
        
        matchingItems = playerGods.size();
        
        // Apply highest applicable set bonus
        SetBonus activeBonus = null;
        int activeBonusCount = 0;
        
        for (Map.Entry<Integer, SetBonus> entry : setBonuses.entrySet()) {
            if (matchingItems >= entry.getKey() && entry.getKey() > activeBonusCount) {
                activeBonus = entry.getValue();
                activeBonusCount = entry.getKey();
            }
        }
        
        if (activeBonus != null) {
            applySetBonus(player, activeBonus, activeBonusCount);
        }
    }
    
    /**
     * Apply set bonus effects to player
     */
    private void applySetBonus(Player player, SetBonus bonus, int itemCount) {
        // Remove existing set bonus effects first
        removeSetBonusEffects(player);
        
        // Apply new set bonus effects
        for (PotionEffect effect : bonus.getEffects()) {
            player.addPotionEffect(effect);
        }
        
        // Notify player of active set bonus
        player.sendMessage("§6§l✦ SET BONUS ACTIVE! ✦");
        player.sendMessage("§7Set: §f" + setName);
        player.sendMessage("§7Bonus: §f" + bonus.getName() + " §7(" + itemCount + " items)");
        player.sendMessage("§7" + bonus.getDescription());
    }
    
    /**
     * Remove set bonus effects from player
     */
    private void removeSetBonusEffects(Player player) {
        // Remove common set bonus effects
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
    }
    
    /**
     * Get all available item sets
     */
    public static Map<String, DivineItemSet> getAllItemSets() {
        return new HashMap<>(ITEM_SETS);
    }
    
    /**
     * Get specific item set by name
     */
    public static DivineItemSet getItemSet(String setName) {
        return ITEM_SETS.get(setName);
    }
    
    /**
     * Check all sets for a player and apply the best bonuses
     */
    public static void checkAllSetsForPlayer(Player player, List<DivineItem> playerDivineItems) {
        DivineItemSet bestSet = null;
        int bestBonusCount = 0;
        
        for (DivineItemSet set : ITEM_SETS.values()) {
            // Count matching items for this set
            Set<GodType> playerGods = new HashSet<>();
            for (DivineItem item : playerDivineItems) {
                if (item.getGodType() != null && set.requiredGods.contains(item.getGodType())) {
                    playerGods.add(item.getGodType());
                }
            }
            
            int matchingItems = playerGods.size();
            
            // Find highest applicable bonus for this set
            for (int requiredCount : set.setBonuses.keySet()) {
                if (matchingItems >= requiredCount && requiredCount > bestBonusCount) {
                    bestSet = set;
                    bestBonusCount = requiredCount;
                }
            }
        }
        
        // Apply the best set bonus found
        if (bestSet != null) {
            SetBonus bonus = bestSet.setBonuses.get(bestBonusCount);
            bestSet.applySetBonus(player, bonus, bestBonusCount);
        }
    }
    
    // Getters
    public String getSetName() { return setName; }
    public List<GodType> getRequiredGods() { return new ArrayList<>(requiredGods); }
    public String getDescription() { return description; }
    public Map<Integer, SetBonus> getSetBonuses() { return new HashMap<>(setBonuses); }
    
    /**
     * Inner class for set bonuses
     */
    public static class SetBonus {
        private final String name;
        private final String description;
        private final List<PotionEffect> effects;
        
        public SetBonus(String name, String description, List<PotionEffect> effects) {
            this.name = name;
            this.description = description;
            this.effects = effects;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<PotionEffect> getEffects() { return new ArrayList<>(effects); }
    }
}