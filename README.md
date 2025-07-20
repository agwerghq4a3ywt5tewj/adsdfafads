# Fallen God Testament - Complete Minecraft Plugin

## 🎮 Overview
Epic Minecraft plugin where players collect 7 rare fragments from each of 12 fallen gods to unlock divine powers and progress towards ultimate rewards. The ultimate goal is achieving **Divine Convergence** - mastering all gods to become the supreme deity.

This plugin currently features a comprehensive testament system with divine items, fragment collection, player progression, and a lives system. Future updates will include a dynamic PvP bounty system, difficulty-scaled boss battles, an enhanced Ender Dragon guardian system, and server-wide coordinate broadcasting. Built for Paper 1.21.5+ with Java 21.

## ⚡ Quick Start

### 1. Build the Plugin
```bash
mvn clean package
```

### 2. Install Plugin
```bash
# Copy the compiled JAR to your server
cp target/MinecraftPlugin-1.0.0.jar /path/to/server/plugins/
```

### 3. Natural Altar Generation (Automatic)
```bash
# Natural altar generation is now implemented and automatic.
# Configure settings in config.yml under 'testament.altars.natural_generation'
# Altars will spawn naturally in appropriate biomes based on configuration.
```

### 4. Start Server
```bash
# Restart server or reload
/reload
```

## 🏛️ The Twelve Gods & Their Rewards

### **Core Six Gods** (Fully Implemented)

#### 1. **Fallen God** - Ultimate Defense
- **Theme**: Death, undeath, ultimate protection
- **Reward**: **Heart of the Fallen God**
- **Power**: Increases max health to 25 hearts (50 HP), Regeneration II when below 50% health.
- **Special**: **Heart Return System** - If the Heart is consumed as a totem, it automatically returns after 45 seconds.
- **Biomes**: Swamps, Dark Forests, Deep Dark
- **Altar Center**: Crying Obsidian

#### 2. **Banishment God** - Fire & Exile
- **Theme**: Fire, exile, destruction
- **Reward**: **Scepter of Banishment**
- **Power**: Passive Fire Resistance, immunity to lava. Active: Right-click to banish nearby enemies with knockback and fire.
- **Biomes**: Deserts, Badlands, Savannas
- **Altar Center**: Magma Block

#### 3. **Abyssal God** - Master of Depths
- **Theme**: Ocean depths, water mastery
- **Reward**: **Trident of the Abyss**
- **Power**: Passive Water Breathing, Dolphin's Grace (faster swimming), Night Vision underwater. Active: Right-click to grant water breathing aura to nearby players.
- **Biomes**: All ocean types (including underwater)
- **Altar Center**: Dark Prismarine

#### 4. **Sylvan God** - Nature's Guardian
- **Theme**: Forests, nature, growth
- **Reward**: **Staff of Sylvan Growth**
- **Power**: Passive Regeneration II, immunity to poison. Active: Right-click to instantly grow crops in a large area.
- **Biomes**: Forests, Jungles, Taigas
- **Altar Center**: Oak Log

#### 5. **Tempest God** - Storm Lord
- **Theme**: Sky, storms, lightning, flight
- **Reward**: **Wings of Tempest**
- **Power**: Passive Slow Falling, Jump Boost II, Speed I. Active: Right-click to launch into the air with a wind burst.
- **Biomes**: Mountains, Hills, Peaks
- **Altar Center**: Lightning Rod

#### 6. **Veil God** - Master of Dimensions
- **Theme**: Reality manipulation, void magic
- **Reward**: **Orb of Veil Reality**
- **Power**: Passive Invisibility while sneaking, magic resistance. Active: Right-click to teleport to where you're looking.
- **Biomes**: End dimension only
- **Altar Center**: End Portal Frame

### **Expansion Six Gods** (Fully Implemented)

#### 7. **Forge God** - Master of Creation
- **Theme**: Smithing, crafting, creation, molten metal
- **Reward**: **Mace of Divine Forging**
- **Power**: Passive Strength I and Haste I. Active: Right-click to repair held item or tools in inventory.
- **Biomes**: Mountains, Caves, Lava areas
- **Altar Center**: Anvil
- **Special**: Conflicts with Heart of the Fallen God (conflict detection and resolution implemented).

