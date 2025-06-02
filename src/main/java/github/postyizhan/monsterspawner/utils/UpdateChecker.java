package github.postyizhan.monsterspawner.utils;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 更新检查器
 * 检查GitHub上是否有新版本的插件可用
 */
public class UpdateChecker implements Listener {

    private final MonsterSpawner plugin;
    private final String owner = "postyizhan";
    private final String repository = "MonsterSpawner";
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;
    private final boolean checkForUpdates;

    /**
     * 创建更新检查器
     * @param plugin 插件实例
     */
    public UpdateChecker(MonsterSpawner plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.checkForUpdates = plugin.getConfig().getBoolean("update-checker.enabled", true);
        
        if (checkForUpdates) {
            checkForUpdate();
        }
    }

    /**
     * 检查更新
     */
    public void checkForUpdate() {
        if (!checkForUpdates) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    latestVersion = getLatestVersion();
                    
                    if (latestVersion != null && !latestVersion.isEmpty()) {
                        // 检查版本是否为更新版
                        if (compareVersions(latestVersion, currentVersion) > 0) {
                            updateAvailable = true;
                            
                            // 在控制台显示更新信息
                            String updateMessage = plugin.getLanguageManager().getString("system.updater.update_available")
                                    .replace("{prefix}", plugin.getLanguageManager().getPrefix())
                                    .replace("{current_version}", currentVersion)
                                    .replace("{latest_version}", latestVersion);
                            
                            plugin.getServer().getConsoleSender().sendMessage(updateMessage);
                            
                            // 延迟2秒显示更新链接，确保在服务器启动消息后显示
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                String updateUrl = plugin.getLanguageManager().getString("system.updater.update_url")
                                        .replace("{prefix}", plugin.getLanguageManager().getPrefix());
                                plugin.getServer().getConsoleSender().sendMessage(updateUrl);
                            }, 40L);
                        } else {
                            plugin.getServer().getConsoleSender().sendMessage(
                                    plugin.getLanguageManager().getString("system.updater.up_to_date")
                                            .replace("{prefix}", plugin.getLanguageManager().getPrefix())
                            );
                        }
                    }
                } catch (Exception e) {
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getServer().getConsoleSender().sendMessage("§c[MonsterSpawner] 检查更新时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * 从GitHub获取最新版本
     * @return 最新版本号
     */
    private String getLatestVersion() {
        try {
            // 构建API URL
            URL url = new URL("https://api.github.com/repos/" + owner + "/" + repository + "/releases/latest");
            
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // 设置User-Agent (GitHub API需要)
            connection.setRequestProperty("User-Agent", "MonsterSpawner Update Checker");
            
            // 获取响应代码
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // 解析JSON响应获取tag_name (版本)
                Pattern pattern = Pattern.compile("\"tag_name\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(response.toString());
                
                if (matcher.find()) {
                    // 移除版本号前的"v"（如果有）
                    String version = matcher.group(1);
                    if (version.startsWith("v")) {
                        version = version.substring(1);
                    }
                    return version;
                }
            } else {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getServer().getConsoleSender().sendMessage("§c[MonsterSpawner] 检查更新失败，HTTP响应码: " + responseCode);
                }
            }
        } catch (IOException e) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getServer().getConsoleSender().sendMessage("§c[MonsterSpawner] 检查更新时发生IO异常: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 比较两个版本号
     * @param version1 版本1
     * @param version2 版本2
     * @return 如果version1 > version2返回正数，version1 < version2返回负数，相等返回0
     */
    private int compareVersions(String version1, String version2) {
        // 将版本号分割为组件
        String[] components1 = version1.split("\\.");
        String[] components2 = version2.split("\\.");
        
        // 获取最小长度
        int length = Math.min(components1.length, components2.length);
        
        // 比较每个组件
        for (int i = 0; i < length; i++) {
            int v1 = getVersionComponent(components1[i]);
            int v2 = getVersionComponent(components2[i]);
            
            if (v1 != v2) {
                return v1 - v2;
            }
        }
        
        // 如果前面的组件都相同，长度更长的版本更新
        return components1.length - components2.length;
    }
    
    /**
     * 从版本组件中提取数字部分
     * 例如：从"1-SNAPSHOT"中提取1
     * @param component 版本组件
     * @return 版本号
     */
    private int getVersionComponent(String component) {
        try {
            // 提取前导数字
            Matcher matcher = Pattern.compile("^(\\d+)").matcher(component);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (NumberFormatException ignored) {
            // 忽略解析错误
        }
        return 0;
    }

    /**
     * 玩家登录事件处理
     * 向管理员显示更新通知
     * @param event 玩家登录事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!checkForUpdates) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("monsterspawner.admin") && updateAvailable) {
            // 延迟2秒显示更新信息，确保在其他加入消息后显示
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String updateMessage = plugin.getLanguageManager().getString("system.updater.update_available")
                        .replace("{prefix}", plugin.getLanguageManager().getPrefix())
                        .replace("{current_version}", currentVersion)
                        .replace("{latest_version}", latestVersion);
                
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', updateMessage));
                
                String updateUrl = plugin.getLanguageManager().getString("system.updater.update_url")
                        .replace("{prefix}", plugin.getLanguageManager().getPrefix());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', updateUrl));
            }, 40L);
        }
    }
    
    /**
     * 获取是否有更新可用
     * @return 是否有更新可用
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    /**
     * 获取最新版本
     * @return 最新版本
     */
    public String getLatestVersionNumber() {
        return latestVersion;
    }
} 