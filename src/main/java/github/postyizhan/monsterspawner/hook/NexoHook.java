package github.postyizhan.monsterspawner.hook;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NexoHook implements ItemHook {
    @Override
    public String getName() {
        return "nexo";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("com.nexomc.nexo.api.NexoItems");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled()) return null;
        return NexoItems.itemFromId(id) != null ? NexoItems.itemFromId(id).build() : null;
    }

    @Override
    public String getItemId(ItemStack item) {
        if (!isEnabled()) return null;
        return NexoItems.idFromItem(item);
    }
} 