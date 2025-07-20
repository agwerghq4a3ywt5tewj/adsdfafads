package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the bounty system for PvP interactions
 */
public class BountyManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    
    // Active bounties: target UUID -> bounty info
    private final Map<UUID, BountyInfo> activeBounties;
    
    // Bounty history for tracking
    private final List<BountyRecord> bountyHistory;
    
    public BountyManager(MinecraftPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDataManager = playerDataManager;
        this.activeBounties = new HashMap<>();
        this.bountyHistory = new ArrayList<>();
        
        // Start bounty expiration task
        startBountyExpirationTask();
        
        logger.info("Bounty Manager initialized");
    }
    
    /**
     * Place a bounty on a target player
     */
    public boolean placeBounty(Player placer, Player target, int amount, Material currency) {
        UUID placerId = placer.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // Validation checks
        if (placer.equals(target)) {
            placer.sendMessage("§c§lBounty System: §r§cYou cannot place a bounty on yourself!");
            return false;
        }
        
        if (amount <= 0) {
            placer.sendMessage("§c§lBounty System: §r§cBounty amount must be positive!");
            return false;
        }
        
        // Check if placer has enough currency
        if (!hasEnoughCurrency(placer, currency, amount)) {
            placer.sendMessage("§c§lBounty System: §r§cYou don't have enough " + currency.name().toLowerCase() + "!");
            return false;
        }
        
        // Check if target already has a bounty
        BountyInfo existingBounty = activeBounties.get(targetId);
        if (existingBounty != null) {
            // Add to existing bounty
            existingBounty.addToBounty(placerId, amount, currency);
            
            // Remove currency from placer
            removeCurrency(placer, currency, amount);
            
            placer.sendMessage("§6§lBounty Added! §r§6Added " + amount + " " + currency.name().toLowerCase() + 
                             " to the bounty on " + target.getName());
            placer.sendMessage("§7Total bounty: §f" + existingBounty.getTotalAmount() + " " + currency.name().toLowerCase());
            
        } else {
            // Create new bounty
            BountyInfo newBounty = new BountyInfo(targetId, target.getName(), placerId, placer.getName(), 
                                                 amount, currency, System.currentTimeMillis());
            activeBounties.put(targetId, newBounty);
            
            // Remove currency from placer
            removeCurrency(placer, currency, amount);
            
            placer.sendMessage("§6§lBounty Placed! §r§6Placed " + amount + " " + currency.name().toLowerCase() + 
                             " bounty on " + target.getName());
        }
        
        // Notify target
        target.sendMessage("§c§l⚠ BOUNTY PLACED ON YOU! ⚠");
        target.sendMessage("§7Someone has placed a bounty on your head!");
        target.sendMessage("§7Amount: §f" + activeBounties.get(targetId).getTotalAmount() + " " + currency.name().toLowerCase());
        target.sendMessage("§7Other players can now hunt you for this reward!");
        
        // Broadcast to server
        plugin.getServer().broadcastMessage("§6§l💰 BOUNTY ALERT! 💰");
        plugin.getServer().broadcastMessage("§7A bounty of §6" + activeBounties.get(targetId).getTotalAmount() + 
                                          " " + currency.name().toLowerCase() + "§7 has been placed on §c" + target.getName() + "§7!");
        
        logger.info(placer.getName() + " placed " + amount + " " + currency.name() + " bounty on " + target.getName());
        return true;
    }
    
    /**
     * Claim a bounty when a target is killed
     */
    public void claimBounty(Player killer, Player victim) {
        UUID victimId = victim.getUniqueId();
        BountyInfo bounty = activeBounties.get(victimId);
        
        if (bounty == null) {
            return; // No bounty on this player
        }
        
        // Remove bounty from active list
        activeBounties.remove(victimId);
        
        // Award bounty to killer
        ItemStack reward = new ItemStack(bounty.getCurrency(), bounty.getTotalAmount());
        killer.getInventory().addItem(reward);
        
        // Create bounty record
        BountyRecord record = new BountyRecord(bounty, killer.getUniqueId(), killer.getName(), 
                                             System.currentTimeMillis());
        bountyHistory.add(record);
        
        // Notify killer
        killer.sendMessage("§6§l💰 BOUNTY CLAIMED! 💰");
        killer.sendMessage("§7You killed §c" + victim.getName() + "§7 and claimed their bounty!");
        killer.sendMessage("§7Reward: §6" + bounty.getTotalAmount() + " " + bounty.getCurrency().name().toLowerCase());
        
        // Notify victim
        victim.sendMessage("§c§l💀 BOUNTY CLAIMED! 💀");
        victim.sendMessage("§7You were killed by §c" + killer.getName() + "§7 who claimed your bounty!");
        victim.sendMessage("§7Lost bounty: §6" + bounty.getTotalAmount() + " " + bounty.getCurrency().name().toLowerCase());
        
        // Broadcast to server
        plugin.getServer().broadcastMessage("§6§l💰 BOUNTY CLAIMED! 💰");
        plugin.getServer().broadcastMessage("§c" + killer.getName() + "§7 has claimed the bounty on §c" + victim.getName() + "§7!");
        plugin.getServer().broadcastMessage("§7Reward: §6" + bounty.getTotalAmount() + " " + bounty.getCurrency().name().toLowerCase());
        
        logger.info(killer.getName() + " claimed bounty on " + victim.getName() + 
                   " for " + bounty.getTotalAmount() + " " + bounty.getCurrency().name());
    }
    
    /**
     * Get bounty information for a player
     */
    public BountyInfo getBounty(Player player) {
        return getBounty(player.getUniqueId());
    }
    
    public BountyInfo getBounty(UUID playerId) {
        return activeBounties.get(playerId);
    }
    
    /**
     * Get all active bounties
     */
    public Map<UUID, BountyInfo> getActiveBounties() {
        return new HashMap<>(activeBounties);
    }
    
    /**
     * Remove a bounty (admin command)
     */
    public boolean removeBounty(UUID targetId) {
        BountyInfo removed = activeBounties.remove(targetId);
        if (removed != null) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                target.sendMessage("§a§lBounty Removed! §r§aThe bounty on your head has been cleared.");
            }
            return true;
        }
        return false;
    }
    
    /**
     * Check if player has enough currency
     */
    private boolean hasEnoughCurrency(Player player, Material currency, int amount) {
        int totalAmount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency) {
                totalAmount += item.getAmount();
            }
        }
        return totalAmount >= amount;
    }
    
    /**
     * Remove currency from player's inventory
     */
    private void removeCurrency(Player player, Material currency, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == currency) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    contents[i] = null;
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        player.getInventory().setContents(contents);
    }
    
    /**
     * Start bounty expiration task
     */
    private void startBountyExpirationTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            long expirationTime = 7 * 24 * 60 * 60 * 1000L; // 7 days
            
            Iterator<Map.Entry<UUID, BountyInfo>> iterator = activeBounties.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, BountyInfo> entry = iterator.next();
                BountyInfo bounty = entry.getValue();
                
                if (currentTime - bounty.getCreatedTime() > expirationTime) {
                    // Bounty expired, refund placers
                    refundBounty(bounty);
                    iterator.remove();
                    
                    Player target = Bukkit.getPlayer(entry.getKey());
                    if (target != null) {
                        target.sendMessage("§a§lBounty Expired! §r§aThe bounty on your head has expired.");
                    }
                }
            }
        }, 0L, 72000L); // Check every hour
    }
    
    /**
     * Refund a bounty to its placers
     */
    private void refundBounty(BountyInfo bounty) {
        for (Map.Entry<UUID, Integer> entry : bounty.getContributors().entrySet()) {
            Player placer = Bukkit.getPlayer(entry.getKey());
            if (placer != null && placer.isOnline()) {
                ItemStack refund = new ItemStack(bounty.getCurrency(), entry.getValue());
                placer.getInventory().addItem(refund);
                placer.sendMessage("§e§lBounty Refund: §r§eReceived " + entry.getValue() + 
                                 " " + bounty.getCurrency().name().toLowerCase() + 
                                 " from expired bounty on " + bounty.getTargetName());
            }
        }
    }
    
    /**
     * Get bounty statistics
     */
    public Map<String, Object> getBountyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("active_bounties", activeBounties.size());
        stats.put("total_claimed", bountyHistory.size());
        
        // Calculate total bounty value
        int totalValue = activeBounties.values().stream()
            .mapToInt(BountyInfo::getTotalAmount)
            .sum();
        stats.put("total_active_value", totalValue);
        
        // Most wanted player
        BountyInfo highestBounty = activeBounties.values().stream()
            .max(Comparator.comparingInt(BountyInfo::getTotalAmount))
            .orElse(null);
        if (highestBounty != null) {
            stats.put("most_wanted", highestBounty.getTargetName());
            stats.put("highest_bounty", highestBounty.getTotalAmount());
        }
        
        return stats;
    }
    
    /**
     * Bounty information class
     */
    public static class BountyInfo {
        private final UUID targetId;
        private final String targetName;
        private final Material currency;
        private final long createdTime;
        private final Map<UUID, Integer> contributors; // placer -> amount
        private final Map<UUID, String> contributorNames; // placer -> name
        
        public BountyInfo(UUID targetId, String targetName, UUID placerId, String placerName, 
                         int amount, Material currency, long createdTime) {
            this.targetId = targetId;
            this.targetName = targetName;
            this.currency = currency;
            this.createdTime = createdTime;
            this.contributors = new HashMap<>();
            this.contributorNames = new HashMap<>();
            
            contributors.put(placerId, amount);
            contributorNames.put(placerId, placerName);
        }
        
        public void addToBounty(UUID placerId, int amount, Material currency) {
            if (this.currency == currency) {
                contributors.merge(placerId, amount, Integer::sum);
                Player placer = Bukkit.getPlayer(placerId);
                if (placer != null) {
                    contributorNames.put(placerId, placer.getName());
                }
            }
        }
        
        public int getTotalAmount() {
            return contributors.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        // Getters
        public UUID getTargetId() { return targetId; }
        public String getTargetName() { return targetName; }
        public Material getCurrency() { return currency; }
        public long getCreatedTime() { return createdTime; }
        public Map<UUID, Integer> getContributors() { return new HashMap<>(contributors); }
        public Map<UUID, String> getContributorNames() { return new HashMap<>(contributorNames); }
    }
    
    /**
     * Bounty record for history
     */
    public static class BountyRecord {
        private final BountyInfo originalBounty;
        private final UUID killerId;
        private final String killerName;
        private final long claimedTime;
        
        public BountyRecord(BountyInfo originalBounty, UUID killerId, String killerName, long claimedTime) {
            this.originalBounty = originalBounty;
            this.killerId = killerId;
            this.killerName = killerName;
            this.claimedTime = claimedTime;
        }
        
        // Getters
        public BountyInfo getOriginalBounty() { return originalBounty; }
        public UUID getKillerId() { return killerId; }
        public String getKillerName() { return killerName; }
        public long getClaimedTime() { return claimedTime; }
    }
}