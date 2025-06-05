package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * NeigeItems插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含NeigeItems的jar文件
 */
public class NeigeItemsHook extends HookAbstract {

    private Class<?> neigeAPIClass;
    private Object neigeAPI;

    public NeigeItemsHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "NeigeItems";
    }

    @Override
    protected boolean initHook() {
        // 尝试不同的类路径
        String[] possibleClassPaths = {
            "pers.neige.neigeitems.utils.NeigeItemsUtils",    // 新版路径
            "pers.neige.neigeitems.manager.ItemManager",      // 旧版路径
            "pers.neige.neigeitems.Main",                     // 主类
            "pers.neige.neigeitems.api.ItemAPI"               // API类
        };
        
        for (String classPath : possibleClassPaths) {
            try {
                plugin.getLogger().info("§a[MonsterSpawner] 尝试加载 NeigeItems 类: " + classPath);
                
                // 尝试加载类
                Class<?> clazz = Class.forName(classPath);
                
                // 根据不同类路径获取API实例
                if (classPath.endsWith("NeigeItemsUtils")) {
                    neigeAPIClass = clazz;
                    try {
                        // 先尝试静态INSTANCE字段
                        neigeAPI = neigeAPIClass.getField("INSTANCE").get(null);
                        plugin.getLogger().info("§a[MonsterSpawner] 成功通过INSTANCE字段获取NeigeItems API");
                    } catch (Exception e) {
                        // 再尝试getInstance方法
                        neigeAPI = neigeAPIClass.getMethod("getInstance").invoke(null);
                        plugin.getLogger().info("§a[MonsterSpawner] 成功通过getInstance方法获取NeigeItems API");
                    }
                } else if (classPath.endsWith("ItemManager")) {
                    // 如果是ItemManager，直接使用静态方法
                    neigeAPIClass = clazz;
                    neigeAPI = clazz;
                    plugin.getLogger().info("§a[MonsterSpawner] 成功加载NeigeItems ItemManager类");
                } else if (classPath.endsWith("ItemAPI")) {
                    neigeAPIClass = clazz;
                    try {
                        neigeAPI = neigeAPIClass.getMethod("getInstance").invoke(null);
                        plugin.getLogger().info("§a[MonsterSpawner] 成功通过getInstance方法获取NeigeItems ItemAPI");
                    } catch (Exception e) {
                        neigeAPI = clazz;
                        plugin.getLogger().info("§a[MonsterSpawner] 将使用NeigeItems ItemAPI类的静态方法");
                    }
                } else {
                    // 主类
                    neigeAPIClass = clazz;
                    neigeAPI = clazz.getMethod("getInstance").invoke(null);
                    plugin.getLogger().info("§a[MonsterSpawner] 成功获取NeigeItems Main实例");
                }
                
                return true;
            } catch (ClassNotFoundException e) {
                plugin.getLogger().info("§c[MonsterSpawner] 未找到NeigeItems类: " + classPath);
            } catch (Exception e) {
                plugin.getLogger().info("§c[MonsterSpawner] 初始化NeigeItems时出错: " + classPath);
                plugin.getLogger().info("§c[MonsterSpawner] 错误详情: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
            }
        }
        
        plugin.getLogger().warning("§c[MonsterSpawner] 所有NeigeItems API路径都尝试失败");
        return false;
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        try {
            Object result = null;
            try {
                // 优先尝试实例方法
                result = neigeAPIClass.getMethod("getItemStack", String.class, int.class).invoke(neigeAPI, id, amount);
            } catch (NoSuchMethodException e) {
                // 再尝试静态方法
                result = neigeAPIClass.getMethod("getItemStack", String.class).invoke(null, id);
                if (result instanceof ItemStack) {
                    ((ItemStack) result).setAmount(amount);
                }
            }
            // 判空处理，避免NPE
            if (result == null) {
                // 获取不到物品，直接返回空物品
                return new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK, 1);
            }
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            } else {
                // 类型不符，输出日志
                plugin.getLogger().warning("§c[MonsterSpawner] NeigeItems返回了非ItemStack类型: " + result.getClass().getName());
            }
        } catch (Exception e) {
            if (isHooked()) {
                plugin.getLogger().warning("§c[MonsterSpawner] 获取NeigeItems物品时出错: " + id);
                plugin.getLogger().warning("§c[MonsterSpawner] 错误详情: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
            }
        }
        // 获取失败时返回一个默认的"空物品"，避免NPE
        return new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK, 1);
    }
    
    @Override
    public ItemStack getItemForPlayer(String id, Player player, int amount) {
        if (!checkHooked() || player == null) {
            return getItem(id, amount);
        }
        try {
            Object result = neigeAPIClass.getMethod("getItemStack", String.class, Player.class, int.class)
                    .invoke(neigeAPI, id, player, amount);
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            }
        } catch (Exception e) {
            // 获取失败时降级为基础方法
            return getItem(id, amount);
        }
        // 获取失败时返回一个默认的"空物品"，避免NPE
        return new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK, 1);
    }

    @Override
    public String getId(ItemStack itemStack) {
        if (!checkHooked() || itemStack == null) {
            return null;
        }
        
        try {
            // 反射调用getItemID方法
            return (String) neigeAPIClass.getMethod("getItemID", ItemStack.class).invoke(neigeAPI, itemStack);
        } catch (Exception e) {
            // 记录错误但不抛出异常
            if (isHooked()) {
                e.printStackTrace();
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
