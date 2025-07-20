package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages raid guilds for team formation and persistent raid groups
 */
public class GuildManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final File guildFile;
    private FileConfiguration guildConfig;
    
    // Guild data
    private final Map<String, Guild> guilds; // guild_id -> Guild
    private final Map<UUID, String> playerGuilds; // player_id -> guild_id
    private final Map<UUID, Set<String>> pendingInvites; // player_id -> set of guild_ids
    
    public GuildManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.guildFile = new File(plugin.getDataFolder(), "guilds.yml");
        this.guilds = new HashMap<>();
        this.playerGuilds = new HashMap<>();
        this.pendingInvites = new HashMap<>();
        
        loadGuildData();
        
        logger.info("Guild Manager initialized with " + guilds.size() + " guilds");
    }
    
    /**
     * Load guild data from file
     */
    private void loadGuildData() {
        if (!guildFile.exists()) {
            try {
                guildFile.getParentFile().mkdirs();
                guildFile.createNewFile();
                logger.info("Created new guild data file: " + guildFile.getName());
            } catch (IOException e) {
                logger.severe("Could not create guild data file: " + e.getMessage());
                return;
            }
        }
        
        guildConfig = YamlConfiguration.loadConfiguration(guildFile);
        
        // Load guilds
        if (guildConfig.contains("guilds")) {
            for (String guildId : guildConfig.getConfigurationSection("guilds").getKeys(false)) {
                try {
                    Guild guild = loadGuildFromConfig(guildId);
                    if (guild != null) {
                        guilds.put(guildId, guild);
                        
                        // Update player guild mapping
                        for (UUID memberId : guild.getAllMembers().keySet()) {
                            playerGuilds.put(memberId, guildId);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to load guild " + guildId + ": " + e.getMessage());
                }
            }
        }
        
        logger.info("Loaded " + guilds.size() + " guilds");
    }
    
    /**
     * Load a guild from config
     */
    private Guild loadGuildFromConfig(String guildId) {
        String path = "guilds." + guildId;
        
        String name = guildConfig.getString(path + ".name");
        String leaderIdString = guildConfig.getString(path + ".leader");
        long createdTime = guildConfig.getLong(path + ".created_time");
        
        if (name == null || leaderIdString == null) {
            return null;
        }
        
        UUID leaderId;
        try {
            leaderId = UUID.fromString(leaderIdString);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid leader UUID in guild " + guildId + ": " + leaderIdString);
            return null;
        }
        
        Guild guild = new Guild(guildId, name, leaderId, createdTime);
        
        // Load members
        if (guildConfig.contains(path + ".members")) {
            for (String memberIdString : guildConfig.getConfigurationSection(path + ".members").getKeys(false)) {
                try {
                    UUID memberId = UUID.fromString(memberIdString);
                    String roleString = guildConfig.getString(path + ".members." + memberIdString + ".role");
                    long joinTime = guildConfig.getLong(path + ".members." + memberIdString + ".join_time");
                    
                    GuildRole role = GuildRole.valueOf(roleString);
                    guild.addMember(memberId, role, joinTime);
                } catch (Exception e) {
                    logger.warning("Failed to load member " + memberIdString + " for guild " + guildId + ": " + e.getMessage());
                }
            }
        }
        
        // Load statistics
        if (guildConfig.contains(path + ".statistics")) {
            int raidsCompleted = guildConfig.getInt(path + ".statistics.raids_completed");
            int totalScore = guildConfig.getInt(path + ".statistics.total_score");
            long bestTime = guildConfig.getLong(path + ".statistics.best_time");
            
            guild.setStatistics(raidsCompleted, totalScore, bestTime);
        }
        
        return guild;
    }
    
    /**
     * Save guild data to file
     */
    public void saveGuildData() {
        for (Map.Entry<String, Guild> entry : guilds.entrySet()) {
            saveGuildToConfig(entry.getKey(), entry.getValue());
        }
        
        try {
            guildConfig.save(guildFile);
            logger.info("Saved guild data for " + guilds.size() + " guilds");
        } catch (IOException e) {
            logger.severe("Could not save guild data: " + e.getMessage());
        }
    }
    
    /**
     * Save a guild to config
     */
    private void saveGuildToConfig(String guildId, Guild guild) {
        String path = "guilds." + guildId;
        
        guildConfig.set(path + ".name", guild.getName());
        guildConfig.set(path + ".leader", guild.getLeader().toString());
        guildConfig.set(path + ".created_time", guild.getCreatedTime());
        
        // Save members
        guildConfig.set(path + ".members", null); // Clear existing
        for (Map.Entry<UUID, GuildRole> entry : guild.getAllMembers().entrySet()) {
            String memberPath = path + ".members." + entry.getKey().toString();
            guildConfig.set(memberPath + ".role", entry.getValue().name());
            guildConfig.set(memberPath + ".join_time", guild.getMemberJoinTime(entry.getKey()));
        }
        
        // Save statistics
        guildConfig.set(path + ".statistics.raids_completed", guild.getRaidsCompleted());
        guildConfig.set(path + ".statistics.total_score", guild.getTotalScore());
        guildConfig.set(path + ".statistics.best_time", guild.getBestTime());
    }
    
    /**
     * Create a new guild
     */
    public boolean createGuild(Player leader, String guildName) {
        UUID leaderId = leader.getUniqueId();
        
        // Check if player is already in a guild
        if (playerGuilds.containsKey(leaderId)) {
            return false;
        }
        
        // Check if guild name is already taken
        for (Guild guild : guilds.values()) {
            if (guild.getName().equalsIgnoreCase(guildName)) {
                return false;
            }
        }
        
        // Create guild
        String guildId = "guild_" + System.currentTimeMillis();
        Guild guild = new Guild(guildId, guildName, leaderId, System.currentTimeMillis());
        guild.addMember(leaderId, GuildRole.LEADER, System.currentTimeMillis());
        
        guilds.put(guildId, guild);
        playerGuilds.put(leaderId, guildId);
        
        saveGuildData();
        
        leader.sendMessage("§a§lGuild Created! §r§aYou have created the guild '" + guildName + "'");
        
        logger.info(leader.getName() + " created guild: " + guildName);
        return true;
    }
    
    /**
     * Invite a player to a guild
     */
    public boolean invitePlayer(Player inviter, Player target, String guildName) {
        UUID inviterId = inviter.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // Find guild
        Guild guild = getGuildByName(guildName);
        if (guild == null) {
            return false;
        }
        
        // Check if inviter has permission
        GuildRole inviterRole = guild.getMemberRole(inviterId);
        if (inviterRole == null || !inviterRole.canInvite()) {
            return false;
        }
        
        // Check if target is already in a guild
        if (playerGuilds.containsKey(targetId)) {
            return false;
        }
        
        // Check if already invited
        Set<String> targetInvites = pendingInvites.computeIfAbsent(targetId, k -> new HashSet<>());
        if (targetInvites.contains(guild.getId())) {
            return false;
        }
        
        // Add invitation
        targetInvites.add(guild.getId());
        
        // Notify players
        inviter.sendMessage("§a§lInvitation Sent! §r§aYou invited " + target.getName() + " to join '" + guild.getName() + "'");
        target.sendMessage("§e§lGuild Invitation! §r§e" + inviter.getName() + " has invited you to join '" + guild.getName() + "'");
        target.sendMessage("§7Use §f/guild join " + guild.getName() + "§7 to accept or §f/guild decline " + guild.getName() + "§7 to decline");
        
        return true;
    }
    
    /**
     * Join a guild
     */
    public boolean joinGuild(Player player, String guildName) {
        UUID playerId = player.getUniqueId();
        
        // Check if player is already in a guild
        if (playerGuilds.containsKey(playerId)) {
            return false;
        }
        
        // Find guild
        Guild guild = getGuildByName(guildName);
        if (guild == null) {
            return false;
        }
        
        // Check if player has pending invitation
        Set<String> playerInvites = pendingInvites.get(playerId);
        if (playerInvites == null || !playerInvites.contains(guild.getId())) {
            return false;
        }
        
        // Add player to guild
        guild.addMember(playerId, GuildRole.MEMBER, System.currentTimeMillis());
        playerGuilds.put(playerId, guild.getId());
        playerInvites.remove(guild.getId());
        
        saveGuildData();
        
        // Notify guild members
        notifyGuildMembers(guild, "§a§l" + player.getName() + " §r§ahas joined the guild!");
        
        logger.info(player.getName() + " joined guild: " + guild.getName());
        return true;
    }
    
    /**
     * Leave a guild
     */
    public boolean leaveGuild(Player player) {
        UUID playerId = player.getUniqueId();
        String guildId = playerGuilds.get(playerId);
        
        if (guildId == null) {
            return false;
        }
        
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return false;
        }
        
        // Check if player is the leader
        if (guild.getLeader().equals(playerId)) {
            // Transfer leadership or disband guild
            if (guild.getAllMembers().size() > 1) {
                // Transfer to highest ranking member
                UUID newLeader = guild.getAllMembers().entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(playerId))
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
                
                if (newLeader != null) {
                    guild.setLeader(newLeader);
                    guild.setMemberRole(newLeader, GuildRole.LEADER);
                    
                    Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                    if (newLeaderPlayer != null) {
                        newLeaderPlayer.sendMessage("§6§lYou are now the leader of '" + guild.getName() + "'!");
                    }
                    
                    notifyGuildMembers(guild, "§e§l" + player.getName() + " §r§ehas left the guild. Leadership transferred to " + 
                                     (newLeaderPlayer != null ? newLeaderPlayer.getName() : "a member"));
                }
            } else {
                // Disband guild
                disbandGuild(guild);
                player.sendMessage("§c§lGuild disbanded! §r§cYou were the last member.");
                return true;
            }
        } else {
            notifyGuildMembers(guild, "§c§l" + player.getName() + " §r§chas left the guild.");
        }
        
        // Remove player from guild
        guild.removeMember(playerId);
        playerGuilds.remove(playerId);
        
        saveGuildData();
        
        player.sendMessage("§c§lLeft Guild! §r§cYou have left '" + guild.getName() + "'");
        
        logger.info(player.getName() + " left guild: " + guild.getName());
        return true;
    }
    
    /**
     * Disband a guild
     */
    private void disbandGuild(Guild guild) {
        // Remove all members from mapping
        for (UUID memberId : guild.getAllMembers().keySet()) {
            playerGuilds.remove(memberId);
        }
        
        // Remove guild
        guilds.remove(guild.getId());
        
        saveGuildData();
        
        logger.info("Guild disbanded: " + guild.getName());
    }
    
    /**
     * Get guild by name
     */
    private Guild getGuildByName(String name) {
        return guilds.values().stream()
            .filter(guild -> guild.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get player's guild
     */
    public Guild getPlayerGuild(Player player) {
        String guildId = playerGuilds.get(player.getUniqueId());
        return guildId != null ? guilds.get(guildId) : null;
    }
    
    /**
     * Check if player is in a guild
     */
    public boolean isInGuild(Player player) {
        return playerGuilds.containsKey(player.getUniqueId());
    }
    
    /**
     * Get all guilds
     */
    public Collection<Guild> getAllGuilds() {
        return new ArrayList<>(guilds.values());
    }
    
    /**
     * Notify all guild members
     */
    private void notifyGuildMembers(Guild guild, String message) {
        for (UUID memberId : guild.getAllMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
    
    /**
     * Record raid completion for guild
     */
    public void recordGuildRaidCompletion(Guild guild, int score, long completionTime) {
        guild.addRaidCompletion(score, completionTime);
        saveGuildData();
    }
    
    /**
     * Get guild statistics
     */
    public Map<String, Object> getGuildStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_guilds", guilds.size());
        stats.put("total_members", playerGuilds.size());
        
        // Average guild size
        double avgSize = guilds.isEmpty() ? 0 : (double) playerGuilds.size() / guilds.size();
        stats.put("average_guild_size", avgSize);
        
        // Most active guild
        Guild mostActive = guilds.values().stream()
            .max(Comparator.comparingInt(Guild::getRaidsCompleted))
            .orElse(null);
        if (mostActive != null) {
            stats.put("most_active_guild", mostActive.getName());
            stats.put("most_raids_completed", mostActive.getRaidsCompleted());
        }
        
        return stats;
    }
    
    /**
     * Shutdown and save data
     */
    public void shutdown() {
        saveGuildData();
        logger.info("Guild Manager shutdown complete");
    }
    
    /**
     * Guild class
     */
    public static class Guild {
        private final String id;
        private final String name;
        private UUID leader;
        private final long createdTime;
        private final Map<UUID, GuildRole> members;
        private final Map<UUID, Long> memberJoinTimes;
        
        // Statistics
        private int raidsCompleted;
        private int totalScore;
        private long bestTime;
        
        public Guild(String id, String name, UUID leader, long createdTime) {
            this.id = id;
            this.name = name;
            this.leader = leader;
            this.createdTime = createdTime;
            this.members = new HashMap<>();
            this.memberJoinTimes = new HashMap<>();
            this.raidsCompleted = 0;
            this.totalScore = 0;
            this.bestTime = Long.MAX_VALUE;
        }
        
        public void addMember(UUID playerId, GuildRole role, long joinTime) {
            members.put(playerId, role);
            memberJoinTimes.put(playerId, joinTime);
        }
        
        public void removeMember(UUID playerId) {
            members.remove(playerId);
            memberJoinTimes.remove(playerId);
        }
        
        public void setMemberRole(UUID playerId, GuildRole role) {
            if (members.containsKey(playerId)) {
                members.put(playerId, role);
            }
        }
        
        public void addRaidCompletion(int score, long completionTime) {
            raidsCompleted++;
            totalScore += score;
            if (completionTime < bestTime) {
                bestTime = completionTime;
            }
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public UUID getLeader() { return leader; }
        public void setLeader(UUID leader) { this.leader = leader; }
        public long getCreatedTime() { return createdTime; }
        public Map<UUID, GuildRole> getAllMembers() { return new HashMap<>(members); }
        public GuildRole getMemberRole(UUID playerId) { return members.get(playerId); }
        public Long getMemberJoinTime(UUID playerId) { return memberJoinTimes.get(playerId); }
        public int getRaidsCompleted() { return raidsCompleted; }
        public int getTotalScore() { return totalScore; }
        public long getBestTime() { return bestTime; }
        
        public void setStatistics(int raidsCompleted, int totalScore, long bestTime) {
            this.raidsCompleted = raidsCompleted;
            this.totalScore = totalScore;
            this.bestTime = bestTime;
        }
    }
    
    /**
     * Guild roles
     */
    public enum GuildRole {
        MEMBER("Member", false, false),
        OFFICER("Officer", true, false),
        LEADER("Leader", true, true);
        
        private final String displayName;
        private final boolean canInvite;
        private final boolean canKick;
        
        GuildRole(String displayName, boolean canInvite, boolean canKick) {
            this.displayName = displayName;
            this.canInvite = canInvite;
            this.canKick = canKick;
        }
        
        public String getDisplayName() { return displayName; }
        public boolean canInvite() { return canInvite; }
        public boolean canKick() { return canKick; }
    }
}