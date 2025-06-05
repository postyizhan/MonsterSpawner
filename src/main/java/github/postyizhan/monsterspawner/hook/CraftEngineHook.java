package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * CraftEngine插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含CraftEngine的jar文件
 */
public class CraftEngineHook extends HookAbstract {

    private Class<?> coreApiClass;
    private Object coreApi;
    private Class<?> itemManagerClass;
    private Object itemManager;

    public CraftEngineHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "CraftEngine";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载CraftEngine相关类
            coreApiClass = Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineAPI");
            
            // 获取API实例
            coreApi = coreApiClass.getMethod("getInstance").invoke(null);
            
            // 获取ItemManager
            itemManagerClass = Class.forName("net.momirealms.craftengine.bukkit.item.ItemManagerImpl");
            itemManager = coreApiClass.getMethod("getItemManager").invoke(coreApi);
            
            return itemManager != null;
        } catch (Exception e) {
            // CraftEngine未安装或不可用
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
            Object result = itemManagerClass.getMethod("getItem", String.class, int.class).invoke(itemManager, id, amount);
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
            // 反射调用getItemForPlayer方法
            Object result = itemManagerClass.getMethod("getItemForPlayer", String.class, Player.class, int.class)
                    .invoke(itemManager, id, player, amount);
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
            return (String) itemManagerClass.getMethod("getItemId", ItemStack.class).invoke(itemManager, itemStack);
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
