package github.postyizhan.monsterspawner.listener;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpawnerListener implements Listener {

    private final MonsterSpawner plugin;
    private final Random random = new Random();

    public SpawnerListener(MonsterSpawner plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // 检查是否是刷怪笼
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        // 检查玩家是否有权限挖掘
        if (!player.hasPermission("monsterspawner.break")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getMessage("spawner.no-permission"));
            return;
        }

        // 获取刷怪笼类型 (添加空值检查)
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        String entityType = "UNKNOWN";
        
        if (spawner.getSpawnedType() != null) {
            entityType = spawner.getSpawnedType().name();
        } else {
            // 如果实体类型为null，设置为默认值PIG
            try {
                spawner.setSpawnedType(EntityType.PIG);
                entityType = "PIG";
                spawner.update();
            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().warning(plugin.getLanguageManager().getString("system.debug.spawner.set_type_error")
                            .replace("%error%", e.getMessage()));
                }
            }
        }

        // 取消原始掉落
        event.setDropItems(false);
        
        // 检查是否使用了精准采集
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean hasSilkTouch = tool.containsEnchantment(Enchantment.SILK_TOUCH);
        
        // 如果有精准采集，执行精准采集动作
        if (hasSilkTouch) {
            plugin.getActionManager().executeActions(player, block, 
                    plugin.getConfig().getStringList("spawner.silk-touch-actions"));
            player.sendMessage(plugin.getLanguageManager().getMessage("spawner.silk-touch-bonus"));
        }

        // 计算挖掘是否成功
        double baseFailChance = plugin.getConfig().getDouble("spawner.fail-chance", 0.3);
        double silkTouchBonus = plugin.getConfig().getDouble("spawner.silk-touch-bonus", 0.2);
        
        // 如果有精准采集，则减少失败概率
        double finalFailChance = hasSilkTouch ? 
                Math.max(0, baseFailChance - silkTouchBonus) : baseFailChance;
        
        boolean success = random.nextDouble() > finalFailChance;

        // 执行对应动作
        if (success) {
            plugin.getActionManager().executeActions(player, block, 
                    plugin.getConfig().getStringList("spawner.success-actions"));
        } else {
            plugin.getActionManager().executeActions(player, block, 
                    plugin.getConfig().getStringList("spawner.fail-actions"));
        }
        
        // 增加破坏的刷怪笼计数
        plugin.incrementSpawnersBroken();
        
        if (plugin.getConfig().getBoolean("debug")) {
            String toolType = hasSilkTouch ? "精准采集" : "普通";
            String result = success ? "成功" : "失败";
            
            plugin.getLogger().info(plugin.getLanguageManager().getString("system.debug.spawner.break_info")
                    .replace("%player%", player.getName())
                    .replace("%tool_type%", toolType)
                    .replace("%entity_type%", entityType)
                    .replace("%result%", result)
                    .replace("%fail_chance%", String.valueOf(finalFailChance)));
        }
    }
} 