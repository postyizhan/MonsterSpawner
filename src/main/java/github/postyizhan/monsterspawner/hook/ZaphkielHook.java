package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * Zaphkiel插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含Zaphkiel的jar文件
 */
public class ZaphkielHook extends HookAbstract {

    private Class<?> apiClass;
    private Object apiInstance;

    public ZaphkielHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Zaphkiel";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载Zaphkiel相关类
            apiClass = Class.forName("ink.ptms.zaphkiel.ZaphkielAPI");
            
            // 获取API实例
            apiInstance = apiClass.getMethod("INSTANCE").invoke(null);
            
            return apiInstance != null;
        } catch (Exception e) {
            // Zaphkiel未安装或不可用
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
            Object result = apiClass.getMethod("getItem", String.class).invoke(apiInstance, id);
            if (result != null) {
                // 反射调用build方法获取ItemStack
                Object buildResult = result.getClass().getMethod("buildItemStack").invoke(result);
                if (buildResult instanceof ItemStack) {
                    ItemStack item = (ItemStack) buildResult;
                    item.setAmount(amount);
                    return item;
                }
            }
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
            // 反射调用getItem方法获取物品ID
            Object item = apiClass.getMethod("getItem", ItemStack.class).invoke(apiInstance, itemStack);
            if (item != null) {
                return (String) item.getClass().getMethod("getId").invoke(item);
            }
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
            return apiClass.getMethod("getItem", String.class).invoke(apiInstance, id) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
