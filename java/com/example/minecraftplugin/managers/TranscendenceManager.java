package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.TranscendenceLevel;
import com.example.minecraftplugin.transcendence.TranscendenceAbilityManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages post-convergence transcendence content for players who have achieved Divine Convergence
 */
public class TranscendenceManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final ConvergenceManager convergenceManager;
    private final TranscendenceAbilityManager abilityManager;
    
    // Track transcendence progress
    private final Map<UUID, Integer> completedChallenges;
    private final Map<UUID, Set<String>> unlockedAbilities;
    private final Map<UUID, Long> lastTranscendenceActivity;
    
    // Available transcendence challenges
    private final Map<String, TranscendenceChallenge> challenges;
    
    public TranscendenceManager(MinecraftPlugin plugin, ConvergenceManager convergenceManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.convergenceManager = convergenceManager;
        this.abilityManager = plugin.getTranscendenceAbilityManager();
        this.completedChallenges = new HashMap<>();
        this.unlockedAbilities = new HashMap<>();
        this.lastTranscendenceActivity = new HashMap<>();
        this.challenges = new HashMap<>();
        
        initializeTranscendenceChallenges();
        startTranscendenceEffectsTask();
        
        logger.info("Transcendence Manager initialized with " + challenges.size() + " challenges");
    }
    
    /**
     * Initialize transcendence challenges
     */
    private void initializeTranscendenceChallenges() {
        // Reality Manipulation Challenges
        challenges.put("reality_forge", new TranscendenceChallenge(
            "reality_forge",
            "The Reality Forge",
            "Create a pocket dimension using pure will",
            TranscendenceLevel.REALITY_SHAPER,
            Arrays.asList("Manipulate 1000 blocks", "Create stable portal", "Maintain for 24 hours")
        ));
        
        challenges.put("time_mastery", new TranscendenceChallenge(
            "time_mastery",
            "Temporal Mastery",
            "Demonstrate complete control over time flow",
            TranscendenceLevel.REALITY_SHAPER,
            Arrays.asList("Freeze time for 10 minutes", "Accelerate growth 1000x", "Reverse entropy")
        ));
        
        challenges.put("void_walking", new TranscendenceChallenge(
            "void_walking",
            "Void Walking",
            "Travel between dimensions without portals",
            TranscendenceLevel.REALITY_SHAPER,
            Arrays.asList("Phase through bedrock", "Walk in void space", "Create void bridges")
        ));
        
        // Cosmic Architecture Challenges
        challenges.put("realm_creation", new TranscendenceChallenge(
            "realm_creation",
            "Realm Creation",
            "Design and build an entirely new realm",
            TranscendenceLevel.COSMIC_ARCHITECT,
            Arrays.asList("Design realm physics", "Populate with life", "Establish natural laws")
        ));
        
        challenges.put("star_forging", new TranscendenceChallenge(
            "star_forging",
            "Star Forging",
            "Create a new star and solar system",
            TranscendenceLevel.COSMIC_ARCHITECT,
            Arrays.asList("Gather cosmic matter", "Ignite stellar core", "Create orbital bodies")
        ));
        
        challenges.put("life_genesis", new TranscendenceChallenge(
            "life_genesis",
            "Life Genesis",
            "Create new forms of sentient life",
            TranscendenceLevel.COSMIC_ARCHITECT,
            Arrays.asList("Design consciousness", "Imbue with souls", "Grant free will")
        ));
        
        // Dimensional Sovereignty Challenges
        challenges.put("multiverse_nexus", new TranscendenceChallenge(
            "multiverse_nexus",
            "Multiverse Nexus",
            "Connect and rule multiple universes",
            TranscendenceLevel.DIMENSIONAL_SOVEREIGN,
            Arrays.asList("Open stable wormholes", "Establish governance", "Maintain cosmic balance")
        ));
        
        challenges.put("entropy_reversal", new TranscendenceChallenge(
            "entropy_reversal",
            "Entropy Reversal",
            "Reverse the heat death of a universe",
            TranscendenceLevel.DIMENSIONAL_SOVEREIGN,
            Arrays.asList("Gather scattered energy", "Rebuild cosmic structure", "Restart time")
        ));
        
        // Universal Deity Challenges
        challenges.put("creation_mastery", new TranscendenceChallenge(
            "creation_mastery",
            "Creation Mastery",
            "Create a new multiverse from nothing",
            TranscendenceLevel.UNIVERSAL_DEITY,
            Arrays.asList("Generate primordial void", "Spark big bang", "Guide evolution")
        ));
        
        challenges.put("omnipresence", new TranscendenceChallenge(
            "omnipresence",
            "Omnipresence",
            "Exist simultaneously across all realities",
            TranscendenceLevel.UNIVERSAL_DEITY,
            Arrays.asList("Split consciousness", "Maintain coherence", "Act in all realms")
        ));
    }
    
    /**
     * Check if a player can access transcendence content
     */
    public boolean canAccessTranscendence(Player player) {
        return convergenceManager.hasAchievedConvergence(player);
    }
    
    /**
     * Get a player's transcendence level
     */
    public TranscendenceLevel getTranscendenceLevel(Player player) {
        if (!canAccessTranscendence(player)) {
            return TranscendenceLevel.NONE;
        }
        
        int challengeCount = completedChallenges.getOrDefault(player.getUniqueId(), 0);
        return TranscendenceLevel.fromChallengeCount(challengeCount);
    }
    
    /**
     * Get available challenges for a player
     */
    public List<TranscendenceChallenge> getAvailableChallenges(Player player) {
        if (!canAccessTranscendence(player)) {
            return new ArrayList<>();
        }
        
        TranscendenceLevel currentLevel = getTranscendenceLevel(player);
        List<TranscendenceChallenge> available = new ArrayList<>();
        
        for (TranscendenceChallenge challenge : challenges.values()) {
            if (challenge.getRequiredLevel().ordinal() <= currentLevel.ordinal() + 1) {
                available.add(challenge);
            }
        }
        
        return available;
    }
    
    /**
     * Complete a transcendence challenge
     */
    public void completeChallenge(Player player, String challengeId) {
        if (!canAccessTranscendence(player)) {
            return;
        }
        
        TranscendenceChallenge challenge = challenges.get(challengeId);
        if (challenge == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        int currentChallenges = completedChallenges.getOrDefault(playerId, 0);
        completedChallenges.put(playerId, currentChallenges + 1);
        
        // Check for level advancement
        TranscendenceLevel newLevel = getTranscendenceLevel(player);
        
        // Create transcendence effects
        createTranscendenceEffects(player, challenge);
        
        // Send messages
        player.sendMessage("§d§l★ TRANSCENDENCE CHALLENGE COMPLETED! ★");
        player.sendMessage("§7Challenge: §f" + challenge.getDisplayName());
        player.sendMessage("§7Level: §f" + newLevel.getTitle());
        player.sendMessage("§7Description: §f" + newLevel.getDescription());
        
        // Unlock new abilities
        unlockTranscendenceAbilities(player, newLevel);
        
        // Update ability manager
        if (abilityManager != null) {
            abilityManager.updatePlayerAbilities(player, newLevel);
            abilityManager.grantRealityPoints(player, 100, "Challenge completion");
        }
        
        // Server announcement
        Bukkit.broadcastMessage("§d§l" + player.getName() + " §r§dhas completed the " + 
                               challenge.getDisplayName() + " transcendence challenge!");
        
        logger.info(player.getName() + " completed transcendence challenge: " + challengeId);
    }
    
    /**
     * Unlock transcendence abilities for a player
     */
    private void unlockTranscendenceAbilities(Player player, TranscendenceLevel level) {
        UUID playerId = player.getUniqueId();
        Set<String> abilities = unlockedAbilities.computeIfAbsent(playerId, k -> new HashSet<>());
        
        // Update ability manager
        if (abilityManager != null) {
            abilityManager.updatePlayerAbilities(player, level);
        }
        
        switch (level) {
            case REALITY_SHAPER:
                abilities.add("reality_manipulation");
                abilities.add("matter_transmutation");
                abilities.add("space_folding");
                player.sendMessage("§d§lUnlocked: §r§dReality Manipulation Abilities");
                break;
                
            case COSMIC_ARCHITECT:
                abilities.add("realm_creation");
                abilities.add("physics_design");
                abilities.add("life_creation");
                player.sendMessage("§d§lUnlocked: §r§dCosmic Architecture Abilities");
                break;
                
            case DIMENSIONAL_SOVEREIGN:
                abilities.add("multiverse_travel");
                abilities.add("dimensional_control");
                abilities.add("cosmic_governance");
                player.sendMessage("§d§lUnlocked: §r§dDimensional Sovereignty Abilities");
                break;
                
            case UNIVERSAL_DEITY:
                abilities.add("omnipotence");
                abilities.add("omniscience");
                abilities.add("omnipresence");
                player.sendMessage("§d§lUnlocked: §r§dUniversal Deity Powers");
                break;
        }
    }
    
    /**
     * Create transcendence completion effects
     */
    private void createTranscendenceEffects(Player player, TranscendenceChallenge challenge) {
        Location location = player.getLocation();
        
        // Play transcendent sounds
        player.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.2f);
        player.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.3f);
        player.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 2.0f);
        
        // Create transcendence particles
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 300) { // 15 seconds
                    cancel();
                    return;
                }
                
                // Create cosmic spiral
                for (int spiral = 0; spiral < 4; spiral++) {
                    double radius = 3.0 + spiral * 0.5;
                    double height = 0.03 * ticks + spiral * 3;
                    double angle = ticks * 0.15 + spiral * 90;
                    
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double y = location.getY() + height;
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    
                    Location particleLocation = new Location(location.getWorld(), x, y, z);
                    
                    // Different particles for each spiral
                    switch (spiral) {
                        case 0:
                            location.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 2, 0.1, 0.1, 0.1, 0.02);
                            break;
                        case 1:
                            location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLocation, 3, 0.2, 0.2, 0.2, 0.05);
                            break;
                        case 2:
                            location.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 5, 0.3, 0.3, 0.3, 0.1);
                            break;
                        case 3:
                            location.getWorld().spawnParticle(Particle.ENCHANT, particleLocation, 8, 0.4, 0.4, 0.4, 0.2);
                            break;
                    }
                }
                
                // Reality distortion waves every 60 ticks
                if (ticks % 60 == 0) {
                    for (int i = 1; i <= 10; i++) {
                        double waveRadius = i * 2.0;
                        for (int j = 0; j < 32; j++) {
                            double waveAngle = (j / 32.0) * 360;
                            double x = location.getX() + waveRadius * Math.cos(Math.toRadians(waveAngle));
                            double z = location.getZ() + waveRadius * Math.sin(Math.toRadians(waveAngle));
                            Location waveLoc = new Location(location.getWorld(), x, location.getY() + 1, z);
                            
                            location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, waveLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Apply transcendence abilities to a player
     */
    public void applyTranscendenceAbilities(Player player) {
        if (!canAccessTranscendence(player)) {
            return;
        }
        
        TranscendenceLevel level = getTranscendenceLevel(player);
        Set<String> abilities = unlockedAbilities.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        // Apply level-specific abilities
        switch (level) {
            case REALITY_SHAPER:
                applyRealityShaperAbilities(player, abilities);
                break;
            case COSMIC_ARCHITECT:
                applyCosmicArchitectAbilities(player, abilities);
                break;
            case DIMENSIONAL_SOVEREIGN:
                applyDimensionalSovereignAbilities(player, abilities);
                break;
            case UNIVERSAL_DEITY:
                applyUniversalDeityAbilities(player, abilities);
                break;
        }
        
        // Update last activity
        lastTranscendenceActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Apply Reality Shaper abilities
     */
    private void applyRealityShaperAbilities(Player player, Set<String> abilities) {
        if (abilities.contains("reality_manipulation")) {
            // Enhanced block manipulation
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.HASTE, 60, 4, false, false));
        }
        
        if (abilities.contains("space_folding")) {
            // Enhanced movement
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, 60, 3, false, false));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.JUMP_BOOST, 60, 4, false, false));
        }
    }
    
    /**
     * Apply Cosmic Architect abilities
     */
    private void applyCosmicArchitectAbilities(Player player, Set<String> abilities) {
        applyRealityShaperAbilities(player, abilities); // Inherit previous abilities
        
        if (abilities.contains("realm_creation")) {
            // Enhanced creative abilities
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.CONDUIT_POWER, 60, 0, false, false));
        }
        
        if (abilities.contains("life_creation")) {
            // Enhanced regeneration and healing
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, 60, 4, false, false));
        }
    }
    
    /**
     * Apply Dimensional Sovereign abilities
     */
    private void applyDimensionalSovereignAbilities(Player player, Set<String> abilities) {
        applyCosmicArchitectAbilities(player, abilities); // Inherit previous abilities
        
        if (abilities.contains("multiverse_travel")) {
            // Enhanced dimensional abilities
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOW_FALLING, 60, 0, false, false));
        }
        
        if (abilities.contains("cosmic_governance")) {
            // Enhanced leadership abilities
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.HERO_OF_THE_VILLAGE, 60, 4, false, false));
        }
    }
    
    /**
     * Apply Universal Deity abilities
     */
    private void applyUniversalDeityAbilities(Player player, Set<String> abilities) {
        applyDimensionalSovereignAbilities(player, abilities); // Inherit previous abilities
        
        if (abilities.contains("omnipotence")) {
            // Ultimate power
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH, 60, 9, false, false));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 60, 4, false, false));
        }
        
        if (abilities.contains("omniscience")) {
            // Ultimate knowledge
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.LUCK, 60, 9, false, false));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION, 60, 0, false, false));
        }
    }
    
    /**
     * Start transcendence effects task
     */
    private void startTranscendenceEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (canAccessTranscendence(player)) {
                        applyTranscendenceAbilities(player);
                        createTranscendenceAura(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Every 5 seconds
    }
    
    /**
     * Create transcendence aura effects
     */
    private void createTranscendenceAura(Player player) {
        TranscendenceLevel level = getTranscendenceLevel(player);
        if (level == TranscendenceLevel.NONE) {
            return;
        }
        
        Location location = player.getLocation();
        
        // Create level-specific aura
        switch (level) {
            case REALITY_SHAPER:
                location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location.clone().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0.05);
                break;
            case COSMIC_ARCHITECT:
                location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location.clone().add(0, 1, 0), 3, 0.6, 0.6, 0.6, 0.08);
                break;
            case DIMENSIONAL_SOVEREIGN:
                location.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0, 1, 0), 4, 0.8, 0.8, 0.8, 0.1);
                break;
            case UNIVERSAL_DEITY:
                location.getWorld().spawnParticle(Particle.HEART, location.clone().add(0, 1, 0), 5, 1.0, 1.0, 1.0, 0.15);
                break;
        }
    }
    
    /**
     * Get transcendence status for a player
     */
    public String getTranscendenceStatus(Player player) {
        if (!canAccessTranscendence(player)) {
            return "§c§lTranscendence not available. Achieve Divine Convergence first.";
        }
        
        TranscendenceLevel level = getTranscendenceLevel(player);
        int challengeCount = completedChallenges.getOrDefault(player.getUniqueId(), 0);
        Set<String> abilities = unlockedAbilities.getOrDefault(player.getUniqueId(), new HashSet<>());
        
        StringBuilder status = new StringBuilder();
        status.append("§d§l=== TRANSCENDENCE STATUS ===§r\n");
        status.append("§7Player: §f").append(player.getName()).append("\n");
        status.append("§7Level: §f").append(level.getTitle()).append("\n");
        status.append("§7Description: §f").append(level.getDescription()).append("\n");
        status.append("§7Challenges Completed: §f").append(challengeCount).append("\n");
        status.append("§7Unlocked Abilities: §f").append(abilities.size()).append("\n");
        
        if (!abilities.isEmpty()) {
            status.append("§d§lActive Abilities:§r\n");
            for (String ability : abilities) {
                status.append("§7• §d").append(ability.replace("_", " ")).append("\n");
            }
        }
        
        return status.toString();
    }
    
    /**
     * Inner class for transcendence challenges
     */
    public static class TranscendenceChallenge {
        private final String id;
        private final String displayName;
        private final String description;
        private final TranscendenceLevel requiredLevel;
        private final List<String> objectives;
        
        public TranscendenceChallenge(String id, String displayName, String description, 
                                    TranscendenceLevel requiredLevel, List<String> objectives) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.requiredLevel = requiredLevel;
            this.objectives = objectives;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public TranscendenceLevel getRequiredLevel() { return requiredLevel; }
        public List<String> getObjectives() { return objectives; }
    }
}