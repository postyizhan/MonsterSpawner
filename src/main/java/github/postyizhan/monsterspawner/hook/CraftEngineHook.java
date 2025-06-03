package github.postyizhan.monsterspawner.hook;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftEngineHook implements ItemHook {
    @Override
    public String getName() {
        return "craftengine";
    }

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineItems");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ItemStack getItem(String id, Player player) {
        if (!isEnabled() || player == null) return null;
        String[] parts = id.split(":", 2);
        if (parts.length != 2) return null;
        return CraftEngineItems.byId(new Key(parts[0], parts[1])).buildItemStack(player);
    }

    @Override
    public String getItemId(ItemStack item) {
        // CraftEngine doesn't provide a way to get item ID from ItemStack
        return null;
    }
} 