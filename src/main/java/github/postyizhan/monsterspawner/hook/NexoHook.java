package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * Nexo插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含Nexo的jar文件
 */
public class NexoHook extends HookAbstract {

    private Class<?> nexoApiClass;
    private Object nexoApi;

    public NexoHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Nexo";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载Nexo相关类
            nexoApiClass = Class.forName("pers.nexo.api.NexoAPI");
            
            // 获取API实例
            nexoApi = nexoApiClass.getMethod("getInstance").invoke(null);
            
            return nexoApi != null;
        } catch (Exception e) {
            // Nexo未安装或不可用
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
            Object result = nexoApiClass.getMethod("getItem", String.class, int.class).invoke(nexoApi, id, amount);
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
    public String getId(ItemStack itemStack) {
        if (!checkHooked() || itemStack == null) {
            return null;
        }
        
        try {
            // 反射调用getItemId方法
            return (String) nexoApiClass.getMethod("getItemId", ItemStack.class).invoke(nexoApi, itemStack);
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
