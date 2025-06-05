package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * MagicGem插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含MagicGem的jar文件
 */
public class MagicGemHook extends HookAbstract {

    private Class<?> magicGemClass;
    private Object magicGemInstance;

    public MagicGemHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "MagicGem";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载MagicGem相关类
            magicGemClass = Class.forName("com.github.steanky.magicgem.api.MagicGemAPI");
            
            // 获取API实例
            magicGemInstance = magicGemClass.getMethod("getInstance").invoke(null);
            
            return magicGemInstance != null;
        } catch (Exception e) {
            // MagicGem未安装或不可用
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
            Object result = magicGemClass.getMethod("getItem", String.class, int.class).invoke(magicGemInstance, id, amount);
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
            return (String) magicGemClass.getMethod("getItemId", ItemStack.class).invoke(magicGemInstance, itemStack);
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
