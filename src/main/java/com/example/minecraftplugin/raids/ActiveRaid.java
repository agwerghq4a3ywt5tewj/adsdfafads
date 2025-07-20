package com.example.minecraftplugin.raids;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.raids.RaidScalingCalculator;
import com.example.minecraftplugin.raids.WeeklyChallenge;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active raid instance
 */
public class ActiveRaid {
    
    private final String instanceId;
    private final RaidDefinition definition;
    private final List<Player> players;
    private final Location startLocation;
    private final long startTime;
    private final RaidScalingCalculator.RaidScaling scaling;
    private final WeeklyChallenge weeklyChallenge;
    
    private RaidState state;
    private BukkitTask timeoutTask;
    private int currentObjective;
    private final List<String> completedObjectives;
    
    // Eternal Crucible specific tracking
    private int nexusCoreHealth;
    private int currentWave;
    private int godsDefeated;
    private boolean nexusCoreActive;
    
    // Divine Council integration
    private boolean councilBonusActive;
    private double rewardMultiplier;
    
    // Performance tracking
    private int objectivesCompleted;
    private int mobsDefeated;
    private int playersRevived;
    
    public ActiveRaid(String instanceId, RaidDefinition definition, List<Player> players, 
                     Location startLocation, RaidScalingCalculator.RaidScaling scaling, 
                     WeeklyChallenge weeklyChallenge) {
        this.instanceId = instanceId;
        this.definition = definition;
        this.players = new ArrayList<>(players);
        this.startLocation = startLocation;
        this.startTime = System.currentTimeMillis();
        this.scaling = scaling;
        this.weeklyChallenge = weeklyChallenge;
        this.state = RaidState.PREPARING;
        this.currentObjective = 0;
        this.completedObjectives = new ArrayList<>();
        this.councilBonusActive = false;
        this.rewardMultiplier = 1.0;
        this.objectivesCompleted = 0;
        this.mobsDefeated = 0;
        this.playersRevived = 0;
        
        // Initialize Eternal Crucible specific data
        if ("eternal_crucible".equals(definition.getId())) {
            this.nexusCoreHealth = 1000;
            this.currentWave = 0;
            this.godsDefeated = 0;
            this.nexusCoreActive = true;
        }
    }
    
    /**
     * Start the raid
     */
    public void start(MinecraftPlugin plugin) {
        state = RaidState.ACTIVE;
        
        // Notify all players
        for (Player player : players) {
            player.sendTitle("§6§l⚔ RAID STARTED! ⚔", "§7" + definition.getDisplayName(), 20, 60, 20);
            player.sendMessage("§6§l=== RAID STARTED ===§r");
            player.sendMessage("§7Raid: §f" + definition.getDisplayName());
            player.sendMessage("§7Objective: §f" + definition.getObjective());
            
            // Calculate actual time limit with scaling
            int actualTimeLimit = (int) (definition.getTimeLimit() * scaling.getTimeMultiplier());
            player.sendMessage("§7Time Limit: §f" + (actualTimeLimit / 60) + " minutes");
            player.sendMessage("§7Players: §f" + players.size());
            
            // Show scaling information
            player.sendMessage("§7Scaling: §f" + scaling.getFormattedDisplay());
            
            // Show weekly challenge if active
            if (weeklyChallenge != null && weeklyChallenge.isActive()) {
                player.sendMessage("§e§l⭐ Weekly Challenge: §r§e" + weeklyChallenge.getDisplayName());
                player.sendMessage("§7" + weeklyChallenge.getDescription());
            }
            
            player.sendMessage("");
            player.sendMessage("§a§lGood luck! Work together to succeed!");
            
            // Play start sound
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.0f);
        }
        
