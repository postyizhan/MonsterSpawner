package github.postyizhan.monsterspawner;

import github.postyizhan.monsterspawner.command.CommandManager;
import github.postyizhan.monsterspawner.hook.HookManager;
import github.postyizhan.monsterspawner.hook.PlaceholderAPIHook;
import github.postyizhan.monsterspawner.listener.SpawnerListener;
import github.postyizhan.monsterspawner.listener.SpawnerPlaceListener;
import github.postyizhan.monsterspawner.utils.ActionManager;
import github.postyizhan.monsterspawner.utils.LanguageManager;
import github.postyizhan.monsterspawner.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.util.concurrent.atomic.AtomicInteger;

public final class MonsterSpawner extends JavaPlugin {

    private LanguageManager languageManager;
    private ActionManager actionManager;
    private CommandManager commandManager;
    private UpdateChecker updateChecker;
    private HookManager hookManager;
    private AtomicInteger spawnersBroken = new AtomicInteger(0);
    private AtomicInteger spawnersPlaced = new AtomicInteger(0);
    
    @Override
    public void onEnable() {
        // bstats 数据统计
        int pluginId = 26042;
        new Metrics(this, pluginId);

        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化语言管理器
        languageManager = new LanguageManager(this);
        
        // 初始化钩子管理器
        hookManager = new HookManager(this);
        
        // 初始化动作管理器
        actionManager = new ActionManager(this);
        
        // 初始化命令管理器
        commandManager = new CommandManager(this);
        getCommand("monsterspawner").setExecutor(commandManager);
        getCommand("monsterspawner").setTabCompleter(commandManager);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(this), this);
        
        // 获取服务器版本信息
        String serverVersion = Bukkit.getServer().getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion();
        String serverName = Bukkit.getServer().getName();

        // 显示插件启动信息
        String[] startupMessage = {
            "§3 ___  ___                 _             §b _____                                     ",
            "§3 |  \\/  |                | |            §b/  ___|                                    ",
            "§3 | .  . | ___  _ __  ___ | |_  ___  ___ §b\\ `--.   _ __   __ _ __      __ _ __   ___ _ __ ",
            "§3 | |\\/| |/ _ \\| '_ \\/ __|| __|/ _ \\/ __| §b`--.  \\| '_ \\ / _` |\\ \\ /\\ / /| '_ \\ / _ \\ '__|",
            "§3 | |  | | (_) | | | \\__ \\| |_|  __/\\__ \\ §b/\\__/ /| |_) | (_| | \\ V  V / | | | |  __/ |   ",
            "§3 \\_|  |_/\\___/|_| |_|___/ \\__|\\___||___/ §b\\____/ | .__/ \\__,_|  \\_/\\_/  |_| |_|\\___|_|   ",
            "§3                                          §b      | |                                 ",
            "§3                                          §b      |_|                                 ",
            "§7MonsterSpawner §fv" + getDescription().getVersion() + " §8- §f" + serverName + " " + serverVersion + " §8| §fBy postyizhan",
            "§7QQ群: §f611076407 §8| §7GitHub: §fhttps://github.com/postyizhan/MonsterSpawner"
        };
        for (String line : startupMessage) {
            getServer().getConsoleSender().sendMessage(line);
        }

        // 初始化PlaceholderAPI钩子
        setupPlaceholderAPI();
        
        // 初始化并注册更新检查器
        setupUpdateChecker();
    }
    
    private void setupUpdateChecker() {
        updateChecker = new UpdateChecker(this);
        if (getConfig().getBoolean("update-checker.enabled", true)) {
            getServer().getPluginManager().registerEvents(updateChecker, this);
        }
    }
    
    private void setupPlaceholderAPI() {
        // PlaceholderAPI
        PlaceholderAPIHook.initialize();
        if (PlaceholderAPIHook.isEnabled()) {
            String message = languageManager.getString("system.hooks.enabled")
                    .replace("{prefix}", languageManager.getPrefix())
                    .replace("{0}", "PlaceholderAPI");
            getServer().getConsoleSender().sendMessage(message);
        } else {
            String message = languageManager.getString("system.hooks.disabled")
                    .replace("{0}", "PlaceholderAPI");
            getServer().getConsoleSender().sendMessage(message);
        }
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(languageManager.getString("system.messages.disabled"));
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public ActionManager getActionManager() {
        return actionManager;
    }
    
    public HookManager getHookManager() {
        return hookManager;
    }
    
    public boolean hasPlaceholderAPI() {
        return PlaceholderAPIHook.isEnabled();
    }
    
    /**
     * 增加破坏的刷怪笼计数
     */
    public void incrementSpawnersBroken() {
        spawnersBroken.incrementAndGet();
    }
    
    /**
     * 增加放置的刷怪笼计数
     */
    public void incrementSpawnersPlaced() {
        spawnersPlaced.incrementAndGet();
    }
    
    /**
     * 获取更新检查器实例
     * @return 更新检查器
     */
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
    
    /**
     * 获取破坏的刷怪笼数量
     * @return 破坏的刷怪笼数量
     */
    public int getSpawnersBroken() {
        return spawnersBroken.get();
    }
    
    /**
     * 获取放置的刷怪笼数量
     * @return 放置的刷怪笼数量
     */
    public int getSpawnersPlaced() {
        return spawnersPlaced.get();
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (languageManager != null) {
            languageManager.reload();
        }
    }
}
