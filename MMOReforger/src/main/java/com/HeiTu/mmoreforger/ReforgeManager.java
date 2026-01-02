package com.HeiTu.mmoreforger;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class ReforgeManager {
    
    private static final Random random = new Random();
    
    /**
     * 检查物品是否可以强化
     * @param item 待检查的物品
     * @return 是否可以强化
     */
    public static boolean canReforge(ItemStack item) {
        return MMOItems.plugin.getMMOItem(item) != null;
    }
    
    /**
     * 获取物品当前的强化等级
     * @param item 待检查的物品
     * @return 当前强化等级
     */
    public static int getReforgeLevel(ItemStack item) {
        if (!canReforge(item)) return 0;
        
        VolatileMMOItem volatileItem = new VolatileMMOItem(MMOItems.plugin.getMMOItem(item));
        return volatileItem.hasData("reforge-level") ? volatileItem.getInt("reforge-level") : 0;
    }
    
    /**
     * 执行强化操作
     * @param player 执行强化的玩家
     * @param item 待强化的物品
     * @return 强化结果
     */
    public static ReforgeResult reforge(Player player, ItemStack item) {
        if (!canReforge(item)) {
            return new ReforgeResult(false, ResultType.NOT_MMOITEM, 0, 0);
        }
        
        MMOItem mmoItem = MMOItems.plugin.getMMOItem(item);
        VolatileMMOItem volatileItem = new VolatileMMOItem(mmoItem);
        
        int currentLevel = getReforgeLevel(item);
        double successRate = getSuccessRate(currentLevel + 1);
        
        // 检查强化材料
        if (!hasEnoughMaterials(player, currentLevel + 1)) {
            return new ReforgeResult(false, ResultType.NO_MATERIALS, currentLevel, 0);
        }
        
        // 消耗强化材料
        consumeMaterials(player, currentLevel + 1);
        
        // 计算成功率
        boolean success = random.nextDouble() * 100 < successRate;
        
        if (success) {
            // 强化成功，提升等级
            int newLevel = currentLevel + 1;
            volatileItem.setData("reforge-level", newLevel);
            
            // 提升属性
            applyStatBoosts(mmoItem, newLevel - currentLevel);
            
            // 保存更改
            volatileItem.writeBack(false);
            
            return new ReforgeResult(true, ResultType.SUCCESS, newLevel, 0);
        } else {
            // 强化失败，处理惩罚
            return handleFailure(player, item, volatileItem, currentLevel);
        }
    }
    
    /**
     * 获取强化成功率
     * @param level 目标强化等级
     * @return 成功率（百分比）
     */
    public static double getSuccessRate(int level) {
        return MMOReforger.getInstance().getConfig().getDouble("success-rates." + level, 50.0);
    }
    
    /**
     * 检查玩家是否有足够的强化材料
     * @param player 玩家
     * @param level 目标强化等级
     * @return 是否有足够的材料
     */
    private static boolean hasEnoughMaterials(Player player, int level) {
        String materialType = MMOReforger.getInstance().getConfig().getString("materials.default.type", "MMOITEM");
        int amount = MMOReforger.getInstance().getConfig().getInt("materials.default.amount", 5);
        
        if (materialType.equalsIgnoreCase("MMOITEM")) {
            // MMOItems材料检查
            String mmoItemType = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-type", "RESOURCE");
            String mmoItemId = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-id", "REINFORCEMENT_GEM");
            
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    MMOItem mmoItem = MMOItems.plugin.getMMOItem(item);
                    if (mmoItem != null && mmoItem.getType().name().equalsIgnoreCase(mmoItemType) && mmoItem.getId().equalsIgnoreCase(mmoItemId)) {
                        count += item.getAmount();
                    }
                }
            }
            return count >= amount;
        } else {
            // 原版材料检查
            Material material = Material.valueOf(MMOReforger.getInstance().getConfig().getString("materials.default.vanilla-item", "DIAMOND"));
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    count += item.getAmount();
                }
            }
            return count >= amount;
        }
    }
    
    /**
     * 消耗强化材料
     * @param player 玩家
     * @param level 目标强化等级
     */
    private static void consumeMaterials(Player player, int level) {
        String materialType = MMOReforger.getInstance().getConfig().getString("materials.default.type", "MMOITEM");
        int amount = MMOReforger.getInstance().getConfig().getInt("materials.default.amount", 5);
        
        if (materialType.equalsIgnoreCase("MMOITEM")) {
            // 消耗MMOItems材料
            String mmoItemType = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-type", "RESOURCE");
            String mmoItemId = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-id", "REINFORCEMENT_GEM");
            
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null) {
                    MMOItem mmoItem = MMOItems.plugin.getMMOItem(item);
                    if (mmoItem != null && mmoItem.getType().name().equalsIgnoreCase(mmoItemType) && mmoItem.getId().equalsIgnoreCase(mmoItemId)) {
                        int take = Math.min(remaining, item.getAmount());
                        item.setAmount(item.getAmount() - take);
                        remaining -= take;
                        
                        if (remaining <= 0) {
                            break;
                        }
                    }
                }
            }
        } else {
            // 消耗原版材料
            Material material = Material.valueOf(MMOReforger.getInstance().getConfig().getString("materials.default.vanilla-item", "DIAMOND"));
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null && item.getType() == material) {
                    int take = Math.min(remaining, item.getAmount());
                    item.setAmount(item.getAmount() - take);
                    remaining -= take;
                    
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * 获取强化所需的MMOItems材料
     * @return 材料物品栈
     */
    public static ItemStack getRequiredMaterial() {
        String materialType = MMOReforger.getInstance().getConfig().getString("materials.default.type", "MMOITEM");
        int amount = MMOReforger.getInstance().getConfig().getInt("materials.default.amount", 5);
        
        if (materialType.equalsIgnoreCase("MMOITEM")) {
            String mmoItemType = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-type", "RESOURCE");
            String mmoItemId = MMOReforger.getInstance().getConfig().getString("materials.default.mmoitem-id", "REINFORCEMENT_GEM");
            return MMOItems.plugin.getItem(mmoItemType, mmoItemId, amount);
        } else {
            Material material = Material.valueOf(MMOReforger.getInstance().getConfig().getString("materials.default.vanilla-item", "DIAMOND"));
            return new ItemStack(material, amount);
        }
    }
    
    /**
     * 应用属性提升
     * @param mmoItem MMO物品
     * @param levelIncrease 等级提升数
     */
    private static void applyStatBoosts(MMOItem mmoItem, int levelIncrease) {
        double damageBoost = MMOReforger.getInstance().getConfig().getDouble("stat-boosts.default.damage", 2.0) * levelIncrease;
        double defenseBoost = MMOReforger.getInstance().getConfig().getDouble("stat-boosts.default.defense", 1.5) * levelIncrease;
        double attackSpeedBoost = MMOReforger.getInstance().getConfig().getDouble("stat-boosts.default.attack-speed", 0.05) * levelIncrease;
        
        // 提升物品属性
        mmoItem.setDamage(mmoItem.getDamage() + damageBoost);
        mmoItem.setDefense(mmoItem.getDefense() + defenseBoost);
        mmoItem.setAttackSpeed(mmoItem.getAttackSpeed() + attackSpeedBoost);
    }
    
    /**
     * 处理强化失败
     * @param player 玩家
     * @param item 物品
     * @param volatileItem 可变MMO物品
     * @param currentLevel 当前等级
     * @return 强化结果
     */
    private static ReforgeResult handleFailure(Player player, ItemStack item, VolatileMMOItem volatileItem, int currentLevel) {
        boolean breakItem = MMOReforger.getInstance().getConfig().getBoolean("failure-penalty.break", false);
        boolean decreaseLevel = MMOReforger.getInstance().getConfig().getBoolean("failure-penalty.decrease-level", true);
        int maxDecrease = MMOReforger.getInstance().getConfig().getInt("failure-penalty.max-decrease", 1);
        
        if (breakItem) {
            // 破坏物品
            player.getInventory().remove(item);
            return new ReforgeResult(false, ResultType.BREAK, 0, 0);
        } else if (decreaseLevel && currentLevel > 0) {
            // 降低等级
            int decrease = Math.min(maxDecrease, currentLevel);
            int newLevel = currentLevel - decrease;
            volatileItem.setData("reforge-level", newLevel);
            volatileItem.writeBack(false);
            
            return new ReforgeResult(false, ResultType.DECREASE, newLevel, decrease);
        } else {
            // 无惩罚
            return new ReforgeResult(false, ResultType.FAILURE, currentLevel, 0);
        }
    }
    
    /**
     * 强化结果类
     */
    public static class ReforgeResult {
        private final boolean success;
        private final ResultType type;
        private final int newLevel;
        private final int decreaseAmount;
        
        public ReforgeResult(boolean success, ResultType type, int newLevel, int decreaseAmount) {
            this.success = success;
            this.type = type;
            this.newLevel = newLevel;
            this.decreaseAmount = decreaseAmount;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public ResultType getType() {
            return type;
        }
        
        public int getNewLevel() {
            return newLevel;
        }
        
        public int getDecreaseAmount() {
            return decreaseAmount;
        }
    }
    
    /**
     * 强化结果类型枚举
     */
    public enum ResultType {
        SUCCESS,
        FAILURE,
        BREAK,
        DECREASE,
        NOT_MMOITEM,
        NO_MATERIALS
    }
}