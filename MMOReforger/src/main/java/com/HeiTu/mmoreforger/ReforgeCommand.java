package com.HeiTu.mmoreforger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReforgeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 检查是否是MMOItems物品
        if (!ReforgeManager.canReforge(item)) {
            String message = MMOReforger.getInstance().getConfig().getString("messages.not-mmoitem")
                    .replace('&', '§');
            player.sendMessage(message);
            return true;
        }
        
        // 打开强化GUI
        ReforgeGUI gui = new ReforgeGUI(player, item);
        gui.registerListeners();
        gui.open();
        
        return true;
    }
}