#### 8. **Void God** - Harbinger of Nothingness
- **Theme**: Emptiness, teleportation, phase shifting
- **Reward**: **Void Walker's Blade**
- **Power**: Passive phase through blocks while sneaking, void immunity. Active: Right-click to teleport 10 blocks forward through any material.
- **Biomes**: End dimension, Deep caves
- **Altar Center**: Obsidian

#### 9. **Time God** - Chronos Incarnate
- **Theme**: Time manipulation, aging, temporal magic
- **Reward**: **Chronos Staff**
- **Power**: Passive Haste II, slow aging effects. Active: Right-click to create time dilation field that slows enemies.
- **Biomes**: Deep Dark, Dripstone Caves, Lush Caves
- **Altar Center**: Amethyst Cluster

#### 10. **Blood God** - Warrior's Patron
- **Theme**: Combat, sacrifice, berserker rage
- **Reward**: **Crimson Blade**
- **Power**: Passive Blood Frenzy (damage increases as health decreases), life steal. Active: Right-click to enter berserker rage.
- **Biomes**: Nether, dangerous areas
- **Altar Center**: Redstone Block

#### 11. **Crystal God** - Resonance Master
- **Theme**: Crystals, sound, vibration, harmony
- **Reward**: **Resonance Crystal**
- **Power**: Passive crystal shield when sneaking, ore sense. Active: Right-click to create sonic boom that damages enemies.
- **Biomes**: Geode caves, crystal formations
- **Altar Center**: Large Amethyst Cluster

#### 12. **Shadow God** - Master of Darkness
- **Theme**: Stealth, darkness, assassination
- **Reward**: **Shadow Mantle**
- **Power**: Passive umbral form (invisibility in darkness), shadow step. Active: Right-click to become one with shadows temporarily.
- **Biomes**: Dark areas, caves, sculk biomes
- **Altar Center**: Sculk Catalyst

## ⚔️ Advanced Divine Item Abilities (Implemented)

### **Active Abilities** (Right-click)
- **Heart of the Fallen God**: Instantly heal 10 hearts (60s cooldown).
- **Scepter of Banishment**: Banish nearby enemies with knockback and fire (45s cooldown).
- **Trident of the Abyss**: Grant water breathing aura to nearby players (60s cooldown).
- **Staff of Sylvan Growth**: Instantly grow crops in a 7-block radius (30s cooldown).
- **Wings of Tempest**: Launch skyward with a wind burst (20s cooldown).
- **Orb of Veil Reality**: Teleport forward up to 32 blocks (15s cooldown).
- **Mace of Divine Forging**: Repair held items or tools in inventory (30s cooldown).
- **Void Walker's Blade**: Void rip teleport 10 blocks forward (5s cooldown).
- **Chronos Staff**: Create time dilation field that slows enemies (25s cooldown).
- **Crimson Blade**: Enter berserker rage with enhanced combat abilities (45s cooldown).
- **Resonance Crystal**: Sonic boom that damages and pushes enemies (20s cooldown).
- **Shadow Mantle**: Become one with shadows for invisibility and speed (30s cooldown).

### **Passive Abilities**
- **Heart of the Fallen God**: Increases max health to 25 hearts, Regeneration II when below 50% health.
- **Scepter of Banishment**: Fire Resistance, immunity to lava.
- **Trident of the Abyss**: Water Breathing, Dolphin's Grace, Night Vision underwater.
- **Staff of Sylvan Growth**: Regeneration II, immunity to poison.
- **Wings of Tempest**: Slow Falling, Jump Boost II, Speed I.
- **Orb of Veil Reality**: Invisibility while sneaking, magic resistance.
- **Mace of Divine Forging**: Strength I and Haste I when held.
- **Void Walker's Blade**: Phase through blocks while sneaking, void immunity.
- **Chronos Staff**: Haste II, slow aging effects, enhanced time perception.
- **Crimson Blade**: Blood Frenzy (damage increases as health decreases), life steal.
- **Resonance Crystal**: Crystal shield when sneaking, ore sense, immunity to mining fatigue.
- **Shadow Mantle**: Umbral form (invisibility in darkness), shadow step, no fall damage.

