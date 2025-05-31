package github.postyizhan.monsterspawner.listener;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 监听刷怪笼放置事件，设置实体类型
 */
public class SpawnerPlaceListener implements Listener {
    
    private final MonsterSpawner plugin;
    private final Pattern entityTypePattern = Pattern.compile("MS-EntityType:(.+)");
    
    public SpawnerPlaceListener(MonsterSpawner plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // 检查是否放置了刷怪笼
        if (block.getType() != Material.SPAWNER) {
            return;
        }
        
        // 获取玩家手中的物品
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.SPAWNER) {
            return;
        }
        
        // 增加放置的刷怪笼计数
        plugin.incrementSpawnersPlaced();
        
        // 尝试从物品Lore中获取实体类型信息
        EntityType entityType = getEntityTypeFromItem(item);
        if (entityType == null) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info(plugin.getLanguageManager().getString("system.debug.spawner.default_type"));
            }
            return;
        }
        
        // 设置刷怪笼实体类型
        try {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            spawner.setSpawnedType(entityType);
            spawner.update();
            
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info(plugin.getLanguageManager().getString("system.debug.spawner.set_type_success")
                        .replace("%entity_type%", entityType.name()));
            }
            
            // 通知玩家
            player.sendMessage(plugin.getLanguageManager().getMessage("spawner.place-success")
                    .replace("%entity_type%", entityType.name()));
        } catch (Exception e) {
            plugin.getLogger().warning(plugin.getLanguageManager().getString("system.debug.spawner.set_entity_type_error")
                    .replace("%error%", e.getMessage()));
            if (plugin.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 从物品Lore中获取实体类型信息
     */
    private EntityType getEntityTypeFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasLore()) {
                return null;
            }
            
            List<String> lore = meta.getLore();
            for (String line : lore) {
                // 移除颜色代码以便匹配
                String cleanLine = ChatColor.stripColor(line);
                Matcher matcher = entityTypePattern.matcher(cleanLine);
                
                if (matcher.find()) {
                    String entityTypeName = matcher.group(1);
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().info(plugin.getLanguageManager().getString("system.debug.spawner.lore_found_type")
                                .replace("%entity_type%", entityTypeName));
                    }
                    
                    try {
                        return EntityType.valueOf(entityTypeName);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning(plugin.getLanguageManager().getString("system.debug.spawner.invalid_type")
                                .replace("%entity_type%", entityTypeName));
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning(plugin.getLanguageManager().getString("system.debug.spawner.read_lore_error")
                    .replace("%error%", e.getMessage()));
            if (plugin.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
} 