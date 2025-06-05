package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * SX-Item插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含SX-Item的jar文件
 */
public class SXItemHook extends HookAbstract {

    private Class<?> sxItemClass;
    private Object sxItemInstance;

    public SXItemHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "SX-Item";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载SX-Item相关类
            sxItemClass = Class.forName("com.github.suxianboao.sxitem.api.SXItemApi");
            
            // 获取API实例
            sxItemInstance = sxItemClass.getMethod("getInstance").invoke(null);
            
            return sxItemInstance != null;
        } catch (Exception e) {
            // SX-Item未安装或不可用
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        
        try {
            // 反射调用getItem方法
            Object result = sxItemClass.getMethod("getItem", String.class, int.class).invoke(sxItemInstance, id, amount);
            return (ItemStack) result;
        } catch (Exception e) {
            // 记录错误但不抛出异常
            if (isHooked()) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    @Override
    public ItemStack getItemForPlayer(String id, Player player, int amount) {
        if (!checkHooked() || player == null) {
            return getItem(id, amount);
        }
        
        try {
            // 反射调用getItem方法(针对玩家的版本)
            Object result = sxItemClass.getMethod("getItem", String.class, Player.class, int.class)
                    .invoke(sxItemInstance, id, player, amount);
            return (ItemStack) result;
        } catch (Exception e) {
            // 如果失败，尝试使用基本方法
            return getItem(id, amount);
        }
    }

    @Override
    public String getId(ItemStack itemStack) {
        if (!checkHooked() || itemStack == null) {
            return null;
        }
        
        try {
            // 反射调用getItemId方法
            return (String) sxItemClass.getMethod("getItemId", ItemStack.class).invoke(sxItemInstance, itemStack);
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