### **Special Mechanics**
- **Heart Return**: Heart of Fallen God returns 45 seconds after being consumed as a totem.
- **Divine Conflicts**: Opposing god pairs automatically remove conflicting items (detection and resolution implemented).
- **Convergence Nexus**: Ultimate divine item granted upon achieving Divine Convergence (all 12 testaments).

### Advanced Divine Item System (Implemented)
- **Item Upgrading**: Enhance divine items through five rarity levels (Divine → Enhanced → Legendary → Mythic → Transcendent)
- **Divine Item Combinations**: Merge compatible divine items to create unique artifacts with combined powers
- **Legendary Variants**: Rare enhanced versions with unique mechanics (5% spawn chance for special variants)
- **Upgrade Materials**: Collect Divine Essence, Cosmic Fragments, Reality Shards, Transcendent Cores, and Fusion Catalysts
- **Divine Forge System**: Complete crafting interface for upgrades and combinations
- **Combination Artifacts**: Steam Lord's Regalia, Worldtree Crown, Genesis Void Hammer, Chronos Reality Orb, and more
- **Material Integration**: Upgrade materials drop from boss defeats, rare mining, and special events

## 🌟 Divine Ascension System (Implemented)

### **Ascension Levels**
- **Mortal** (0 testaments): Base gameplay
- **Blessed** (1 testament): Minor divine favor
- **Chosen** (3 testaments): Significant abilities
- **Divine** (5 testaments): Enhanced effects, reality manipulation
- **Godlike** (7 testaments): Maximum effects, ultimate cosmic powers
- **CONVERGENCE** (12 testaments): **Master of All Divinity** - Transcends all limitations, grants Convergence Nexus.

### **Testament Conflicts**
Opposing god pairs create strategic choices. Conflicts are detected and automatically resolved through item removal.
- **Fallen vs Veil**: Death vs Reality
- **Banishment vs Abyssal**: Fire vs Water
- **Sylvan vs Tempest**: Nature vs Storm
- **Forge vs Void**: Creation vs Destruction
- **Time vs Shadow**: Light vs Dark
- **Blood vs Crystal**: Chaos vs Order

## 🎯 Fragment Collection System (Implemented)

### **Spawn Mechanics**
- **Chest Spawning**: Configurable chance (`testament.fragments.chest_spawn_chance`) after opening chests.
- **Mob Drops**: Configurable chance (`testament.fragments.mob_drop_chance`) from dangerous mobs.
- **Cooldowns**: Configurable hours between chest fragments (`testament.fragments.chest_cooldown_hours`) and mob fragments (`testament.fragments.mob_cooldown_hours`).

## 🏗️ Altar System (Implemented)

### **Altar Interaction**
Players can complete testaments by right-clicking the correct center block for each god. Full altar structure validation ensures proper construction.

### **Natural Altar Generation**
Altars spawn naturally in appropriate biomes based on configuration settings. Generation rates and biome compatibility are fully configurable.

## 📋 Commands (Implemented)

### **Player Commands**
- `/testament status` - View player's fragment progress and ascension level.
- `/testament fragments [god]` - Show fragment collection status for all gods or a specific god.
- `/testament lives` - Show current death count and void prisoner status.
- `/testament conflicts` - Show active divine conflicts.
- `/testament ascension` - Show ascension status and effects.
- `/testament convergence` - Show Divine Convergence status.
- `/godlex` - Show a list of all available gods.
- `/godlex <god_name>` - Display detailed information and lore about a specific god.
- `/tutorial [start|skip|reset|progress|status]` - Access the Testament System tutorial.
- `/raid [list|info|start|leave|status]` - Manage and participate in custom raids.
- `/transcendence [status|challenges|abilities|help]` - Access post-convergence transcendence content.
- `/council [status|join|leave|members|propose|vote|proposals|help]` - Access the Divine Council system for converged players.

