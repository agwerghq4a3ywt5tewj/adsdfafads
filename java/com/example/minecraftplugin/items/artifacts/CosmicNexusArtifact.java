package com.example.minecraftplugin.items.artifacts;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * Cosmic Nexus Artifact - Ultimate artifact containing all 12 gods
 */
public class CosmicNexusArtifact extends UltimateArtifact {
    
    public CosmicNexusArtifact() {
        super(
            "§0§l🌌 Cosmic Nexus Artifact 🌌§r",
            Material.NETHER_STAR,
            createCosmicLore(),
            Arrays.asList(GodType.values()), // All 12 gods
            "Cosmic Nexus",
            10 // Maximum power level
        );
    }
    
    private static List<String> createCosmicLore() {
        return Arrays.asList(
            "§7The ultimate convergence of all divine power,",
            "§7containing the essence of every fallen god.",
            "§7This artifact transcends the boundaries of",
            "§7mortality and grants cosmic omnipotence.",
            "",
            "§0§lCosmic Abilities:",
            "§7• Reality Rewrite: Reshape existence itself",
            "§7• Temporal Dominion: Control time flow",
            "§7• Dimensional Mastery: Rule over all realms",
            "§7• Life and Death: Command creation and destruction",
            "§7• Omnipotent Aura: Affect all nearby entities",
            "",
            "§0§lUltimate Ability:",
            "§7• Right-click for Cosmic Convergence",
            "§7• Affects entire world with divine power",
            "§7• Reshapes reality on a massive scale",
            "§7• Cooldown: §f2 minutes",
            "",
            "§8\"I am become cosmos, reshaper of worlds.\""
        );
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        return executeUltimateAbility(player, item);
    }
    
