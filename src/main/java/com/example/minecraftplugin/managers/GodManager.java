package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.enums.AscensionLevel;
import com.example.minecraftplugin.managers.PlayerDataManager;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.items.HeartOfFallenGod;
import com.example.minecraftplugin.items.MaceOfDivineForging;
import com.example.minecraftplugin.items.ScepterOfBanishment;
import com.example.minecraftplugin.items.TridentOfTheAbyss;
import com.example.minecraftplugin.items.StaffOfSylvanGrowth;
import com.example.minecraftplugin.items.WingsOfTempest;
import com.example.minecraftplugin.items.OrbOfVeilReality;
import com.example.minecraftplugin.items.VoidWalkersBlade;
import com.example.minecraftplugin.items.ChronosStaff;
import com.example.minecraftplugin.items.CrimsonBlade;
import com.example.minecraftplugin.items.ResonanceCrystal;
import com.example.minecraftplugin.items.ShadowMantle;
import com.example.minecraftplugin.items.ConvergenceNexus;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Central manager for all god-related operations in the Testament system
 */
public class GodManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    private final Map<GodType, DivineItem> divineItems;
    private ConvergenceManager convergenceManager;
    
    // Track players with ascension effects for periodic reapplication
    private final Set<UUID> playersWithAscensionEffects;
    
    public GodManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.divineItems = new HashMap<>();
        this.playersWithAscensionEffects = new HashSet<>();
        
        // Initialize convergence manager after this manager is created
        
        initializeGods();
        startAscensionEffectsTask();
    }
    
    /**
     * Initialize all gods and their associated systems
     */
    private void initializeGods() {
        logger.info("Initializing Testament God System...");
        
        // Register divine items
        registerDivineItem(GodType.FALLEN, new HeartOfFallenGod());
        registerDivineItem(GodType.FORGE, new MaceOfDivineForging());
        registerDivineItem(GodType.BANISHMENT, new ScepterOfBanishment());
        registerDivineItem(GodType.ABYSSAL, new TridentOfTheAbyss());
        registerDivineItem(GodType.SYLVAN, new StaffOfSylvanGrowth());
        registerDivineItem(GodType.TEMPEST, new WingsOfTempest());
        registerDivineItem(GodType.VEIL, new OrbOfVeilReality());
        
        // Register expansion god divine items
        registerDivineItem(GodType.VOID, new VoidWalkersBlade());
        registerDivineItem(GodType.TIME, new ChronosStaff());
        registerDivineItem(GodType.BLOOD, new CrimsonBlade());
        registerDivineItem(GodType.CRYSTAL, new ResonanceCrystal());
        registerDivineItem(GodType.SHADOW, new ShadowMantle());
        
        // Register the Convergence Nexus (not tied to a specific god)
        // This will be handled by the ConvergenceManager
        
        // Log all available gods
        for (GodType god : GodType.values()) {
            logger.info("Registered God: " + god.getDisplayName() + " (" + god.getTier().getDisplayName() + ")");
        }
        
        logger.info("Testament God System initialized with " + GodType.values().length + " gods");
        
        // Initialize convergence manager
        this.convergenceManager = new ConvergenceManager(plugin, this, playerDataManager);
    }
    
    /**
     * Start a task that periodically reapplies ascension effects
     */
    private void startAscensionEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Reapply ascension effects for all online players
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (playersWithAscensionEffects.contains(player.getUniqueId())) {
                        AscensionLevel level = getAscensionLevel(player);
                        if (level.hasEffects()) {
                            applyAscensionEffectsToPlayer(player, level);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Run every 60 seconds (1200 ticks)
        
        logger.info("Ascension effects task started - effects will be reapplied every 60 seconds");
    }
    
    /**
     * Register a divine item for a specific god
     */
    public void registerDivineItem(GodType god, DivineItem item) {
        divineItems.put(god, item);
        logger.info("Registered divine item for " + god.getDisplayName() + ": " + item.getClass().getSimpleName());
    }
    
    /**
     * Get the divine item associated with a god
     */
    public DivineItem getDivineItem(GodType god) {
        return divineItems.get(god);
    }
    
    /**
     * Check if a player has completed a testament for a specific god
     */
    public boolean hasCompletedTestament(Player player, GodType god) {
        return playerDataManager.hasCompletedTestament(player, god);
    }
    
    /**
     * Mark a testament as completed for a player
     */
    public void completeTestament(Player player, GodType god) {
        playerDataManager.addCompletedTestament(player, god);
        
        logger.info(player.getName() + " completed testament for " + god.getDisplayName());
        
        // Create enhanced visual effects
        plugin.getVisualEffectsManager().createTestamentCompletionEffect(player, god);
        
        // Check for conflicts
        checkAndResolveConflicts(player, god);
        
        // Update ascension level
        updatePlayerAscension(player);
        
        // Check for Divine Convergence
        convergenceManager.checkForConvergence(player);
        
        // Ensure ascension effects are applied
        AscensionLevel level = getAscensionLevel(player);
        applyAscensionEffects(player, level);
        
        // Grant divine item for this god
        DivineItem divineItem = getDivineItem(god);
        if (divineItem != null) {
            ItemStack divineItemStack = divineItem.createItemStack();
            player.getInventory().addItem(divineItemStack);
            divineItem.onObtained(player, divineItemStack);
        } else {
            player.sendMessage("§e§lDivine Item: §r§eThe divine item for " + god.getDisplayName() + " is not yet available.");
        }
        
        // Log the completion
        plugin.getLogger().info(player.getName() + " completed testament for " + god.getDisplayName());
    }
    
    /**
     * Get all completed testaments for a player
     */
    public Set<GodType> getCompletedTestaments(Player player) {
        return playerDataManager.getCompletedTestaments(player);
    }
    
    /**
     * Add a fragment to a player's collection
     */
    public void addFragment(Player player, GodType god, int fragmentNumber) {
        playerDataManager.addFragment(player, god, fragmentNumber);
        
        logger.info(player.getName() + " collected fragment " + fragmentNumber + 
                   " for " + god.getDisplayName());
    }
    
    /**
     * Check if a player has a specific fragment
     */
    public boolean hasFragment(Player player, GodType god, int fragmentNumber) {
        return playerDataManager.hasFragment(player, god, fragmentNumber);
    }
    
    /**
     * Get all fragments a player has for a specific god
     */
    public Set<Integer> getPlayerFragments(Player player, GodType god) {
        return playerDataManager.getPlayerFragments(player, god);
    }
    
    /**
     * Check if a player has all 7 fragments for a god
     */
    public boolean hasAllFragments(Player player, GodType god) {
        return playerDataManager.hasAllFragments(player, god);
    }
    
    /**
     * Get a missing fragment number for a god (returns -1 if all fragments are collected)
     */
    public int getMissingFragment(Player player, GodType god) {
        return playerDataManager.getMissingFragment(player, god);
    }
    
    /**
     * Get the number of completed testaments for a player
     */
    public int getTestamentCount(Player player) {
        return playerDataManager.getTestamentCount(player);
    }
    
    /**
     * Get a player's current ascension level
     */
    public AscensionLevel getAscensionLevel(Player player) {
        int testamentCount = getTestamentCount(player);
        return AscensionLevel.fromTestamentCount(testamentCount);
    }
    
    /**
     * Check if a player has achieved Divine Convergence (all 12 testaments)
     */
    public boolean hasAchievedConvergence(Player player) {
        return getTestamentCount(player) >= 12;
    }
    
    /**
     * Check and resolve conflicts between opposing gods
     */
    private void checkAndResolveConflicts(Player player, GodType newGod) {
        Set<GodType> completed = getCompletedTestaments(player);
        
        for (GodType existingGod : completed) {
            if (newGod.conflictsWith(existingGod)) {
                logger.info("Divine conflict detected for " + player.getName() + ": " + 
                           newGod.getDisplayName() + " vs " + existingGod.getDisplayName());
                
                // Notify player of testament-level conflict
                handleTestamentConflict(player, newGod, existingGod);
            }
        }
    }
    
    /**
     * Handle testament-level conflicts between gods
     */
    private void handleTestamentConflict(Player player, GodType newGod, GodType existingGod) {
        player.sendMessage("§e§lTESTAMENT CONFLICT! §r§eYou have mastered opposing gods:");
        player.sendMessage("§7• " + newGod.getDisplayName() + " vs " + existingGod.getDisplayName());
        player.sendMessage("§7This may affect your divine powers and create instability.");
        player.sendMessage("§7Consider your path carefully...");
        
        logger.info("Testament conflict for " + player.getName() + 
                   ": " + newGod.getDisplayName() + " vs " + existingGod.getDisplayName());
    }
    
    /**
     * Update a player's ascension level and apply effects
     */
    private void updatePlayerAscension(Player player) {
        AscensionLevel level = getAscensionLevel(player);
        
        player.sendMessage("§6§lASCENSION UPDATE! §r§6You are now: " + level.getTitle());
        player.sendMessage("§7" + level.getDescription());
        
        // Apply ascension effects (to be implemented)
        applyAscensionEffects(player, level);
        
        // Check for convergence after ascension update
        convergenceManager.checkForConvergence(player);
        
        // Track this player for periodic effect reapplication
        playersWithAscensionEffects.add(player.getUniqueId());
    }
    
    /**
     * Apply effects based on ascension level
     */
    public void applyAscensionEffects(Player player, AscensionLevel level) {
        if (!plugin.getPerformanceManager().areEffectsEnabled()) {
            return;
        }
        
        applyAscensionEffectsToPlayer(player, level);
        
        logger.info("Applied " + level.getTitle() + " effects to " + player.getName());
    }
    
    /**
     * Apply ascension effects to a specific player
     */
    private void applyAscensionEffectsToPlayer(Player player, AscensionLevel level) {
        // Remove any existing ascension effects first
        removeAscensionEffects(player);
        
        // Apply new effects based on ascension level
        for (AscensionLevel.EffectData effectData : level.getEffects()) {
            PotionEffect effect = new PotionEffect(
                effectData.getType(),
                effectData.getDuration(),
                effectData.getAmplifier(),
                false, // ambient
                false  // show particles
            );
            
            player.addPotionEffect(effect);
        }
        
        // Send ascension effect message to player
        if (level.hasEffects()) {
            player.sendMessage("§6§l✦ Ascension Effects Active: §r§6" + level.getTitle());
            
            // List active effects
            StringBuilder effectsList = new StringBuilder("§7Active: ");
            for (int i = 0; i < level.getEffects().size(); i++) {
                AscensionLevel.EffectData effectData = level.getEffects().get(i);
                String effectName = getEffectDisplayName(effectData.getType());
                int displayLevel = effectData.getAmplifier() + 1;
                
                effectsList.append("§f").append(effectName);
                if (displayLevel > 1) {
                    effectsList.append(" ").append(displayLevel);
                }
                
                if (i < level.getEffects().size() - 1) {
                    effectsList.append("§7, ");
                }
            }
            player.sendMessage(effectsList.toString());
        }
    }
    
    /**
     * Remove ascension effects from a player
     */
    private void removeAscensionEffects(Player player) {
        // Remove all ascension-related effects
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.SATURATION);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
    }
    
    /**
     * Get display name for potion effect
     */
    private String getEffectDisplayName(PotionEffectType type) {
        String name = type.getName().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    /**
     * Remove a player from ascension effects tracking (called when player leaves)
     */
    public void removePlayerFromAscensionTracking(UUID playerId) {
        playersWithAscensionEffects.remove(playerId);
    }
    
    /**
     * Get god information for display
     */
    public String getGodInfo(GodType god) {
        StringBuilder info = new StringBuilder();
        info.append("§6§l").append(god.getDisplayName()).append("§r\n");
        info.append("§7Theme: §f").append(god.getTheme()).append("\n");
        info.append("§7Tier: §f").append(god.getTier().getDisplayName()).append("\n");
        info.append("§7Altar Center: §f").append(god.getAltarCenterBlock().name()).append("\n");
        info.append("§7Biomes: §f").append(String.join(", ", god.getBiomes()));
        
        return info.toString();
    }
    
    /**
     * Get player testament status for display
     */
    public String getPlayerStatus(Player player) {
        StringBuilder status = new StringBuilder();
        AscensionLevel level = getAscensionLevel(player);
        int testamentCount = getTestamentCount(player);
        
        status.append("§6§l=== TESTAMENT STATUS ===§r\n");
        status.append("§7Player: §f").append(player.getName()).append("\n");
        status.append("§7Ascension: §f").append(level.getTitle()).append("\n");
        status.append("§7Testaments: §f").append(testamentCount).append("/12\n");
        
        if (hasAchievedConvergence(player)) {
            status.append("§5§l★ DIVINE CONVERGENCE ACHIEVED! ★§r\n");
        }
        
        status.append("§7Completed Gods: §f");
        
        Set<GodType> completed = getCompletedTestaments(player);
        if (completed.isEmpty()) {
            status.append("None");
        } else {
            List<String> godNames = new ArrayList<>();
            for (GodType completedGod : completed) {
                godNames.add(completedGod.getDisplayName());
            }
            status.append(String.join(", ", godNames));
        }
        
        return status.toString();
    }
    
    /**
     * Get the convergence manager
     */
    public ConvergenceManager getConvergenceManager() {
        return convergenceManager;
    }
}