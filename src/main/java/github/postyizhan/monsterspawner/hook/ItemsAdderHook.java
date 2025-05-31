package github.postyizhan.monsterspawner.hook;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderHook {
    
    private static boolean enabled = false;
    
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
                // 直接使用ItemsAdder API
                enabled = true;
                // Bukkit.getLogger().info("§a[MonsterSpawner] 成功连接ItemsAdder插件");
            } else {
                enabled = false;
                // Bukkit.getLogger().warning("§e[MonsterSpawner] 未找到ItemsAdder插件，相关功能已禁用");
            }
        } catch (Exception e) {
            enabled = false;
            Bukkit.getLogger().warning("§c[MonsterSpawner] 初始化ItemsAdder钩子时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static boolean isItemsAdderItem(String itemId) {
        if (!enabled) return false;
        try {
            // 使用ItemsAdder API检查物品是否存在
            return ItemsAdder.isCustomItem(itemId);
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[MonsterSpawner] 检查ItemsAdder物品时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    public static ItemStack getItemStack(String itemId, int amount) {
        if (!enabled) return null;
        try {
            // 使用ItemsAdder API获取物品
            CustomStack customStack = CustomStack.getInstance(itemId);
            if (customStack != null) {
                ItemStack item = customStack.getItemStack();
                if (item != null) {
                    item.setAmount(amount);
                    return item;
                }
            }
            return null;
        } catch (Exception e) {
            Bukkit.getLogger().warning("§c[MonsterSpawner] 获取ItemsAdder物品时发生错误: " + e.getMessage());
            return null;
        }
    }
}
