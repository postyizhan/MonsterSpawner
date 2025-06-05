package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.inventory.ItemStack;

/**
 * MMOItems插件钩子实现
 * 使用运行时依赖方式，无需在libs中包含MMOItems的jar文件
 */
public class MMOItemsHook extends HookAbstract {

    private Class<?> mmoItemsClass;
    private Object mmoItemsPlugin;

    public MMOItemsHook(MonsterSpawner plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "MMOItems";
    }

    @Override
    protected boolean initHook() {
        try {
            // 尝试加载MMOItems主类
            mmoItemsClass = Class.forName("net.Indyuce.mmoitems.MMOItems");
            
            // 获取插件实例
            mmoItemsPlugin = mmoItemsClass.getField("plugin").get(null);
            
            return mmoItemsPlugin != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            // 捕获可能的异常，表示MMOItems未安装或不可用
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, int amount) {
        if (!checkHooked()) {
            return null;
        }
        
        try {
            // 解析类型和ID
            String[] parts = id.split(":", 2);
            if (parts.length != 2) {
                return null;
            }
            
            String type = parts[0];
            String itemId = parts[1];
            
            // 反射调用MMOItems的getItem方法
            Object result = mmoItemsPlugin.getClass().getMethod("getItem", String.class, String.class)
                    .invoke(mmoItemsPlugin, type, itemId);
            
            if (result instanceof ItemStack) {
                ItemStack item = (ItemStack) result;
                item.setAmount(amount);
                return item;
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
        if (!checkHooked()) {
            return null;
        }
        
        try {
            // 获取NBT API
            Object nbtAPI = mmoItemsPlugin.getClass().getMethod("getNBT").invoke(mmoItemsPlugin);
            
            // 获取物品ID和类型
            String id = (String) nbtAPI.getClass().getMethod("getString", ItemStack.class, String.class)
                    .invoke(nbtAPI, itemStack, "MMOITEMS_ITEM_ID");
            String type = (String) nbtAPI.getClass().getMethod("getString", ItemStack.class, String.class)
                    .invoke(nbtAPI, itemStack, "MMOITEMS_ITEM_TYPE");
            
            // 如果都不为空，返回类型:ID格式
            if (id != null && type != null) {
                return type + ":" + id;
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
        // MMOItems通常使用类型:ID格式，这里我们简单检查是否包含:
        return id.contains(":");
    }
}
