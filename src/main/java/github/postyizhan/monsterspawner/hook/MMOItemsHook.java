package github.postyizhan.monsterspawner.hook;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MMOItemsHook implements ItemHook {
    @Override
    public String getName() {
        return "mmoitems";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("net.Indyuce.mmoitems.MMOItems");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled()) return null;
        String[] parts = id.split(":", 2);
        if (parts.length != 2) return null;
        return MMOItems.plugin.getItem(parts[0], parts[1]);
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!isEnabled()) return null;
        String id = MMOItems.plugin.getNBT().getString(item, "MMOITEMS_ITEM_ID");
        String type = MMOItems.plugin.getNBT().getString(item, "MMOITEMS_ITEM_TYPE");
        return id != null && type != null ? type + ":" + id : null;
    }
} 