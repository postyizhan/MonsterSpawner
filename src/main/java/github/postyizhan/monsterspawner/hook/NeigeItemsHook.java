package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NeigeItemsHook {
    
    private static boolean enabled = false;
    private static Object itemManagerInstance;
    private static Class<?> itemManagerClass;
    private static java.lang.reflect.Method getItemStackMethod;
    private static java.lang.reflect.Method getItemStackWithPlayerMethod;
    
    /**
     * 初始化NeigeItems钩子
     */
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("NeigeItems") != null) {
                // 获取ItemManager类
                itemManagerClass = Class.forName("pers.neige.neigeitems.manager.ItemManager");
                
                // 获取INSTANCE静态字段
                java.lang.reflect.Field instanceField = itemManagerClass.getField("INSTANCE");
                itemManagerInstance = instanceField.get(null);
                
                // 获取getItemStack(String)方法
                try {
                    getItemStackMethod = itemManagerClass.getMethod("getItemStack", String.class);
                    
                    // 尝试获取getItemStack(String, OfflinePlayer)方法
                    try {
                        getItemStackWithPlayerMethod = itemManagerClass.getMethod("getItemStack", String.class, org.bukkit.OfflinePlayer.class);
                    } catch (NoSuchMethodException e) {
                        Bukkit.getLogger().warning("[MonsterSpawner] 无法找到getItemStack(String, OfflinePlayer)方法，将仅使用基本方法");
                    }
                    
                    enabled = true;
                } catch (NoSuchMethodException e) {
                    Bukkit.getLogger().warning("[MonsterSpawner] 无法找到基本的getItemStack方法: " + e.getMessage());
                    Bukkit.getLogger().info("[MonsterSpawner] 尝试获取替代方法...");
                    
                    // 列出所有可用的方法
                    Bukkit.getLogger().info("[MonsterSpawner] 可用方法列表:");
                    for (java.lang.reflect.Method method : itemManagerClass.getMethods()) {
                        if (method.getName().equals("getItemStack")) {
                            Bukkit.getLogger().info("  - " + method.getName() + "(" 
                                + java.util.Arrays.stream(method.getParameterTypes())
                                    .map(Class::getName)
                                    .collect(java.util.stream.Collectors.joining(", ")) 
                                + ")");
                        }
                    }
                    
                    Bukkit.getLogger().severe("[MonsterSpawner] 无法找到基本的getItemStack方法，NeigeItems支持将不可用");
                    return;
                }
            } else {
                Bukkit.getLogger().warning("[MonsterSpawner] 未检测到NeigeItems插件");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[MonsterSpawner] 连接NeigeItems插件失败: " + e.getMessage());
            e.printStackTrace();
            enabled = false;
        }
    }
    
    /**
     * 检查NeigeItems钩子是否启用
     * @return 是否启用
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取NeigeItems物品
     * @param itemId 物品ID
     * @param amount 数量（NeigeItems API不支持直接设置数量，会被忽略）
     * @return 物品实例
     */
    public static ItemStack getItemStack(String itemId, int amount) {
        if (!enabled) {
            Bukkit.getLogger().warning("[MonsterSpawner] NeigeItems钩子未启用，无法获取物品: " + itemId);
            return null;
        }
        try {
            // 调用ItemManager.INSTANCE.getItemStack(itemId)
            ItemStack result = (ItemStack) getItemStackMethod.invoke(itemManagerInstance, itemId);
            if (result == null) {
                Bukkit.getLogger().warning("[MonsterSpawner] NeigeItems返回了null物品，物品ID可能不存在: " + itemId);
            } else {
                // 手动设置数量，因为API不支持直接设置
                if (amount > 1) {
                    result.setAmount(amount);
                }
            }
            return result;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[MonsterSpawner] 获取NeigeItems物品失败: " + itemId + " - " + e.getMessage());
            if (Bukkit.getPluginManager().getPlugin("MonsterSpawner").getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /**
     * 获取NeigeItems物品（为特定玩家）
     * @param itemId 物品ID
     * @param player 玩家
     * @param amount 数量（NeigeItems API不支持直接设置数量，会被忽略）
     * @return 物品实例
     */
    public static ItemStack getItemStack(String itemId, Player player, int amount) {
        if (!enabled) {
            Bukkit.getLogger().warning("[MonsterSpawner] NeigeItems钩子未启用，无法获取物品: " + itemId);
            return null;
        }
        try {
            ItemStack result;
            // 检查是否有带玩家参数的方法
            if (getItemStackWithPlayerMethod != null) {
                // 调用ItemManager.INSTANCE.getItemStack(itemId, player)
                result = (ItemStack) getItemStackWithPlayerMethod.invoke(itemManagerInstance, itemId, player);
            } else {
                // 回退到基本方法
                result = (ItemStack) getItemStackMethod.invoke(itemManagerInstance, itemId);
            }
            
            if (result == null) {
                Bukkit.getLogger().warning("[MonsterSpawner] NeigeItems返回了null物品，物品ID可能不存在: " + itemId);
            } else {
                // 手动设置数量，因为API不支持直接设置
                if (amount > 1) {
                    result.setAmount(amount);
                }
            }
            return result;
        } catch (Exception e) {
            Bukkit.getLogger().severe("[MonsterSpawner] 获取NeigeItems物品失败: " + itemId + " - " + e.getMessage());
            if (Bukkit.getPluginManager().getPlugin("MonsterSpawner").getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return null;
        }
    }
} 