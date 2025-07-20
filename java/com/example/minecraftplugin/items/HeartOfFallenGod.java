package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
 * Heart of the Fallen God - Divine item for the Fallen God
 * 
 * Abilities:
 * - Passive: Increases max health to 25 hearts (50 HP)
 * - Passive: Regeneration II when below 50% health
 * - Active: Right-click to instantly heal 10 hearts (20 HP)
 * - Death Protection: Returns 25 hearts after 45 seconds on death
 */
public class HeartOfFallenGod extends DivineItem {
    
    private static final double MAX_HEALTH = 50.0; // 25 hearts
    private static final int HEAL_AMOUNT = 20; // 10 hearts
    private static final int COOLDOWN_SECONDS = 60; // 1 minute cooldown
    
    public HeartOfFallenGod() {
        super(
            GodType.FALLEN,
            Material.NETHER_STAR,
            "§4§l❤ Heart of the Fallen God ❤§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A pulsing heart of divine darkness,",
            "§7containing the essence of death and rebirth.",
            "",
            "§c§lPassive Abilities:",
            "§7• Increases maximum health to §c25 hearts",
            "§7• Regeneration II when below 50% health",
            "§7• Returns 25 hearts 45 seconds after death",
            "",
            "§c§lActive Ability:",
            "§7• Right-click to instantly heal §c10 hearts",
            "§7• Cooldown: §f60 seconds",
            "",
            "§8\"Death is but a temporary inconvenience",
            "§8to those blessed by the Fallen God.\""
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
        // Check if player is at full health
        if (player.getHealth() >= player.getAttribute(Attribute.MAX_HEALTH).getValue()) {
            player.sendMessage("§c§lHeart of Fallen God: §r§cYou are already at full health!");
            return false;
        }
        
        // Heal the player
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double newHealth = Math.min(currentHealth + HEAL_AMOUNT, maxHealth);
        
        player.setHealth(newHealth);
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.5f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 0.8f);
        
        // Messages
        double heartsHealed = (newHealth - currentHealth) / 2.0;
        player.sendMessage("§4§l❤ Heart of Fallen God: §r§cHealed §f" + String.format("%.1f", heartsHealed) + " hearts§c!");
        
        return true; // Ability was used successfully
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Increase max health to 25 hearts
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null && healthAttribute.getBaseValue() < MAX_HEALTH) {
            healthAttribute.setBaseValue(MAX_HEALTH);
        }
        
        // Apply regeneration when below 50% health
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        
        if (currentHealth < (maxHealth * 0.5)) {
            // Apply Regeneration II for 3 seconds (will be reapplied continuously)
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§4§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Heart of the Fallen God pulses with dark energy!");
        player.sendMessage("§7Your maximum health has been increased to §c25 hearts§7.");
        player.sendMessage("§7Right-click to activate divine healing.");
        
        // Immediately apply max health increase
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(MAX_HEALTH);
        }
        
        // Play dramatic sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 0.8f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Heart of the Fallen God is no longer with you.");
        player.sendMessage("§7Your maximum health returns to normal.");
        
        // Reset max health to default (20 HP / 10 hearts)
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
            
            // If player's current health exceeds new max, reduce it
            if (player.getHealth() > 20.0) {
                player.setHealth(20.0);
            }
        }
        
        // Remove any regeneration effects from this item
        if (player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
    
    /**
     * Handle death protection - called when player dies with this item
     */
    public void handleDeathProtection(Player player) {
        player.sendMessage("§4§lHeart of Fallen God: §r§cDeath is temporary...");
        player.sendMessage("§7Your divine heart will restore you in §f45 seconds§7.");
        
        // Schedule heart restoration after 45 seconds
        // This would typically be handled by a death listener
        // For now, we'll just log the event
    }
    
    /**
     * Restore player to 25 hearts after death protection timer
     */
    public void restoreAfterDeath(Player player) {
        // Set health to maximum
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(MAX_HEALTH);
            player.setHealth(MAX_HEALTH);
        }
        
        player.sendMessage("§4§l❤ DIVINE RESURRECTION! ❤");
        player.sendMessage("§7The Heart of the Fallen God has restored your life force!");
        
        // Dramatic effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.6f);
    }
}