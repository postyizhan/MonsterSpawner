# MonsterSpawner

![版本](https://img.shields.io/badge/版本-1.0--SNAPSHOT-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.13+-green)
![语言](https://img.shields.io/badge/语言-简体中文|English-orange)

一个轻量级的 Minecraft 刷怪笼管理插件，提供丰富的功能和灵活的配置。

## 📚 功能特性

- ✅ **刷怪笼管理**：支持精准采集和普通工具挖掘刷怪笼
- ✅ **多语言支持**：内置中文和英文语言文件，可轻松扩展
- ✅ **丰富的动作系统**：支持命令执行、物品掉落、音效播放等多种动作
- ✅ **钩子系统**：兼容 PlaceholderAPI、ItemsAdder、MythicMobs、NeigeItems、Oraxen 等插件
- ✅ **灵活配置**：完全可自定义的配置和消息系统

## 💻 安装

1. 下载最新版本的 MonsterSpawner 插件
2. 将插件文件放入服务器的 plugins 文件夹
3. 重启服务器
4. 开始使用插件

## 🔧 命令

| 命令         | 描述                     | 权限                 |
|--------------|--------------------------|----------------------|
| `/ms reload` | 重载插件配置和语言文件   | monsterspawner.admin |
| `/ms help`   | 显示帮助信息             | -                    |

## 🔒 权限

| 权限                 | 描述               | 默认 |
|----------------------|--------------------|------|
| monsterspawner.admin | 允许使用管理员命令 | op   |
| monsterspawner.break | 允许破坏刷怪笼     | true |
| monsterspawner.use   | 允许使用基础命令   | true |

## 🛠️ 动作

MonsterSpawner 提供了丰富的动作系统，可以在配置文件中使用以下格式的动作：

**格式说明：** `[]` 为必填参数，`()` 为选填参数

| 动作语法                                | 描述                       | 示例                                     |
|-----------------------------------------|----------------------------|------------------------------------------|
| `[command] [命令]`                      | 让玩家执行命令             | `[command] spawn`                        |
| `[op] [命令]`                           | 临时给予玩家OP权限执行命令 | `[op] gamemode creative`                 |
| `[console] [命令]`                      | 在控制台执行命令           | `[console] broadcast 有人挖掘了刷怪笼！` |
| `[sound] [音效] (音量) (音调)`          | 为玩家播放音效             | `[sound] BLOCK_ANVIL_LAND 1.0 1.5`       |
| `[message] [文本]`                      | 向玩家发送消息             | `[message] &a你成功挖掘了刷怪笼！`       |
| `[title] [主标题] (副标题)`             | 向玩家展示标题             | `[title] &a挖掘成功 &7获得了一个刷怪笼`  |
| `[drop_monster_spawner]`                | 掉落被破坏的刷怪笼         | `[drop_monster_spawner]`                 |
| `[drop] [物品ID] (数量)`                | 在指定位置掉落物品         | `[drop] DIAMOND 5`                       |
| `[give] [物品ID] (数量)`                | 给予玩家物品               | `[give] neigeitems:special_sword 1`      |
| `[buff] [药水效果] (持续时间秒) (等级)` | 给予玩家药水效果           | `[buff] SPEED 30 2`                      |

### 物品ID格式

| 物品库      | 语法               |
|------------|---------------------|
| Minecraft  | 物品ID              |
| NeigeItems | neigeitems:物品ID   |
| ItemsAdder | namespace:物品ID    |
| MythicMobs | mythicmobs:物品ID   |
| Oraxen     | oraxen:物品ID       |

### 内建变量

在动作中可以使用以下内建变量：

- `%player%` - 玩家名称
- `%block_type%` - 方块类型
- `%block_x%` - 方块X坐标
- `%block_y%` - 方块Y坐标
- `%block_z%` - 方块Z坐标
- `%block_world%` - 方块所在世界

此外，如果安装了 PlaceholderAPI，还可以使用 PlaceholderAPI 变量。

## 🌐 多语言支持

插件支持多语言，默认提供中文和英文两种语言文件。您可以在 `plugins/MonsterSpawner/lang/` 目录中找到并编辑这些文件。

要切换语言，请在 `config.yml` 中修改 `language` 设置：

```yaml
language: "zh_CN"  # 或 "en_US"
```

## 📋 常见问题

**Q: 如何让精准采集工具百分百获取刷怪笼？**
A: 在 `config.yml` 中设置 `spawner.fail-chance` 为 `0.2`，`spawner.silk-touch-bonus` 为 `0.2`

**Q: 如何修改刷怪笼掉落物品？**
A: 修改 `config.yml` 中的 `spawner.success-actions` 部分

## 👥 支持与反馈

如有问题或建议，请通过以下方式联系我：

- QQ群：611076407
- GitHub：https://github.com/postyizhan/MonsterSpawner

## 📝 许可证

本项目采用 [GPL-2.0](LICENSE)
