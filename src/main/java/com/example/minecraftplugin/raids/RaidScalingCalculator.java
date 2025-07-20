package com.example.minecraftplugin.raids;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.AscensionLevel;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Calculates dynamic scaling for raids based on player count and power level
 */
public class RaidScalingCalculator {
    
    private final MinecraftPlugin plugin;
    
    public RaidScalingCalculator(MinecraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Calculate scaling parameters for a raid
     */
    public RaidScaling calculateScaling(List<Player> players, RaidDefinition definition, WeeklyChallenge weeklyChallenge) {
        // Base scaling factors
        double healthMultiplier = 1.0;
        double damageMultiplier = 1.0;
        double mobCountMultiplier = 1.0;
        double timeMultiplier = 1.0;
        
        // Player count scaling
        int playerCount = players.size();
        double playerCountFactor = Math.max(1.0, playerCount * 0.3); // 30% increase per additional player
        
        healthMultiplier *= playerCountFactor;
        mobCountMultiplier *= Math.max(1.0, playerCount * 0.2); // 20% more mobs per player
        
        // Player power level scaling
        double averagePowerLevel = calculateAveragePowerLevel(players);
        double powerFactor = 1.0 + (averagePowerLevel * 0.15); // 15% increase per power level
        
        healthMultiplier *= powerFactor;
        damageMultiplier *= Math.max(1.0, powerFactor * 0.8); // Slightly less damage scaling
        
        // Server difficulty scaling
        double difficultyFactor = calculateDifficultyFactor(players);
        healthMultiplier *= difficultyFactor;
        damageMultiplier *= difficultyFactor;
        
        // Raid tier scaling
        switch (definition.getTier()) {
            case NOVICE:
                // No additional scaling for novice
                break;
            case ADEPT:
                healthMultiplier *= 1.2;
                damageMultiplier *= 1.1;
                break;
            case MASTER:
                healthMultiplier *= 1.5;
                damageMultiplier *= 1.3;
                mobCountMultiplier *= 1.2;
                break;
            case CONVERGENCE:
                healthMultiplier *= 2.0;
                damageMultiplier *= 1.5;
                mobCountMultiplier *= 1.5;
                timeMultiplier *= 0.8; // Less time for convergence raids
                break;
        }
        
        // Weekly challenge scaling
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            switch (weeklyChallenge.getType()) {
                case INCREASED_MOB_HEALTH:
                    healthMultiplier *= weeklyChallenge.getModifier();
                    break;
                case INCREASED_MOB_SPEED:
                    // Speed handled separately in mob spawning
                    break;
                case REDUCED_PLAYER_HEALING:
                    // Healing reduction handled in raid logic
                    break;
                case INCREASED_MOB_DAMAGE:
                    damageMultiplier *= weeklyChallenge.getModifier();
                    break;
                case TIME_PRESSURE:
                    timeMultiplier *= weeklyChallenge.getModifier();
                    break;
                case SWARM_MODE:
                    mobCountMultiplier *= weeklyChallenge.getModifier();
                    break;
                case ELITE_MOBS:
                    healthMultiplier *= 1.3;
                    damageMultiplier *= 1.2;
                    break;
                default:
                    // Other challenges handled elsewhere
                    break;
            }
        }
        
        return new RaidScaling(healthMultiplier, damageMultiplier, mobCountMultiplier, timeMultiplier);
    }
    
    /**
     * Calculate average power level of players
     */
    private double calculateAveragePowerLevel(List<Player> players) {
        if (players.isEmpty()) {
            return 0.0;
        }
        
        double totalPowerLevel = 0.0;
        
        for (Player player : players) {
            // Base power from testaments
            int testamentCount = plugin.getGodManager().getTestamentCount(player);
            double playerPower = testamentCount * 0.5; // 0.5 power per testament
            
            // Bonus from ascension level
            AscensionLevel ascension = plugin.getGodManager().getAscensionLevel(player);
            playerPower += ascension.ordinal() * 0.3; // 0.3 power per ascension level
            
            // Convergence bonus
            if (plugin.getGodManager().getConvergenceManager().hasAchievedConvergence(player)) {
                playerPower += 2.0; // Significant bonus for convergence
            }
            
            totalPowerLevel += playerPower;
        }
        
        return totalPowerLevel / players.size();
    }
    