- `/guild [create|invite|join|leave|info|list|help]` - Manage raid guilds and team formation.
- `/forge [upgrade|combine|info|materials|combinations|help]` - Access the Divine Forge system for item enhancement.
### **Admin Commands**
- `/testament admin setdeaths <player> <count>` - Set a player's death count.
- `/testament admin setvoid <player> <true/false>` - Set a player's void prisoner status.
- `/testament admin resetplayer <player>` - Reset all lives system data for a player.
- `/testament admin give-fragment <player> <god> <number>` - Give specific fragments to players.
- `/testament admin complete <player> <god>` - Complete a testament for a player.
- `/testament admin convergence <grant|remove> <player>` - Manage Divine Convergence status.
- `/altar generate <god>` - Generate an altar at your location.
- `/altar stats` - Show altar generation statistics.
- `/transcendence complete <challenge_id>` - Complete transcendence challenges (admin only).
- `/council execute <proposal_id>` - Manually execute passed proposals (admin only).
- `/council emergency-stop <proposal_id>` - Emergency stop dangerous proposals (admin only).
- `/council rollback <type>` - Rollback council-made changes (admin only).
- `/forge give <material_type> <amount>` - Give upgrade materials to players (admin only).
- `/forge stats` - Show Divine Forge system statistics (admin only).
- `/config [reload|get|set|template|export|import|stats|validate]` - Advanced configuration management (admin only).

## 🎮 Player Title System (Implemented)
- `/guild stats` - Show guild statistics (admin only).

Players earn titles based on achievements and behavior:
- **Blessed**: Complete 2+ testaments
- **Champion**: Complete 5+ testaments  
- **Legend**: Achieve Divine Convergence
- **Toxic**: Negative behavior (temporary)
- **Fallen**: Toxic + excessive deaths (permanent)

## 🎮 Custom Raids System (Implemented)

Tiered raid system based on testament progression:
- **Novice Raids**: 0-2 testaments (1-2 players)
- **Adept Raids**: 3-6 testaments (2-3 players)
- **Master Raids**: 7-11 testaments (3-4 players)
- **Convergence Raids**: 12 testaments (3+ players)
- **Eternal Crucible**: Ultimate convergence challenge

### **Guild Integration**
- Guilds can participate in raids together for bonus rewards
- Guild raid statistics and leaderboards
- Team formation and persistent raid groups

## 🎓 Tutorial System (Implemented)

Comprehensive tutorial system for new players:
- Automatic tutorial start for new players
- Step-by-step guidance through fragment collection
- Altar building and testament completion tutorials
- Divine power and ascension system explanations

## 🔧 Configuration (Implemented)

Customize settings in `config.yml`:
- `plugin.enabled`, `plugin.debug`
- `testament.fragments.chest_spawn_chance`, `mob_drop_chance`, `chest_cooldown_hours`, `mob_cooldown_hours`
- `testament.ascension.effects_enabled`, `level_announcements`
- `testament.conflicts.enabled`, `remove_conflicting_items`, `announce_conflicts`
- `testament.gods.fallen.heart_return_seconds`, `max_hearts`
- `testament.gods.forge.repair_cooldown_seconds`, `wind_burst_damage`
- `testament.gods.veil.nullification_range`, `reality_effects`
- `testament.lives_system.death_threshold`, `void_world_name`, `void_teleport_coords`, `prisoner_message`, `redemption_broadcast`, `restrict_unpledged`, `vulnerable_effects`
- `testament.altars.natural_generation` - Natural altar spawning settings
- `tutorial.enabled`, `auto_start_for_new_players` - Tutorial system settings
- `performance.caching.enabled`, `async_operations.enabled` - Performance optimization
- `effects.enabled`, `particle_density`, `god_auras` - Visual effects settings
- `raids.enabled`, `max_concurrent_raids` - Custom raids configuration
- `transcendence.enabled`, `challenge_completion_effects` - Transcendence system settings
- `transcendence.abilities.*` - Individual transcendence ability configurations
- `raids.guilds.*` - Guild system configuration for raids

