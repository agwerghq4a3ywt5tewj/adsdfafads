package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
 * Orb of Veil Reality - Divine item for the Veil God
 * 
 * Abilities:
 * - Passive: Invisibility when sneaking, magic resistance
 * - Active: Right-click to teleport to where you're looking
 * - Theme: Reality manipulation, void magic
 */
public class OrbOfVeilReality extends DivineItem {
    
    private static final int TELEPORT_RANGE = 32;
    private static final int COOLDOWN_SECONDS = 15;
    
    public OrbOfVeilReality() {
        super(
            GodType.VEIL,
            Material.ENDER_PEARL,
            "§5§l👁 Orb of Veil Reality 👁§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A swirling orb that bends reality itself,",
            "§7revealing the spaces between worlds.",
            "",
            "§5§lPassive Abilities:",
            "§7• Invisibility while sneaking",
            "§7• Resistance to magic effects",
            "§7• See through illusions",
            "",
            "§5§lActive Ability:",
            "§7• Right-click to teleport forward",
            "§7• Range: §f32 blocks",
            "§7• Cooldown: §f15 seconds",
            "",
            "§8\"Reality is merely a suggestion.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Get target location
        Location targetLocation = player.getTargetBlock(null, TELEPORT_RANGE).getLocation();
        
        // Adjust to safe teleport location (above the block)
        targetLocation.add(0.5, 1, 0.5);
        targetLocation.setDirection(player.getLocation().getDirection());
        
        // Check if location is safe
        if (!isSafeLocation(targetLocation)) {
            player.sendMessage("§5§l👁 Orb of Veil Reality: §r§cCannot teleport to unsafe location!");
            return false;
        }
        
        // Teleport player
        player.teleport(targetLocation);
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.5f);
        
        // Temporary invisibility after teleport
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        
        // Messages
        player.sendMessage("§5§l👁 REALITY SHIFT! 👁");
        player.sendMessage("§7You bend space to your will!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply invisibility when sneaking
        if (player.isSneaking()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
        }
        
        // Apply resistance to magic effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false));
        
        // Remove negative magic effects
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (player.hasPotionEffect(PotionEffectType.NAUSEA)) {
            player.removePotionEffect(PotionEffectType.NAUSEA);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Orb of Veil Reality warps space around you!");
        player.sendMessage("§7Reality bends to your will.");
        player.sendMessage("§7Right-click to teleport, sneak for invisibility.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Orb of Veil Reality is no longer with you.");
        player.sendMessage("§7Reality returns to normal.");
        
        // Remove veil effects
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Check if a location is safe for teleportation
     */
    private boolean isSafeLocation(Location location) {
        // Check if the location and the block above are air or passable
        Material blockType = location.getBlock().getType();
        Material aboveType = location.clone().add(0, 1, 0).getBlock().getType();
        
        return (blockType == Material.AIR || !blockType.isSolid()) &&
               (aboveType == Material.AIR || !aboveType.isSolid());
    }
}