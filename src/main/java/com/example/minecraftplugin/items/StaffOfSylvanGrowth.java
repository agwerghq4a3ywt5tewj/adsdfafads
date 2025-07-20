package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff of Sylvan Growth - Divine item for the Sylvan God
 * 
 * Abilities:
 * - Passive: Regeneration, increased crop growth nearby
 * - Active: Right-click to instantly grow crops in a large area
 * - Theme: Forests, nature, growth
 */
public class StaffOfSylvanGrowth extends DivineItem {
    
    private static final int GROWTH_RADIUS = 7;
    private static final int COOLDOWN_SECONDS = 30;
    
    public StaffOfSylvanGrowth() {
        super(
            GodType.SYLVAN,
            Material.STICK,
            "§a§l🌿 Staff of Sylvan Growth 🌿§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A living staff that pulses with the heartbeat",
            "§7of the ancient forests and endless growth.",
            "",
            "§a§lPassive Abilities:",
            "§7• Regeneration II",
            "§7• Increased crop growth nearby",
            "§7• Immunity to poison",
            "",
            "§a§lActive Ability:",
            "§7• Right-click to instantly grow crops",
            "§7• Affects all crops within 7 blocks",
            "§7• Cooldown: §f30 seconds",
            "",
            "§8\"In every leaf, the promise of tomorrow.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.EFFICIENCY, 5);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        int grownCount = 0;
        
        // Get all blocks in radius
        for (int x = -GROWTH_RADIUS; x <= GROWTH_RADIUS; x++) {
            for (int y = -GROWTH_RADIUS; y <= GROWTH_RADIUS; y++) {
                for (int z = -GROWTH_RADIUS; z <= GROWTH_RADIUS; z++) {
                    Block block = player.getLocation().add(x, y, z).getBlock();
                    
                    if (isCrop(block)) {
                        if (growCrop(block)) {
                            grownCount++;
                        }
                    }
                }
            }
        }
        
        if (grownCount == 0) {
            player.sendMessage("§a§l🌿 Staff of Sylvan Growth: §r§cNo crops found to grow!");
            return false;
        }
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
        
        // Messages
        player.sendMessage("§a§l🌿 DIVINE GROWTH! 🌿");
        player.sendMessage("§7Accelerated growth of §f" + grownCount + "§7 crops!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply regeneration
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
        
        // Apply poison immunity
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            player.removePotionEffect(PotionEffectType.POISON);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§a§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Staff of Sylvan Growth pulses with natural energy!");
        player.sendMessage("§7You feel the life force of nature flowing through you.");
        player.sendMessage("§7Right-click to accelerate crop growth.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Staff of Sylvan Growth is no longer with you.");
        player.sendMessage("§7The natural energy dissipates.");
        
        // Remove regeneration if it was from this item
        if (player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Check if a block is a crop
     */
    private boolean isCrop(Block block) {
        Material type = block.getType();
        return type == Material.WHEAT ||
               type == Material.CARROTS ||
               type == Material.POTATOES ||
               type == Material.BEETROOTS ||
               type == Material.NETHER_WART ||
               type == Material.COCOA ||
               type == Material.SWEET_BERRY_BUSH ||
               type == Material.BAMBOO_SAPLING ||
               type == Material.BAMBOO;
    }
    
    /**
     * Grow a crop to its next stage
     */
    private boolean growCrop(Block block) {
        BlockData data = block.getBlockData();
        
        if (data instanceof Ageable) {
            Ageable ageable = (Ageable) data;
            if (ageable.getAge() < ageable.getMaximumAge()) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
                return true;
            }
        }
        
        return false;
    }
}