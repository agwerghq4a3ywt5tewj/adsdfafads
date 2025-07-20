package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.managers.GodManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GodlexCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    
    public GodlexCommand(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.godManager = plugin.getGodManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.godlex")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Check if no arguments provided - show list of all gods
        if (args.length == 0) {
            showGodList(player);
            return true;
        }
        
        // Check if too many arguments
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Usage: /godlex [god_name]");
            return true;
        }
        
        // Get the god name from arguments
        String godName = args[0];
        GodType god = GodType.fromString(godName);
        
        if (god == null) {
            player.sendMessage(ChatColor.RED + "§c§lInvalid God! §r§cAvailable gods:");
            showGodList(player);
            return true;
        }
        
        // Show detailed god information
        showGodInfo(player, god);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete god names
            for (GodType god : GodType.values()) {
                String godName = god.name().toLowerCase();
                if (godName.startsWith(args[0].toLowerCase())) {
                    completions.add(godName);
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Show a list of all available gods
     */
    private void showGodList(Player player) {
        player.sendMessage("§6§l=== GODLEX - DIVINE ENCYCLOPEDIA ===§r");
        player.sendMessage("§7Available gods to research:");
        player.sendMessage("");
        
        // Show Core Gods
        player.sendMessage("§e§lCore Gods (Fully Implemented):§r");
        for (GodType god : GodType.getCoreGods()) {
            boolean completed = godManager.hasCompletedTestament(player, god);
            String status = completed ? "§a✓" : "§7○";
            player.sendMessage("§7• " + status + " §f" + god.name().toLowerCase() + " §7- " + god.getDisplayName());
        }
        
        player.sendMessage("");
        
        // Show Expansion Gods
        player.sendMessage("§d§lExpansion Gods (Ready for Activation):§r");
        for (GodType god : GodType.getExpansionGods()) {
            boolean completed = godManager.hasCompletedTestament(player, god);
            String status = completed ? "§a✓" : "§7○";
            player.sendMessage("§7• " + status + " §f" + god.name().toLowerCase() + " §7- " + god.getDisplayName());
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §f/godlex <god_name>§7 for detailed information");
        player.sendMessage("§7Example: §f/godlex fallen§7 or §f/godlex forge");
    }
    
    /**
     * Show detailed information about a specific god
     */
    private void showGodInfo(Player player, GodType god) {
        boolean completed = godManager.hasCompletedTestament(player, god);
        int fragmentCount = godManager.getPlayerFragments(player, god).size();
        
        player.sendMessage("§6§l=== " + god.getDisplayName().toUpperCase() + " ===§r");
        player.sendMessage("");
        
        // Basic Information
        player.sendMessage("§e§lBasic Information:§r");
        player.sendMessage("§7Theme: §f" + god.getTheme());
        player.sendMessage("§7Tier: §f" + god.getTier().getDisplayName());
        player.sendMessage("§7Altar Center: §f" + god.getAltarCenterBlock().name());
        
        // Biomes
        player.sendMessage("§7Sacred Biomes: §f" + String.join(", ", god.getBiomes()));
        
        player.sendMessage("");
        
        // Player Progress
        player.sendMessage("§e§lYour Progress:§r");
        if (completed) {
            player.sendMessage("§a§l✓ TESTAMENT COMPLETED!§r");
            player.sendMessage("§7You have received divine power from this god.");
        } else {
            player.sendMessage("§7Fragments Collected: §f" + fragmentCount + "/7");
            if (fragmentCount == 7) {
                player.sendMessage("§e§lReady for Testament! Find an altar to complete.");
            } else {
                player.sendMessage("§7Continue exploring to find more fragments.");
            }
        }
        
        player.sendMessage("");
        
        // Divine Conflicts
        player.sendMessage("§e§lDivine Conflicts:§r");
        boolean hasConflicts = false;
        for (GodType otherGod : GodType.values()) {
            if (god.conflictsWith(otherGod)) {
                boolean otherCompleted = godManager.hasCompletedTestament(player, otherGod);
                String conflictStatus = otherCompleted ? "§c⚡ ACTIVE CONFLICT" : "§7⚠ Potential Conflict";
                player.sendMessage("§7• " + conflictStatus + " §7with " + otherGod.getDisplayName());
                hasConflicts = true;
            }
        }
        if (!hasConflicts) {
            player.sendMessage("§7• §aNone - This god has no conflicts");
        }
        
        player.sendMessage("");
        
        // Lore and Mythology
        showGodLore(player, god);
    }
    
    /**
     * Show detailed lore and mythology for a specific god
     */
    private void showGodLore(Player player, GodType god) {
        player.sendMessage("§e§lMythology & Lore:§r");
        
        switch (god) {
            case FALLEN:
                player.sendMessage("§7The Fallen God rules over death, undeath, and ultimate");
                player.sendMessage("§7protection. Once a deity of life, they embraced the");
                player.sendMessage("§7darkness to grant their followers immunity to death's");
                player.sendMessage("§7sting. Their heart beats with the power of resurrection.");
                player.sendMessage("§8\"Death is but a temporary inconvenience.\"");
                break;
                
            case BANISHMENT:
                player.sendMessage("§7The Banishment God commands fire, exile, and destruction.");
                player.sendMessage("§7Born from the first act of divine punishment, they");
                player.sendMessage("§7wield flames that can banish enemies to distant realms");
                player.sendMessage("§7or reduce them to ash and memory.");
                player.sendMessage("§8\"Let the flames carry away what should not be.\"");
                break;
                
            case ABYSSAL:
                player.sendMessage("§7The Abyssal God dwells in the deepest ocean trenches,");
                player.sendMessage("§7master of water in all its forms. They command the");
                player.sendMessage("§7crushing depths, the gentle rain, and the mighty");
                player.sendMessage("§7tsunami with equal ease.");
                player.sendMessage("§8\"The depths hold secrets older than land.\"");
                break;
                
            case SYLVAN:
                player.sendMessage("§7The Sylvan God embodies the wild growth of nature.");
                player.sendMessage("§7Ancient forests whisper their name, and every seed");
                player.sendMessage("§7carries their blessing. They represent the endless");
                player.sendMessage("§7cycle of growth, decay, and renewal.");
                player.sendMessage("§8\"In every leaf, the promise of tomorrow.\"");
                break;
                
            case TEMPEST:
                player.sendMessage("§7The Tempest God rules the skies, storms, and lightning.");
                player.sendMessage("§7They dance among the clouds and speak in thunder,");
                player.sendMessage("§7granting their followers the power of flight and");
                player.sendMessage("§7command over the very air itself.");
                player.sendMessage("§8\"The sky knows no boundaries, nor should you.\"");
                break;
                
            case VEIL:
                player.sendMessage("§7The Veil God manipulates reality itself, bending the");
                player.sendMessage("§7fabric of existence to their will. They see through");
                player.sendMessage("§7all illusions and grant power over the void between");
                player.sendMessage("§7worlds, where possibility becomes reality.");
                player.sendMessage("§8\"Reality is merely a suggestion.\"");
                break;
                
            case FORGE:
                player.sendMessage("§7The Forge God masters smithing, crafting, and creation.");
                player.sendMessage("§7Their hammer shapes not just metal, but destiny itself.");
                player.sendMessage("§7Every tool, weapon, and work of art carries a spark");
                player.sendMessage("§7of their divine fire and creative passion.");
                player.sendMessage("§8\"In the forge, raw potential becomes perfection.\"");
                break;
                
            case VOID:
                player.sendMessage("§7The Void God embodies emptiness, teleportation, and");
                player.sendMessage("§7phase shifting. They exist in the spaces between,");
                player.sendMessage("§7the gaps in reality where nothing and everything");
                player.sendMessage("§7coexist in perfect, terrifying harmony.");
                player.sendMessage("§8\"In nothingness, find everything.\"");
                break;
                
            case TIME:
                player.sendMessage("§7The Time God controls the flow of temporal energy,");
                player.sendMessage("§7aging, and temporal magic. They see all moments");
                player.sendMessage("§7simultaneously and can grant glimpses of past and");
                player.sendMessage("§7future to those worthy of such knowledge.");
                player.sendMessage("§8\"Time is a river that flows in all directions.\"");
                break;
                
            case BLOOD:
                player.sendMessage("§7The Blood God thrives on combat, sacrifice, and");
                player.sendMessage("§7berserker rage. They transform pain into power and");
                player.sendMessage("§7weakness into strength, demanding only that their");
                player.sendMessage("§7followers never yield in battle.");
                player.sendMessage("§8\"Strength is earned through struggle.\"");
                break;
                
            case CRYSTAL:
                player.sendMessage("§7The Crystal God resonates with crystals, sound, and");
                player.sendMessage("§7vibration. They sing the songs that shaped the world");
                player.sendMessage("§7and grant harmony to those who listen to the music");
                player.sendMessage("§7hidden within all things.");
                player.sendMessage("§8\"Every crystal holds a note of creation's song.\"");
                break;
                
            case SHADOW:
                player.sendMessage("§7The Shadow God masters stealth, darkness, and");
                player.sendMessage("§7assassination. They move unseen through the world,");
                player.sendMessage("§7granting their followers the power to become one");
                player.sendMessage("§7with darkness and strike from the void.");
                player.sendMessage("§8\"In shadow, find truth. In darkness, find power.\"");
                break;
        }
    }
}