### **Divine Forge Configuration**
- `forge.enabled` - Enable/disable the Divine Forge system
- `forge.upgrades.success_chances.*` - Success rates for each rarity upgrade
- `forge.material_drops.*` - Drop rates for upgrade materials from various sources
- `forge.combinations.enabled` - Enable/disable divine item combinations
- `forge.legendary_variants.spawn_chances.*` - Spawn rates for legendary variants
- `forge.rewards.*` - Material rewards for achievements and completions

### **Divine Council Configuration (Advanced)**

**⚠️ CRITICAL WARNING: The Divine Council system is DISABLED by default (`divine_council.enabled: false`) for important reasons:**

**Why Disabled by Default:**
1. **Reality Manipulation** - Council members can alter terrain, spawn structures, and modify the physical world
2. **Economic Disruption** - Fragment rain events and divine blessings can dramatically alter server economy
3. **Power Imbalance** - Converged players gain governance over all other players' gameplay experience
4. **Irreversible Changes** - Some cosmic interventions create permanent world modifications
5. **Save Corruption Risk** - Reality manipulation features stress the world save system

**STRONGLY RECOMMENDED: Reset your world when enabling Divine Council for the first time!**

The Divine Council system introduces fundamental changes to server dynamics that work best with a fresh start. Consider this a "New Game+" feature for servers ready for cosmic-level gameplay.

**Divine Council Configuration Options:**
- `divine_council.enabled` - Enable/disable the entire council system (default: false)
- `divine_council.max_size` - Maximum council members (default: 12)
- `divine_council.approval_threshold` - Vote percentage needed for proposals (default: 0.6)
- `divine_council.powers.*` - Individual power toggles (server_events, reality_manipulation, etc.)
- `divine_council.integration.*` - Integration with raids, events, economy systems
- `divine_council.safety.*` - Safety measures and backup settings
- `divine_council.events.*` - Event-specific configurations for proposal execution

## 🏗️ Technical Implementation

### **Requirements**
- **Server**: Paper 1.21.5+
- **Java**: 21+
- **Maven**: 3.6+

### **Features (Implemented)**
- **Complete Divine Item System**: All 12 divine items with unique abilities
- **Divine Convergence System**: Ultimate power for mastering all gods
- **Full Altar System**: Structure validation and natural generation
- **Advanced Fragment System**: Configurable spawning and collection
- **Lives System**: Death tracking and void prisoner mechanics
- **Redemption System**: Shards of Atonement and Key to Redemption
- **Conflict Resolution**: Automatic detection and item removal
- **Ascension System**: Progressive power levels with effects
- **Player Title System**: Achievement and behavior-based titles
- **Custom Raids System**: Tiered challenges for all progression levels
- **Tutorial System**: Comprehensive new player guidance
- **Performance Optimization**: Caching and async operations
- **Transcendence System**: Post-convergence progression with reality manipulation
- **Enhanced Altar Effects**: Complex multi-phase completion sequences
- **Visual Effects System**: Enhanced particles and god-specific auras
- **Transcendence System**: Post-convergence content with reality manipulation
- **Enhanced Altar Effects**: Complex particle systems and dramatic completion sequences
- **Persistent Data Storage**: YAML-based player data management
- **Comprehensive Command System**: Player and admin functionality
- **Divine Council System**: Governance system for converged players with proposal execution
- **Advanced Configuration System**: Hot-reload configuration with validation and templates
- **Cosmic Intervention System**: Reality manipulation and world-altering proposals
- **Council-Raid Integration**: Enhanced raid rewards and special council-triggered raids
- **Economic Control System**: Council oversight of server economy and progression
- **Emergency Safety Systems**: Rollback capabilities and emergency stops for dangerous proposals
- **Guild System**: Raid guilds for team formation and persistent raid groups.
- **Enhanced Raid Leaderboards**: Comprehensive tracking of raid completions and guild achievements.

