package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 钩子管理器
 * 用于统一管理所有钩子
 */
public class HookManager {

    private final MonsterSpawner plugin;
    private final Map<String, HookAbstract> hookMap = new HashMap<>();
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("([a-zA-Z0-9_-]+):(.+)");

    public HookManager(MonsterSpawner plugin) {
        this.plugin = plugin;
        registerHooks();
    }

    /**
     * 注册所有钩子
     */
    private void registerHooks() {
        // 注册Oraxen
        registerHook(new OraxenHookImpl());
        
        // 注册ItemsAdder
        registerHook(new ItemsAdderHookImpl());
        
        // 注册MythicMobs
        registerHook(new MythicMobsHookImpl());
        
        // 注册NeigeItems
        registerHook(new NeigeItemsHookImpl());
        
        // 初始化所有钩子
        initializeHooks();
    }
    
    /**
     * 注册钩子
     * @param hook 钩子实例
     */
    public void registerHook(HookAbstract hook) {
        hookMap.put(hook.getNamespace(), hook);
    }
    
    /**
     * 初始化所有钩子
     */
    public void initializeHooks() {
        for (HookAbstract hook : hookMap.values()) {
            try {
                hook.initialize();
            } catch (Exception e) {
                plugin.getLogger().warning("§c初始化" + hook.getName() + "钩子时出错: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
        
        // 打印钩子状态
        printHookStatus();
    }
    
    /**
     * 打印钩子状态
     */
    private void printHookStatus() {
        Bukkit.getLogger().info("§a[MonsterSpawner] 物品库钩子状态:");
        for (HookAbstract hook : hookMap.values()) {
            Bukkit.getLogger().info("§a[MonsterSpawner] - " + hook.getName() + ": " + 
                    (hook.isHooked() ? "§a已连接" : "§c未连接"));
        }
    }
    
    /**
     * 根据命名空间获取钩子
     * @param namespace 命名空间
     * @return 钩子实例，如果不存在则返回null
     */
    public HookAbstract getHook(String namespace) {
        return hookMap.get(namespace.toLowerCase());
    }
    
    /**
     * 解析物品ID字符串获取物品
     * 格式支持以下几种：
     * 1. minecraft:stone - 原版物品
     * 2. oraxen:item_id - 带命名空间的物品
     * 3. item_id - 无命名空间的物品，将尝试所有钩子
     * 
     * @param itemString 物品ID字符串
     * @param amount 数量
     * @param player 玩家，可为null
     * @return 物品实例，如果获取失败则返回null
     */
    public ItemStack parseItemStack(String itemString, int amount, Player player) {
        if (itemString == null || itemString.isEmpty()) {
            return null;
        }
        
        // 检查是否有命名空间
        java.util.regex.Matcher matcher = NAMESPACE_PATTERN.matcher(itemString);
        if (matcher.matches()) {
            String namespace = matcher.group(1).toLowerCase();
            String id = matcher.group(2);
            
            // 处理原版物品
            if (namespace.equals("minecraft")) {
                try {
                    Material material = Material.valueOf(id.toUpperCase());
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
            
            // 处理带命名空间的自定义物品
            HookAbstract hook = getHook(namespace);
            if (hook != null && hook.isHooked()) {
                // 为NeigeItems提供特殊处理，支持玩家相关物品
                if (hook instanceof NeigeItemsHookImpl && player != null) {
                    return ((NeigeItemsHookImpl) hook).getItemForPlayer(id, player, amount);
                }
                return hook.getItem(id, amount);
            }
        } else {
            // 无命名空间，尝试所有钩子
            
            // 首先尝试作为原版物品处理
            try {
                Material material = Material.valueOf(itemString.toUpperCase());
                return new ItemStack(material, amount);
            } catch (IllegalArgumentException ignored) {
                // 继续尝试其他钩子
            }
            
            // 尝试所有钩子
            for (HookAbstract hook : hookMap.values()) {
                if (hook.isHooked() && hook.isCustomItem(itemString)) {
                    // 为NeigeItems提供特殊处理，支持玩家相关物品
                    if (hook instanceof NeigeItemsHookImpl && player != null) {
                        return ((NeigeItemsHookImpl) hook).getItemForPlayer(itemString, player, amount);
                    }
                    return hook.getItem(itemString, amount);
                }
            }
        }
        
        // 所有尝试都失败
        return null;
    }
    
    /**
     * 从物品获取ID
     * @param itemStack 物品实例
     * @return 物品ID字符串，如果不是自定义物品则返回null
     */
    public String getItemId(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        
        // 尝试所有钩子
        for (HookAbstract hook : hookMap.values()) {
            if (hook.isHooked()) {
                String id = hook.getId(itemStack);
                if (id != null) {
                    return hook.getNamespace() + ":" + id;
                }
            }
        }
        
        // 对于原版物品，返回minecraft命名空间
        return "minecraft:" + itemStack.getType().name().toLowerCase();
    }
} 