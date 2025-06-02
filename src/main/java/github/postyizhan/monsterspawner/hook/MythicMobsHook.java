package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class MythicMobsHook {
    
    private static boolean enabled = false;
    private static Object instance;
    private static Object itemManager;
    private static Method getItemStackMethod;
    
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
                Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Method instMethod = mythicBukkitClass.getMethod("inst");
                instance = instMethod.invoke(null);
                
                Method getItemManagerMethod = mythicBukkitClass.getMethod("getItemManager");
                itemManager = getItemManagerMethod.invoke(instance);
                
                Class<?> itemManagerClass = itemManager.getClass();
                getItemStackMethod = itemManagerClass.getMethod("getItemStack", String.class, int.class);
                
                enabled = true;
            }
        } catch (Exception e) {
            enabled = false;
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static ItemStack getItemStack(String itemId, int amount) {
        if (!enabled) return null;
        try {
            return (ItemStack) getItemStackMethod.invoke(itemManager, itemId, amount);
        } catch (Exception e) {
            return null;
        }
    }
} 