package github.postyizhan.monsterspawner.utils;

import github.postyizhan.monsterspawner.MonsterSpawner;
import github.postyizhan.monsterspawner.hook.ItemsAdderHook;
import github.postyizhan.monsterspawner.hook.MythicMobsHook;
import github.postyizhan.monsterspawner.hook.NeigeItemsHook;
import github.postyizhan.monsterspawner.hook.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("尝试掉落物品: " + content);
                }
                ItemStack dropItem = parseItemStack(content, player);
                if (dropItem != null) {
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().info("成功掉落物品: " + dropItem.getType() + " x" + dropItem.getAmount());
                    }
                    
                    // 根据是否有方块参数决定掉落位置
                    if (block != null) {
                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), dropItem);
                        if (plugin.getConfig().getBoolean("debug")) {
                            plugin.getLogger().info("在方块位置掉落物品: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
                        }
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                        if (plugin.getConfig().getBoolean("debug")) {
                            plugin.getLogger().info("在玩家位置掉落物品");
                        }
                    }
                } else {
                    plugin.getLogger().warning("掉落物品失败，物品为null: " + content);
                }
                break;
                
            case "give":
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("尝试给予物品: " + content);
                }
                ItemStack giveItem = parseItemStack(content, player);
                if (giveItem != null) {
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().info("成功给予物品: " + giveItem.getType() + " x" + giveItem.getAmount());
                    }
                    player.getInventory().addItem(giveItem).forEach((index, leftover) -> 
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover));
                } else {
                    plugin.getLogger().warning("给予物品失败，物品为null: " + content);
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
     * 解析物品字符串为ItemStack
     * @param content 物品内容
     * @return 物品实例
     */
    private ItemStack parseItemStack(String content) {
        return parseItemStack(content, null);
    }
    
    /**
     * 解析物品字符串为ItemStack
     * @param content 物品内容
     * @param player 玩家（可以为null）
     * @return 物品实例
     */
    private ItemStack parseItemStack(String content, Player player) {
        try {
            String[] itemData = content.split(" ");
            String itemId = itemData[0];
            int amount = 1;
            
            if (itemData.length > 1) {
                amount = Integer.parseInt(itemData[1]);
            }

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("解析物品: " + content + ", 物品ID: " + itemId + ", 数量: " + amount);
            }
            
            // 处理普通物品
            if (itemId.startsWith("minecraft:")) {
                itemId = itemId.substring(10); // 去掉"minecraft:"前缀
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("处理Minecraft原版物品: " + itemId);
                }
            }
            
            // 检查是否是物品库插件的物品
            if (ItemsAdderHook.isEnabled() && ItemsAdderHook.isItemsAdderItem(itemId)) {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("通过ItemsAdder获取物品: " + itemId);
                }
                return ItemsAdderHook.getItemStack(itemId, amount);
            } else if (MythicMobsHook.isEnabled() && itemId.startsWith("mythicmobs:")) {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("通过MythicMobs获取物品: " + itemId.substring(11));
                }
                return MythicMobsHook.getItemStack(itemId.substring(11), amount);
            } else if (NeigeItemsHook.isEnabled() && itemId.startsWith("neigeitems:")) {
                String neigeItemId = itemId.substring(11);
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("通过NeigeItems获取物品: " + neigeItemId + ", 是否有玩家: " + (player != null));
                }
                
                ItemStack result;
                if (player != null) {
                    // 如果有玩家，使用带玩家参数的方法
                    result = NeigeItemsHook.getItemStack(neigeItemId, player, amount);
                } else {
                    // 否则使用不带玩家参数的方法
                    result = NeigeItemsHook.getItemStack(neigeItemId, amount);
                }
                
                if (plugin.getConfig().getBoolean("debug")) {
                    if (result == null) {
                        plugin.getLogger().warning("NeigeItems返回的物品为null: " + neigeItemId);
                    } else {
                        plugin.getLogger().info("成功获取NeigeItems物品: " + neigeItemId + ", 类型: " + result.getType());
                    }
                }
                
                return result;
            } else {
                // 普通Minecraft物品
                try {
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().info("尝试作为Minecraft原版物品处理: " + itemId.toUpperCase());
                    }
                    Material material = Material.valueOf(itemId.toUpperCase());
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的物品ID: " + itemId);
                    return null;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("解析物品错误: " + content + " - " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
