package github.postyizhan.monsterspawner.hook;

import github.postyizhan.monsterspawner.MonsterSpawner;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 钩子管理器
 * 用于统一管理所有钩子
 */
public class HookManager {

    private final MonsterSpawner plugin;
    private final Map<String, HookAbstract> hookMap = new HashMap<>();
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("([a-zA-Z0-9_-]+):(.+)");

    public HookManager(MonsterSpawner plugin) {
        this.plugin = plugin;
        registerHooks();
    }

    /**
     * 注册所有钩子
     */
    private void registerHooks() {
        // 注册所有物品库钩子
        registerHook(new OraxenHook(plugin));
        registerHook(new ItemsAdderHook(plugin));
        registerHook(new MythicMobsHook(plugin));
        registerHook(new NeigeItemsHook(plugin));
        registerHook(new MMOItemsHook(plugin));
        registerHook(new ZaphkielHook(plugin));
        registerHook(new CraftEngineHook(plugin));
        registerHook(new NexoHook(plugin));
        registerHook(new SXItemHook(plugin));
        registerHook(new MagicGemHook(plugin));
        
        // 初始化所有钩子
        initializeHooks();
    }
    
    /**
     * 注册钩子
     * @param hook 钩子实例
     */
    public void registerHook(HookAbstract hook) {
        hookMap.put(hook.getNamespace(), hook);
    }
    
