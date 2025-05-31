package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ItemsAdderHook {
    
    private static boolean enabled = false;
    private static Class<?> iaClass;
    private static Method getItemMethod;
    private static Method isCustomItemMethod;
    
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
                iaClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
                getItemMethod = iaClass.getMethod("getCustomItem", String.class, int.class);
                isCustomItemMethod = iaClass.getMethod("isCustomItem", String.class);
                enabled = true;
            }
        } catch (Exception e) {
            enabled = false;
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static boolean isItemsAdderItem(String itemId) {
        if (!enabled) return false;
        try {
            return (boolean) isCustomItemMethod.invoke(null, itemId);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static ItemStack getItemStack(String itemId, int amount) {
        if (!enabled) return null;
        try {
            return (ItemStack) getItemMethod.invoke(null, itemId, amount);
        } catch (Exception e) {
            return null;
        }
    }
} 