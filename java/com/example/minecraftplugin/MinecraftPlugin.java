package com.example.minecraftplugin;

import com.example.minecraftplugin.commands.ExampleCommand;
import com.example.minecraftplugin.commands.TestamentCommand;
import com.example.minecraftplugin.commands.GodlexCommand;
import com.example.minecraftplugin.commands.AltarCommand;
import com.example.minecraftplugin.commands.TutorialCommand;
import com.example.minecraftplugin.commands.RaidCommand;
import com.example.minecraftplugin.commands.ConfigCommand;
import com.example.minecraftplugin.commands.TranscendenceCommand;
import com.example.minecraftplugin.commands.CouncilCommand;
import com.example.minecraftplugin.commands.BountyCommand;
import com.example.minecraftplugin.commands.GuildCommand;
import com.example.minecraftplugin.listeners.PlayerListener;
import com.example.minecraftplugin.listeners.FragmentListener;
import com.example.minecraftplugin.listeners.AltarListener;
import com.example.minecraftplugin.listeners.DivineItemListener;
import com.example.minecraftplugin.listeners.DeathListener;
import com.example.minecraftplugin.listeners.ConflictResolutionListener;
import com.example.minecraftplugin.listeners.RedemptionAltarListener;
import com.example.minecraftplugin.listeners.RedemptionListener;
import com.example.minecraftplugin.listeners.BountyListener;
import com.example.minecraftplugin.managers.GodManager;
import com.example.minecraftplugin.managers.CooldownManager;
import com.example.minecraftplugin.managers.PlayerDataManager;
import com.example.minecraftplugin.managers.PlayerTitleManager;
import com.example.minecraftplugin.managers.ConvergenceManager;
import com.example.minecraftplugin.managers.TutorialManager;
import com.example.minecraftplugin.managers.PerformanceManager;
import com.example.minecraftplugin.managers.TranscendenceManager;
import com.example.minecraftplugin.managers.DivineCouncilManager;
import com.example.minecraftplugin.managers.RaidLeaderboardManager;
import com.example.minecraftplugin.managers.BountyManager;
import com.example.minecraftplugin.managers.BroadcastManager;
import com.example.minecraftplugin.managers.EnderDragonCombatManager;
import com.example.minecraftplugin.managers.GuildManager;
import com.example.minecraftplugin.managers.DivineForgeManager;
import com.example.minecraftplugin.commands.ForgeCommand;
import com.example.minecraftplugin.listeners.DivineForgeListener;
import com.example.minecraftplugin.database.DatabaseManager;
import com.example.minecraftplugin.database.DataMigrationTool;
import com.example.minecraftplugin.crossserver.CrossServerManager;
import com.example.minecraftplugin.transcendence.TranscendenceAbilityManager;
import com.example.minecraftplugin.raids.CrossServerRaidManager;
import com.example.minecraftplugin.effects.VisualEffectsManager;
import com.example.minecraftplugin.raids.RaidManager;
import com.example.minecraftplugin.world.AltarGenerator;
import com.example.minecraftplugin.config.ConfigManager;
import com.example.minecraftplugin.effects.AltarEffectsManager;
import com.example.minecraftplugin.effects.EnhancedAltarEffects;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftPlugin extends JavaPlugin {

    private GodManager godManager;
    private CooldownManager cooldownManager;
    private PlayerDataManager playerDataManager;
    private PlayerTitleManager playerTitleManager;
    private AltarGenerator altarGenerator;
    private ConvergenceManager convergenceManager;
    private TutorialManager tutorialManager;
    private PerformanceManager performanceManager;
    private VisualEffectsManager visualEffectsManager;
    private RaidManager raidManager;
    private ConfigManager configManager;
    private AltarEffectsManager altarEffectsManager;
    private TranscendenceManager transcendenceManager;
    private EnhancedAltarEffects enhancedAltarEffects;
    private DivineCouncilManager divineCouncilManager;
    private RaidLeaderboardManager raidLeaderboardManager;
    private BountyManager bountyManager;
    private BroadcastManager broadcastManager;
    private EnderDragonCombatManager enderDragonCombatManager;
    private GuildManager guildManager;
    private DivineForgeManager divineForgeManager;
    private DatabaseManager databaseManager;
    private DataMigrationTool dataMigrationTool;
    private CrossServerManager crossServerManager;
    private TranscendenceAbilityManager transcendenceAbilityManager;
    private CrossServerRaidManager crossServerRaidManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("MinecraftPlugin has been enabled!");
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize configuration manager first
        this.configManager = new ConfigManager(this);
        
        // Initialize database system
        this.databaseManager = new DatabaseManager(this);
        this.dataMigrationTool = new DataMigrationTool(this, databaseManager);
        
        // Initialize god management system
        initializeGodSystem();
        
        // Register commands
        registerCommands();
        
        // Register event listeners
        registerListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (playerDataManager != null) {
            playerDataManager.shutdown();
        }
        if (performanceManager != null) {
            performanceManager.shutdown();
        }
        if (raidManager != null) {
            raidManager.shutdown();
        }
        if (configManager != null) {
            configManager.shutdown();
        }
        if (raidLeaderboardManager != null) {
            raidLeaderboardManager.shutdown();
        }
        if (guildManager != null) {
            guildManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (crossServerManager != null) {
            crossServerManager.shutdown();
        }
        if (crossServerRaidManager != null) {
            crossServerRaidManager.shutdown();
        }
        getLogger().info("MinecraftPlugin has been disabled!");
    }
    
    private void initializeGodSystem() {
        // Initialize the player data manager first
        this.playerDataManager = new PlayerDataManager(this);
        
        // Initialize performance manager early
        this.performanceManager = new PerformanceManager(this);
        
        // Initialize the god manager
        this.godManager = new GodManager(this);
        this.cooldownManager = new CooldownManager(this, playerDataManager);
        this.playerTitleManager = new PlayerTitleManager(this, playerDataManager, godManager);
        this.altarGenerator = new AltarGenerator(this);
        this.tutorialManager = new TutorialManager(this, playerDataManager);
        this.visualEffectsManager = new VisualEffectsManager(this);
        this.raidManager = new RaidManager(this);
        this.altarEffectsManager = new AltarEffectsManager(this);
        this.transcendenceManager = new TranscendenceManager(this, convergenceManager);
        this.enhancedAltarEffects = new EnhancedAltarEffects(this);
        this.divineCouncilManager = new DivineCouncilManager(this, convergenceManager);
        this.broadcastManager = new BroadcastManager(this);
        this.bountyManager = new BountyManager(this, playerDataManager);
        this.enderDragonCombatManager = new EnderDragonCombatManager(this, broadcastManager);
        this.guildManager = new GuildManager(this);
        this.divineForgeManager = new DivineForgeManager(this, godManager);
        this.crossServerManager = new CrossServerManager(this, databaseManager);
        this.transcendenceAbilityManager = new TranscendenceAbilityManager(this);
        this.crossServerRaidManager = new CrossServerRaidManager(this, crossServerManager, databaseManager, raidManager);
        
        // Initialize raid leaderboard manager (will be created by RaidManager)
        // this.raidLeaderboardManager = new RaidLeaderboardManager(this);
        
        // Check Divine Council configuration and warn appropriately
        checkDivineCouncilConfiguration();
        
        // Start background tasks
        playerTitleManager.startDecayTask();
        
        getLogger().info("God management system initialized!");
    }
    
    private void registerCommands() {
        // Register the example command
        getCommand("example").setExecutor(new ExampleCommand(this));
        getCommand("example").setTabCompleter(new ExampleCommand(this));
        
        // Register the testament command
        TestamentCommand testamentCommand = new TestamentCommand(this);
        getCommand("testament").setExecutor(testamentCommand);
        getCommand("testament").setTabCompleter(testamentCommand);
        
        // Register the godlex command
        GodlexCommand godlexCommand = new GodlexCommand(this);
        getCommand("godlex").setExecutor(godlexCommand);
        getCommand("godlex").setTabCompleter(godlexCommand);
        
        // Register the altar command
        AltarCommand altarCommand = new AltarCommand(this, altarGenerator);
        getCommand("altar").setExecutor(altarCommand);
        getCommand("altar").setTabCompleter(altarCommand);
        
        // Register the tutorial command
        TutorialCommand tutorialCommand = new TutorialCommand(this, tutorialManager);
        getCommand("tutorial").setExecutor(tutorialCommand);
        getCommand("tutorial").setTabCompleter(tutorialCommand);
        
        // Register the raid command
        RaidCommand raidCommand = new RaidCommand(this, raidManager);
        getCommand("raid").setExecutor(raidCommand);
        getCommand("raid").setTabCompleter(raidCommand);
        
        // Register the config command
        ConfigCommand configCommand = new ConfigCommand(this, configManager);
        getCommand("config").setExecutor(configCommand);
        getCommand("config").setTabCompleter(configCommand);
        
        // Register the transcendence command
        TranscendenceCommand transcendenceCommand = new TranscendenceCommand(this, transcendenceManager);
        getCommand("transcendence").setExecutor(transcendenceCommand);
        getCommand("transcendence").setTabCompleter(transcendenceCommand);
        
        // Register the council command
        CouncilCommand councilCommand = new CouncilCommand(this, divineCouncilManager);
        getCommand("council").setExecutor(councilCommand);
        getCommand("council").setTabCompleter(councilCommand);
        
        // Register the bounty command
        BountyCommand bountyCommand = new BountyCommand(this, bountyManager);
        getCommand("bounty").setExecutor(bountyCommand);
        getCommand("bounty").setTabCompleter(bountyCommand);
        
        // Register the guild command
        GuildCommand guildCommand = new GuildCommand(this, guildManager);
        getCommand("guild").setExecutor(guildCommand);
        getCommand("guild").setTabCompleter(guildCommand);
        
        // Register the forge command
        ForgeCommand forgeCommand = new ForgeCommand(this, divineForgeManager);
        getCommand("forge").setExecutor(forgeCommand);
        getCommand("forge").setTabCompleter(forgeCommand);
    }
    
    private void registerListeners() {
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new FragmentListener(this, godManager, cooldownManager), this);
        getServer().getPluginManager().registerEvents(new AltarListener(this, godManager), this);
        getServer().getPluginManager().registerEvents(new DivineItemListener(this, godManager, cooldownManager), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this, playerDataManager), this);
        getServer().getPluginManager().registerEvents(new ConflictResolutionListener(this, godManager), this);
        getServer().getPluginManager().registerEvents(new RedemptionAltarListener(this), this);
        getServer().getPluginManager().registerEvents(new RedemptionListener(this, playerDataManager), this);
        getServer().getPluginManager().registerEvents(new BountyListener(this, bountyManager), this);
        getServer().getPluginManager().registerEvents(new DivineForgeListener(this, divineForgeManager), this);
    }
    
    // Getter for god manager
    public GodManager getGodManager() {
        return godManager;
    }
    
    // Getter for cooldown manager
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    // Getter for player data manager
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    // Getter for player title manager
    public PlayerTitleManager getPlayerTitleManager() {
        return playerTitleManager;
    }
    
    // Getter for altar generator
    public AltarGenerator getAltarGenerator() {
        return altarGenerator;
    }
    
    // Getter for convergence manager
    public ConvergenceManager getConvergenceManager() {
        return convergenceManager;
    }
    
    // Getter for tutorial manager
    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }
    
    // Getter for performance manager
    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }
    
    // Getter for visual effects manager
    public VisualEffectsManager getVisualEffectsManager() {
        return visualEffectsManager;
    }
    
    // Getter for raid manager
    public RaidManager getRaidManager() {
        return raidManager;
    }
    
    // Getter for config manager
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    // Getter for altar effects manager
    public AltarEffectsManager getAltarEffectsManager() {
        return altarEffectsManager;
    }
    
    // Getter for transcendence manager
    public TranscendenceManager getTranscendenceManager() {
        return transcendenceManager;
    }
    
    // Getter for enhanced altar effects
    public EnhancedAltarEffects getEnhancedAltarEffects() {
        return enhancedAltarEffects;
    }
    
    // Getter for divine council manager
    public DivineCouncilManager getDivineCouncilManager() {
        return divineCouncilManager;
    }
    
    // Getter for bounty manager
    public BountyManager getBountyManager() {
        return bountyManager;
    }
    
    // Getter for broadcast manager
    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }
    
    // Getter for ender dragon combat manager
    public EnderDragonCombatManager getEnderDragonCombatManager() {
        return enderDragonCombatManager;
    }
    
    // Getter for guild manager
    public GuildManager getGuildManager() {
        return guildManager;
    }
    
    // Getter for divine forge manager
    public DivineForgeManager getDivineForgeManager() {
        return divineForgeManager;
    }
    
    // Getter for database manager
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    // Getter for data migration tool
    public DataMigrationTool getDataMigrationTool() {
        return dataMigrationTool;
    }
    
    // Getter for cross-server manager
    public CrossServerManager getCrossServerManager() {
        return crossServerManager;
    }
    
    // Getter for transcendence ability manager
    public TranscendenceAbilityManager getTranscendenceAbilityManager() {
        return transcendenceAbilityManager;
    }
    
    // Getter for cross-server raid manager
    public CrossServerRaidManager getCrossServerRaidManager() {
        return crossServerRaidManager;
    }
    
    // Method to check Divine Council configuration
    private void checkDivineCouncilConfiguration() {
        boolean councilEnabled = getConfig().getBoolean("divine_council.enabled", false);
        
        if (councilEnabled) {
            getLogger().warning("=".repeat(60));
            getLogger().warning("DIVINE COUNCIL SYSTEM IS ENABLED!");
            getLogger().warning("This system allows reality manipulation and world changes.");
            getLogger().warning("Ensure you have backed up your world before proceeding.");
            getLogger().warning("=".repeat(60));
        }
    }
}