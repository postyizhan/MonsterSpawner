package github.postyizhan.monsterspawner.hook;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MythicMobsHook implements ItemHook {
    @Override
    public String getName() {
        return "mythicmobs";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled()) return null;
        return MythicBukkit.inst().getItemManager().getItemStack(id);
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!isEnabled()) return null;
        return MythicBukkit.inst().getItemManager().getItemStackId(item);
    }
} 