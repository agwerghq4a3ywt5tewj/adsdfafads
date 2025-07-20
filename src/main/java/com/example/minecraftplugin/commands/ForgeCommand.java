package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.ItemRarity;
import com.example.minecraftplugin.items.upgrades.DivineItemUpgrade;
import com.example.minecraftplugin.items.upgrades.UpgradeMaterial;
import com.example.minecraftplugin.items.combinations.DivineItemCombination;
import com.example.minecraftplugin.managers.DivineForgeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ForgeCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final DivineForgeManager forgeManager;
    
    public ForgeCommand(MinecraftPlugin plugin, DivineForgeManager forgeManager) {
        this.plugin = plugin;
        this.forgeManager = forgeManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.forge")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showForgeHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "upgrade":
                handleUpgrade(player);
                break;
                
            case "combine":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /forge combine <slot1> [slot2] [slot3]");
                    return true;
                }
                handleCombine(player, args);
                break;
                
            case "info":
                showItemInfo(player);
                break;
                
            case "materials":
                showUpgradeMaterials(player);
                break;
                
            case "combinations":
                showAvailableCombinations(player);
                break;
                
            case "give":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /forge give <material_type> <amount>");
                    return true;
                }
                if (player.hasPermission("minecraftplugin.admin")) {
                    handleGiveMaterial(player, args[1], args[2]);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to give materials!");
                }
                break;
                
            case "stats":
                if (player.hasPermission("minecraftplugin.admin")) {
                    showForgeStatistics(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view forge statistics!");
                }
                break;
                
            default:
                showForgeHelp(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subcommands = Arrays.asList("upgrade", "combine", "info", "materials", "combinations");
            if (sender.hasPermission("minecraftplugin.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("give");
                subcommands.add("stats");
            }
            
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("minecraftplugin.admin")) {
                // Material types
                for (UpgradeMaterial.UpgradeType type : UpgradeMaterial.UpgradeType.values()) {
                    String typeName = type.name().toLowerCase();
                    if (typeName.startsWith(args[1].toLowerCase())) {
                        completions.add(typeName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("combine")) {
                // Inventory slot numbers
                for (int i = 0; i < 36; i++) {
                    String slot = String.valueOf(i);
                    if (slot.startsWith(args[1])) {
                        completions.add(slot);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private void showForgeHelp(Player player) {
        player.sendMessage("§6§l=== DIVINE FORGE COMMANDS ===§r");
        player.sendMessage("§7/forge upgrade - Upgrade held divine item");
        player.sendMessage("§7/forge combine <slot1> [slot2] [slot3] - Combine divine items");
        player.sendMessage("§7/forge info - Show information about held item");
        player.sendMessage("§7/forge materials - Show upgrade materials in inventory");
        player.sendMessage("§7/forge combinations - Show available combinations");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/forge give <material_type> <amount> - Give upgrade materials");
            player.sendMessage("§7/forge stats - Show forge statistics");
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lDivine Forge System:");
        player.sendMessage("§7• Upgrade divine items through rarity levels");
        player.sendMessage("§7• Combine compatible divine items for unique powers");
        player.sendMessage("§7• Discover legendary variants with special abilities");
        player.sendMessage("§7• Collect upgrade materials from raids and challenges");
        
        player.sendMessage("");
        player.sendMessage("§e§lRarity Levels:");
        for (ItemRarity rarity : ItemRarity.values()) {
            player.sendMessage("§7• " + rarity.getFormattedName() + " §7- " + rarity.getDescription());
        }
    }
    
    private void handleUpgrade(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem == null || heldItem.getType() == org.bukkit.Material.AIR) {
            player.sendMessage("§c§lNo item to upgrade! §r§cHold a divine item in your main hand.");
            return;
        }
        
        if (!DivineItemUpgrade.canUpgrade(heldItem)) {
            player.sendMessage("§c§lCannot upgrade! §r§cThis item cannot be upgraded further.");
            return;
        }
        
        ItemRarity currentRarity = DivineItemUpgrade.getCurrentRarity(heldItem);
        ItemRarity targetRarity = DivineItemUpgrade.getNextRarity(currentRarity);
        DivineItemUpgrade.UpgradeRequirement requirement = DivineItemUpgrade.getUpgradeRequirement(targetRarity);
        
        player.sendMessage("§6§l=== DIVINE FORGE UPGRADE ===§r");
        player.sendMessage("§7Current Rarity: " + currentRarity.getFormattedName());
        player.sendMessage("§7Target Rarity: " + targetRarity.getFormattedName());
        player.sendMessage("§7Required: §f" + requirement.getAmount() + "x " + requirement.getMaterialType().getDisplayName());
        
        double successChance = DivineItemUpgrade.getUpgradeSuccessChance(currentRarity, targetRarity);
        player.sendMessage("§7Success Chance: §f" + String.format("%.0f%%", successChance * 100));
        
        DivineForgeManager.UpgradeResult result = forgeManager.upgradeItem(player, heldItem);
        
        player.sendMessage("");
        if (result.isSuccess()) {
            player.sendMessage("§a§l✦ " + result.getMessage() + " ✦");
            player.getInventory().setItemInMainHand(result.getResultItem());
        } else {
            player.sendMessage("§c§l✗ " + result.getMessage() + " ✗");
        }
    }
    
    private void handleCombine(Player player, String[] args) {
        List<ItemStack> itemsToCombine = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        
        // Parse slot numbers
        for (int i = 1; i < args.length; i++) {
            try {
                int slot = Integer.parseInt(args[i]);
                if (slot >= 0 && slot < 36) {
                    ItemStack item = player.getInventory().getItem(slot);
                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
                        itemsToCombine.add(item);
                        slots.add(slot);
                    }
                } else {
                    player.sendMessage("§cInvalid slot number: " + slot + " (must be 0-35)");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid slot number: " + args[i]);
                return;
            }
        }
        
        if (itemsToCombine.size() < 2) {
            player.sendMessage("§c§lNeed at least 2 items to combine!");
            return;
        }
        
        player.sendMessage("§6§l=== DIVINE FORGE COMBINATION ===§r");
        player.sendMessage("§7Items to combine: §f" + itemsToCombine.size());
        player.sendMessage("§7Required: §f1x Fusion Catalyst");
        
        DivineForgeManager.CombinationResult result = forgeManager.combineItems(player, itemsToCombine);
        
        player.sendMessage("");
        if (result.isSuccess()) {
            player.sendMessage("§a§l★ " + result.getMessage() + " ★");
            
            // Remove source items from inventory
            for (int slot : slots) {
                player.getInventory().setItem(slot, null);
            }
            
            // Give combined item
            player.getInventory().addItem(result.getResultItem());
        } else {
            player.sendMessage("§c§l✗ " + result.getMessage() + " ✗");
        }
    }
    
    private void showItemInfo(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        if (heldItem == null || heldItem.getType() == org.bukkit.Material.AIR) {
            player.sendMessage("§c§lNo item! §r§cHold an item in your main hand.");
            return;
        }
        
        player.sendMessage("§6§l=== ITEM INFORMATION ===§r");
        player.sendMessage("§7Item: §f" + heldItem.getType().name());
        
        if (heldItem.hasItemMeta() && heldItem.getItemMeta().hasDisplayName()) {
            player.sendMessage("§7Display Name: " + heldItem.getItemMeta().getDisplayName());
        }
        
        // Check if it's a divine item
        boolean isDivine = false;
        if (heldItem.hasItemMeta() && heldItem.getItemMeta().hasLore()) {
            for (String line : heldItem.getItemMeta().getLore()) {
                if (line.contains("Divine Item of the")) {
                    isDivine = true;
                    break;
                }
            }
        }
        
        if (isDivine) {
            ItemRarity rarity = DivineItemUpgrade.getCurrentRarity(heldItem);
            player.sendMessage("§7Rarity: " + rarity.getFormattedName());
            player.sendMessage("§7Can Upgrade: §f" + (DivineItemUpgrade.canUpgrade(heldItem) ? "Yes" : "No"));
            
            if (DivineItemUpgrade.canUpgrade(heldItem)) {
                ItemRarity nextRarity = DivineItemUpgrade.getNextRarity(rarity);
                DivineItemUpgrade.UpgradeRequirement req = DivineItemUpgrade.getUpgradeRequirement(nextRarity);
                player.sendMessage("§7Next Upgrade: " + nextRarity.getFormattedName());
                player.sendMessage("§7Required: §f" + req.getAmount() + "x " + req.getMaterialType().getDisplayName());
            }
        } else {
            // Check if it's an upgrade material
            UpgradeMaterial.UpgradeType materialType = UpgradeMaterial.getUpgradeType(heldItem);
            if (materialType != null) {
                player.sendMessage("§7Type: §bUpgrade Material");
                player.sendMessage("§7Material: §f" + materialType.getDisplayName());
            } else {
                player.sendMessage("§7Type: §7Regular Item");
            }
        }
    }
    
    private void showUpgradeMaterials(Player player) {
        player.sendMessage("§6§l=== UPGRADE MATERIALS ===§r");
        
        Map<UpgradeMaterial.UpgradeType, Integer> materials = new HashMap<>();
        
        // Count materials in inventory
        for (ItemStack item : player.getInventory().getContents()) {
            UpgradeMaterial.UpgradeType type = UpgradeMaterial.getUpgradeType(item);
            if (type != null) {
                materials.merge(type, item.getAmount(), Integer::sum);
            }
        }
        
        if (materials.isEmpty()) {
            player.sendMessage("§7You have no upgrade materials.");
            player.sendMessage("§7Obtain them from raids, challenges, and special events!");
            return;
        }
        
        player.sendMessage("§7Your upgrade materials:");
        for (Map.Entry<UpgradeMaterial.UpgradeType, Integer> entry : materials.entrySet()) {
            UpgradeMaterial.UpgradeType type = entry.getKey();
            int amount = entry.getValue();
            player.sendMessage("§7• §f" + amount + "x §b" + type.getDisplayName());
        }
        
        player.sendMessage("");
        player.sendMessage("§e§lMaterial Uses:");
        for (UpgradeMaterial.UpgradeType type : UpgradeMaterial.UpgradeType.values()) {
            player.sendMessage("§7• §b" + type.getDisplayName() + "§7: " + type.getLore().get(1));
        }
    }
    
    private void showAvailableCombinations(Player player) {
        player.sendMessage("§6§l=== DIVINE COMBINATIONS ===§r");
        player.sendMessage("§7Available divine item combinations:");
        player.sendMessage("");
        
        Map<java.util.Set<com.example.minecraftplugin.enums.GodType>, DivineItemCombination.CombinationResult> combinations = 
            DivineItemCombination.getAllCombinations();
        
        for (Map.Entry<java.util.Set<com.example.minecraftplugin.enums.GodType>, DivineItemCombination.CombinationResult> entry : combinations.entrySet()) {
            java.util.Set<com.example.minecraftplugin.enums.GodType> gods = entry.getKey();
            DivineItemCombination.CombinationResult result = entry.getValue();
            
            List<String> godNames = new ArrayList<>();
            for (com.example.minecraftplugin.enums.GodType god : gods) {
                godNames.add(god.getDisplayName());
            }
            
            player.sendMessage("§e§l" + result.getName());
            player.sendMessage("§7• Gods: §f" + String.join(" + ", godNames));
            player.sendMessage("§7• Result: " + result.getDisplayName());
            player.sendMessage("§7• Requires: §f1x Fusion Catalyst");
            player.sendMessage("");
        }
        
        player.sendMessage("§7Use §f/forge combine <slot1> <slot2> [slot3]§7 to combine items");
    }
    
    private void handleGiveMaterial(Player player, String materialType, String amountStr) {
        UpgradeMaterial.UpgradeType type;
        try {
            type = UpgradeMaterial.UpgradeType.valueOf(materialType.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid material type: " + materialType);
            player.sendMessage("§7Available types: divine_essence, cosmic_fragment, reality_shard, transcendent_core, fusion_catalyst");
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                player.sendMessage("§cAmount must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount: " + amountStr);
            return;
        }
        
        forgeManager.grantUpgradeMaterials(player, type, amount, "Admin command");
        player.sendMessage("§a§lMaterials granted successfully!");
    }
    
    private void showForgeStatistics(Player admin) {
        Map<String, Object> stats = forgeManager.getForgeStatistics();
        
        admin.sendMessage("§6§l=== DIVINE FORGE STATISTICS ===§r");
        admin.sendMessage("§7Total Legendary Variants: §f" + stats.get("total_legendary_variants"));
        admin.sendMessage("§7Gods with Variants: §f" + stats.get("gods_with_variants"));
        admin.sendMessage("§7Available Combinations: §f" + stats.get("available_combinations"));
        admin.sendMessage("§7Upgrade Materials: §f" + stats.get("upgrade_materials"));
        admin.sendMessage("§7Rarity Levels: §f" + stats.get("rarity_levels"));
        
        admin.sendMessage("");
        admin.sendMessage("§e§lSystem Status: §aFully Operational");
        admin.sendMessage("§7The Divine Forge is ready for advanced item crafting!");
    }
}