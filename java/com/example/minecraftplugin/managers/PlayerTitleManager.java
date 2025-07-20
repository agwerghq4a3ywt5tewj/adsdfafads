package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.PlayerTitle;
import com.example.minecraftplugin.enums.AscensionLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Manages player titles and their assignment based on behavior and achievements
 */
public class PlayerTitleManager implements Listener {
    
    private final MinecraftPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final GodManager godManager;
    
    // Track player titles
    private final Map<UUID, PlayerTitle> playerTitles;
    
    // Toxicity detection patterns
    private final Pattern[] toxicPatterns = {
        Pattern.compile("(?i).*\\b(noob|scrub|trash|garbage|suck|terrible)\\b.*"),
        Pattern.compile("(?i).*\\b(ez|easy|rekt|owned|destroyed)\\b.*"),
        Pattern.compile("(?i).*\\b(kys|kill yourself)\\b.*"),
        Pattern.compile("(?i).*\\b(hacker|cheater|exploiter)\\b.*")
    };
    
    // Track toxic behavior
    private final Map<UUID, Integer> toxicityScores;
    private final Map<UUID, Long> lastToxicMessage;
    
    public PlayerTitleManager(MinecraftPlugin plugin, PlayerDataManager playerDataManager, GodManager godManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.godManager = godManager;
        this.playerTitles = new HashMap<>();
        this.toxicityScores = new HashMap<>();
        this.lastToxicMessage = new HashMap<>();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        plugin.getLogger().info("Player Title Manager initialized");
    }
    
    /**
     * Get a player's current title
     */
    public PlayerTitle getPlayerTitle(Player player) {
        return getPlayerTitle(player.getUniqueId());
    }
    
    public PlayerTitle getPlayerTitle(UUID playerId) {
        return playerTitles.getOrDefault(playerId, PlayerTitle.NONE);
    }
    
    /**
     * Set a player's title
     */
    public void setPlayerTitle(Player player, PlayerTitle title) {
        setPlayerTitle(player.getUniqueId(), title);
    }
    
    public void setPlayerTitle(UUID playerId, PlayerTitle title) {
        playerTitles.put(playerId, title);
        
        // Save to persistent storage (would need to extend PlayerDataManager)
        plugin.getLogger().info("Set title for " + playerId + " to " + title.name());
    }
    
    /**
     * Update a player's title based on their current status
     */
    public void updatePlayerTitle(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerTitle currentTitle = getPlayerTitle(playerId);
        
        // Check for Fallen status (permanent)
        if (currentTitle == PlayerTitle.FALLEN) {
            return; // Fallen status is permanent
        }
        
        // Check for toxic behavior
        int toxicScore = toxicityScores.getOrDefault(playerId, 0);
        if (toxicScore >= 5) {
            // Check death count for Fallen status
            int deathCount = playerDataManager.getDeathCount(player);
            if (deathCount >= 3) {
                setPlayerTitle(player, PlayerTitle.FALLEN);
                player.sendMessage("§4§l⚠ FALLEN STATUS ASSIGNED! ⚠");
                player.sendMessage("§cYour toxic behavior and excessive deaths have marked you as Fallen.");
                player.sendMessage("§cThis status is permanent and affects your divine powers.");
                
                // Broadcast to server
                plugin.getServer().broadcastMessage("§4§l" + player.getName() + " §r§4has been marked as Fallen!");
                return;
            } else if (currentTitle != PlayerTitle.TOXIC) {
                setPlayerTitle(player, PlayerTitle.TOXIC);
                player.sendMessage("§c§lToxic behavior detected! Your title has been updated.");
                return;
            }
        }
        
        // Check for positive titles based on achievements
        AscensionLevel ascension = godManager.getAscensionLevel(player);
        int testamentCount = godManager.getTestamentCount(player);
        
        if (ascension == AscensionLevel.CONVERGENCE) {
            setPlayerTitle(player, PlayerTitle.LEGEND);
        } else if (testamentCount >= 5) {
            setPlayerTitle(player, PlayerTitle.CHAMPION);
        } else if (testamentCount >= 2) {
            setPlayerTitle(player, PlayerTitle.BLESSED);
        } else if (toxicScore == 0 && currentTitle == PlayerTitle.TOXIC) {
            // Reset toxic title if behavior improves
            setPlayerTitle(player, PlayerTitle.NONE);
        }
    }
    
    /**
     * Get formatted display name with title
     */
    public String getDisplayNameWithTitle(Player player) {
        PlayerTitle title = getPlayerTitle(player);
        if (title == PlayerTitle.NONE) {
            return player.getName();
        }
        return title.getDisplayName() + " " + player.getName();
    }
    
    /**
     * Handle chat events for toxicity detection
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        
        // Check for toxic patterns
        boolean isToxic = false;
        for (Pattern pattern : toxicPatterns) {
            if (pattern.matcher(message).matches()) {
                isToxic = true;
                break;
            }
        }
        
        if (isToxic) {
            handleToxicBehavior(player);
        }
        
        // Update chat format with title
        String displayName = getDisplayNameWithTitle(player);
        event.setFormat(displayName + "§r: " + event.getMessage());
    }
    
    /**
     * Handle toxic behavior detection
     */
    private void handleToxicBehavior(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Increment toxicity score
        int currentScore = toxicityScores.getOrDefault(playerId, 0);
        toxicityScores.put(playerId, currentScore + 1);
        lastToxicMessage.put(playerId, currentTime);
        
        // Warn player
        if (currentScore == 0) {
            player.sendMessage("§e§lWarning: §r§eToxic behavior detected. Continued toxicity may result in consequences.");
        } else if (currentScore >= 3) {
            player.sendMessage("§c§lFinal Warning: §r§cContinued toxic behavior will result in permanent consequences.");
        }
        
        // Update title
        updatePlayerTitle(player);
        
        plugin.getLogger().info("Toxic behavior detected from " + player.getName() + " (score: " + (currentScore + 1) + ")");
    }
    
    /**
     * Decay toxicity scores over time
     */
    public void decayToxicityScores() {
        long currentTime = System.currentTimeMillis();
        long decayTime = 24 * 60 * 60 * 1000L; // 24 hours
        
        toxicityScores.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            long lastToxic = lastToxicMessage.getOrDefault(playerId, 0L);
            
            if (currentTime - lastToxic > decayTime) {
                // Reset toxicity score
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && getPlayerTitle(playerId) == PlayerTitle.TOXIC) {
                    setPlayerTitle(player, PlayerTitle.NONE);
                    player.sendMessage("§a§lBehavior Improved: §r§aYour toxic status has been cleared.");
                }
                return true;
            }
            return false;
        });
    }
    
    /**
     * Get all players with a specific title
     */
    public Map<UUID, PlayerTitle> getAllPlayerTitles() {
        return new HashMap<>(playerTitles);
    }
    
    /**
     * Clear a player's title (admin command)
     */
    public void clearPlayerTitle(Player player) {
        clearPlayerTitle(player.getUniqueId());
    }
    
    public void clearPlayerTitle(UUID playerId) {
        playerTitles.remove(playerId);
        toxicityScores.remove(playerId);
        lastToxicMessage.remove(playerId);
        
        plugin.getLogger().info("Cleared title for " + playerId);
    }
    
    /**
     * Start background task for toxicity score decay
     */
    public void startDecayTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            this::decayToxicityScores, 
            0L, 
            72000L // Run every hour (72000 ticks)
        );
    }
}