package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.CouncilRole;
import com.example.minecraftplugin.council.CouncilProposal;
import com.example.minecraftplugin.council.ProposalExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the Divine Council system for converged players
 */
public class DivineCouncilManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final ConvergenceManager convergenceManager;
    private final ProposalExecutor proposalExecutor;
    
    // Council membership and roles
    private final Map<UUID, CouncilRole> councilMembers;
    private final Map<String, CouncilProposal> activeProposals;
    private final Map<String, CouncilProposal> proposalHistory;
    
    // Council settings
    private int maxCouncilSize;
    private double approvalThreshold;
    private int proposalDurationHours;
    
    public DivineCouncilManager(MinecraftPlugin plugin, ConvergenceManager convergenceManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.convergenceManager = convergenceManager;
        this.proposalExecutor = new ProposalExecutor(plugin);
        this.councilMembers = new HashMap<>();
        this.activeProposals = new HashMap<>();
        this.proposalHistory = new HashMap<>();
        
        loadCouncilSettings();
        startProposalMonitoringTask();
        
        logger.info("Divine Council Manager initialized");
    }
    
    /**
     * Load council settings from configuration
     */
    private void loadCouncilSettings() {
        maxCouncilSize = plugin.getConfig().getInt("divine_council.max_size", 12);
        approvalThreshold = plugin.getConfig().getDouble("divine_council.approval_threshold", 0.6);
        proposalDurationHours = plugin.getConfig().getInt("divine_council.proposal_duration_hours", 72);
    }
    
    /**
     * Check if a player can join the Divine Council
     */
    public boolean canJoinCouncil(Player player) {
        // Must have achieved convergence
        if (!convergenceManager.hasAchievedConvergence(player)) {
            return false;
        }
        
        // Must not already be a member
        if (isCouncilMember(player)) {
            return false;
        }
        
        // Council must not be full
        return councilMembers.size() < maxCouncilSize;
    }
    
    /**
     * Add a player to the Divine Council
     */
    public boolean joinCouncil(Player player) {
        if (!canJoinCouncil(player)) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Determine initial role
        CouncilRole initialRole = determineInitialRole();
        councilMembers.put(playerId, initialRole);
        
        // Notify player
        player.sendMessage("§d§l★ WELCOME TO THE DIVINE COUNCIL! ★");
        player.sendMessage("§7You have joined the Divine Council as a " + initialRole.getDisplayName());
        player.sendMessage("§7The council governs cosmic affairs and makes decisions");
        player.sendMessage("§7that affect the entire server and beyond.");
        
        // Announce to server
        Bukkit.broadcastMessage("§d§l" + player.getName() + " §r§dhas joined the Divine Council as " + 
                               initialRole.getDisplayName() + "!");
        
        // Notify other council members
        notifyCouncilMembers("§d§lCouncil Update: §r§d" + player.getName() + 
                           " has joined as " + initialRole.getDisplayName());
        
        logger.info(player.getName() + " joined the Divine Council as " + initialRole.getDisplayName());
        return true;
    }
    
    /**
     * Remove a player from the Divine Council
     */
    public boolean leaveCouncil(Player player) {
        UUID playerId = player.getUniqueId();
        CouncilRole role = councilMembers.remove(playerId);
        
        if (role == null) {
            return false; // Player wasn't in council
        }
        
        // Notify player
        player.sendMessage("§c§lYou have left the Divine Council.");
        player.sendMessage("§7Your cosmic responsibilities have been relinquished.");
        
        // Announce to server
        Bukkit.broadcastMessage("§c§l" + player.getName() + " §r§chas left the Divine Council.");
        
        // Notify other council members
        notifyCouncilMembers("§c§lCouncil Update: §r§c" + player.getName() + " has left the council.");
        
        // Handle role redistribution if necessary
        redistributeRoles();
        
        logger.info(player.getName() + " left the Divine Council");
        return true;
    }
    
    /**
     * Check if a player is a council member
     */
    public boolean isCouncilMember(Player player) {
        return isCouncilMember(player.getUniqueId());
    }
    
    public boolean isCouncilMember(UUID playerId) {
        return councilMembers.containsKey(playerId);
    }
    
    /**
     * Get a player's council role
     */
    public CouncilRole getCouncilRole(Player player) {
        return getCouncilRole(player.getUniqueId());
    }
    
    public CouncilRole getCouncilRole(UUID playerId) {
        return councilMembers.get(playerId);
    }
    
    /**
     * Get all council members
     */
    public Map<UUID, CouncilRole> getCouncilMembers() {
        return new HashMap<>(councilMembers);
    }
    
    /**
     * Get all active proposals
     */
    public Map<String, CouncilProposal> getActiveProposals() {
        return new HashMap<>(activeProposals);
    }
    
    /**
     * Get proposal by ID
     */
    public CouncilProposal getProposal(String proposalId) {
        CouncilProposal proposal = activeProposals.get(proposalId);
        if (proposal == null) {
            proposal = proposalHistory.get(proposalId);
        }
        return proposal;
    }
    
    /**
     * Get proposals that a player can vote on
     */
    public List<CouncilProposal> getVotableProposals(Player player) {
        List<CouncilProposal> votable = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        
        for (CouncilProposal proposal : activeProposals.values()) {
            if (proposal.getStatus() == CouncilProposal.ProposalStatus.ACTIVE && 
                !proposal.hasExpired() && 
                !proposal.hasPlayerVoted(playerId)) {
                votable.add(proposal);
            }
        }
        
        return votable;
    }
    
    /**
     * Get proposals by status
     */
    public List<CouncilProposal> getProposalsByStatus(CouncilProposal.ProposalStatus status) {
        List<CouncilProposal> filtered = new ArrayList<>();
        
        // Check active proposals
        for (CouncilProposal proposal : activeProposals.values()) {
            if (proposal.getStatus() == status) {
                filtered.add(proposal);
            }
        }
        
        // Check historical proposals
        for (CouncilProposal proposal : proposalHistory.values()) {
            if (proposal.getStatus() == status) {
                filtered.add(proposal);
            }
        }
        
        return filtered;
    }
    
    /**
     * Create a new proposal
     */
    public boolean createProposal(Player proposer, String title, String description, 
                                CouncilProposal.ProposalType type) {
        UUID proposerId = proposer.getUniqueId();
        CouncilRole role = getCouncilRole(proposerId);
        
        // Check if player can propose
        if (role == null || !role.canPerformAction(CouncilRole.CouncilAction.PROPOSE)) {
            return false;
        }
        
        // Generate unique proposal ID
        String proposalId = "proposal_" + System.currentTimeMillis();
        
        // Create proposal
        CouncilProposal proposal = new CouncilProposal(proposalId, proposerId, title, description, 
                                                      type, proposalDurationHours);
        activeProposals.put(proposalId, proposal);
        
        // Notify council
        notifyCouncilMembers("§e§lNew Proposal: §r§e" + title);
        notifyCouncilMembers("§7Proposed by: §f" + proposer.getName());
        notifyCouncilMembers("§7Type: §f" + type.getDisplayName());
        notifyCouncilMembers("§7ID: §f" + proposalId);
        notifyCouncilMembers("§7Use §f/council info " + proposalId + "§7 for details");
        notifyCouncilMembers("§7Use §f/council vote " + proposalId + " <approve/reject/abstain>§7 to vote");
        
        logger.info(proposer.getName() + " created proposal: " + title);
        return true;
    }
    
    /**
     * Vote on a proposal
     */
    public boolean voteOnProposal(Player voter, String proposalId, CouncilProposal.Vote vote) {
        UUID voterId = voter.getUniqueId();
        
        // Check if player is council member
        if (!isCouncilMember(voterId)) {
            return false;
        }
        
        // Get proposal
        CouncilProposal proposal = activeProposals.get(proposalId);
        if (proposal == null) {
            return false;
        }
        
        // Check if proposal is still active
        if (proposal.hasExpired() || proposal.getStatus() != CouncilProposal.ProposalStatus.ACTIVE) {
            return false;
        }
        
        // Add vote
        proposal.addVote(voterId, vote);
        
        // Notify other council members of the vote (without revealing the vote)
        for (UUID memberId : councilMembers.keySet()) {
            if (!memberId.equals(voterId)) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("§d§lCouncil Update: §r§d" + voter.getName() + " has voted on \"" + proposal.getTitle() + "\"");
                }
            }
        }
        
        // Check if all members have voted
        if (proposal.getVotes().size() >= councilMembers.size()) {
            processProposalCompletion(proposal);
        }
        
        return true;
    }
    
    /**
     * Process proposal completion
     */
    private void processProposalCompletion(CouncilProposal proposal) {
        boolean passed = proposal.passes(councilMembers.size(), approvalThreshold);
        
        if (passed) {
            proposal.setStatus(CouncilProposal.ProposalStatus.PASSED);
            notifyCouncilMembers("§a§lProposal PASSED: §r§a" + proposal.getTitle());
            executeProposal(proposal);
            notifyCouncilMembers("§7ID: §f" + proposal.getId());
        } else {
            proposal.setStatus(CouncilProposal.ProposalStatus.REJECTED);
            notifyCouncilMembers("§c§lProposal REJECTED: §r§c" + proposal.getTitle());
        }
            notifyCouncilMembers("§7ID: §f" + proposal.getId());
        
        // Move to history
        activeProposals.remove(proposal.getId());
        proposalHistory.put(proposal.getId(), proposal);
        
        // Show vote breakdown
        CouncilProposal.VoteCount count = proposal.getVoteCount();
        notifyCouncilMembers("§7Final Vote Count:");
        notifyCouncilMembers("§a  ✓ Approve: " + count.getApprove());
        notifyCouncilMembers("§c  ✗ Reject: " + count.getReject());
        notifyCouncilMembers("§7  ○ Abstain: " + count.getAbstain());
        notifyCouncilMembers("§7  Total: " + count.getTotal() + "/" + councilMembers.size());
        
        double approvalRate = (double) count.getApprove() / councilMembers.size();
        notifyCouncilMembers("§7Approval Rate: §f" + String.format("%.1f%%", approvalRate * 100) + 
                           " §7(Required: §f" + String.format("%.1f%%", approvalThreshold * 100) + "§7)");
        notifyCouncilMembers("§d§l========================");
    }
    
    /**
     * Execute a passed proposal
     */
    private void executeProposal(CouncilProposal proposal) {
        switch (proposal.getType()) {
            case SERVER_EVENT:
                proposalExecutor.executeServerEvent(proposal);
                break;
            case RULE_CHANGE:
                proposalExecutor.executeRuleChange(proposal);
                break;
            case MEMBER_ACTION:
                proposalExecutor.executeMemberAction(proposal);
                break;
            case COSMIC_INTERVENTION:
                proposalExecutor.executeCosmicIntervention(proposal);
                break;
        }
        
        proposal.setStatus(CouncilProposal.ProposalStatus.EXECUTED);
        
        // Notify council of execution
        notifyCouncilMembers("§a§lProposal Executed: §r§a" + proposal.getTitle());
        notifyCouncilMembers("§7The effects of this proposal are now active!");
    }
    
    /**
     * Manually execute a proposal (admin command)
     */
    public void executeProposalManually(CouncilProposal proposal) {
        executeProposal(proposal);
    }
    
    /**
     * Determine initial role for new council member
     */
    private CouncilRole determineInitialRole() {
        // First member becomes Supreme
        if (councilMembers.isEmpty()) {
            return CouncilRole.SUPREME;
        }
        
        // Check if we need more leadership roles
        long elderCount = councilMembers.values().stream()
            .filter(role -> role.ordinal() >= CouncilRole.ELDER.ordinal())
            .count();
        
        // Promote to Elder if we have few leaders
        if (elderCount < councilMembers.size() / 3) {
            return CouncilRole.ELDER;
        }
        
        return CouncilRole.MEMBER;
    }
    
    /**
     * Redistribute roles when members leave
     */
    private void redistributeRoles() {
        // Ensure we have at least one Supreme
        boolean hasSupreme = councilMembers.values().contains(CouncilRole.SUPREME);
        
        if (!hasSupreme && !councilMembers.isEmpty()) {
            // Promote highest ranking member to Supreme
            UUID newSupreme = councilMembers.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (newSupreme != null) {
                councilMembers.put(newSupreme, CouncilRole.SUPREME);
                Player player = Bukkit.getPlayer(newSupreme);
                if (player != null) {
                    player.sendMessage("§d§lYou have been promoted to Supreme Deity of the Divine Council!");
                }
                notifyCouncilMembers("§d§l" + (player != null ? player.getName() : "A member") + 
                                   " has been promoted to Supreme Deity!");
            }
        }
    }
    
    /**
     * Notify all council members
     */
    private void notifyCouncilMembers(String message) {
        for (UUID memberId : councilMembers.keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
    
    /**
     * Start task to monitor proposal expiry
     */
    private void startProposalMonitoringTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkExpiredProposals();
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Check every minute
    }
    
    /**
     * Check for expired proposals
     */
    private void checkExpiredProposals() {
        Iterator<Map.Entry<String, CouncilProposal>> iterator = activeProposals.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, CouncilProposal> entry = iterator.next();
            CouncilProposal proposal = entry.getValue();
            
            if (proposal.hasExpired()) {
                proposal.setStatus(CouncilProposal.ProposalStatus.EXPIRED);
                notifyCouncilMembers("§8§l=== PROPOSAL EXPIRED ===");
                notifyCouncilMembers("§8§lTitle: §r§8" + proposal.getTitle());
                notifyCouncilMembers("§7ID: §f" + proposal.getId());
                notifyCouncilMembers("§7This proposal expired without reaching a decision.");
                
                // Show final vote count
                CouncilProposal.VoteCount count = proposal.getVoteCount();
                if (count.getTotal() > 0) {
                    notifyCouncilMembers("§7Final votes: " + proposal.getVoteSummary() + 
                                       " §7(§f" + count.getTotal() + "/" + councilMembers.size() + "§7)");
                }
                notifyCouncilMembers("§8§l====================");
                
                // Move to history
                iterator.remove();
                proposalHistory.put(proposal.getId(), proposal);
            }
        }
    }
    
    /**
     * Get council statistics
     */
    public Map<String, Object> getCouncilStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total_members", councilMembers.size());
        stats.put("max_size", maxCouncilSize);
        stats.put("active_proposals", activeProposals.size());
        stats.put("proposal_history", proposalHistory.size());
        
        // Role distribution
        Map<CouncilRole, Integer> roleDistribution = new HashMap<>();
        for (CouncilRole role : councilMembers.values()) {
            roleDistribution.merge(role, 1, Integer::sum);
        }
        stats.put("role_distribution", roleDistribution);
        
        return stats;
    }
    
    /**
     * Get council status for display
     */
    public String getCouncilStatus() {
        // Check if council is enabled
        if (!plugin.getConfig().getBoolean("divine_council.enabled", false)) {
            return "§c§lDivine Council is disabled in configuration.";
        }
        
        StringBuilder status = new StringBuilder();
        status.append("§d§l=== DIVINE COUNCIL STATUS ===§r\n");
        status.append("§7Members: §f").append(councilMembers.size()).append("/").append(maxCouncilSize).append("\n");
        status.append("§7Active Proposals: §f").append(activeProposals.size()).append("\n");
        status.append("§7Approval Threshold: §f").append((int)(approvalThreshold * 100)).append("%\n");
        
        // Show enabled powers
        status.append("§7Enabled Powers: ");
        List<String> enabledPowers = new ArrayList<>();
        if (plugin.getConfig().getBoolean("divine_council.powers.server_events", true)) enabledPowers.add("Events");
        if (plugin.getConfig().getBoolean("divine_council.powers.reality_manipulation", true)) enabledPowers.add("Reality");
        if (plugin.getConfig().getBoolean("divine_council.powers.cosmic_intervention", true)) enabledPowers.add("Cosmic");
        if (plugin.getConfig().getBoolean("divine_council.powers.rule_enforcement", true)) enabledPowers.add("Rules");
        status.append("§f").append(enabledPowers.isEmpty() ? "None" : String.join(", ", enabledPowers)).append("\n");
        
        status.append("\n");
        
        if (councilMembers.isEmpty()) {
            status.append("§7The Divine Council awaits its first members...\n");
            status.append("§7Requirement: §fAchieve Divine Convergence (12 testaments)\n");
        } else {
            status.append("§d§lCouncil Members:§r\n");
            for (Map.Entry<UUID, CouncilRole> entry : councilMembers.entrySet()) {
                Player member = Bukkit.getPlayer(entry.getKey());
                String memberName = member != null ? member.getName() : "Unknown";
                String onlineStatus = (member != null && member.isOnline()) ? "§a●" : "§7●";
                status.append("§7• ").append(entry.getValue().getDisplayName()).append(": ").append(onlineStatus).append(" §f").append(memberName).append("\n");
            }
        }
        
        // Show integration status
        if (plugin.getConfig().getBoolean("divine_council.integration.raids.enabled", true)) {
            status.append("\n§e§lIntegrations: §r§eRaids");
            if (plugin.getConfig().getBoolean("divine_council.integration.events.enabled", true)) status.append(", Events");
            if (plugin.getConfig().getBoolean("divine_council.integration.economy.enabled", true)) status.append(", Economy");
        }
        
        return status.toString();
    }
}