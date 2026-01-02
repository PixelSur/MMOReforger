package com.HeiTu.mmoreforger;

import org.bukkit.plugin.java.JavaPlugin;

public class MMOReforger extends JavaPlugin {
    
    private static MMOReforger instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 加载配置文件
        saveDefaultConfig();
        
        // 注册命令和事件监听
        getCommand("reforge").setExecutor(new ReforgeCommand());
        getServer().getPluginManager().registerEvents(new ReforgeListener(), this);
        
        getLogger().info("MMOReforger 插件已启用！");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MMOReforger 插件已禁用！");
    }
    
    public static MMOReforger getInstance() {
        return instance;
    }
}