### **File Structure (Current)**
```
src/main/java/com/example/minecraftplugin/
├── MinecraftPlugin.java          # Main plugin class
├── enums/                        # God types, ascension levels, titles
├── items/                        # Divine items and fragments
├── managers/                     # Core system managers
├── commands/                     # Command handlers
├── listeners/                    # Event listeners
├── effects/                      # Visual effects management
├── raids/                        # Custom raids system
├── enums/TranscendenceLevel.java # Post-convergence progression levels
├── managers/TranscendenceManager.java # Transcendence system management
├── effects/EnhancedAltarEffects.java # Complex altar completion effects
├── commands/TranscendenceCommand.java # Transcendence command handler
├── council/                      # Divine Council system
│   ├── CouncilProposal.java     # Proposal management
│   └── ProposalExecutor.java    # Proposal execution mechanics
├── managers/DivineCouncilManager.java # Council governance system
├── commands/CouncilCommand.java # Council command interface
├── config/                       # Advanced configuration system
│   ├── ConfigManager.java       # Hot-reload configuration management
│   └── ConfigValidator.java     # Configuration validation
└── world/                        # Natural altar generation
├── managers/GuildManager.java    # Guild system management
├── commands/GuildCommand.java    # Guild command handler
```

## 🎮 Gameplay Flow

1. **Exploration Phase**: Players explore, open chests, fight dangerous mobs.
2. **Collection Phase**: Gather 7 unique fragments for chosen god(s).
3. **Reunification Phase**: Locate appropriate altar and complete testament.
4. **Ascension Phase**: Gain divine powers and strategic advantages.
5. **Mastery Phase**: Complete multiple testaments for ultimate power.
6. **Conflict Resolution**: Navigate divine conflicts and strategic choices.
7. **CONVERGENCE PHASE**: Master all 12 gods to achieve ultimate divinity.
8. **Post-Convergence**: Access to Convergence Raids and ultimate challenges.
9. **Transcendence Phase**: Progress through reality manipulation challenges.
10. **Divine Council Phase**: Join cosmic governance and shape server destiny (if enabled).
11. **Guild Formation**: Create or join raid guilds for enhanced teamwork and competition.
12. **Upgrade Divine Items** through the Divine Forge system to unlock enhanced powers and legendary variants!
13. **Combine Divine Items** to create unique artifacts with powers from multiple gods!

## 🌟 End Game Content

### **Testament Completion Rewards (All Divine Items Implemented)**
- **Fallen God**: Heart of the Fallen God (ultimate survivability).
- **Banishment God**: Scepter of Banishment (fire and exile powers).
- **Abyssal God**: Trident of the Abyss (water mastery).
- **Sylvan God**: Staff of Sylvan Growth (nature and growth).
- **Tempest God**: Wings of Tempest (sky and flight).
- **Veil God**: Orb of Veil Reality (reality manipulation).
- **Forge God**: Mace of Divine Forging (crafting and repair).
- **Void God**: Void Walker's Blade (phase shifting and void teleportation).
- **Time God**: Chronos Staff (time dilation and temporal effects).
- **Blood God**: Crimson Blade (blood frenzy and berserker rage).
- **Crystal God**: Resonance Crystal (sonic boom and ore sense).
- **Shadow God**: Shadow Mantle (shadow form and darkness mastery).

### **Ultimate Rewards (Implemented)**
- **Divine Convergence**: Master all 12 gods to receive the Convergence Nexus
- **Convergence Nexus**: Ultimate divine item with all powers combined
- **Custom Raids**: Access to Convergence-tier raids including Eternal Crucible
- **Transcendent Status**: 30 hearts, reality manipulation, server-wide recognition
- **Divine Council Access**: Governance participation for cosmic decision-making
- **Cosmic Intervention Powers**: Reality manipulation and world-shaping abilities (if council enabled)
- **Economic Governance**: Control over server economy and progression rates (if council enabled)
- **Transcendence Progression**: Reality Shaper → Cosmic Architect → Dimensional Sovereign → Universal Deity
- **Guild Leadership**: Lead raid guilds to victory and climb guild leaderboards

## 🚀 Getting Started

