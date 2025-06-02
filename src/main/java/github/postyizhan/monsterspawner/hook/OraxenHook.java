package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Oraxen插件钩子
 * 提供与Oraxen插件的集成支持
 */
public class OraxenHook {
    
    private static boolean enabled = false;
    private static Class<?> oraxenItemsClass;
    private static Method isAnItemMethod;
    private static Method getItemByIdMethod;
    private static Method buildMethod;
    private static Method getIdByItemMethod;
    
    /**
     * 初始化Oraxen钩子
     */
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
                // 简化日志，只保留版本信息
                String oraxenVersion = Bukkit.getPluginManager().getPlugin("Oraxen").getDescription().getVersion();
                Bukkit.getLogger().info("§a[MonsterSpawner] 检测到Oraxen插件，版本: " + oraxenVersion);
                
                // 尝试找到Oraxen API类
                try {
                    oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
                    
                    // 尝试常见的方法名称和参数类型
                    tryFindIsAnItemMethod();
                    tryFindGetItemByIdMethod();
                    tryFindGetIdByItemMethod();
                    
                    // 如果获取到了必要的方法，则启用钩子
                    if (isAnItemMethod != null && getItemByIdMethod != null) {
                        enabled = true;
                        Bukkit.getLogger().info("§a[MonsterSpawner] 成功连接Oraxen插件");
                    } else {
                        throw new RuntimeException("未找到必要的Oraxen API方法");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("§c[MonsterSpawner] 初始化Oraxen API方法时出错: " + e.getMessage());
                }
            } else {
                enabled = false;
                Bukkit.getLogger().info("§7[MonsterSpawner] 未找到Oraxen插件，相关功能已禁用");
            }
        } catch (Exception e) {
            enabled = false;
            Bukkit.getLogger().warning("§c[MonsterSpawner] 初始化Oraxen钩子时发生错误: " + e.getMessage());
        }
    }
    
    private static void tryFindIsAnItemMethod() {
        // 简化方法查找逻辑，减少日志输出
        try {
            isAnItemMethod = oraxenItemsClass.getMethod("isAnItem", String.class);
            return;
        } catch (NoSuchMethodException e) {
            // 尝试替代方法
            try {
                isAnItemMethod = oraxenItemsClass.getMethod("exists", String.class);
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().warning("§c[MonsterSpawner] 未找到可用的物品检查方法");
            }
        }
    }
    
    private static void tryFindGetItemByIdMethod() {
        try {
            getItemByIdMethod = oraxenItemsClass.getMethod("getItemById", String.class);
            
            // 获取返回类型，并尝试找到build方法
            Class<?> returnType = getItemByIdMethod.getReturnType();
            try {
                buildMethod = returnType.getMethod("build");
            } catch (NoSuchMethodException e) {
                Bukkit.getLogger().warning("§c[MonsterSpawner] 找到 getItemById 但未找到 build 方法");
            }
            return;
        } catch (NoSuchMethodException e) {
            // 尝试替代方法
            try {
                getItemByIdMethod = oraxenItemsClass.getMethod("getItem", String.class);
                buildMethod = getItemByIdMethod.getReturnType().getMethod("build");
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().warning("§c[MonsterSpawner] 未找到可用的获取物品方法");
            }
        }
    }
    
    private static void tryFindGetIdByItemMethod() {
        try {
            getIdByItemMethod = oraxenItemsClass.getMethod("getIdByItem", ItemStack.class);
            return;
        } catch (NoSuchMethodException e) {
            // 尝试替代方法
            try {
                getIdByItemMethod = oraxenItemsClass.getMethod("getOraxenId", ItemStack.class);
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().warning("§c[MonsterSpawner] 未找到可用的获取物品ID方法");
            }
        }
    }
    
    /**
     * 检查Oraxen钩子是否启用
     * @return 是否启用
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 检查指定ID是否为Oraxen物品
     * @param itemId Oraxen物品ID
     * @return 是否为Oraxen物品
     */
    public static boolean isOraxenItem(String itemId) {
        if (!enabled || isAnItemMethod == null) return false;
        try {
            return (boolean) isAnItemMethod.invoke(null, itemId);
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[MonsterSpawner] 检查Oraxen物品时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取Oraxen物品
     * @param itemId Oraxen物品ID
     * @param amount 数量
     * @return 物品栈
     */
    public static ItemStack getItemStack(String itemId, int amount) {
        if (!enabled || getItemByIdMethod == null || buildMethod == null) return null;
        try {
            Object itemBuilder = getItemByIdMethod.invoke(null, itemId);
            if (itemBuilder != null) {
                ItemStack item = (ItemStack) buildMethod.invoke(itemBuilder);
                if (item != null) {
                    item.setAmount(amount);
                    return item;
                }
            }
            return null;
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[MonsterSpawner] 获取Oraxen物品时发生错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从物品栈中获取Oraxen物品ID
     * @param item 物品栈
     * @return Oraxen物品ID，如果不是Oraxen物品则返回null
     */
    public static String getOraxenId(ItemStack item) {
        if (!enabled || getIdByItemMethod == null || item == null) return null;
        try {
            Object result = getIdByItemMethod.invoke(null, item);
            if (result instanceof String) {
                return (String) result;
            }
            return null;
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[MonsterSpawner] 获取Oraxen物品ID时发生错误: " + e.getMessage());
            return null;
        }
    }
}
