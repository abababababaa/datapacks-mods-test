# 工作台换深层钻石矿石

Minecraft Java 版 1.21.1 原版数据包，无需模组。

把 1 个工作台放入任意合成格，输出 1 个深层钻石矿石。配方为无序合成，可在背包 2×2 或工作台 3×3 合成栏使用。

## 安装到已有世界

1. 将 `crafting_table_to_deepslate_diamond_ore` 文件夹或发布的同名 ZIP 放进目标世界的 `datapacks` 文件夹。`pack.mcmeta` 必须直接位于数据包文件夹或 ZIP 根目录。
2. 进入世界；如果世界已经打开，使用有权限的账号执行 `/reload`，服务器控制台使用 `reload`。
3. 执行 `/datapack list enabled`，确认数据包已启用。
4. 在合成栏任意位置放入一个工作台，确认输出一个深层钻石矿石。

可选：执行 `/recipe give @s table_to_diamond:deepslate_diamond_ore` 将配方加入配方书。若世界启用了 `doLimitedCrafting`，需要先授予配方。

Java 版数据包按世界安装，而不是放在启动器的 `mods` 或 `resourcepacks` 目录。单人世界可从“编辑世界 → 打开世界文件夹”找到安装位置；服务器则使用 `server.properties` 中 `level-name` 指定的世界目录。

移除数据包后执行 `/reload` 即可卸载配方，不会移除已经合成的物品。
