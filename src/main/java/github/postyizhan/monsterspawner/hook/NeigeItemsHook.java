package github.postyizhan.monsterspawner.hook;

import pers.neige.neigeitems.manager.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NeigeItemsHook implements ItemHook {
    @Override
    public String getName() {
        return "neigeitems";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("pers.neige.neigeitems.manager.ItemManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled()) return null;
        return ItemManager.getItemStack(id);
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!isEnabled()) return null;
        return ItemManager.isNiItem(item) != null ? ItemManager.isNiItem(item).getId() : null;
    }
} 