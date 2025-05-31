package github.postyizhan.monsterspawner.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class PlaceholderAPIHook {
    
    private static boolean enabled = false;
    private static Method setPlaceholdersMethod;
    
    public static void initialize() {
        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                setPlaceholdersMethod = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                enabled = true;
            }
        } catch (Exception e) {
            enabled = false;
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static String setPlaceholders(Player player, String text) {
        if (!enabled || player == null) return text;
        try {
            return (String) setPlaceholdersMethod.invoke(null, player, text);
        } catch (Exception e) {
            return text;
        }
    }
} 