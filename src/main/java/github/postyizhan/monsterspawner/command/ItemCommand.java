package github.postyizhan.monsterspawner.command;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 物品相关命令
 */
public class ItemCommand {

    private final MonsterSpawner plugin;

    public ItemCommand(MonsterSpawner plugin) {
        this.plugin = plugin;
    }

    /**
     * 处理物品获取命令
     * @param sender 命令发送者
     * @param args 命令参数
     */
    public void handleGetItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getString("commands.errors.player-only")
                    .replace("{prefix}", plugin.getLanguageManager().getPrefix()));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "使用方法: /ms item <物品ID> [数量]");
            player.sendMessage(ChatColor.GRAY + "物品ID格式: [命名空间:]物品ID");
            player.sendMessage(ChatColor.GRAY + "例如: minecraft:diamond, oraxen:custom_sword, itemsadder:ruby");
            return;
        }

        String itemId = args[1];
        int amount = 1;

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "无效的数量，使用默认数量1");
                amount = 1;
            }
        }

        ItemStack item = plugin.getHookManager().parseItemStack(itemId, amount, player);

        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "成功获取物品: " + ChatColor.YELLOW + itemId + ChatColor.GREEN + " x" + amount);
        } else {
            player.sendMessage(ChatColor.RED + "找不到指定的物品: " + itemId);
            player.sendMessage(ChatColor.GRAY + "请确认物品ID格式正确，且相应的物品库插件已加载");
        }
    }

    /**
     * 处理物品信息命令
     * @param sender 命令发送者
     */
    public void handleItemInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getString("commands.errors.player-only")
                    .replace("{prefix}", plugin.getLanguageManager().getPrefix()));
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            player.sendMessage(ChatColor.RED + "请手持一个物品以查看信息");
            return;
        }

        String itemId = plugin.getHookManager().getItemId(item);
        player.sendMessage(ChatColor.GREEN + "物品信息:");
        player.sendMessage(ChatColor.GRAY + "- 物品ID: " + ChatColor.WHITE + itemId);
        player.sendMessage(ChatColor.GRAY + "- 材质: " + ChatColor.WHITE + item.getType().name());
        player.sendMessage(ChatColor.GRAY + "- 数量: " + ChatColor.WHITE + item.getAmount());
        
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                player.sendMessage(ChatColor.GRAY + "- 显示名称: " + item.getItemMeta().getDisplayName());
            }
            
            if (item.getItemMeta().hasLore()) {
                player.sendMessage(ChatColor.GRAY + "- 描述:");
                for (String line : item.getItemMeta().getLore()) {
                    player.sendMessage(ChatColor.GRAY + "  " + line);
                }
            }
        }
    }
} 