    /**
     * Calculate difficulty factor based on server difficulty
     */
    private double calculateDifficultyFactor(List<Player> players) {
        if (players.isEmpty()) {
            return 1.0;
        }
        
        // Get difficulty from the first player's world (assuming all in same world)
        Difficulty difficulty = players.get(0).getWorld().getDifficulty();
        
        switch (difficulty) {
            case PEACEFUL:
                return 0.5; // 50% scaling for peaceful
            case EASY:
                return 0.8; // 80% scaling for easy
            case NORMAL:
                return 1.0; // 100% scaling for normal (baseline)
            case HARD:
                return 1.3; // 130% scaling for hard
            default:
                return 1.0;
        }
    }
    
    /**
     * Calculate scaling for boss encounters specifically
     */
    public RaidScaling calculateBossScaling(List<Player> players, String bossType) {
        // Base scaling
        RaidScaling baseScaling = calculateScaling(players, null, null);
        
        // Boss-specific modifiers
        double bossHealthMultiplier = baseScaling.getHealthMultiplier();
        double bossDamageMultiplier = baseScaling.getDamageMultiplier();
        
        // Apply boss-specific scaling
        switch (bossType.toLowerCase()) {
            case "ender_dragon":
                bossHealthMultiplier *= 1.5; // Dragons are tougher
                bossDamageMultiplier *= 1.2;
                break;
            case "wither":
                bossHealthMultiplier *= 1.3;
                bossDamageMultiplier *= 1.4; // Wither hits harder
                break;
            case "elder_guardian":
                bossHealthMultiplier *= 1.2;
                bossDamageMultiplier *= 1.1;
                break;
            default:
                // Standard boss scaling
                bossHealthMultiplier *= 1.2;
                bossDamageMultiplier *= 1.1;
                break;
        }
        
        return new RaidScaling(bossHealthMultiplier, bossDamageMultiplier, 
                              baseScaling.getMobCountMultiplier(), baseScaling.getTimeMultiplier());
    }
    
    /**
     * Calculate score for raid completion
     */
    public int calculateCompletionScore(RaidDefinition definition, long completionTime, 
                                       List<Player> players, RaidScaling scaling, 
                                       WeeklyChallenge weeklyChallenge) {
        int baseScore = 1000;
        
        // Tier multiplier
        switch (definition.getTier()) {
            case NOVICE:
                baseScore *= 1;
                break;
            case ADEPT:
                baseScore *= 2;
                break;
            case MASTER:
                baseScore *= 3;
                break;
            case CONVERGENCE:
                baseScore *= 5;
                break;
        }
        
        // Time bonus (faster completion = higher score)
        long timeLimit = (long) (definition.getTimeLimit() * scaling.getTimeMultiplier() * 1000);
        double timeRatio = (double) completionTime / timeLimit;
        double timeBonus = Math.max(0.5, 2.0 - timeRatio); // 0.5x to 2.0x multiplier
        baseScore = (int) (baseScore * timeBonus);
        
        // Player count bonus
        baseScore += players.size() * 100;
        
        // Scaling difficulty bonus
        double difficultyMultiplier = (scaling.getHealthMultiplier() + scaling.getDamageMultiplier() + 
                                     scaling.getMobCountMultiplier()) / 3.0;
        baseScore = (int) (baseScore * difficultyMultiplier);
        
        // Weekly challenge bonus
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            baseScore = (int) (baseScore * 1.5); // 50% bonus for weekly challenge
        }
        
        return Math.max(100, baseScore); // Minimum score of 100
    }
    
    /**
     * Inner class to hold scaling values
     */
    public static class RaidScaling {
        private final double healthMultiplier;
        private final double damageMultiplier;
        private final double mobCountMultiplier;
        private final double timeMultiplier;
        
        public RaidScaling(double healthMultiplier, double damageMultiplier, 
                          double mobCountMultiplier, double timeMultiplier) {
            this.healthMultiplier = healthMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.mobCountMultiplier = mobCountMultiplier;
            this.timeMultiplier = timeMultiplier;
        }
        
        public double getHealthMultiplier() { return healthMultiplier; }
        public double getDamageMultiplier() { return damageMultiplier; }
        public double getMobCountMultiplier() { return mobCountMultiplier; }
        public double getTimeMultiplier() { return timeMultiplier; }
        
        public String getFormattedDisplay() {
            return String.format("Health: %.1fx, Damage: %.1fx, Mobs: %.1fx, Time: %.1fx", 
                               healthMultiplier, damageMultiplier, mobCountMultiplier, timeMultiplier);
        }
    }
}