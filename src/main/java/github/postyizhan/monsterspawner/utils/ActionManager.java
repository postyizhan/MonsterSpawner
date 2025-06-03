package github.postyizhan.monsterspawner.utils;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionManager {
    private final MonsterSpawner plugin;
    private final Pattern actionPattern = Pattern.compile("\\[([a-zA-Z_]+)\\]\\s*(.*)");

    public ActionManager(MonsterSpawner plugin) {
        this.plugin = plugin;
    }

    /**
     * 执行一系列动作
     * @param player 玩家
     * @param actions 动作列表
     */
    public void executeActions(Player player, List<String> actions) {
        for (String actionLine : actions) {
            try {
                Matcher matcher = actionPattern.matcher(actionLine);
                if (matcher.find()) {
                    String actionType = matcher.group(1).toLowerCase();
                    String actionContent = matcher.group(2).trim();
                    
                    // 处理占位符
                    if (plugin.hasPlaceholderAPI()) {
                        actionContent = PlaceholderAPIHook.setPlaceholders(player, actionContent);
                    }
                    
                    executeAction(player, null, actionType, actionContent);
                } else {
                    plugin.getLogger().warning("无效的动作格式: " + actionLine);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("执行动作时出错: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 在特定方块位置执行一系列动作
     * @param player 玩家
     * @param block 方块
     * @param actions 动作列表
     */
    public void executeActions(Player player, Block block, List<String> actions) {
        for (String actionLine : actions) {
            try {
                Matcher matcher = actionPattern.matcher(actionLine);
                if (matcher.find()) {
                    String actionType = matcher.group(1).toLowerCase();
                    String actionContent = matcher.group(2).trim();
                    
                    // 处理占位符
                    if (plugin.hasPlaceholderAPI()) {
                        actionContent = PlaceholderAPIHook.setPlaceholders(player, actionContent);
                    }
                    
                    // 替换方块相关的特殊变量
                    actionContent = actionContent
                            .replace("%block_type%", block.getType().name())
                            .replace("%block_x%", String.valueOf(block.getX()))
                            .replace("%block_y%", String.valueOf(block.getY()))
                            .replace("%block_z%", String.valueOf(block.getZ()))
                            .replace("%block_world%", block.getWorld().getName());
                    
                    executeAction(player, block, actionType, actionContent);
                } else {
                    plugin.getLogger().warning("无效的动作格式: " + actionLine);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("执行动作时出错: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void executeAction(Player player, String actionType, String content) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("执行动作: [" + actionType + "] " + content);
        }
        
        // 没有方块参数时，默认在玩家位置执行动作
        executeAction(player, null, actionType, content);
    }
    
    private void executeAction(Player player, Block block, String actionType, String content) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("执行动作: [" + actionType + "] " + content + (block != null ? " 在方块位置" : " 在玩家位置"));
        }
        
        switch (actionType) {
            case "command":
                player.performCommand(content);
                break;
                
            case "op":
                boolean isOp = player.isOp();
                try {
                    player.setOp(true);
                    player.performCommand(content);
                } finally {
                    player.setOp(isOp);
                }
                break;
                
            case "console":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
                break;
                
            case "sound":
                try {
                    String[] soundData = content.split(" ");
                    Sound sound = Sound.valueOf(soundData[0]);
                    float volume = 1.0f;
                    float pitch = 1.0f;
                    
                    if (soundData.length > 1) {
                        volume = Float.parseFloat(soundData[1]);
                    }
                    if (soundData.length > 2) {
                        pitch = Float.parseFloat(soundData[2]);
                    }
                    
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (Exception e) {
                    plugin.getLogger().warning("播放声音错误: " + content + " - " + e.getMessage());
                }
                break;
                
            case "message":
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', content));
                break;
                
            case "title":
                try {
                    String[] titleData = content.split(" ", 2);
                    String title = ChatColor.translateAlternateColorCodes('&', titleData[0]);
                    String subtitle = "";
                    
                    if (titleData.length > 1) {
                        subtitle = ChatColor.translateAlternateColorCodes('&', titleData[1]);
                    }
                    
                    player.sendTitle(title, subtitle, 10, 70, 20);
                } catch (Exception e) {
                    plugin.getLogger().warning("显示标题错误: " + content);
                }
                break;
                
            case "drop_monster_spawner":
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("尝试掉落刷怪笼");
                }
                
                // 检查是否有方块信息
                if (block == null) {
                    plugin.getLogger().warning("无法掉落刷怪笼：没有方块信息");
                    return;
                }
                
                // 创建刷怪笼物品
                ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);
                
                // 如果方块是刷怪笼，尝试保留实体类型
                if (block.getType() == Material.SPAWNER) {
                    try {
                        // 获取刷怪笼状态
                        org.bukkit.block.CreatureSpawner spawner = (org.bukkit.block.CreatureSpawner) block.getState();
                        
                        // 获取实体类型
                        org.bukkit.entity.EntityType entityType = spawner.getSpawnedType();
                        
                        if (plugin.getConfig().getBoolean("debug")) {
                            plugin.getLogger().info("刷怪笼实体类型: " + (entityType != null ? entityType.name() : "null"));
                        }
                        
                        if (entityType != null) {
                            // 设置物品显示信息
                            org.bukkit.inventory.meta.ItemMeta meta = spawnerItem.getItemMeta();
                            if (meta != null) {
                                // 设置显示名
                                String displayName = ChatColor.RESET + "" + ChatColor.YELLOW + entityType.name() + " 刷怪笼";
                                meta.setDisplayName(displayName);
                                
                                // 在Lore中存储实体类型信息（使用特殊格式便于后续识别）
                                java.util.List<String> lore = new java.util.ArrayList<>();
                                lore.add(ChatColor.GRAY + "实体类型: " + ChatColor.WHITE + entityType.name());
                                // 特殊标记用于后续识别和解析
                                lore.add(ChatColor.BLACK + "MS-EntityType:" + entityType.name());
                                meta.setLore(lore);
                                
                                spawnerItem.setItemMeta(meta);
                                
                                if (plugin.getConfig().getBoolean("debug")) {
                                    plugin.getLogger().info("成功在物品Lore中保存实体类型: " + entityType.name());
                                }
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("处理刷怪笼实体类型时出错: " + e.getMessage());
                        if (plugin.getConfig().getBoolean("debug")) {
                            e.printStackTrace();
                        }
                    }
                }
                
                // 掉落物品
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), spawnerItem);
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("成功在方块位置掉落刷怪笼: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
                }
                break;
                
            case "drop":
                try {
                    String[] dropData = content.split(" ");
                    String itemId = dropData[0];
                    int amount = 1;
                    
                    if (dropData.length > 1) {
                        amount = Integer.parseInt(dropData[1]);
                    }
                    
                    ItemStack itemToDrop = parseItemStack(itemId, amount, player);
                    if (itemToDrop != null) {
                        if (block != null) {
                            block.getWorld().dropItemNaturally(block.getLocation(), itemToDrop);
                        } else {
                            player.getWorld().dropItemNaturally(player.getLocation(), itemToDrop);
                        }
                    } else {
                        plugin.getLogger().warning("掉落物品错误: 无法识别物品 " + itemId);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("掉落物品错误: " + content + " - " + e.getMessage());
                }
                break;
                
            case "give":
                try {
                    String[] giveData = content.split(" ");
                    String itemId = giveData[0];
                    int amount = 1;
                    
                    if (giveData.length > 1) {
                        amount = Integer.parseInt(giveData[1]);
                    }
                    
                    ItemStack itemToGive = parseItemStack(itemId, amount, player);
                    if (itemToGive != null) {
                        player.getInventory().addItem(itemToGive);
                    } else {
                        plugin.getLogger().warning("给予物品错误: 无法识别物品 " + itemId);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("给予物品错误: " + content + " - " + e.getMessage());
                }
                break;
                
            case "buff":
                try {
                    String[] buffData = content.split(" ");
                    PotionEffectType effectType = PotionEffectType.getByName(buffData[0]);
                    if (effectType != null) {
                        int duration = 200; // 默认10秒
                        int amplifier = 0;  // 默认等级1
                        
                        if (buffData.length > 1) {
                            duration = Integer.parseInt(buffData[1]) * 20; // 转换为tick
                        }
                        if (buffData.length > 2) {
                            amplifier = Integer.parseInt(buffData[2]) - 1; // 调整等级
                        }
                        
                        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                    } else {
                        plugin.getLogger().warning("无效的药水效果: " + buffData[0]);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("添加药水效果错误: " + content);
                }
                break;
                
            default:
                plugin.getLogger().warning("未知的动作类型: " + actionType);
        }
    }

    /**
     * 解析物品ID并获取物品栈
     * @param content 物品ID
     * @param amount 数量
     * @param player 玩家（用于替换占位符）
     * @return 物品栈
     */
    private ItemStack parseItemStack(String content, int amount, Player player) {
        // 使用HookManager解析物品
        return plugin.getHookManager().parseItemStack(content, amount, player);
    }
}