    @Override
    protected boolean executeUltimateAbility(Player player, ItemStack item) {
        Location center = player.getLocation();
        
        // Phase 1: Cosmic Awakening
        executeCosmicAwakening(player, center);
        
        // Phase 2: Reality Rewrite (delayed)
        // Note: plugin reference not available in item classes
        org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            executeRealityRewrite(player, center);
        }, 60L); // 3 seconds
        
        // Phase 3: Dimensional Convergence (delayed)
        org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            executeDimensionalConvergence(player, center);
        }, 120L); // 6 seconds
        
        // Phase 4: Cosmic Restoration (delayed)
        org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            executeCosmicRestoration(player, center);
        }, 180L); // 9 seconds
        
        return true;
    }
    
    /**
     * Get plugin instance (workaround for item classes)
     */
    private org.bukkit.plugin.Plugin getPlugin() {
        return org.bukkit.Bukkit.getPluginManager().getPlugin("MinecraftPlugin");
    }
    
    /**
     * Phase 1: Cosmic Awakening
     */
    private void executeCosmicAwakening(Player player, Location center) {
        // Grant player temporary cosmic powers
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 9)); // 1 minute
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 9));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 9));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 1200, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 19)); // 20 extra hearts
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 9));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 1200, 9));
        
        // Cosmic visual effects
        for (int i = 0; i < 100; i++) {
            double angle = (i / 100.0) * 360;
            double radius = 10.0;
            double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
            double z = center.getZ() + radius * Math.sin(Math.toRadians(angle));
            Location effectLoc = new Location(center.getWorld(), x, center.getY() + 5, z);
            
            center.getWorld().spawnParticle(Particle.END_ROD, effectLoc, 5, 0.5, 0.5, 0.5, 0.1);
            center.getWorld().spawnParticle(Particle.DRAGON_BREATH, effectLoc, 3, 0.3, 0.3, 0.3, 0.05);
        }
        
        // Sound effects
        player.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.1f);
        player.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 2.0f, 0.3f);
        
        // Messages
        player.sendMessage("§0§l🌌 COSMIC AWAKENING! 🌌");
        player.sendMessage("§7The power of the cosmos flows through you!");
        
        // Server announcement
        org.bukkit.Bukkit.broadcastMessage("§0§l" + player.getName() + " §r§0has awakened the Cosmic Nexus Artifact!");
        org.bukkit.Bukkit.broadcastMessage("§7Reality itself trembles before their cosmic power!");
    }
    
    /**
     * Phase 2: Reality Rewrite
     */
    private void executeRealityRewrite(Player player, Location center) {
        // Affect large area around player
        int radius = 50;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -10; y <= 10; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    
                    // Only affect blocks within sphere
                    if (blockLoc.distance(center) <= radius) {
                        // Random reality alterations
                        if (Math.random() < 0.01) { // 1% chance per block
                            alterReality(blockLoc);
                        }
                    }
                }
            }
        }
        
        // Affect all entities in range
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity target = (LivingEntity) entity;
                
                // Apply cosmic effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1200, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 2));
                
                // Heal friendly mobs, damage hostile ones
                if (isFriendlyMob(target)) {
                    target.setHealth(target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
                } else {
                    target.damage(20.0, player);
                }
            } else if (entity instanceof Player && entity != player) {
                Player otherPlayer = (Player) entity;
                
                // Grant temporary cosmic blessing to other players
                otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 2));
                otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 4));
                otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 600, 2));
                
                otherPlayer.sendMessage("§0§l🌌 COSMIC BLESSING! 🌌");
                otherPlayer.sendMessage("§7You have been blessed by cosmic power!");
            }
        }
        
        // Reality distortion effects
        for (int i = 0; i < 200; i++) {
            Location randomLoc = center.clone().add(
                (Math.random() - 0.5) * radius * 2,
                (Math.random() - 0.5) * 20,
                (Math.random() - 0.5) * radius * 2
            );
            
            center.getWorld().spawnParticle(Particle.PORTAL, randomLoc, 10, 1, 1, 1, 0.3);
            center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, randomLoc, 5, 0.5, 0.5, 0.5, 0.2);
        }
        
        player.sendMessage("§0§l🌌 REALITY REWRITE! 🌌");
        player.sendMessage("§7You have reshaped reality across " + radius + " blocks!");
    }
    
    /**
     * Phase 3: Dimensional Convergence
     */
    private void executeDimensionalConvergence(Player player, Location center) {
        // Create dimensional rifts
        for (int i = 0; i < 12; i++) { // One for each god
            double angle = (i / 12.0) * 360;
            double radius = 25.0;
            double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
            double z = center.getZ() + radius * Math.sin(Math.toRadians(angle));
            Location riftLoc = new Location(center.getWorld(), x, center.getY() + 10, z);
            
            createDimensionalRift(riftLoc, GodType.values()[i]);
        }
        
        // Lightning strikes for each god
        for (int i = 0; i < 12; i++) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                Location strikeLoc = center.clone().add(
                    (Math.random() - 0.5) * 40,
                    0,
                    (Math.random() - 0.5) * 40
                );
                center.getWorld().strikeLightningEffect(strikeLoc);
            }, i * 5L);
        }
        
        player.sendMessage("§0§l🌌 DIMENSIONAL CONVERGENCE! 🌌");
        player.sendMessage("§7All divine realms converge at your location!");
    }
    
    /**
     * Phase 4: Cosmic Restoration
     */
    private void executeCosmicRestoration(Player player, Location center) {
        // Restore and enhance the area
        int radius = 30;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location surfaceLoc = center.clone().add(x, 0, z);
                
                // Find surface level
                for (int y = center.getBlockY() + 10; y >= center.getBlockY() - 10; y--) {
                    surfaceLoc.setY(y);
                    if (surfaceLoc.getBlock().getType().isSolid()) {
                        surfaceLoc.setY(y + 1);
                        break;
                    }
                }
                
                // Enhance surface with cosmic materials
                if (surfaceLoc.distance(center) <= radius && Math.random() < 0.05) {
                    enhanceWithCosmicMaterials(surfaceLoc);
                }
            }
        }
        
        // Final cosmic burst
        center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 20);
        center.getWorld().spawnParticle(Particle.FIREWORK, center, 200, 15, 15, 15, 0.5);
        center.getWorld().spawnParticle(Particle.END_ROD, center, 100, 10, 10, 10, 0.3);
        
        player.sendMessage("§0§l🌌 COSMIC RESTORATION! 🌌");
        player.sendMessage("§7The cosmos has been reshaped by your will!");
        
        // Final server announcement
        org.bukkit.Bukkit.broadcastMessage("§0§l" + player.getName() + " §r§0has completed the Cosmic Convergence!");
        org.bukkit.Bukkit.broadcastMessage("§7Reality has been permanently altered by cosmic power!");
    }
    
    /**
     * Alter reality at specific location
     */
    private void alterReality(Location location) {
        Material currentMaterial = location.getBlock().getType();
        
        // Reality alteration rules
        if (currentMaterial == Material.STONE) {
            location.getBlock().setType(Material.DIAMOND_ORE);
        } else if (currentMaterial == Material.DIRT) {
            location.getBlock().setType(Material.GRASS_BLOCK);
        } else if (currentMaterial == Material.WATER) {
            location.getBlock().setType(Material.ICE);
        } else if (currentMaterial == Material.LAVA) {
            location.getBlock().setType(Material.OBSIDIAN);
        } else if (currentMaterial == Material.AIR) {
            // Randomly create valuable blocks
            Material[] valuableBlocks = {
                Material.GOLD_ORE, Material.IRON_ORE, Material.DIAMOND_ORE,
                Material.EMERALD_ORE, Material.ANCIENT_DEBRIS
            };
            location.getBlock().setType(valuableBlocks[(int) (Math.random() * valuableBlocks.length)]);
        }
    }
    
    /**
     * Create dimensional rift
     */
    private void createDimensionalRift(Location location, GodType god) {
        // Create god-specific rift effects
        for (int i = 0; i < 50; i++) {
            Location riftPoint = location.clone().add(
                (Math.random() - 0.5) * 4,
                (Math.random() - 0.5) * 4,
                (Math.random() - 0.5) * 4
            );
            
            switch (god) {
                case FALLEN:
                    location.getWorld().spawnParticle(Particle.SOUL, riftPoint, 3, 0.2, 0.2, 0.2, 0.05);
                    break;
                case BANISHMENT:
                    location.getWorld().spawnParticle(Particle.FLAME, riftPoint, 3, 0.2, 0.2, 0.2, 0.05);
                    break;
                case ABYSSAL:
                    location.getWorld().spawnParticle(Particle.FALLING_DRIPSTONE_WATER, riftPoint, 3, 0.2, 0.2, 0.2, 0.1);
                    break;
                case TEMPEST:
                    location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, riftPoint, 3, 0.2, 0.2, 0.2, 0.05);
                    break;
                default:
                    location.getWorld().spawnParticle(Particle.PORTAL, riftPoint, 3, 0.2, 0.2, 0.2, 0.1);
                    break;
            }
        }
    }
    
    /**
     * Enhance location with cosmic materials
     */
    private void enhanceWithCosmicMaterials(Location location) {
        Material[] cosmicMaterials = {
            Material.BEACON, Material.CONDUIT, Material.END_CRYSTAL,
            Material.DRAGON_EGG, Material.NETHER_STAR, Material.HEART_OF_THE_SEA
        };
        
        Material enhancement = cosmicMaterials[(int) (Math.random() * cosmicMaterials.length)];
        
        if (location.getBlock().getType() == Material.AIR) {
            location.getBlock().setType(enhancement);
            
            // Add cosmic particle effect
            location.getWorld().spawnParticle(Particle.END_ROD, location, 10, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    /**
     * Check if entity is friendly
     */
    private boolean isFriendlyMob(LivingEntity entity) {
        return entity instanceof org.bukkit.entity.Animals ||
               entity instanceof org.bukkit.entity.Villager ||
               entity instanceof org.bukkit.entity.IronGolem ||
               entity instanceof org.bukkit.entity.Allay;
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        // Call parent implementation manually since we can't use super in this context
        player.sendMessage("§c§lDivine power fades... §r§c" + getDisplayName() + " is no longer with you.");
        
        player.sendMessage("§0§lCosmic power fades... §r§0The universe returns to normal.");
        
        // Remove cosmic effects
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.LUCK);
    }
}