1. **Install** the plugin.
2. **Explore** to find fragments (start with chests and mob drops).
3. **Collect** 7 fragments from your chosen god.
4. **Locate** the appropriate altar (check `Godlex` for altar center blocks).
5. **Complete** your first testament by right-clicking the altar center.
6. **Ascend** through divine power levels.
7. **Master** multiple gods for ultimate abilities.
8. **Navigate** divine conflicts strategically.
9. **ACHIEVE CONVERGENCE** by completing all 12 testaments for ultimate power!
10. **Access Raids** for additional challenges and rewards.
11. **Access Raids** for additional challenges and rewards.
12. **Transcend Reality** through post-convergence challenges and cosmic abilities!
13. **Join the Divine Council** to govern cosmic affairs and shape server destiny!
13. **Form or Join a Guild** to tackle raids with persistent teams and compete on leaderboards!

## 🎯 Design Philosophy

1. **Rarity Creates Value**: Low drop rates make fragments precious.
2. **Exploration Rewards**: Dangerous areas have better fragment chances.
3. **Strategic Depth**: Testament conflicts create tactical decisions.
4. **Long-term Engagement**: Multiple testaments provide extended goals.
5. **Server Community**: Epic announcements create shared experiences.
6. **Balanced Power**: Each god offers unique advantages without being overpowered.
7. **Conflict Resolution**: Divine powers have meaningful trade-offs.
8. **Immersive Experience**: Rich effects and feedback enhance gameplay.
9. **Ultimate Achievement**: Divine Convergence provides the ultimate long-term goal.
10. **Endless Progression**: Transcendence system provides infinite advancement beyond convergence.
11. **Epic Moments**: Server-wide celebrations for legendary achievements.
12. **Democratic Governance**: Divine Council enables player-driven server management.
13. **Progressive Enhancement**: Divine Forge provides endless item improvement and customization.
14. **Legendary Discovery**: Rare variants create excitement and unique player experiences.

## 🔮 Future Development & Roadmap

This section outlines remaining features and enhancements planned for future updates.

### **Planned Major Features**
- **Database Integration**: MySQL support for large servers with cross-server data synchronization.
- **Cross-Server Raids**: Multi-server raid participation with global leaderboards.
- **Enhanced Ender Dragon System**: A challenging boss with difficulty scaling, phase combat, and unique abilities.
- **Strategic PvP Balance**: Including a Bounty System and enhanced PvP mechanics.
- **Dynamic Boss Scaling**: Automatic scaling of all god bosses based on server difficulty.
- **Server Broadcasting System**: Coordinate broadcasting for testament completions, convergence, and other events.
- **World Events & Divine Interventions**: Rare world events like god manifestations and divine storms.
- **Advanced Combat & PvP**: Divine arenas, dragon coliseums, and testament tournaments.
- **Roleplay & Lore Expansion**: God avatars, divine quests, and dynamic storylines.
- **Customization & Content**: Custom god creators, testament modifiers, and dynamic altars.
- **Community Features**: Testament mentorship, fragment trading markets, and guild systems.
- **Integration & Compatibility**: Discord bot integration, Twitch integration, and VR support.
- **Cross-Server Council**: Multi-server Divine Council coordination and governance.
- **Advanced Reality Manipulation**: More sophisticated world-shaping tools and safeguards.
- **Council Expansion**: Additional proposal types and governance mechanics.

### **Community-Requested Features**
- Testament Pets, Dragon Companions, Divine Crafting, Seasonal Gods, Testament Leaderboards, Divine Music.

### **Transcendence System (Complete)**
Beyond Divine Convergence lies the path of Transcendence - ultimate progression for those who have mastered all gods:

#### **Transcendence Levels**
- **Reality Shaper**: Manipulate the fabric of existence itself
- **Cosmic Architect**: Design and create new realms and dimensions  
- **Dimensional Sovereign**: Rule over multiple universes
- **Universal Deity**: Command the very forces of creation

#### **Transcendence Challenges**
- **Reality Forge**: Create pocket dimensions using pure will
- **Time Mastery**: Demonstrate complete control over temporal flow
- **Void Walking**: Travel between dimensions without portals
- **Realm Creation**: Design and build entirely new realms
- **Star Forging**: Create new stars and solar systems
- **Life Genesis**: Bring new forms of sentient life into existence
- **Multiverse Nexus**: Connect and rule multiple universes
- **Entropy Reversal**: Reverse the heat death of universes
- **Creation Mastery**: Create new multiverses from nothing
- **Omnipresence**: Exist simultaneously across all realities

