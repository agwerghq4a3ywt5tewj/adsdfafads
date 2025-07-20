package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a divine fragment for a specific god
 */
public class FragmentItem extends DivineItem {
    
    private final int fragmentNumber;
    
    public FragmentItem(GodType godType, int fragmentNumber) {
        super(
            godType,
            Material.PAPER,
            getFragmentDisplayName(godType, fragmentNumber),
            getFragmentLore(godType, fragmentNumber),
            getFragmentEnchantments(),
            false
        );
        this.fragmentNumber = fragmentNumber;
    }
    
    private static String getFragmentDisplayName(GodType godType, int fragmentNumber) {
        return "§6§lFragment of " + godType.getDisplayName() + " §7(" + fragmentNumber + "/7)";
    }
    
    private static List<String> getFragmentLore(GodType godType, int fragmentNumber) {
        return Arrays.asList(
            "§7A divine fragment containing the essence",
            "§7of the " + godType.getDisplayName() + ".",
            "",
            "§7Fragment: §f" + fragmentNumber + " §7of §f7",
            "§7Theme: §f" + godType.getTheme(),
            "",
            "§e§lCollect all 7 fragments to complete",
            "§e§lthe Testament of " + godType.getDisplayName() + "!"
        );
    }
    
    private static Map<Enchantment, Integer> getFragmentEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.FORTUNE, 1); // Makes it glow
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Fragments don't have active abilities
        player.sendMessage("§7This is a divine fragment. Collect all 7 to complete the testament!");
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Fragments don't have passive effects
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§6§l✦ DIVINE FRAGMENT OBTAINED! ✦");
        player.sendMessage("§7You have found a fragment of the " + godType.getDisplayName() + "!");
        player.sendMessage("§7Fragment " + fragmentNumber + " of 7 collected.");
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine fragment lost: " + getDisplayName());
    }
    
    @Override
    public int getCooldownSeconds() {
        return 0; // No cooldown for fragments
    }
    
    public int getFragmentNumber() {
        return fragmentNumber;
    }
    
    /**
     * Check if an ItemStack is a fragment for a specific god and number
     */
    public static boolean isFragment(ItemStack item, GodType godType, int fragmentNumber) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }
        
        FragmentItem fragment = new FragmentItem(godType, fragmentNumber);
        return fragment.isDivineItem(item);
    }
    
    /**
     * Get the god type and fragment number from an ItemStack
     */
    public static FragmentInfo getFragmentInfo(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return null;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Parse the display name to extract god and fragment number
        for (GodType god : GodType.values()) {
            for (int i = 1; i <= 7; i++) {
                String expectedName = getFragmentDisplayName(god, i);
                if (displayName.equals(expectedName)) {
                    return new FragmentInfo(god, i);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Helper class to store fragment information
     */
    public static class FragmentInfo {
        private final GodType godType;
        private final int fragmentNumber;
        
        public FragmentInfo(GodType godType, int fragmentNumber) {
            this.godType = godType;
            this.fragmentNumber = fragmentNumber;
        }
        
        public GodType getGodType() {
            return godType;
        }
        
        public int getFragmentNumber() {
            return fragmentNumber;
        }
    }
}