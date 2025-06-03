package github.postyizhan.monsterspawner.command;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final MonsterSpawner plugin;
    private final ItemCommand itemCommand;

    public CommandManager(MonsterSpawner plugin) {
        this.plugin = plugin;
        this.itemCommand = new ItemCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("monsterspawner.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLanguageManager().loadLanguage();
                sender.sendMessage(plugin.getLanguageManager().getMessage("reload"));
                return true;
                
            case "help":
                showHelp(sender);
                return true;
                
            case "item":
                if (!sender.hasPermission("monsterspawner.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
                    return true;
                }
                itemCommand.handleGetItem(sender, args);
                return true;
                
            case "iteminfo":
                if (!sender.hasPermission("monsterspawner.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no-permission"));
                    return true;
                }
                itemCommand.handleItemInfo(sender);
                return true;
                
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("invalid-command"));
                return true;
        }
    }

    private void showHelp(CommandSender sender) {
        List<String> helpMessages = plugin.getLanguageManager().getStringList("messages.help");
        for (String message : helpMessages) {
            sender.sendMessage(message);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            
            if (sender.hasPermission("monsterspawner.admin")) {
                commands.add("reload");
                commands.add("item");
                commands.add("iteminfo");
            }
            
            commands.add("help");
            
            return commands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("item") && sender.hasPermission("monsterspawner.admin")) {
            completions.add("minecraft:diamond");
            completions.add("minecraft:emerald");
            
            // 添加常见命名空间提示
            completions.add("oraxen:");
            completions.add("itemsadder:");
            completions.add("mythicmobs:");
            completions.add("neigeitems:");
            
            return completions.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("item") && sender.hasPermission("monsterspawner.admin")) {
            completions.add("1");
            completions.add("8");
            completions.add("16");
            completions.add("32");
            completions.add("64");
            
            return completions;
        }
        
        return completions;
    }
}