#### **Transcendence Abilities**
- **Reality Manipulation**: Enhanced block manipulation and matter transmutation
- **Space Folding**: Advanced teleportation and dimensional travel
- **Cosmic Architecture**: Ability to design new realms and modify physics
- **Life Creation**: Power to bring new beings into existence
- **Multiverse Travel**: Journey between parallel universes
- **Dimensional Control**: Rule over multiple realities simultaneously
- **Omnipotence**: Unlimited power over all existence
- **Omniscience**: Knowledge of all things across all realities
- **Omnipresence**: Exist everywhere simultaneously

### **Divine Forge System (Complete)**
The ultimate crafting system for divine item enhancement and combination:

#### **Upgrade System**
- **Five Rarity Tiers**: Divine → Enhanced → Legendary → Mythic → Transcendent
- **Progressive Enhancement**: Each tier provides significant power increases
- **Success Rates**: Balanced risk/reward with decreasing success chances
- **Material Requirements**: Unique materials for each upgrade tier

#### **Combination System**
- **Compatible Combinations**: Specific god pairs create unique artifacts
- **Fusion Catalysts**: Required materials for combining divine items
- **Combined Powers**: Artifacts inherit and enhance abilities from source items
- **Ultimate Combinations**: Triple-god combinations for legendary artifacts

### **Long-Term Vision**
To create the most comprehensive divine power progression system in Minecraft, expanding the universe, building community, providing endless progression, fostering creative expression, and ensuring cross-platform unity.

## 📊 Performance & Compatibility

- **Minimal Impact**: Efficient caching and async operations.
- **Thread Safe**: All implementations designed for server stability.
- **Balanced Rates**: Spawn rates tuned for long-term engagement.
- **Scalable**: Supports multiple players and extensive gameplay.
- **Performance Optimized**: Caching system and async operations for better server performance.
- **Visual Effects**: Enhanced particle systems and god-specific auras.

## 🔍 Troubleshooting (Current)

### **Altars Not Generating**
- Check natural generation settings in `config.yml` under `testament.altars.natural_generation`.
- Ensure the correct biomes are available for the desired god types.
- Verify minimum distance requirements between altars.
- Use `/altar stats` to check generation statistics.

### **Fragments Not Spawning**
- Ensure you meet the conditions (e.g., open chests, kill dangerous mobs).
- Check cooldown timers in `config.yml`.
- Verify plugin permissions.

### **Testament Not Completing**
- Ensure you have all 7 fragments in your inventory for the specific god.
- Right-click the correct altar center block for that god.
- Verify the complete altar structure is built correctly.
- Check console for debug messages.

### **Heart/Forge Conflict**
- The plugin automatically detects and resolves conflicts between the Heart of the Fallen God and the Mace of Divine Forging.
- Conflicting items are automatically removed and dropped at your location.
- Check `testament.conflicts` settings in `config.yml` to modify this behavior.

### **Lives System Issues**
- Check `testament.lives_system` settings in `config.yml`.
- Use `/testament lives` to check your status.
- Admin commands (`/testament admin setdeaths`, `/testament admin setvoid`, `/testament admin resetplayer`) can be used for management.

### **Tutorial Not Starting**
- Check `tutorial.enabled` and `auto_start_for_new_players` in `config.yml`.
- Use `/tutorial start` to manually begin the tutorial.
- Use `/tutorial reset` if the tutorial becomes stuck.

### **Performance Issues**
- Enable performance optimization in `config.yml` under `performance`.
- Adjust `effects.particle_density` to reduce visual effects load.
- Check `/altar stats` and raid statistics for resource usage.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Transform your Minecraft server into an epic realm where players forge their own divine destinies through strategic choices, divine conflicts, ultimate power, and endless item progression! Every testament completion becomes a server-wide event, every divine item can be enhanced to legendary status, and every achievement is a step towards becoming a supreme deity with reality-bending artifacts!**