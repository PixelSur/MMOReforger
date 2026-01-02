package com.HeiTu.mmoreforger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ReforgeListener implements Listener {
    
    /**
     * 当玩家切换手持物品时更新物品显示名称
     */
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && ReforgeManager.canReforge(item)) {
            updateItemDisplayName(item);
        }
    }
    
    /**
     * 当玩家在背包中点击物品时更新物品显示名称
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && ReforgeManager.canReforge(item)) {
            updateItemDisplayName(item);
        }
    }
    
    /**
     * 更新物品的显示名称，添加强化等级信息
     * @param item 待更新的物品
     */
    private void updateItemDisplayName(ItemStack item) {
        if (!ReforgeManager.canReforge(item)) return;
        
        int reforgeLevel = ReforgeManager.getReforgeLevel(item);
        if (reforgeLevel <= 0) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        String displayName = meta.getDisplayName();
        
        // 移除旧的强化等级标记
        if (displayName.contains(" +")) {
            displayName = displayName.substring(0, displayName.lastIndexOf(" +"));
        }
        
        // 添加新的强化等级标记
        displayName = displayName + " §6+" + reforgeLevel;
        meta.setDisplayName(displayName);
        
        // 更新物品描述
        updateItemLore(meta, reforgeLevel);
        
        item.setItemMeta(meta);
    }
    
    /**
     * 更新物品描述，添加强化属性信息
     * @param meta 物品元数据
     * @param reforgeLevel 强化等级
     */
    private void updateItemLore(ItemMeta meta, int reforgeLevel) {
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        // 移除旧的强化属性描述
        lore.removeIf(line -> line.contains("强化等级") || line.contains("Reforge Level"));
        
        // 添加新的强化属性描述
        lore.add("§7强化等级: §6+" + reforgeLevel);
        
        meta.setLore(lore);
    }
}