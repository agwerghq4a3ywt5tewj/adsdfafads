package com.example.minecraftplugin.items.combinations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;

/**
 * Represents a combined divine item with powers from multiple gods
 */
public class CombinedDivineItem extends DivineItem {

    private final List<GodType> sourceGods;
    private final String itemName;

    public CombinedDivineItem(String itemName, Material material, String displayName,
                              List<String> lore, List<GodType> sourceGods) {
        super(
            null, // Combined items don't have a single god type
            material,
            displayName,
            lore,
            createCombinedEnchantments(),
            true
        );
        this.itemName = itemName;
        this.sourceGods = sourceGods;
    }

    private static Map<Enchantment, Integer> createCombinedEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.UNBREAKING, 15);
        enchants.put(Enchantment.MENDING, 3);
        enchants.put(Enchantment.LOOTING, 5);
        return enchants;
    }

    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        switch (itemName) {
            case "Steam Lord's Regalia":
                return executeSteamLordAbility(player);
            case "Worldtree Crown":
                return executeWorldtreeCrownAbility(player);
            case "Genesis Void Hammer":
                return executeGenesisVoidHammerAbility(player);
            case "Chronos Reality Orb":
                return executeChronosRealityOrbAbility(player);
            case "Harmonic Chaos Blade":
                return executeHarmonicChaosBlade(player);
            case "Twilight Sovereign Mantle":
                return executeTwilightSovereignMantle(player);
            case "Oblivion Crown":
                return executeOblivionCrown(player);
            default:
                return false;
        }
    }

    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        for (GodType god : sourceGods) {
            applyGodPassiveEffects(player, god);
        }
        applyCombinationBonuses(player);
    }

    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l★ DIVINE COMBINATION ACHIEVED! ★");
        player.sendMessage("§7You have forged " + getDisplayName() + "!");
        player.sendMessage("§7The powers of multiple gods flow as one!");
        player.sendMessage("§7Source Gods: §f" + getSourceGodsString());

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
    }

    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine combination fades... §r§c" + getDisplayName() + " is no longer with you.");
        player.sendMessage("§7The unified divine powers separate.");
        removeCombinedEffects(player);
    }

    @Override
    public int getCooldownSeconds() {
        return 45;
    }

    private void applyGodPassiveEffects(Player player, GodType god) {
        switch (god) {
            case FALLEN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, false, false));
                break;
            case BANISHMENT:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
                break;
            case ABYSSAL:
                player.getWorld().spawnParticle(Particle.DRIPPING_WATER, player.getLocation(), 3, 0.2, 0.2, 0.2, 0.1);
                break;
            case SYLVAN:
                if (player.hasPotionEffect(PotionEffectType.POISON)) {
                    player.removePotionEffect(PotionEffectType.POISON);
                }
                break;
            case TEMPEST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
                break;
            case VEIL:
                if (player.isSneaking()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
                }
                break;
        }
    }

    private void applyCombinationBonuses(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 60, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 60, 1, false, false));
    }

    private void removeCombinedEffects(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
    }

    private String getSourceGodsString() {
        List<String> godNames = new ArrayList<>();
        for (GodType god : sourceGods) {
            godNames.add(god.getDisplayName());
        }
        return String.join(", ", godNames);
    }

    public List<GodType> getSourceGods() {
        return sourceGods;
    }

    public String getItemName() {
        return itemName;
    }

    // Ability method stubs
    private boolean executeSteamLordAbility(Player player) { return false; }
    private boolean executeWorldtreeCrownAbility(Player player) { return false; }
    private boolean executeGenesisVoidHammerAbility(Player player) { return false; }
    private boolean executeChronosRealityOrbAbility(Player player) { return false; }
    private boolean executeHarmonicChaosBlade(Player player) { return false; }
    private boolean executeTwilightSovereignMantle(Player player) { return false; }
    private boolean executeOblivionCrown(Player player) { return false; }
}