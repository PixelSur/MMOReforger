package com.HeiTu.mmoreforger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ReforgeGUI implements InventoryHolder, Listener {
    
    private final Inventory inventory;
    private final Player player;
    private final ItemStack reforgeItem;
    
    // GUI槽位定义
    private static final int ITEM_SLOT = 13;
    private static final int MATERIAL_SLOT = 21;
    private static final int SUCCESS_RATE_SLOT = 23;
    private static final int REFORGE_BUTTON_SLOT = 31;
    
    public ReforgeGUI(Player player, ItemStack item) {
        this.player = player;
        this.reforgeItem = item;
        this.inventory = Bukkit.createInventory(this, 45, "§6强化界面");
        
        initializeInventory();
    }
    
    /**
     * 初始化GUI界面
     */
    private void initializeInventory() {
        // 填充背景玻璃
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i != ITEM_SLOT && i != MATERIAL_SLOT && i != SUCCESS_RATE_SLOT && i != REFORGE_BUTTON_SLOT) {
                inventory.setItem(i, createBackgroundItem());
            }
        }
        
        // 设置待强化物品
        inventory.setItem(ITEM_SLOT, reforgeItem.clone());
        
        // 设置所需材料
        ItemStack requiredMaterial = ReforgeManager.getRequiredMaterial();
        ItemMeta materialMeta = requiredMaterial.getItemMeta();
        if (materialMeta != null) {
            List<String> lore = materialMeta.getLore() != null ? materialMeta.getLore() : new ArrayList<>();
            lore.add("§7点击放入背包");
            materialMeta.setLore(lore);
            requiredMaterial.setItemMeta(materialMeta);
        }
        inventory.setItem(MATERIAL_SLOT, requiredMaterial);
        
        // 设置成功率显示
        int currentLevel = ReforgeManager.getReforgeLevel(reforgeItem);
        double successRate = ReforgeManager.getSuccessRate(currentLevel + 1);
        inventory.setItem(SUCCESS_RATE_SLOT, createSuccessRateItem(successRate));
        
        // 设置强化按钮
        inventory.setItem(REFORGE_BUTTON_SLOT, createReforgeButton(currentLevel + 1));
    }
    
    /**
     * 创建背景玻璃物品
     */
    private ItemStack createBackgroundItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建成功率显示物品
     */
    private ItemStack createSuccessRateItem(double successRate) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a成功率");
            List<String> lore = new ArrayList<>();
            lore.add("§7当前成功率: §6" + String.format("%.1f%%", successRate));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建强化按钮
     */
    private ItemStack createReforgeButton(int targetLevel) {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6点击强化");
            List<String> lore = new ArrayList<>();
            lore.add("§7目标等级: +" + targetLevel);
            lore.add("§7点击进行强化");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 打开GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * 处理GUI点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;
        
        event.setCancelled(true);
        
        Player clicker = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        // 强化按钮点击
        if (slot == REFORGE_BUTTON_SLOT) {
            // 执行强化操作
            ReforgeManager.ReforgeResult result = ReforgeManager.reforge(player, reforgeItem);
            
            // 发送结果消息
            sendResultMessage(clicker, result);
            
            // 更新玩家手中的物品
            clicker.getInventory().setItemInMainHand(reforgeItem);
            
            // 关闭GUI
            clicker.closeInventory();
        }
        // 材料槽点击 - 给玩家查看材料信息
        else if (slot == MATERIAL_SLOT) {
            ItemStack material = inventory.getItem(MATERIAL_SLOT);
            if (material != null && material.getType() != Material.AIR) {
                clicker.sendMessage("§6强化所需材料: §r" + material.getAmount() + "x " + material.getItemMeta().getDisplayName());
            }
        }
    }
    
    /**
     * 处理GUI关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != this) return;
        
        // 清理GUI中的临时物品（如果有）
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                // 这里可以添加逻辑来处理关闭GUI时的物品
            }
        }
    }
    
    /**
     * 发送强化结果消息
     */
    private void sendResultMessage(Player player, ReforgeManager.ReforgeResult result) {
        String message = "";
        
        switch (result.getType()) {
            case SUCCESS:
                message = MMOReforger.getInstance().getConfig().getString("messages.success")
                        .replace("%level%", String.valueOf(result.getNewLevel()))
                        .replace('&', '§');
                break;
            case FAILURE:
                message = MMOReforger.getInstance().getConfig().getString("messages.failure")
                        .replace('&', '§');
                break;
            case BREAK:
                message = MMOReforger.getInstance().getConfig().getString("messages.break")
                        .replace('&', '§');
                break;
            case DECREASE:
                message = MMOReforger.getInstance().getConfig().getString("messages.decrease")
                        .replace("%decrease%", String.valueOf(result.getDecreaseAmount()))
                        .replace('&', '§');
                break;
            case NOT_MMOITEM:
                message = MMOReforger.getInstance().getConfig().getString("messages.not-mmoitem")
                        .replace('&', '§');
                break;
            case NO_MATERIALS:
                message = MMOReforger.getInstance().getConfig().getString("messages.no-materials")
                        .replace('&', '§');
                break;
        }
        
        player.sendMessage(message);
    }
    
    /**
     * 注册GUI监听器
     */
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, MMOReforger.getInstance());
    }
}