package com.example.minecraftplugin.council;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a proposal within the Divine Council
 */
public class CouncilProposal {
    
    private final String id;
    private final UUID proposerId;
    private final String title;
    private final String description;
    private final ProposalType type;
    private final long createdTime;
    private final long expiryTime;
    
    private final Map<UUID, Vote> votes;
    private ProposalStatus status;
    
    public CouncilProposal(String id, UUID proposerId, String title, String description, 
                          ProposalType type, long durationHours) {
        this.id = id;
        this.proposerId = proposerId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.createdTime = System.currentTimeMillis();
        this.expiryTime = createdTime + (durationHours * 60 * 60 * 1000L);
        this.votes = new HashMap<>();
        this.status = ProposalStatus.ACTIVE;
    }
    
    /**
     * Add a vote to this proposal
     */
    public void addVote(UUID voterId, Vote vote) {
        if (status != ProposalStatus.ACTIVE) {
            return;
        }
        
        votes.put(voterId, vote);
    }
    
    /**
     * Check if proposal has expired
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
    
    /**
     * Get vote counts
     */
    public VoteCount getVoteCount() {
        int approve = 0;
        int reject = 0;
        int abstain = 0;
        
        for (Vote vote : votes.values()) {
            switch (vote) {
                case APPROVE:
                    approve++;
                    break;
                case REJECT:
                    reject++;
                    break;
                case ABSTAIN:
                    abstain++;
                    break;
            }
        }
        
        return new VoteCount(approve, reject, abstain);
    }
    
    /**
     * Check if proposal passes based on vote threshold
     */
    public boolean passes(int totalMembers, double approvalThreshold) {
        VoteCount count = getVoteCount();
        double approvalRate = (double) count.getApprove() / totalMembers;
        return approvalRate >= approvalThreshold;
    }
    
    // Getters
    public String getId() { return id; }
    public UUID getProposerId() { return proposerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProposalType getType() { return type; }
    public long getCreatedTime() { return createdTime; }
    public long getExpiryTime() { return expiryTime; }
    public Map<UUID, Vote> getVotes() { return new HashMap<>(votes); }
    public ProposalStatus getStatus() { return status; }
    
    public void setStatus(ProposalStatus status) { this.status = status; }
    
    /**
     * Get formatted info for display
     */
    public String getFormattedInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("§d§l=== PROPOSAL DETAILS ===§r\n");
        info.append("§7ID: §f").append(id).append("\n");
        info.append("§7Title: §f").append(title).append("\n");
        info.append("§7Type: §f").append(type.getDisplayName()).append("\n");
        info.append("§7Status: ").append(getStatusColor()).append(status.name()).append("§r\n");
        info.append("\n");
        info.append("§e§lDescription:§r\n");
        info.append("§7").append(description).append("\n");
        info.append("\n");
        
        // Show proposer
        org.bukkit.entity.Player proposer = org.bukkit.Bukkit.getPlayer(proposerId);
        String proposerName = proposer != null ? proposer.getName() : "Unknown";
        info.append("§7Proposed by: §f").append(proposerName).append("\n");
        
        // Show timing
        long timeRemaining = expiryTime - System.currentTimeMillis();
        if (timeRemaining > 0) {
            long hoursRemaining = timeRemaining / (60 * 60 * 1000);
            info.append("§7Time Remaining: §f").append(hoursRemaining).append(" hours\n");
        } else {
            info.append("§7Status: §cExpired\n");
        }
        
        // Show vote progress
        VoteCount count = getVoteCount();
        info.append("\n");
        info.append("§e§lVote Progress:§r\n");
        info.append("§a✓ Approve: §f").append(count.getApprove()).append("\n");
        info.append("§c✗ Reject: §f").append(count.getReject()).append("\n");
        info.append("§7○ Abstain: §f").append(count.getAbstain()).append("\n");
        info.append("§7Total Votes: §f").append(count.getTotal()).append("\n");
        
        // Show execution examples for proposal types
        if (status == ProposalStatus.ACTIVE) {
            info.append("\n");
            info.append("§e§lExecution Examples:§r\n");
            switch (type) {
                case SERVER_EVENT:
                    info.append("§7• Fragment Rain - Increases fragment drops\n");
                    info.append("§7• Divine Blessing - Grants beneficial effects\n");
                    info.append("§7• Cosmic Storm - Reality distortion effects\n");
                    info.append("§7• Altar Manifestation - Spawns new altars\n");
                    break;
                case RULE_CHANGE:
                    info.append("§7• Fragment Rate changes\n");
                    info.append("§7• Death Threshold adjustments\n");
                    info.append("§7• Cooldown modifications\n");
                    info.append("§7• Ascension rule changes\n");
                    break;
                case MEMBER_ACTION:
                    info.append("§7• Member promotions/demotions\n");
                    info.append("§7• Council expulsions\n");
                    info.append("§7• Honor recognitions\n");
                    break;
                case COSMIC_INTERVENTION:
                    info.append("§7• Reality Reset - Major world changes\n");
                    info.append("§7• Divine Punishment - Justice enforcement\n");
                    info.append("§7• Cosmic Balance - Equilibrium restoration\n");
                    info.append("§7• Dimensional Rifts - Portal creation\n");
                    break;
            }
        }
        
        return info.toString();
    }
    
    /**
     * Get status color for display
     */
    private String getStatusColor() {
        switch (status) {
            case ACTIVE:
                return "§a";
            case PASSED:
                return "§2";
            case REJECTED:
                return "§c";
            case EXPIRED:
                return "§8";
            case EXECUTED:
                return "§6";
            default:
                return "§7";
        }
    }
    
    /**
     * Get formatted vote summary for listings
     */
    public String getVoteSummary() {
        VoteCount count = getVoteCount();
        return "§a" + count.getApprove() + " §c" + count.getReject() + " §7" + count.getAbstain();
    }
    
    /**
     * Check if a specific player has voted
     */
    public boolean hasPlayerVoted(UUID playerId) {
        return votes.containsKey(playerId);
    }
    
    /**
     * Get a player's vote
     */
    public Vote getPlayerVote(UUID playerId) {
        return votes.get(playerId);
    }
    
    /**
     * Proposal types
     */
    public enum ProposalType {
        SERVER_EVENT("Server Event", "Propose a server-wide event"),
        RULE_CHANGE("Rule Change", "Propose changes to server rules"),
        MEMBER_ACTION("Member Action", "Actions regarding council members"),
        COSMIC_INTERVENTION("Cosmic Intervention", "Divine intervention in server affairs");
        
        private final String displayName;
        private final String description;
        
        ProposalType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Proposal status
     */
    public enum ProposalStatus {
        ACTIVE,
        PASSED,
        REJECTED,
        EXPIRED,
        EXECUTED
    }
    
    /**
     * Vote options
     */
    public enum Vote {
        APPROVE("Approve", "§a✓"),
        REJECT("Reject", "§c✗"),
        ABSTAIN("Abstain", "§7○");
        
        private final String displayName;
        private final String symbol;
        
        Vote(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }
        
        public String getDisplayName() { return displayName; }
        public String getSymbol() { return symbol; }
    }
    
    /**
     * Vote count helper class
     */
    public static class VoteCount {
        private final int approve;
        private final int reject;
        private final int abstain;
        
        public VoteCount(int approve, int reject, int abstain) {
            this.approve = approve;
            this.reject = reject;
            this.abstain = abstain;
        }
        
        public int getApprove() { return approve; }
        public int getReject() { return reject; }
        public int getAbstain() { return abstain; }
        public int getTotal() { return approve + reject + abstain; }
    }
}