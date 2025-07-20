package com.example.minecraftplugin.items.combinations;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.items.combinations.CombinedDivineItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Handles combining compatible divine items into new powerful artifacts
 */
public class DivineItemCombination {
    
    /**
     * Define compatible god combinations and their results
     */
    private static final Map<Set<GodType>, CombinationResult> COMBINATIONS = new HashMap<>();
    
    static {
        // Elemental Combinations
        COMBINATIONS.put(Set.of(GodType.BANISHMENT, GodType.ABYSSAL), new CombinationResult(
            "Steam Lord's Regalia", Material.CONDUIT,
            "§c§l🌊 Steam Lord's Regalia 🔥§r",
            Arrays.asList(
                "§7The perfect fusion of fire and water,",
                "§7creating devastating steam-based attacks.",
                "",
                "§e§lCombined Abilities:",
                "§7• Steam Burst: Scalding steam damage",
                "§7• Thermal Manipulation: Control temperature",
                "§7• Mist Form: Become incorporeal steam",
                "§7• Pressure Wave: Explosive steam attacks"
            ),
            Arrays.asList(GodType.BANISHMENT, GodType.ABYSSAL)
        ));
        
        COMBINATIONS.put(Set.of(GodType.SYLVAN, GodType.TEMPEST), new CombinationResult(
            "Worldtree Crown", Material.FLOWERING_AZALEA_LEAVES,
            "§a§l🌳 Worldtree Crown ⚡§r",
            Arrays.asList(
                "§7The harmony of nature and storm,",
                "§7channeling the power of the world tree.",
                "",
                "§e§lCombined Abilities:",
                "§7• Nature's Wrath: Lightning-charged vines",
                "§7• Storm Growth: Accelerated plant growth",
                "§7• Wind Seeds: Spread life with storms",
                "§7• Gaia's Thunder: Earth-shaking strikes"
            ),
            Arrays.asList(GodType.SYLVAN, GodType.TEMPEST)
        ));
        
        // Creation vs Destruction
        COMBINATIONS.put(Set.of(GodType.FORGE, GodType.VOID), new CombinationResult(
            "Genesis Void Hammer", Material.NETHERITE_AXE,
            "§6§l⚒ Genesis Void Hammer 🕳§r",
            Arrays.asList(
                "§7The ultimate paradox: creation and destruction",
                "§7unified in a single, reality-shaping tool.",
                "",
                "§e§lCombined Abilities:",
                "§7• Create or Destroy: Build or unmake reality",
                "§7• Void Forging: Craft from nothingness",
                "§7• Reality Hammer: Reshape existence",
                "§7• Genesis Strike: Create new dimensions"
            ),
            Arrays.asList(GodType.FORGE, GodType.VOID)
        ));
        
        // Time and Space
        COMBINATIONS.put(Set.of(GodType.TIME, GodType.VEIL), new CombinationResult(
            "Chronos Reality Orb", Material.CLOCK,
            "§d§l⏰ Chronos Reality Orb 👁§r",
            Arrays.asList(
                "§7Mastery over time and space combined,",
                "§7allowing manipulation of spacetime itself.",
                "",
                "§e§lCombined Abilities:",
                "§7• Spacetime Rift: Tear holes in reality",
                "§7• Temporal Anchor: Freeze time and space",
                "§7• Reality Rewind: Undo recent events",
                "§7• Dimensional Shift: Move between timelines"
            ),
            Arrays.asList(GodType.TIME, GodType.VEIL)
        ));
        
        // Order and Chaos
        COMBINATIONS.put(Set.of(GodType.CRYSTAL, GodType.BLOOD), new CombinationResult(
            "Harmonic Chaos Blade", Material.NETHERITE_SWORD,
            "§d§l💎 Harmonic Chaos Blade ⚔§r",
            Arrays.asList(
                "§7Perfect balance between order and chaos,",
                "§7creating unpredictable yet harmonious power.",
                "",
                "§e§lCombined Abilities:",
                "§7• Chaos Resonance: Random powerful effects",
                "§7• Harmonic Strike: Perfectly timed attacks",
                "§7• Order from Chaos: Control randomness",
                "§7• Crystal Frenzy: Berserker precision"
            ),
            Arrays.asList(GodType.CRYSTAL, GodType.BLOOD)
        ));
        
        // Light and Dark
        COMBINATIONS.put(Set.of(GodType.FALLEN, GodType.SHADOW), new CombinationResult(
            "Twilight Sovereign Mantle", Material.NETHERITE_CHESTPLATE,
            "§8§l🌑 Twilight Sovereign Mantle 💀§r",
            Arrays.asList(
                "§7The eternal balance of death and shadow,",
                "§7granting dominion over the twilight realm.",
                "",
                "§e§lCombined Abilities:",
                "§7• Twilight Form: Exist between life and death",
                "§7• Shadow Death: Kill with darkness",
                "§7• Necro Stealth: Invisible undead form",
                "§7• Soul Shadow: Command spirit armies"
            ),
            Arrays.asList(GodType.FALLEN, GodType.SHADOW)
        ));
        
        // Triple Combinations (Legendary)
        COMBINATIONS.put(Set.of(GodType.FALLEN, GodType.VEIL, GodType.VOID), new CombinationResult(
            "Oblivion Crown", Material.NETHERITE_HELMET,
            "§0§l👑 Oblivion Crown 👑§r",
            Arrays.asList(
                "§7The ultimate expression of endings:",
                "§7death, reality manipulation, and void.",
                "",
                "§e§lTriple Fusion Abilities:",
                "§7• Oblivion Wave: Erase from existence",
                "§7• Reality Death: Kill concepts themselves",
                "§7• Void Resurrection: Return from nothingness",
                "§7• Entropy Mastery: Control universal decay"
            ),
            Arrays.asList(GodType.FALLEN, GodType.VEIL, GodType.VOID)
        ));
    }
    
