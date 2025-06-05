package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * Oraxen插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含Oraxen的jar文件
 */
public class OraxenHook extends HookAbstract {

    private Class<?> oraxenItemsClass;
    private Class<?> oraxenAPIClass;
    private Object oraxenAPI;

    public OraxenHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Oraxen";
    }

    @Override
    protected boolean initHook() {
        // 尝试新版API路径
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 Oraxen 新版API");
            
            // 尝试加载OraxenAPI类
            oraxenAPIClass = Class.forName("io.th0rgal.oraxen.api.OraxenAPI");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 OraxenAPI 类");
            
            // 获取OraxenAPI实例 (可能是静态方法)
            try {
                oraxenAPI = oraxenAPIClass.getMethod("getInstance").invoke(null);
                plugin.getLogger().info("§a[MonsterSpawner] 成功获取 OraxenAPI 实例");
            } catch (Exception e) {
                // 如果getInstance不存在，直接使用静态方法
                oraxenAPI = oraxenAPIClass;
                plugin.getLogger().info("§a[MonsterSpawner] 将使用 OraxenAPI 静态方法");
            }
            
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 OraxenAPI 类，尝试其他路径");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 OraxenAPI 时出错: " + e.getMessage());
        }
        
        // 尝试旧版OraxenItems路径
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 Oraxen 旧版API (OraxenItems)");
            
            // 尝试加载OraxenItems类
            oraxenItemsClass = Class.forName("io.th0rgal.oraxen.items.OraxenItems");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 OraxenItems 类");
            
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 OraxenItems 类");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 OraxenItems 时出错: " + e.getMessage());
        }
        
        // 尝试API包中的OraxenItems路径
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 Oraxen API包中的 OraxenItems 类");
            
            // 尝试加载API包中的OraxenItems类
            oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 API包中的 OraxenItems 类");
            
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 API包中的 OraxenItems 类");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 API包中的 OraxenItems 时出错: " + e.getMessage());
        }
        
        plugin.getLogger().warning("§c[MonsterSpawner] 所有 Oraxen API 路径都尝试失败");
        return false;
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        try {
            Object result = null;
            // 1. 新API
            if (oraxenAPIClass != null) {
                try {
                    result = oraxenAPIClass.getMethod("getItemById", String.class).invoke(oraxenAPI, id);
                } catch (NoSuchMethodException e) {
                    result = oraxenAPIClass.getMethod("getItemById", String.class).invoke(null, id);
                }
            } else if (oraxenItemsClass != null) {
                // 2. 旧API
                result = oraxenItemsClass.getMethod("getItemById", String.class).invoke(null, id);
            }
            // 兼容ItemStack和ItemBuilder
            if (result instanceof ItemStack) {
                ((ItemStack) result).setAmount(amount);
                return (ItemStack) result;
            } else if (result != null && result.getClass().getSimpleName().equals("ItemBuilder")) {
                // 反射调用build方法，获取ItemStack
                Object built = result.getClass().getMethod("build").invoke(result);
                if (built instanceof ItemStack) {
                    ((ItemStack) built).setAmount(amount);
                    return (ItemStack) built;
                }
            }
        } catch (Exception e) {
            // 捕获异常并输出调试信息
            if (isHooked()) {
                plugin.getLogger().warning("§c[MonsterSpawner] 获取Oraxen物品时出错: " + id);
                plugin.getLogger().warning("§c[MonsterSpawner] 错误详情: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String getId(ItemStack itemStack) {
        if (!checkHooked() || itemStack == null) {
            return null;
        }
        
        try {
            // 尝试通过不同的API获取物品ID
            if (oraxenAPIClass != null) {
                // 使用OraxenAPI
                try {
                    // 尝试实例方法
                    return (String) oraxenAPIClass.getMethod("getIdByItem", ItemStack.class).invoke(oraxenAPI, itemStack);
                } catch (NoSuchMethodException e) {
                    // 尝试静态方法
                    return (String) oraxenAPIClass.getMethod("getIdByItem", ItemStack.class).invoke(null, itemStack);
                }
            } else if (oraxenItemsClass != null) {
                // 使用OraxenItems类
                return (String) oraxenItemsClass.getMethod("getIdByItem", ItemStack.class).invoke(null, itemStack);
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            if (isHooked()) {
                plugin.getLogger().warning("§c[MonsterSpawner] 获取Oraxen物品ID时出错");
                plugin.getLogger().warning("§c[MonsterSpawner] 错误详情: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }

    @Override
    public boolean isCustomItem(String id) {
        if (!checkHooked()) {
            return false;
        }
        
        try {
            // 尝试通过不同的API检查物品是否存在
            if (oraxenAPIClass != null) {
                // 使用OraxenAPI
                try {
                    // 尝试实例方法
                    return (boolean) oraxenAPIClass.getMethod("exists", String.class).invoke(oraxenAPI, id);
                } catch (NoSuchMethodException e) {
                    // 尝试静态方法
                    return (boolean) oraxenAPIClass.getMethod("exists", String.class).invoke(null, id);
                }
            } else if (oraxenItemsClass != null) {
                // 使用OraxenItems类
                return (boolean) oraxenItemsClass.getMethod("exists", String.class).invoke(null, id);
            }
            
            // 如果上述方法都失败，尝试获取物品
            return getItem(id, 1) != null;
        } catch (Exception e) {
            // 尝试获取物品
            return getItem(id, 1) != null;
        }
    }
}
