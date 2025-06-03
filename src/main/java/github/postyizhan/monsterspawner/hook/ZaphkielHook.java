package github.postyizhan.monsterspawner.hook;

import ink.ptms.zaphkiel.ZaphkielAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ZaphkielHook implements ItemHook {
    @Override
    public String getName() {
        return "zaphkiel";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("ink.ptms.zaphkiel.ZaphkielAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled()) return null;
        return ZaphkielAPI.getItemStack(id, player);
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!isEnabled()) return null;
        return ZaphkielAPI.getItemId(item);
    }
} 