    /**
     * Check if items can be combined
     */
    public static boolean canCombine(List<ItemStack> items) {
        if (items.size() < 2) {
            return false;
        }
        
        Set<GodType> gods = new HashSet<>();
        for (ItemStack item : items) {
            GodType god = getGodTypeFromItem(item);
            if (god != null) {
                gods.add(god);
            }
        }
        
        return COMBINATIONS.containsKey(gods);
    }
    
    /**
     * Get combination result for given items
     */
    public static CombinationResult getCombinationResult(List<ItemStack> items) {
        Set<GodType> gods = new HashSet<>();
        for (ItemStack item : items) {
            GodType god = getGodTypeFromItem(item);
            if (god != null) {
                gods.add(god);
            }
        }
        
        return COMBINATIONS.get(gods);
    }
    
    /**
     * Combine divine items into a new artifact
     */
    public static ItemStack combineItems(List<ItemStack> items, CombinationResult result) {
        if (result == null) {
            return null;
        }
        
        // Create the combined item
        CombinedDivineItem combinedItem = new CombinedDivineItem(
            result.getName(),
            result.getMaterial(),
            result.getDisplayName(),
            result.getLore(),
            result.getSourceGods()
        );
        
        ItemStack combinedItemStack = combinedItem.createItemStack();
        
        // Enhance based on source items
        enhanceCombinedItem(combinedItemStack, items);
        
        return combinedItemStack;
    }
    
    /**
     * Enhance combined item based on source items
     */
    private static void enhanceCombinedItem(ItemStack combinedItem, List<ItemStack> sourceItems) {
        Map<Enchantment, Integer> combinedEnchants = new HashMap<>();
        
        // Combine enchantments from source items
        for (ItemStack sourceItem : sourceItems) {
            for (Map.Entry<Enchantment, Integer> entry : sourceItem.getEnchantments().entrySet()) {
                combinedEnchants.merge(entry.getKey(), entry.getValue(), Integer::max);
            }
        }
        
        // Apply enhanced enchantments
        for (Map.Entry<Enchantment, Integer> entry : combinedEnchants.entrySet()) {
            int enhancedLevel = entry.getValue() + 2; // Bonus levels for combination
            combinedItem.addUnsafeEnchantment(entry.getKey(), enhancedLevel);
        }
        
        // Add combination-specific enchantments
        combinedItem.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        combinedItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 15);
        combinedItem.addUnsafeEnchantment(Enchantment.MENDING, 3);
    }
    
    /**
     * Get god type from divine item
     */
    private static GodType getGodTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }
        
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains("Divine Item of the")) {
                for (GodType god : GodType.values()) {
                    if (line.contains(god.getDisplayName())) {
                        return god;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all possible combinations
     */
    public static Map<Set<GodType>, CombinationResult> getAllCombinations() {
        return new HashMap<>(COMBINATIONS);
    }
    
    /**
     * Inner class for combination results
     */
    public static class CombinationResult {
        private final String name;
        private final Material material;
        private final String displayName;
        private final List<String> lore;
        private final List<GodType> sourceGods;
        
        public CombinationResult(String name, Material material, String displayName, 
                               List<String> lore, List<GodType> sourceGods) {
            this.name = name;
            this.material = material;
            this.displayName = displayName;
            this.lore = lore;
            this.sourceGods = sourceGods;
        }
        
        public String getName() { return name; }
        public Material getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public List<String> getLore() { return lore; }
        public List<GodType> getSourceGods() { return sourceGods; }
    }
}