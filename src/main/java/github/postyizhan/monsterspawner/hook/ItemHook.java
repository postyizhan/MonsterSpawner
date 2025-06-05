package github.postyizhan.monsterspawner.hook;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ItemHook {
    /**
     * Get the name of this hook
     * @return The hook name
     */
    String getName();

    /**
     * Check if this hook is enabled
     * @return true if the hook is enabled
     */
    boolean isEnabled();

    /**
     * Get an item from this hook
     * @param id The item ID
     * @param player The player (may be null)
     * @return The item stack, or null if not found
     */
    ItemStack getItem(String id, Player player);

    /**
     * Get the ID of an item from this hook
     * @param item The item stack
     * @return The item ID, or null if not found
     */
    String getItemId(ItemStack item);
}
