package github.postyizhan.monsterspawner.hook;

import github.saukiya.sxitem.SXItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SXItemHook implements ItemHook {
    @Override
    public String getName() {
        return "sxitem";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("github.saukiya.sxitem.SXItem");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled() || player == null) return null;
        return SXItem.getItemManager().getItem(id, player);
    }

    @Override
    public String getItemId(ItemStack item) {
        // SX-Item doesn't provide a way to get item ID from ItemStack
        return null;
    }
} 