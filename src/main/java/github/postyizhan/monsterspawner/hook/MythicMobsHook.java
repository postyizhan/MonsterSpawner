package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * MythicMobs插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含MythicMobs的jar文件
 */
public class MythicMobsHook extends HookAbstract {

    private Class<?> mythicLibClass;
    private Class<?> itemManagerClass;
    private Object itemManager;
    private boolean legacyMode = false;

    public MythicMobsHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "MythicMobs";
    }

    @Override
    protected boolean initHook() {
        // 尝试新版API (MythicLib)
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 MythicMobs 新版API (MythicLib)");
            
            // 尝试加载MythicLib类
            mythicLibClass = Class.forName("io.lumine.mythic.lib.MythicLib");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 MythicLib 类");
            
            // 获取MythicLib实例
            Object mythicLib = mythicLibClass.getMethod("inst").invoke(null);
            plugin.getLogger().info("§a[MonsterSpawner] 成功获取 MythicLib 实例");
            
            // 获取ItemManager
            itemManager = mythicLibClass.getMethod("getItemManager").invoke(mythicLib);
            
            if (itemManager != null) {
                itemManagerClass = itemManager.getClass();
                plugin.getLogger().info("§a[MonsterSpawner] 成功获取 MythicLib ItemManager 实例");
                return true;
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 MythicLib 类，尝试旧版API");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 MythicLib 时出错: " + e.getMessage());
        }
        
        // 尝试旧版API (MythicBukkit)
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 MythicMobs 旧版API (MythicBukkit)");
            
            // 尝试加载MythicBukkit类
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 MythicBukkit 类");
            
            // 获取MythicBukkit实例
            Object mythicBukkit = mythicBukkitClass.getMethod("inst").invoke(null);
            plugin.getLogger().info("§a[MonsterSpawner] 成功获取 MythicBukkit 实例");
            
            // 获取ItemManager
            itemManager = mythicBukkitClass.getMethod("getItemManager").invoke(mythicBukkit);
            
            if (itemManager != null) {
                itemManagerClass = itemManager.getClass();
                legacyMode = true;
                plugin.getLogger().info("§a[MonsterSpawner] 成功获取 MythicBukkit ItemManager 实例");
                return true;
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 MythicBukkit 类，尝试更旧版本");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 MythicBukkit 时出错: " + e.getMessage());
        }
        
        // 尝试最旧版API (直接MythicMobs类)
        try {
            plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 MythicMobs 最旧版API");
            
            // 尝试加载MythicMobs类
            Class<?> mythicMobsClass = Class.forName("net.elseland.xikage.MythicMobs.MythicMobs");
            plugin.getLogger().info("§a[MonsterSpawner] 成功加载 旧版MythicMobs 类");
            
            // 获取MythicMobs实例
            Object mythicMobs = mythicMobsClass.getMethod("getInstance").invoke(null);
            plugin.getLogger().info("§a[MonsterSpawner] 成功获取 旧版MythicMobs 实例");
            
            // 获取ItemManager
            itemManager = mythicMobsClass.getMethod("getItemManager").invoke(mythicMobs);
            
            if (itemManager != null) {
                itemManagerClass = itemManager.getClass();
                legacyMode = true;
                plugin.getLogger().info("§a[MonsterSpawner] 成功获取 旧版MythicMobs ItemManager 实例");
                return true;
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("§c[MonsterSpawner] 未找到 旧版MythicMobs 类");
        } catch (Exception e) {
            plugin.getLogger().info("§c[MonsterSpawner] 初始化 旧版MythicMobs 时出错: " + e.getMessage());
        }
        
        plugin.getLogger().warning("§c[MonsterSpawner] 所有 MythicMobs API 路径都尝试失败");
        return false;
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        
        try {
            // 根据版本调用不同的方法
            ItemStack result;
            if (legacyMode) {
                // 旧版API
                result = (ItemStack) itemManagerClass.getMethod("getItemStack", String.class).invoke(itemManager, id);
            } else {
                // 新版API
                result = (ItemStack) itemManagerClass.getMethod("getItemStack", String.class).invoke(itemManager, id);
            }
            
            if (result != null) {
                result.setAmount(amount);
                return result;
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            if (isHooked()) {
                plugin.getLogger().warning("§c[MonsterSpawner] 获取MythicMobs物品时出错: " + id);
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
            // 根据版本调用不同的方法
            if (legacyMode) {
                // 旧版API
                boolean isMythicItem = (boolean) itemManagerClass.getMethod("isMythicItem", ItemStack.class).invoke(itemManager, itemStack);
                if (isMythicItem) {
                    return (String) itemManagerClass.getMethod("getMythicTypeFromItem", ItemStack.class).invoke(itemManager, itemStack);
                }
            } else {
                // 新版API
                boolean isMythicItem = (boolean) itemManagerClass.getMethod("isMythicItem", ItemStack.class).invoke(itemManager, itemStack);
                if (isMythicItem) {
                    return (String) itemManagerClass.getMethod("getMythicTypeId", ItemStack.class).invoke(itemManager, itemStack);
                }
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            if (isHooked()) {
                plugin.getLogger().warning("§c[MonsterSpawner] 获取MythicMobs物品ID时出错");
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
            // 尝试获取物品，如果不为null则表示是自定义物品
            return getItem(id, 1) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