    /**
     * 初始化所有钩子
     */
    public void initializeHooks() {
        // 延迟加载钩子，确保所有依赖插件都已经完全加载
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("§a[MonsterSpawner] 开始初始化物品库钩子...");
                
                for (HookAbstract hook : hookMap.values()) {
                    try {
                        plugin.getLogger().info("§a[MonsterSpawner] 正在初始化 " + hook.getName() + " 钩子...");
                        boolean success = hook.initialize();
                        plugin.getLogger().info("§a[MonsterSpawner] " + hook.getName() + " 钩子初始化" + 
                                (success ? "§a成功" : "§c失败"));
                    } catch (Exception e) {
                        plugin.getLogger().warning("§c初始化" + hook.getName() + "钩子时出错: " + e.getMessage());
                        if (plugin.getConfig().getBoolean("debug", false)) {
                            e.printStackTrace();
                        }
                    }
                }
                
                // 打印钩子状态
                printHookStatus();
                
                // 尝试重新初始化失败的钩子
                plugin.getLogger().info("§a[MonsterSpawner] 尝试重新初始化失败的钩子...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean anySuccess = false;
                        for (HookAbstract hook : hookMap.values()) {
                            if (!hook.isHooked()) {
                                try {
                                    plugin.getLogger().info("§a[MonsterSpawner] 重新尝试初始化 " + hook.getName() + " 钩子...");
                                    boolean success = hook.initialize();
                                    if (success) {
                                        anySuccess = true;
                                        plugin.getLogger().info("§a[MonsterSpawner] " + hook.getName() + " 钩子初始化§a成功");
                                    }
                                } catch (Exception e) {
                                    if (plugin.getConfig().getBoolean("debug", false)) {
                                        plugin.getLogger().warning("§c重新初始化" + hook.getName() + "钩子时出错: " + e.getMessage());
                                    }
                                }
                            }
                        }
                        
                        if (anySuccess) {
                            // 如果有成功初始化的钩子，重新打印状态
                            printHookStatus();
                        }
                    }
                }.runTaskLater(plugin, 100L); // 再延迟5秒进行第二次尝试
            }
        }.runTaskLater(plugin, 60L); // 延迟3秒，等待所有插件加载完成
    }
    
    /**
     * 打印钩子状态
     */
    private void printHookStatus() {
        plugin.getLogger().info("§a[MonsterSpawner] 物品库钩子状态:");
        for (HookAbstract hook : hookMap.values()) {
            plugin.getLogger().info("§a[MonsterSpawner] - " + hook.getName() + ": " + 
                    (hook.isHooked() ? "§a已连接" : "§c未连接"));
        }
    }
    
    /**
     * 根据命名空间获取钩子
     * @param namespace 命名空间
     * @return 钩子实例，如果不存在则返回null
     */
    public HookAbstract getHook(String namespace) {
        return hookMap.get(namespace.toLowerCase());
    }
    
    /**
     * 获取Oraxen钩子
     */
    public OraxenHook getOraxen() {
        return (OraxenHook) getHook("oraxen");
    }
    
    /**
     * 获取ItemsAdder钩子
     */
    public ItemsAdderHook getItemsAdder() {
        return (ItemsAdderHook) getHook("itemsadder");
    }
    
    /**
     * 获取MythicMobs钩子
     */
    public MythicMobsHook getMythicMobs() {
        return (MythicMobsHook) getHook("mythicmobs");
    }
    
    /**
     * 获取NeigeItems钩子
     */
    public NeigeItemsHook getNeigeItems() {
        return (NeigeItemsHook) getHook("neigeitems");
    }
    
    /**
     * 获取MMOItems钩子
     */
    public MMOItemsHook getMMOItems() {
        return (MMOItemsHook) getHook("mmoitems");
    }
    
    /**
     * 获取Zaphkiel钩子
     */
    public ZaphkielHook getZaphkiel() {
        return (ZaphkielHook) getHook("zaphkiel");
    }
    
    /**
     * 获取CraftEngine钩子
     */
    public CraftEngineHook getCraftEngine() {
        return (CraftEngineHook) getHook("craftengine");
    }
    
    /**
     * 获取Nexo钩子
     */
    public NexoHook getNexo() {
        return (NexoHook) getHook("nexo");
    }
    
    /**
     * 获取SXItem钩子
     */
    public SXItemHook getSXItem() {
        return (SXItemHook) getHook("sxitem");
    }
    
    /**
     * 获取MagicGem钩子
     */
    public MagicGemHook getMagicGem() {
        return (MagicGemHook) getHook("magicgem");
    }
    
    /**
     * 解析物品ID字符串获取物品
     * 格式支持以下几种：
     * 1. minecraft:stone - 原版物品
     * 2. oraxen:item_id - 带命名空间的物品
     * 3. item_id - 无命名空间的物品，将尝试所有钩子
     * 
     * @param itemString 物品ID字符串
     * @param amount 数量
     * @param player 玩家，可为null
     * @return 物品实例，如果获取失败则返回null
     */
    public ItemStack parseItemStack(String itemString, int amount, Player player) {
        if (itemString == null || itemString.isEmpty()) {
            return null;
        }
        
        // 检查是否有命名空间
        java.util.regex.Matcher matcher = NAMESPACE_PATTERN.matcher(itemString);
        if (matcher.matches()) {
            String namespace = matcher.group(1).toLowerCase();
            String id = matcher.group(2);
            
            // 处理原版物品
            if (namespace.equals("minecraft")) {
                try {
                    Material material = Material.valueOf(id.toUpperCase());
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
            
            // 处理带命名空间的自定义物品
            HookAbstract hook = getHook(namespace);
            if (hook != null && hook.isHooked()) {
                if (player != null) {
                    return hook.getItemForPlayer(id, player, amount);
                } else {
                    return hook.getItem(id, amount);
                }
            }
        } else {
            // 无命名空间，尝试所有钩子
            
            // 首先尝试作为原版物品处理
            try {
                Material material = Material.valueOf(itemString.toUpperCase());
                return new ItemStack(material, amount);
            } catch (IllegalArgumentException ignored) {
                // 继续尝试其他钩子
            }
            
            // 尝试所有钩子
            for (HookAbstract hook : hookMap.values()) {
                if (hook.isHooked() && hook.isCustomItem(itemString)) {
                    if (player != null) {
                        return hook.getItemForPlayer(itemString, player, amount);
                    } else {
                        return hook.getItem(itemString, amount);
                    }
                }
            }
        }
        
        // 所有尝试都失败
        return null;
    }
    
    /**
     * 从物品获取ID
     * @param itemStack 物品实例
     * @return 物品ID字符串，如果不是自定义物品则返回null
     */
    public String getItemId(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        
        // 尝试所有钩子
        for (HookAbstract hook : hookMap.values()) {
            if (hook.isHooked()) {
                String id = hook.getId(itemStack);
                if (id != null) {
                    return hook.getNamespace() + ":" + id;
                }
            }
        }
        
        // 对于原版物品，返回minecraft命名空间
        return "minecraft:" + itemStack.getType().name().toLowerCase();
    }
}
