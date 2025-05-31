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

    public CommandManager(MonsterSpawner plugin) {
        this.plugin = plugin;
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
            }
            
            commands.add("help");
            
            return commands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}
