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
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Void Walker's Blade - Divine item for the Void God
 * 
 * Abilities:
 * - Passive: Phase through blocks when sneaking, void immunity
 * - Active: Right-click to teleport 10 blocks forward through any material
 * - Theme: Emptiness, teleportation, phase shifting
 */
public class VoidWalkersBlade extends DivineItem {
    
    private static final int TELEPORT_DISTANCE = 10;
    private static final int COOLDOWN_SECONDS = 5;
    
    public VoidWalkersBlade() {
        super(
            GodType.VOID,
            Material.NETHERITE_SWORD,
            "§8§l⚫ Void Walker's Blade ⚫§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A blade forged from the essence of nothingness,",
            "§7cutting through reality itself with ease.",
            "",
            "§8§lPassive Abilities:",
            "§7• Phase through blocks while sneaking",
            "§7• Immunity to void damage",
            "§7• Attacks ignore 50% of armor",
            "",
            "§8§lActive Ability:",
            "§7• Right-click to void rip teleport",
            "§7• Teleports 10 blocks forward",
            "§7• Phases through any material",
            "§7• Cooldown: §f5 seconds",
            "",
            "§8\"In nothingness, find everything.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.SHARPNESS, 5);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        enchants.put(Enchantment.SWEEPING_EDGE, 3);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Calculate teleport location
        Vector direction = player.getLocation().getDirection().normalize();
        Location targetLocation = player.getLocation().add(direction.multiply(TELEPORT_DISTANCE));
        
        // Find safe location (adjust Y if needed)
        while (targetLocation.getBlock().getType().isSolid() && targetLocation.getY() < 320) {
            targetLocation.add(0, 1, 0);
        }
        
        // Ensure we don't teleport into the void
        if (targetLocation.getY() < -64) {
            targetLocation.setY(-63);
        }
        
        // Teleport player
        player.teleport(targetLocation);
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.8f, 0.8f);
        
        // Temporary invisibility after teleport
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
        
        // Messages
        player.sendMessage("§8§l⚫ VOID RIP! ⚫");
        player.sendMessage("§7You phase through reality itself!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply void immunity (no fall damage from void)
        if (player.getLocation().getY() < -64) {
            player.setFallDistance(0);
        }
        
        // Apply night vision for void walking
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§8§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Void Walker's Blade phases in and out of reality!");
        player.sendMessage("§7You can now walk between dimensions.");
        player.sendMessage("§7Right-click to void rip teleport, sneak to phase.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.8f, 0.5f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Void Walker's Blade is no longer with you.");
        player.sendMessage("§7Reality solidifies around you.");
        
        // Remove void effects
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}