package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * ItemsAdder插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含ItemsAdder的jar文件
 */
public class ItemsAdderHook extends HookAbstract {

    private Class<?> customStackClass;

    public ItemsAdderHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "ItemsAdder";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载ItemsAdder主类
            customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            return true;
        } catch (ClassNotFoundException e) {
            // ItemsAdder未安装或不可用
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        
        try {
            // 反射调用CustomStack.getInstance方法
            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, id);
            if (customStack == null) {
                return null;
            }
            
            // 获取ItemStack
            ItemStack itemStack = (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);
            if (itemStack != null) {
                itemStack.setAmount(amount);
                return itemStack;
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
            // 反射调用CustomStack.byItemStack方法
            Object customStack = customStackClass.getMethod("byItemStack", ItemStack.class).invoke(null, itemStack);
            if (customStack != null) {
                return (String) customStackClass.getMethod("getId").invoke(customStack);
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
        // ItemsAdder的ID通常是命名空间:物品ID格式
        return id.contains(":");
    }
}