        // Start timeout timer
        int actualTimeLimit = (int) (definition.getTimeLimit() * scaling.getTimeMultiplier());
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state == RaidState.ACTIVE) {
                    // Raid timed out
                    for (Player player : players) {
                        player.sendTitle("§c§l⏰ TIME'S UP! ⏰", "§7Raid failed due to timeout", 20, 80, 20);
                        player.sendMessage("§c§lRaid Failed! §r§cTime limit exceeded.");
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
                    }
                    
                    // End raid with timeout result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.TIMEOUT);
                    });
                }
            }
        }.runTaskLater(plugin, actualTimeLimit * 20L); // Convert seconds to ticks with scaling
        
        // Start raid progress monitoring
        startProgressMonitoring(plugin);
    }
    
    /**
     * Start monitoring raid progress
     */
    private void startProgressMonitoring(MinecraftPlugin plugin) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (state != RaidState.ACTIVE) {
                    cancel();
                    return;
                }
                
                // Update every 30 seconds
                if (ticks % 600 == 0) {
                    updateRaidProgress();
                }
                
                // Check for raid completion conditions
                if (checkRaidCompletion()) {
                    // Raid completed successfully
                    for (Player player : players) {
                        player.sendTitle("§a§l✓ RAID COMPLETED! ✓", "§7Victory achieved!", 20, 100, 20);
                        player.sendMessage("§a§l✓ RAID COMPLETED SUCCESSFULLY! ✓");
                        player.sendMessage("§7Congratulations! You have conquered " + definition.getDisplayName());
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    
                    // End raid with success result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.SUCCESS);
                    });
                    cancel();
                    return;
                }
                
                // Check for raid failure conditions
                if (checkRaidFailure()) {
                    // Raid failed
                    for (Player player : players) {
                        player.sendTitle("§c§l✗ RAID FAILED! ✗", "§7Better luck next time", 20, 80, 20);
                        player.sendMessage("§c§l✗ RAID FAILED! ✗");
                        player.sendMessage("§7The raid has ended in failure. Regroup and try again!");
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
                    }
                    
                    // End raid with failure result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.FAILURE);
                    });
                    cancel();
                    return;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Update raid progress for all players
     */
    private void updateRaidProgress() {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        long remainingTime = definition.getTimeLimit() - elapsedTime;
        
        for (Player player : players) {
            player.sendMessage("§e§lRaid Progress Update:");
            player.sendMessage("§7Time Remaining: §f" + (remainingTime / 60) + ":" + String.format("%02d", remainingTime % 60));
            player.sendMessage("§7Objectives Completed: §f" + completedObjectives.size());
            player.sendMessage("§7Current Objective: §f" + getCurrentObjectiveDescription());
        }
    }
    
    /**
     * Check if raid is completed
     */
    private boolean checkRaidCompletion() {
        // Check for Eternal Crucible specific completion
        if ("eternal_crucible".equals(definition.getId())) {
            return checkEternalCrucibleCompletion();
        }
        
        // Default completion check for other raids
        return completedObjectives.size() >= getRequiredObjectives();
    }
    
    /**
     * Check if raid has failed
     */
    private boolean checkRaidFailure() {
        // Check for Eternal Crucible specific failure
        if ("eternal_crucible".equals(definition.getId())) {
            return checkEternalCrucibleFailure();
        }
        
        // Check if all players are dead or disconnected
        int alivePlayers = 0;
        for (Player player : players) {
            if (player.isOnline() && player.getHealth() > 0) {
                alivePlayers++;
            }
        }
        
        return alivePlayers == 0;
    }
    
    /**
     * Check Eternal Crucible completion conditions
     */
    private boolean checkEternalCrucibleCompletion() {
        // Must defeat all 12 god manifestations and keep Nexus Core alive
        return godsDefeated >= 12 && nexusCoreActive && nexusCoreHealth > 0;
    }
    
    /**
     * Check Eternal Crucible failure conditions
     */
    private boolean checkEternalCrucibleFailure() {
        // Fail if Nexus Core is destroyed or all players are dead
        if (!nexusCoreActive || nexusCoreHealth <= 0) {
            for (Player player : players) {
                player.sendMessage("§c§l✗ THE NEXUS CORE HAS BEEN DESTROYED! ✗");
                player.sendMessage("§7The cosmic balance has been shattered!");
            }
            return true;
        }
        
        // Check if all players are dead
        int alivePlayers = 0;
        for (Player player : players) {
            if (player.isOnline() && player.getHealth() > 0) {
                alivePlayers++;
            }
        }
        
        return alivePlayers == 0;
    }
    
    /**
     * Handle Eternal Crucible specific events
     */
    public void handleEternalCrucibleEvent(String eventType, Object... params) {
        switch (eventType) {
            case "god_defeated":
                godsDefeated++;
                String godName = (String) params[0];
                completeObjective("Defeated " + godName + " manifestation (" + godsDefeated + "/12)");
                break;
            case "nexus_damage":
                int damage = (Integer) params[0];
                nexusCoreHealth = Math.max(0, nexusCoreHealth - damage);
                for (Player player : players) {
                    player.sendMessage("§c§l⚠ NEXUS CORE DAMAGED! ⚠ Health: " + nexusCoreHealth + "/1000");
                }
                break;
            case "wave_complete":
                currentWave++;
                break;
        }
    }
    
    /**
     * Get current objective description
     */
    private String getCurrentObjectiveDescription() {
        if ("eternal_crucible".equals(definition.getId())) {
            return "Wave " + (currentWave + 1) + " - Defeat god manifestations (" + godsDefeated + "/12) - Protect Nexus Core (" + nexusCoreHealth + "/1000 HP)";
        }
        
        String baseObjective = definition.getObjective();
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            return baseObjective + " §e(Weekly Challenge: " + weeklyChallenge.getDisplayName() + ")";
        }
        return baseObjective;
    }
    
    /**
     * Get required number of objectives for completion
     */
    private int getRequiredObjectives() {
        // This would be defined per raid type
        switch (definition.getTier()) {
            case NOVICE:
                return 3;
            case ADEPT:
                return 5;
            case MASTER:
                return 7;
            case CONVERGENCE:
                return 10;
            default:
                return 1;
        }
    }
    
    /**
     * Complete an objective
     */
    public void completeObjective(String objectiveDescription) {
        completedObjectives.add(objectiveDescription);
        currentObjective++;
        objectivesCompleted++;
        
        // Notify all players
        for (Player player : players) {
            player.sendMessage("§a§l✓ Objective Completed: §r§a" + objectiveDescription);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        }
    }
    
    /**
     * Record mob defeat for statistics
     */
    public void recordMobDefeat() {
        mobsDefeated++;
    }
    
    /**
     * Record player revival for statistics
     */
    public void recordPlayerRevival() {
        playersRevived++;
    }
    
    /**
     * Remove a player from the raid
     */
    public void removePlayer(Player player) {
        players.remove(player);
        
        // Notify remaining players
        for (Player remainingPlayer : players) {
            remainingPlayer.sendMessage("§e§l" + player.getName() + " §r§ehas left the raid.");
        }
    }
    
    /**
     * End the raid
     */
    public void end(RaidManager.RaidResult result) {
        state = RaidState.COMPLETED;
        
        // Cancel timeout task
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        
        // Teleport players back to start location
        for (Player player : players) {
            if (player.isOnline()) {
                player.teleport(startLocation);
            }
        }
        
        // Award rewards based on result
        if (result == RaidManager.RaidResult.SUCCESS) {
            awardRewards();
        }
    }
    
    /**
     * Award rewards to players
     */
    private void awardRewards() {
        for (Player player : players) {
            if (!player.isOnline()) {
                continue;
            }
            
            // Award XP
            int xpReward = calculateXpReward();
            player.giveExp(xpReward);
            
            // Award items based on raid tier
            awardTierSpecificRewards(player);
            
            // Show council bonus if active
            String bonusText = councilBonusActive ? " §d(Council Bonus!)" : "";
            
            player.sendMessage("§a§lRewards Earned:");
            player.sendMessage("§7• Experience: §f" + xpReward + " XP" + bonusText);
            player.sendMessage("§7• Tier-specific rewards based on raid difficulty");
            
            if (councilBonusActive) {
                player.sendMessage("§d§l✦ Divine Council Blessing Applied! ✦");
                player.sendMessage("§7The Divine Council has enhanced your raid rewards!");
            }
        }
    }
    
    /**
     * Calculate XP reward based on raid tier and performance
     */
    private int calculateXpReward() {
        int baseXp;
        switch (definition.getTier()) {
            case NOVICE:
                baseXp = 100;
                break;
            case ADEPT:
                baseXp = 250;
                break;
            case MASTER:
                baseXp = 500;
                break;
            case CONVERGENCE:
                baseXp = 1000;
                break;
            default:
                baseXp = 50;
                break;
        }
        
        // Bonus for completing objectives
        int objectiveBonus = completedObjectives.size() * 50;
        
        int totalXp = baseXp + objectiveBonus;
        
        // Apply Divine Council bonus if active
        if (councilBonusActive) {
            totalXp = (int) (totalXp * rewardMultiplier);
        }
        
        // Apply weekly challenge bonus
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            totalXp = (int) (totalXp * 1.3); // 30% bonus for weekly challenge
        }
        
        return totalXp;
    }
    
    /**
     * Award tier-specific rewards
     */
    private void awardTierSpecificRewards(Player player) {
        switch (definition.getTier()) {
            case NOVICE:
                awardNoviceRewards(player);
                break;
            case ADEPT:
                awardAdeptRewards(player);
                break;
            case MASTER:
                awardMasterRewards(player);
                break;
            case CONVERGENCE:
                awardConvergenceRewards(player);
                break;
        }
    }
    
    /**
     * Award rewards for Novice tier raids
     */
    private void awardNoviceRewards(Player player) {
        player.sendMessage("§a§l✦ NOVICE RAID REWARDS ✦");
        
        // Guaranteed divine fragment
        player.sendMessage("§7• §6Divine Fragment §7(guaranteed)");
        // TODO: Actually give a random divine fragment
        
        // Basic upgrade materials
        player.sendMessage("§7• §fBasic Upgrade Materials:");
        
        // Iron ingots (5-10)
        int ironAmount = 5 + (int)(Math.random() * 6);
        ItemStack iron = new ItemStack(org.bukkit.Material.IRON_INGOT, ironAmount);
        player.getInventory().addItem(iron);
        player.sendMessage("§7  - §fIron Ingots x" + ironAmount);
        
        // Coal (8-15)
        int coalAmount = 8 + (int)(Math.random() * 8);
        ItemStack coal = new ItemStack(org.bukkit.Material.COAL, coalAmount);
        player.getInventory().addItem(coal);
        player.sendMessage("§7  - §fCoal x" + coalAmount);
        
        // String (3-8)
        int stringAmount = 3 + (int)(Math.random() * 6);
        ItemStack string = new ItemStack(org.bukkit.Material.STRING, stringAmount);
        player.getInventory().addItem(string);
        player.sendMessage("§7  - §fString x" + stringAmount);
        
        // Bones (2-6)
        int boneAmount = 2 + (int)(Math.random() * 5);
        ItemStack bones = new ItemStack(org.bukkit.Material.BONE, boneAmount);
        player.getInventory().addItem(bones);
        player.sendMessage("§7  - §fBones x" + boneAmount);
        
        // Leather (1-4)
        int leatherAmount = 1 + (int)(Math.random() * 4);
        ItemStack leather = new ItemStack(org.bukkit.Material.LEATHER, leatherAmount);
        player.getInventory().addItem(leather);
        player.sendMessage("§7  - §fLeather x" + leatherAmount);
    }
    
    /**
     * Award rewards for Adept tier raids
     */
    private void awardAdeptRewards(Player player) {
        player.sendMessage("§e§l✦ ADEPT RAID REWARDS ✦");
        
        // Rare divine fragments (higher chance)
        player.sendMessage("§7• §6Rare Divine Fragments §7(increased chance)");
        // TODO: Actually give rare divine fragments with higher probability
        
        // Divine item upgrade components
        player.sendMessage("§7• §bDivine Item Upgrade Components:");
        
        // Gold ingots (3-7)
        int goldAmount = 3 + (int)(Math.random() * 5);
        ItemStack gold = new ItemStack(org.bukkit.Material.GOLD_INGOT, goldAmount);
        player.getInventory().addItem(gold);
        player.sendMessage("§7  - §6Gold Ingots x" + goldAmount);
        
        // Redstone (10-20)
        int redstoneAmount = 10 + (int)(Math.random() * 11);
        ItemStack redstone = new ItemStack(org.bukkit.Material.REDSTONE, redstoneAmount);
        player.getInventory().addItem(redstone);
        player.sendMessage("§7  - §cRedstone x" + redstoneAmount);
        
        // Lapis Lazuli (5-12)
        int lapisAmount = 5 + (int)(Math.random() * 8);
        ItemStack lapis = new ItemStack(org.bukkit.Material.LAPIS_LAZULI, lapisAmount);
        player.getInventory().addItem(lapis);
        player.sendMessage("§7  - §9Lapis Lazuli x" + lapisAmount);
        
        // Ender Pearls (1-3)
        int pearlAmount = 1 + (int)(Math.random() * 3);
        ItemStack pearls = new ItemStack(org.bukkit.Material.ENDER_PEARL, pearlAmount);
        player.getInventory().addItem(pearls);
        player.sendMessage("§7  - §5Ender Pearls x" + pearlAmount);
        
        // Blaze Powder (2-5)
        int blazeAmount = 2 + (int)(Math.random() * 4);
        ItemStack blaze = new ItemStack(org.bukkit.Material.BLAZE_POWDER, blazeAmount);
        player.getInventory().addItem(blaze);
        player.sendMessage("§7  - §6Blaze Powder x" + blazeAmount);
        
        // Ghast Tear (1-2) - rare component
        if (Math.random() < 0.7) { // 70% chance
            int tearAmount = 1 + (int)(Math.random() * 2);
            ItemStack tears = new ItemStack(org.bukkit.Material.GHAST_TEAR, tearAmount);
            player.getInventory().addItem(tears);
            player.sendMessage("§7  - §fGhast Tears x" + tearAmount + " §7(rare!)");
        }
    }
    
    /**
     * Award rewards for Master tier raids
     */
    private void awardMasterRewards(Player player) {
        player.sendMessage("§6§l✦ MASTER RAID REWARDS ✦");
        
        // Legendary materials
        player.sendMessage("§7• §dLegendary Materials:");
        
        // Diamond (2-5)
        int diamondAmount = 2 + (int)(Math.random() * 4);
        ItemStack diamonds = new ItemStack(org.bukkit.Material.DIAMOND, diamondAmount);
        player.getInventory().addItem(diamonds);
        player.sendMessage("§7  - §bDiamonds x" + diamondAmount);
        
        // Netherite Scrap (1-3)
        int netheriteAmount = 1 + (int)(Math.random() * 3);
        ItemStack netherite = new ItemStack(org.bukkit.Material.NETHERITE_SCRAP, netheriteAmount);
        player.getInventory().addItem(netherite);
        player.sendMessage("§7  - §8Netherite Scrap x" + netheriteAmount);
        
        // Ancient Debris (1-2) - very rare
        if (Math.random() < 0.5) { // 50% chance
            int debrisAmount = 1 + (int)(Math.random() * 2);
            ItemStack debris = new ItemStack(org.bukkit.Material.ANCIENT_DEBRIS, debrisAmount);
            player.getInventory().addItem(debris);
            player.sendMessage("§7  - §8Ancient Debris x" + debrisAmount + " §7(legendary!)");
        }
        
        // Emerald (3-8)
        int emeraldAmount = 3 + (int)(Math.random() * 6);
        ItemStack emeralds = new ItemStack(org.bukkit.Material.EMERALD, emeraldAmount);
        player.getInventory().addItem(emeralds);
        player.sendMessage("§7  - §aEmeralds x" + emeraldAmount);
        
        // Nether Star (1) - guaranteed legendary component
        ItemStack netherStar = new ItemStack(org.bukkit.Material.NETHER_STAR, 1);
        player.getInventory().addItem(netherStar);
        player.sendMessage("§7  - §fNether Star x1 §7(guaranteed!)");
        
        // Advanced divine enhancements
        player.sendMessage("§7• §5Advanced Divine Enhancements:");
        player.sendMessage("§7  - Enhanced divine item properties");
        player.sendMessage("§7  - Increased divine power effectiveness");
    }
    
    /**
     * Award specific rewards for Convergence raids
     */
    private void awardConvergenceRewards(Player player) {
        if ("eternal_crucible".equals(definition.getId())) {
            // Eternal Crucible specific rewards
            player.sendMessage("§5§l✦ ETERNAL CRUCIBLE REWARDS ✦");
            player.sendMessage("§7• §5Crucible Crown §7- Legendary headpiece");
            player.sendMessage("§7• §5Divine Essence Crystals §7(x5)");
            player.sendMessage("§7• §5Nexus Core Fragment §7- Ultimate crafting material");
            player.sendMessage("§7• §5Eternal Title §7- 'Crucible Survivor'");
            player.sendMessage("§7• §5Permanent +2 Hearts §7bonus");
            
            // Apply permanent health bonus
            org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double currentMax = healthAttr.getBaseValue();
                healthAttr.setBaseValue(Math.min(60.0, currentMax + 4.0)); // +2 hearts, max 30 hearts
                player.sendMessage("§a§lPermanent Health Bonus Applied! §r§aYou now have " + (int)(healthAttr.getBaseValue() / 2) + " hearts!");
            }
        } else {
            // Default convergence rewards
            player.sendMessage("§7• Convergence Nexus upgrades");
            player.sendMessage("§7• Unique cosmetic rewards");
            player.sendMessage("§7• Server-wide recognition");
        }
    }
    
    // Getters
    public String getInstanceId() {
        return instanceId;
    }
    
    public RaidDefinition getDefinition() {
        return definition;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public Location getStartLocation() {
        return startLocation;
    }
    
    public RaidState getState() {
        return state;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public List<String> getCompletedObjectives() {
        return new ArrayList<>(completedObjectives);
    }
    
    // Getters for Eternal Crucible specific data
    public int getNexusCoreHealth() {
        return nexusCoreHealth;
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public int getGodsDefeated() {
        return godsDefeated;
    }
    
    public boolean isNexusCoreActive() {
        return nexusCoreActive;
    }
    
    // Divine Council integration getters/setters
    public boolean isCouncilBonusActive() {
        return councilBonusActive;
    }
    
    public void setCouncilBonusActive(boolean councilBonusActive) {
        this.councilBonusActive = councilBonusActive;
        this.rewardMultiplier = councilBonusActive ? 1.5 : 1.0; // 50% bonus
    }
    
    public double getRewardMultiplier() {
        return rewardMultiplier;
    }
    
    // Getters for scaling and weekly challenge
    public RaidScalingCalculator.RaidScaling getScaling() {
        return scaling;
    }
    
    public WeeklyChallenge getWeeklyChallenge() {
        return weeklyChallenge;
    }
    
    // Getters for performance tracking
    public int getObjectivesCompleted() {
        return objectivesCompleted;
    }
    
    public int getMobsDefeated() {
        return mobsDefeated;
    }
    
    public int getPlayersRevived() {
        return playersRevived;
    }
    
    /**
     * Raid states
     */
    public enum RaidState {
        PREPARING,
        ACTIVE,
        COMPLETED
    }
}