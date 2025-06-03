package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * 插件钩子抽象基类
 * 提供基本的钩子功能和接口
 */
public abstract class HookAbstract {

    /**
     * 获取插件名称
     * @return 插件名称
     */
    public abstract String getName();

    /**
     * 获取插件命名空间
     * @return 插件命名空间，用于物品ID前缀
     */
    public String getNamespace() {
        return getName().toLowerCase();
    }

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(getName());
    }

    /**
     * 检查插件是否已被钩子连接
     * @return 是否成功连接
     */
    public boolean isHooked() {
        Plugin plugin = getPlugin();
        return plugin != null && plugin.isEnabled();
    }

    /**
     * 检查钩子是否已启用并可用
     * @return 是否已启用
     */
    public boolean checkHooked() {
        if (isHooked()) {
            return true;
        } else {
            reportAbuse();
            return false;
        }
    }

    /**
     * 报告未启用钩子的滥用情况
     */
    public void reportAbuse() {
        Bukkit.getLogger().warning("§c[MonsterSpawner] 尝试使用未启用的钩子: " + getName());
    }
    
    /**
     * 初始化钩子
     * @return 是否初始化成功
     */
    public abstract boolean initialize();

    /**
     * 根据ID获取物品
     * @param id 物品ID
     * @param amount 数量
     * @return 物品实例，如果获取失败则返回null
     */
    public abstract ItemStack getItem(String id, int amount);

    /**
     * 从物品实例获取物品ID
     * @param itemStack 物品实例
     * @return 物品ID，如果不属于该钩子管理的物品则返回null
     */
    public abstract String getId(ItemStack itemStack);
    
    /**
     * 检查物品是否属于该钩子管理的物品
     * @param id 物品ID
     * @return 是否为该钩子管理的物品
     */
    public abstract boolean isCustomItem(String id);
} 