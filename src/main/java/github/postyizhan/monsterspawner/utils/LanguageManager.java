package github.postyizhan.monsterspawner.utils;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class LanguageManager {
    private final MonsterSpawner plugin;
    private FileConfiguration langConfig;
    private String language;

    public LanguageManager(MonsterSpawner plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void reload() {
        loadLanguage();
    }
    
    public void loadLanguage() {
        language = plugin.getConfig().getString("language", "zh_CN");
        
        // 创建语言文件夹
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().severe("无法创建语言文件夹!");
            return;
        }
        
        // 保存默认语言文件
        saveDefaultLanguageFile("zh_CN.yml");
        saveDefaultLanguageFile("en_US.yml");
        
        // 加载语言文件
        File langFile = new File(plugin.getDataFolder() + "/lang", language + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("找不到语言文件: " + language + ".yml，使用默认语言zh_CN.yml");
            language = "zh_CN";
            langFile = new File(plugin.getDataFolder() + "/lang", "zh_CN.yml");
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        plugin.getLogger().info("已加载语言: " + language);
    }

    private void saveDefaultLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder() + "/lang", fileName);
        if (!langFile.exists()) {
            try {
                InputStream in = plugin.getResource("lang/" + fileName);
                if (in != null) {
                    java.nio.file.Files.copy(in, langFile.toPath());
                    plugin.getLogger().info("已创建语言文件: " + fileName);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("保存语言文件时出错: " + e.getMessage());
            }
        }
    }

    public String getString(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            return "Missing text: " + path;
        }
        message = message.replace("{prefix}", getPrefix());
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', langConfig.getString("prefix", "&8[&aMonsterSpawner&8] "));
    }

    public List<String> getStringList(String path) {
        List<String> messages = langConfig.getStringList(path);
        String prefix = getPrefix();
        messages.replaceAll(message -> {
            message = message.replace("{prefix}", prefix);
            return ChatColor.translateAlternateColorCodes('&', message);
        });
        return messages;
    }

    public String getMessage(String path) {
        return getString("messages." + path);
    }
} 