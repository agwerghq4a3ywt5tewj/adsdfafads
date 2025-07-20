package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.managers.DivineCouncilManager;
import com.example.minecraftplugin.enums.CouncilRole;
import com.example.minecraftplugin.council.CouncilProposal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class CouncilCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final DivineCouncilManager councilManager;
    
    public CouncilCommand(MinecraftPlugin plugin, DivineCouncilManager councilManager) {
        this.plugin = plugin;
        this.councilManager = councilManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.council")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showCouncilStatus(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                showCouncilStatus(player);
                break;
                
            case "join":
                handleJoinCouncil(player);
                break;
                
            case "leave":
                handleLeaveCouncil(player);
                break;
                
            case "members":
                showCouncilMembers(player);
                break;
                
            case "propose":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /council propose <type> <title> <description>");
                    return true;
                }
                handleCreateProposal(player, args);
                break;
                
            case "vote":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /council vote <proposal_id> <approve/reject/abstain>");
                    return true;
                }
                handleVoteOnProposal(player, args[1], args[2]);
                break;
                
            case "execute":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /council execute <proposal_id>");
                    return true;
                }
                if (player.hasPermission("minecraftplugin.admin")) {
                    handleExecuteProposal(player, args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to execute proposals manually!");
                }
                break;
                
            case "proposals":
                if (args.length == 1) {
                    showActiveProposals(player);
                } else {
                    handleProposalSubcommand(player, args);
                }
                break;
                
            case "info":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /council info <proposal_id>");
                    return true;
                }
                showProposalInfo(player, args[1]);
                break;
                
            case "history":
                showProposalHistory(player);
                break;
                
            case "help":
                showCouncilHelp(player);
                break;
                
            default:
                showCouncilHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("status", "join", "leave", "members", 
                "propose", "vote", "proposals", "info", "history", "help");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("execute");
            }
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("propose")) {
                // Proposal types
                for (CouncilProposal.ProposalType type : CouncilProposal.ProposalType.values()) {
                    String typeName = type.name().toLowerCase();
                    if (typeName.startsWith(args[1].toLowerCase())) {
                        completions.add(typeName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("vote") || args[0].equalsIgnoreCase("info") || 
                      args[0].equalsIgnoreCase("execute")) {
                // Proposal IDs (simplified - in practice you'd get actual proposal IDs)
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (councilManager.isCouncilMember(player) || sender.hasPermission("minecraftplugin.admin")) {
                        for (String proposalId : councilManager.getActiveProposals().keySet()) {
                            if (proposalId.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(proposalId);
                            }
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("proposals")) {
                // Proposal subcommands
                List<String> proposalSubs = Arrays.asList("active", "pending", "completed", "my");
                for (String sub : proposalSubs) {
                    if (sub.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("vote")) {
            // Vote options
            for (CouncilProposal.Vote vote : CouncilProposal.Vote.values()) {
                String voteName = vote.name().toLowerCase();
                if (voteName.startsWith(args[2].toLowerCase())) {
                    completions.add(voteName);
                }
            }
        }
        
        return completions;
    }
    
    private void showCouncilStatus(Player player) {
        String status = councilManager.getCouncilStatus();
        player.sendMessage(status);
        
        // Show player's role if they're a member
        if (councilManager.isCouncilMember(player)) {
            CouncilRole role = councilManager.getCouncilRole(player);
            player.sendMessage("§d§lYour Role: §r§d" + role.getDisplayName());
            player.sendMessage("§7" + role.getDescription());
        } else {
            // Show how to join
            if (councilManager.canJoinCouncil(player)) {
                player.sendMessage("§a§lYou can join the Divine Council! §r§aUse §f/council join");
            } else {
                player.sendMessage("§c§lRequirement: §r§cMust achieve Divine Convergence to join the council.");
            }
        }
    }
    
    private void handleJoinCouncil(Player player) {
        if (councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lAlready a member! §r§cYou are already part of the Divine Council.");
            return;
        }
        
        if (!councilManager.canJoinCouncil(player)) {
            player.sendMessage("§c§lCannot join council! §r§cRequirements:");
            player.sendMessage("§7• Must have achieved Divine Convergence");
            player.sendMessage("§7• Council must not be full");
            player.sendMessage("§7• Must not already be a member");
            return;
        }
        
        boolean success = councilManager.joinCouncil(player);
        if (!success) {
            player.sendMessage("§c§lFailed to join council! §r§cPlease try again later.");
        }
    }
    
    private void handleLeaveCouncil(Player player) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a member! §r§cYou are not part of the Divine Council.");
            return;
        }
        
        boolean success = councilManager.leaveCouncil(player);
        if (!success) {
            player.sendMessage("§c§lFailed to leave council! §r§cPlease try again later.");
        }
    }
    
    private void showCouncilMembers(Player player) {
        Map<UUID, CouncilRole> members = councilManager.getCouncilMembers();
        
        if (members.isEmpty()) {
            player.sendMessage("§7The Divine Council has no members yet.");
            return;
        }
        
        player.sendMessage("§d§l=== DIVINE COUNCIL MEMBERS ===§r");
        
        // Group by role
        for (CouncilRole role : CouncilRole.values()) {
            List<String> roleMembers = new ArrayList<>();
            
            for (Map.Entry<UUID, CouncilRole> entry : members.entrySet()) {
                if (entry.getValue() == role) {
                    Player member = plugin.getServer().getPlayer(entry.getKey());
                    String memberName = member != null ? member.getName() : "Unknown";
                    String status = member != null && member.isOnline() ? "§a●" : "§7●";
                    roleMembers.add(status + " §f" + memberName);
                }
            }
            
            if (!roleMembers.isEmpty()) {
                player.sendMessage("§d§l" + role.getDisplayName() + ":");
                for (String memberInfo : roleMembers) {
                    player.sendMessage("§7  " + memberInfo);
                }
            }
        }
        
        player.sendMessage("§7Legend: §a● §7Online, §7● §7Offline");
    }
    
    private void handleCreateProposal(Player player, String[] args) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can create proposals.");
            return;
        }
        
        CouncilRole role = councilManager.getCouncilRole(player);
        if (!role.canPerformAction(CouncilRole.CouncilAction.PROPOSE)) {
            player.sendMessage("§c§lInsufficient permissions! §r§cYour role (" + role.getDisplayName() + 
                             ") cannot create proposals.");
            player.sendMessage("§7Required role: §fCouncil Elder or higher");
            return;
        }
        
        // Parse proposal type
        CouncilProposal.ProposalType type;
        try {
            type = CouncilProposal.ProposalType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c§lInvalid proposal type! §r§cAvailable types:");
            for (CouncilProposal.ProposalType proposalType : CouncilProposal.ProposalType.values()) {
                player.sendMessage("§7• §f" + proposalType.name().toLowerCase() + " §7- " + proposalType.getDescription());
            }
            return;
        }
        
        // Combine remaining args for title and description
        String title = args[2];
        StringBuilder descBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            descBuilder.append(args[i]);
            if (i < args.length - 1) {
                descBuilder.append(" ");
            }
        }
        String description = descBuilder.toString();
        
        boolean success = councilManager.createProposal(player, title, description, type);
        if (success) {
            player.sendMessage("§a§lProposal created! §r§aYour proposal has been submitted to the Divine Council.");
            player.sendMessage("§7Council members can now vote on your proposal.");
        } else {
            player.sendMessage("§c§lFailed to create proposal! §r§cPlease try again later.");
        }
    }
    
    private void handleVoteOnProposal(Player player, String proposalId, String voteStr) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can vote.");
            return;
        }
        
        // Get proposal first to provide better error messages
        CouncilProposal proposal = councilManager.getProposal(proposalId);
        if (proposal == null) {
            player.sendMessage("§c§lProposal not found! §r§cUse §f/council proposals§c to see available proposals.");
            return;
        }
        
        // Check if proposal is still active
        if (proposal.getStatus() != CouncilProposal.ProposalStatus.ACTIVE) {
            player.sendMessage("§c§lProposal not active! §r§cThis proposal has status: " + proposal.getStatus().name());
            return;
        }
        
        // Check if proposal has expired
        if (proposal.hasExpired()) {
            player.sendMessage("§c§lProposal expired! §r§cThis proposal is no longer accepting votes.");
            return;
        }
        
        // Check if player has already voted
        if (proposal.hasPlayerVoted(player.getUniqueId())) {
            CouncilProposal.Vote existingVote = proposal.getPlayerVote(player.getUniqueId());
            player.sendMessage("§c§lAlready voted! §r§cYou have already voted " + existingVote.getDisplayName() + " on this proposal.");
            player.sendMessage("§7Proposal: §f" + proposal.getTitle());
            return;
        }
        
        // Parse vote
        CouncilProposal.Vote vote;
        try {
            vote = CouncilProposal.Vote.valueOf(voteStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c§lInvalid vote! §r§cUse: approve, reject, or abstain");
            player.sendMessage("§7Example: §f/council vote " + proposalId + " approve");
            return;
        }
        
        boolean success = councilManager.voteOnProposal(player, proposalId, vote);
        if (success) {
            // Show updated vote status
            player.sendMessage("§a§lVote recorded! §r§aYou voted " + vote.getDisplayName() + " on:");
            player.sendMessage("§7\"" + proposal.getTitle() + "\"");
            
            // Show current vote tally
            CouncilProposal.VoteCount count = proposal.getVoteCount();
            int totalMembers = councilManager.getCouncilMembers().size();
            player.sendMessage("§7Current votes: " + proposal.getVoteSummary() + " §7(§f" + count.getTotal() + "/" + totalMembers + "§7)");
            
            // Check if all members have voted
            if (count.getTotal() >= totalMembers) {
                player.sendMessage("§e§lAll members have voted! §r§eProposal will be processed shortly.");
            }
        } else {
            player.sendMessage("§c§lVote failed! §r§cAn unexpected error occurred. Please try again.");
        }
    }
    
    private void handleExecuteProposal(Player admin, String proposalId) {
        CouncilProposal proposal = councilManager.getProposal(proposalId);
        if (proposal == null) {
            admin.sendMessage("§c§lProposal not found! §r§cUse §f/council proposals§c to see available proposals.");
            return;
        }
        
        if (proposal.getStatus() != CouncilProposal.ProposalStatus.PASSED) {
            admin.sendMessage("§c§lCannot execute! §r§cProposal must be in PASSED status to execute.");
            admin.sendMessage("§7Current status: §f" + proposal.getStatus().name());
            return;
        }
        
        // Safety check for critical proposals
        if (isCriticalProposal(proposal) && !admin.hasPermission("minecraftplugin.admin.critical")) {
            admin.sendMessage("§c§lCritical Proposal: §r§cRequires special permission to execute manually!");
            return;
        }
        
        // Execute the proposal manually
        councilManager.executeProposalManually(proposal);
        
        admin.sendMessage("§a§lProposal executed manually!");
        admin.sendMessage("§7Proposal: §f" + proposal.getTitle());
        admin.sendMessage("§7Type: §f" + proposal.getType().getDisplayName());
        
        // Notify council members
        for (UUID memberId : councilManager.getCouncilMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§d§lAdmin Execution: §r§d" + admin.getName() + " manually executed proposal \"" + proposal.getTitle() + "\"");
            }
        }
    }
    
    /**
     * Handle emergency stop of proposal
     */
    private void handleEmergencyStop(Player admin, String proposalId) {
        CouncilProposal proposal = councilManager.getProposal(proposalId);
        if (proposal == null) {
            admin.sendMessage("§c§lProposal not found!");
            return;
        }
        
        if (proposal.getStatus() != CouncilProposal.ProposalStatus.PASSED && 
            proposal.getStatus() != CouncilProposal.ProposalStatus.ACTIVE) {
            admin.sendMessage("§c§lCannot emergency stop! §r§cProposal is not active or passed.");
            return;
        }
        
        // Emergency stop the proposal
        proposal.setStatus(CouncilProposal.ProposalStatus.REJECTED);
        
        admin.sendMessage("§c§l⚠ EMERGENCY STOP ACTIVATED! ⚠");
        admin.sendMessage("§7Proposal: §f" + proposal.getTitle());
        admin.sendMessage("§7Status changed to: §cREJECTED");
        
        // Notify all players
        plugin.getServer().broadcastMessage("§c§l⚠ EMERGENCY STOP ⚠");
        plugin.getServer().broadcastMessage("§7Administrator " + admin.getName() + " has emergency stopped a Divine Council proposal!");
        plugin.getServer().broadcastMessage("§7Proposal: §f" + proposal.getTitle());
        
        // Notify council members
        for (UUID memberId : councilManager.getCouncilMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c§lEmergency Stop: §r§cProposal \"" + proposal.getTitle() + "\" has been stopped by " + admin.getName());
            }
        }
    }
    
    /**
     * Handle rollback of council changes
     */
    private void handleRollback(Player admin, String[] args) {
        if (args.length < 2) {
            admin.sendMessage("§cUsage: /council rollback <type> [details]");
            admin.sendMessage("§7Types: storm, reality, terrain, all");
            return;
        }
        
        String rollbackType = args[1].toLowerCase();
        
        switch (rollbackType) {
            case "storm":
                admin.sendMessage("§e§lRolling back cosmic storm effects...");
                admin.sendMessage("§7This would restore terrain modified by cosmic storms.");
                // In a real implementation, this would restore saved block states
                break;
                
            case "reality":
                admin.sendMessage("§e§lRolling back reality manipulation...");
                admin.sendMessage("§7This would restore reality-altered areas.");
                break;
                
            case "terrain":
                admin.sendMessage("§e§lRolling back terrain modifications...");
                admin.sendMessage("§7This would restore all council-modified terrain.");
                break;
                
            case "all":
                admin.sendMessage("§c§l⚠ FULL ROLLBACK INITIATED ⚠");
                admin.sendMessage("§7Rolling back ALL Divine Council changes...");
                admin.sendMessage("§7This may take several minutes to complete.");
                break;
                
            default:
                admin.sendMessage("§cInvalid rollback type: " + rollbackType);
                admin.sendMessage("§7Available types: storm, reality, terrain, all");
                return;
        }
        
        // Broadcast rollback to server
        plugin.getServer().broadcastMessage("§e§lAdministrator " + admin.getName() + " is performing a Divine Council rollback!");
        plugin.getServer().broadcastMessage("§7Type: §f" + rollbackType);
        plugin.getServer().broadcastMessage("§7Recent council changes are being reversed...");
    }
    
    /**
     * Show council powers and their status
     */
    private void showCouncilPowers(Player player) {
        player.sendMessage("§d§l=== DIVINE COUNCIL POWERS ===§r");
        player.sendMessage("§7Current power configuration:");
        player.sendMessage("");
        
        showPowerStatus(player, "server_events", "Server Events", "Trigger server-wide events");
        showPowerStatus(player, "rule_enforcement", "Rule Enforcement", "Enforce and modify server rules");
        showPowerStatus(player, "cosmic_intervention", "Cosmic Intervention", "Perform reality-altering interventions");
        showPowerStatus(player, "reality_manipulation", "Reality Manipulation", "Alter world terrain and structures");
        showPowerStatus(player, "economic_control", "Economic Control", "Affect server economy and progression");
        showPowerStatus(player, "player_governance", "Player Governance", "Make decisions affecting all players");
        showPowerStatus(player, "dimensional_access", "Dimensional Access", "Access and modify other dimensions");
        
        player.sendMessage("");
        player.sendMessage("§7Powers can be configured in config.yml under 'divine_council.powers'");
    }
    
    /**
     * Show power status
     */
    private void showPowerStatus(Player player, String powerKey, String powerName, String description) {
        boolean enabled = plugin.getConfig().getBoolean("divine_council.powers." + powerKey, true);
        String status = enabled ? "§a✓ Enabled" : "§c✗ Disabled";
        player.sendMessage("§7• " + status + " §f" + powerName);
        player.sendMessage("§7  " + description);
    }
    
    /**
     * Show council integrations
     */
    private void showCouncilIntegrations(Player player) {
        player.sendMessage("§d§l=== COUNCIL INTEGRATIONS ===§r");
        player.sendMessage("§7Integration with other server systems:");
        player.sendMessage("");
        
        showIntegrationStatus(player, "raids", "Raid System", "Control raid difficulty and trigger special raids");
        showIntegrationStatus(player, "events", "Event System", "Create and manage server events");
        showIntegrationStatus(player, "economy", "Economy System", "Regulate fragment economy and progression");
        showIntegrationStatus(player, "governance", "Governance System", "Democratic rule creation and enforcement");
        
        player.sendMessage("");
        player.sendMessage("§7Integrations can be configured in config.yml under 'divine_council.integration'");
    }
    
    /**
     * Show integration status
     */
    private void showIntegrationStatus(Player player, String integrationKey, String integrationName, String description) {
        boolean enabled = plugin.getConfig().getBoolean("divine_council.integration." + integrationKey + ".enabled", true);
        String status = enabled ? "§a✓ Active" : "§c✗ Inactive";
        player.sendMessage("§7• " + status + " §f" + integrationName);
        player.sendMessage("§7  " + description);
    }
    
    /**
     * Check if a proposal is critical
     */
    private boolean isCriticalProposal(CouncilProposal proposal) {
        String combined = (proposal.getTitle() + " " + proposal.getDescription()).toLowerCase();
        return combined.contains("reality reset") || 
               combined.contains("world reshape") || 
               combined.contains("terrain modification") ||
               combined.contains("divine punishment") ||
               combined.contains("permanent") ||
               combined.contains("irreversible");
    }
    private void showActiveProposals(Player player) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can view proposals.");
            return;
        }
        
        Map<String, CouncilProposal> activeProposals = councilManager.getActiveProposals();
        
        if (activeProposals.isEmpty()) {
            player.sendMessage("§e§lNo active proposals at this time.");
            player.sendMessage("§7Use §f/council propose§7 to create a new proposal.");
            return;
        }
        
        player.sendMessage("§d§l=== ACTIVE PROPOSALS ===§r");
        player.sendMessage("§7Total: §f" + activeProposals.size() + " §7active proposals");
        player.sendMessage("");
        
        for (CouncilProposal proposal : activeProposals.values()) {
            showProposalSummary(player, proposal);
        }
        
        player.sendMessage("§7Use §f/council info <proposal_id>§7 for detailed information");
        player.sendMessage("§7Use §f/council vote <proposal_id> <approve/reject/abstain>§7 to vote");
    }
    
    /**
     * Handle proposal subcommands
     */
    private void handleProposalSubcommand(Player player, String[] args) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can view proposals.");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "active":
                showActiveProposals(player);
                break;
                
            case "pending":
                showPendingVotes(player);
                break;
                
            case "completed":
                showCompletedProposals(player);
                break;
                
            case "my":
                showMyProposals(player);
                break;
                
            default:
                player.sendMessage("§cUsage: /council proposals [active|pending|completed|my]");
                break;
        }
    }
    
    /**
     * Show proposals pending the player's vote
     */
    private void showPendingVotes(Player player) {
        List<CouncilProposal> pendingVotes = councilManager.getVotableProposals(player);
        
        if (pendingVotes.isEmpty()) {
            player.sendMessage("§a§lNo pending votes! §r§aYou have voted on all active proposals.");
            return;
        }
        
        player.sendMessage("§e§l=== PENDING VOTES ===§r");
        player.sendMessage("§7Proposals awaiting your vote: §f" + pendingVotes.size());
        player.sendMessage("");
        
        for (CouncilProposal proposal : pendingVotes) {
            showProposalSummary(player, proposal);
            player.sendMessage("§c§l⚠ YOUR VOTE NEEDED! §r§cUse §f/council vote " + proposal.getId() + " <approve/reject/abstain>");
            player.sendMessage("");
        }
    }
    
    /**
     * Show completed proposals
     */
    private void showCompletedProposals(Player player) {
        List<CouncilProposal> completed = new ArrayList<>();
        completed.addAll(councilManager.getProposalsByStatus(CouncilProposal.ProposalStatus.PASSED));
        completed.addAll(councilManager.getProposalsByStatus(CouncilProposal.ProposalStatus.REJECTED));
        completed.addAll(councilManager.getProposalsByStatus(CouncilProposal.ProposalStatus.EXECUTED));
        completed.addAll(councilManager.getProposalsByStatus(CouncilProposal.ProposalStatus.EXPIRED));
        
        if (completed.isEmpty()) {
            player.sendMessage("§7No completed proposals yet.");
            return;
        }
        
        // Sort by creation time (newest first)
        completed.sort((p1, p2) -> Long.compare(p2.getCreatedTime(), p1.getCreatedTime()));
        
        player.sendMessage("§6§l=== COMPLETED PROPOSALS ===§r");
        player.sendMessage("§7Recent completed proposals (last 10):");
        player.sendMessage("");
        
        int count = 0;
        for (CouncilProposal proposal : completed) {
            if (count >= 10) break;
            
            showProposalSummary(player, proposal);
            count++;
        }
        
        if (completed.size() > 10) {
            player.sendMessage("§7... and " + (completed.size() - 10) + " more");
        }
    }
    
    /**
     * Show proposals created by the player
     */
    private void showMyProposals(Player player) {
        UUID playerId = player.getUniqueId();
        List<CouncilProposal> myProposals = new ArrayList<>();
        
        // Check active proposals
        for (CouncilProposal proposal : councilManager.getActiveProposals().values()) {
            if (proposal.getProposerId().equals(playerId)) {
                myProposals.add(proposal);
            }
        }
        
        // Check historical proposals
        for (CouncilProposal.ProposalStatus status : CouncilProposal.ProposalStatus.values()) {
            for (CouncilProposal proposal : councilManager.getProposalsByStatus(status)) {
                if (proposal.getProposerId().equals(playerId)) {
                    myProposals.add(proposal);
                }
            }
        }
        
        if (myProposals.isEmpty()) {
            player.sendMessage("§7You have not created any proposals yet.");
            player.sendMessage("§7Use §f/council propose§7 to create your first proposal.");
            return;
        }
        
        // Sort by creation time (newest first)
        myProposals.sort((p1, p2) -> Long.compare(p2.getCreatedTime(), p1.getCreatedTime()));
        
        player.sendMessage("§b§l=== MY PROPOSALS ===§r");
        player.sendMessage("§7Your proposals: §f" + myProposals.size() + " §7total");
        player.sendMessage("");
        
        for (CouncilProposal proposal : myProposals) {
            showProposalSummary(player, proposal);
        }
    }
    
    /**
     * Show detailed information about a specific proposal
     */
    private void showProposalInfo(Player player, String proposalId) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can view proposal details.");
            return;
        }
        
        CouncilProposal proposal = councilManager.getProposal(proposalId);
        if (proposal == null) {
            player.sendMessage("§c§lProposal not found! §r§cUse §f/council proposals§c to see available proposals.");
            return;
        }
        
        // Show detailed proposal information
        player.sendMessage(proposal.getFormattedInfo());
        
        // Show voting details
        showVotingDetails(player, proposal);
        
        // Show action buttons if applicable
        if (proposal.getStatus() == CouncilProposal.ProposalStatus.ACTIVE && 
            !proposal.hasExpired() && 
            !proposal.hasPlayerVoted(player.getUniqueId())) {
            player.sendMessage("");
            player.sendMessage("§e§l=== VOTING OPTIONS ===§r");
            player.sendMessage("§a§l/council vote " + proposalId + " approve §r§a- Support this proposal");
            player.sendMessage("§c§l/council vote " + proposalId + " reject §r§c- Oppose this proposal");
            player.sendMessage("§7§l/council vote " + proposalId + " abstain §r§7- Abstain from voting");
        }
    }
    
    /**
     * Show voting details for a proposal
     */
    private void showVotingDetails(Player player, CouncilProposal proposal) {
        Map<UUID, CouncilProposal.Vote> votes = proposal.getVotes();
        Map<UUID, CouncilRole> members = councilManager.getCouncilMembers();
        
        player.sendMessage("");
        player.sendMessage("§e§l=== VOTING DETAILS ===§r");
        
        if (votes.isEmpty()) {
            player.sendMessage("§7No votes cast yet.");
            return;
        }
        
        // Group votes by type
        Map<CouncilProposal.Vote, List<String>> voteGroups = new HashMap<>();
        for (CouncilProposal.Vote voteType : CouncilProposal.Vote.values()) {
            voteGroups.put(voteType, new ArrayList<>());
        }
        
        for (Map.Entry<UUID, CouncilProposal.Vote> entry : votes.entrySet()) {
            Player voter = plugin.getServer().getPlayer(entry.getKey());
            String voterName = voter != null ? voter.getName() : "Unknown";
            CouncilRole voterRole = members.get(entry.getKey());
            String roleDisplay = voterRole != null ? " (" + voterRole.getDisplayName() + ")" : "";
            
            voteGroups.get(entry.getValue()).add(voterName + roleDisplay);
        }
        
        // Display votes by type
        for (CouncilProposal.Vote voteType : CouncilProposal.Vote.values()) {
            List<String> voters = voteGroups.get(voteType);
            if (!voters.isEmpty()) {
                player.sendMessage(voteType.getSymbol() + " §l" + voteType.getDisplayName() + " (" + voters.size() + "):§r");
                for (String voter : voters) {
                    player.sendMessage("§7  • " + voter);
                }
            }
        }
        
        // Show who hasn't voted yet
        List<String> notVoted = new ArrayList<>();
        for (Map.Entry<UUID, CouncilRole> entry : members.entrySet()) {
            if (!votes.containsKey(entry.getKey())) {
                Player member = plugin.getServer().getPlayer(entry.getKey());
                String memberName = member != null ? member.getName() : "Unknown";
                notVoted.add(memberName + " (" + entry.getValue().getDisplayName() + ")");
            }
        }
        
        if (!notVoted.isEmpty()) {
            player.sendMessage("§7§lPending Votes (" + notVoted.size() + "):§r");
            for (String member : notVoted) {
                player.sendMessage("§7  • " + member);
            }
        }
    }
    
    /**
     * Show proposal history
     */
    private void showProposalHistory(Player player) {
        if (!councilManager.isCouncilMember(player)) {
            player.sendMessage("§c§lNot a council member! §r§cOnly Divine Council members can view proposal history.");
            return;
        }
        
        List<CouncilProposal> allProposals = new ArrayList<>();
        allProposals.addAll(councilManager.getActiveProposals().values());
        
        for (CouncilProposal.ProposalStatus status : CouncilProposal.ProposalStatus.values()) {
            allProposals.addAll(councilManager.getProposalsByStatus(status));
        }
        
        if (allProposals.isEmpty()) {
            player.sendMessage("§7No proposals in council history yet.");
            return;
        }
        
        // Sort by creation time (newest first)
        allProposals.sort((p1, p2) -> Long.compare(p2.getCreatedTime(), p1.getCreatedTime()));
        
        player.sendMessage("§6§l=== COUNCIL PROPOSAL HISTORY ===§r");
        player.sendMessage("§7Total proposals: §f" + allProposals.size());
        player.sendMessage("");
        
        // Show statistics
        Map<CouncilProposal.ProposalStatus, Integer> statusCounts = new HashMap<>();
        for (CouncilProposal proposal : allProposals) {
            statusCounts.merge(proposal.getStatus(), 1, Integer::sum);
        }
        
        player.sendMessage("§e§lStatistics:§r");
        for (Map.Entry<CouncilProposal.ProposalStatus, Integer> entry : statusCounts.entrySet()) {
            String statusColor = getStatusColor(entry.getKey());
            player.sendMessage("§7• " + statusColor + entry.getKey().name() + "§7: §f" + entry.getValue());
        }
        
        player.sendMessage("");
        player.sendMessage("§7Recent proposals (last 5):");
        
        int count = 0;
        for (CouncilProposal proposal : allProposals) {
            if (count >= 5) break;
            showProposalSummary(player, proposal);
            count++;
        }
        
        player.sendMessage("§7Use §f/council info <proposal_id>§7 for detailed information");
    }
    
    /**
     * Show a summary of a proposal
     */
    private void showProposalSummary(Player player, CouncilProposal proposal) {
        String statusColor = getStatusColor(proposal.getStatus());
        String typeColor = getTypeColor(proposal.getType());
        
        player.sendMessage(statusColor + "§l" + proposal.getTitle() + "§r");
        player.sendMessage("§7• ID: §f" + proposal.getId());
        player.sendMessage("§7• Type: " + typeColor + proposal.getType().getDisplayName());
        player.sendMessage("§7• Status: " + statusColor + proposal.getStatus().name());
        player.sendMessage("§7• Votes: " + proposal.getVoteSummary());
        
        // Show if player has voted
        if (proposal.hasPlayerVoted(player.getUniqueId())) {
            CouncilProposal.Vote playerVote = proposal.getPlayerVote(player.getUniqueId());
            player.sendMessage("§7• Your Vote: " + playerVote.getSymbol() + " " + playerVote.getDisplayName());
        } else if (proposal.getStatus() == CouncilProposal.ProposalStatus.ACTIVE && !proposal.hasExpired()) {
            player.sendMessage("§e§l• VOTE NEEDED!");
        }
        
        player.sendMessage("");
    }
    
    /**
     * Get color for proposal status
     */
    private String getStatusColor(CouncilProposal.ProposalStatus status) {
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
     * Get color for proposal type
     */
    private String getTypeColor(CouncilProposal.ProposalType type) {
        switch (type) {
            case SERVER_EVENT:
                return "§b";
            case RULE_CHANGE:
                return "§c";
            case MEMBER_ACTION:
                return "§d";
            case COSMIC_INTERVENTION:
                return "§5";
            default:
                return "§7";
        }
    }
    
    private void showCouncilHelp(Player player) {
        player.sendMessage("§d§l=== DIVINE COUNCIL COMMANDS ===§r");
        player.sendMessage("§7/council status - Show council status and your role");
        player.sendMessage("§7/council join - Join the Divine Council (requires convergence)");
        player.sendMessage("§7/council leave - Leave the Divine Council");
        player.sendMessage("§7/council members - List all council members");
        player.sendMessage("§7/council powers - Show council powers and their status");
        player.sendMessage("§7/council integrations - Show system integrations");
        
        if (councilManager.isCouncilMember(player)) {
            CouncilRole role = councilManager.getCouncilRole(player);
            player.sendMessage("");
            player.sendMessage("§d§lMember Commands:");
            player.sendMessage("§7/council proposals [active|pending|completed|my] - View proposals");
            player.sendMessage("§7/council info <proposal_id> - View detailed proposal information");
            player.sendMessage("§7/council vote <proposal_id> <approve/reject/abstain> - Vote on proposal");
            player.sendMessage("§7/council history - View council proposal history");
            
            if (player.hasPermission("minecraftplugin.admin")) {
                player.sendMessage("§c§lAdmin Commands:");
                player.sendMessage("§7/council execute <proposal_id> - Manually execute a passed proposal");
                player.sendMessage("§7/council emergency-stop <proposal_id> - Emergency stop a proposal");
                player.sendMessage("§7/council rollback <type> - Rollback council changes");
            }
            
            if (role.canPerformAction(CouncilRole.CouncilAction.PROPOSE)) {
                player.sendMessage("§7/council propose <type> <title> <description> - Create proposal");
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lAbout the Divine Council:");
        player.sendMessage("§7The Divine Council is an exclusive governing body for players");
        player.sendMessage("§7who have achieved Divine Convergence. Council members can:");
        player.sendMessage("§7• Create and vote on server-wide proposals");
        player.sendMessage("§7• Influence cosmic events and server rules");
        player.sendMessage("§7• Govern the affairs of transcendent beings");
        player.sendMessage("§7• Manipulate reality and alter the world");
        player.sendMessage("§7• Control server economy and progression");
        player.sendMessage("");
        player.sendMessage("§e§lProposal Types:");
        player.sendMessage("§7• §bServer Event §7- Propose server-wide events");
        player.sendMessage("§7• §cRule Change §7- Propose changes to server rules");
        player.sendMessage("§7• §dMember Action §7- Actions regarding council members");
        player.sendMessage("§7• §5Cosmic Intervention §7- Divine intervention in server affairs");
        player.sendMessage("");
        player.sendMessage("§e§lAdvanced Proposal Examples:");
        player.sendMessage("§7• §bFragment Rain §7- Increase fragment drops server-wide");
        player.sendMessage("§7• §aDivine Blessing §7- Grant beneficial effects to all players");
        player.sendMessage("§7• §5Cosmic Storm §7- Create reality distortion effects");
        player.sendMessage("§7• §6Altar Manifestation §7- Spawn new altars across the world");
        player.sendMessage("§7• §cDivine Punishment §7- Enforce justice on toxic players");
        player.sendMessage("§7• §bCosmic Balance §7- Restore equilibrium to all players");
        player.sendMessage("§7• §5Reality Storm §7- Tear holes in reality for chaos effects");
        player.sendMessage("§7• §6Council Raid §7- Trigger special convergence raids");
        player.sendMessage("§7• §eTime Manipulation §7- Accelerate or decelerate time flow");
        player.sendMessage("§7• §bReality Anchor §7- Protect players from reality distortions");
        player.sendMessage("");
        player.sendMessage("§c§l⚠ WARNING: §r§cSome proposals have irreversible effects!");
        player.sendMessage("§7Critical proposals require careful consideration and may");
        player.sendMessage("§7require administrator approval for execution.");
    }
}