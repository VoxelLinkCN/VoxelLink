# VoxelLink 0.3.0

VoxelLink 首个正式版本！基于 NeoBot 架构全面重命名与重构，稳定支持 Bukkit / Folia / Velocity 三平台。

> **从 NeoBot 0.2.x 升级？** 包名已从 `dev.neobot` 迁移至 `dev.voxellink`，插件数据目录为 `plugins/VoxelLink/`，旧配置迁移逻辑已内置，首次启动自动完成。

## ✨ 新特性

### QQ 群聊转发白名单
- 新增配置 `bot.options.enable-groups`：只有名单内的 QQ 群消息才会转发进游戏
- 未配置或配置为空时，**所有群聊均不进游戏**（远程命令功能不受影响，仍按原有权限工作）
- 与远程命令共用同一份白名单，配置一次即可

## 🛠 修复

### 消息处理线程自锁（重要）
修复 QQ / Discord 消息分发路径上的线程自锁问题：昵称解析在事件线程内同步等待
`MessageSnapshot` 完成时可能永久阻塞，导致**消息静默消失**且事件队列反复重试。
现在消息分发统一走插件异步调度器（`submitAsync`），事件线程永不阻塞。

### 脚本超时与异常不再静默
- 脚本处理超时（默认 10 秒）现在会打印明确 warn 日志：`timed out after 10000 ms`
- 脚本异常会打印完整 error 堆栈，不再吞掉
- 排障时日志能直接定位是哪个事件、哪段脚本出了问题

## 📦 安装 / 升级

1. 将对应平台的 `VoxelLink-{bukkit|folia|velocity}-0.3.0-all.jar` 放入 `plugins/`（Velocity 为 `plugins/`）
2. **已在用旧版且改过脚本的，务必手动更新脚本**（见下方注意事项）
3. 在 `config.json` 中配置白名单：
   ```json
   "bot": {
     "options": {
       "enable-groups": [123456789, 987654321]
     }
   }
   ```
4. 重启服务器

### ⚠️ 升级必读：脚本更新

插件遵循「脚本已安装则跳过」的原则，**不会自动覆盖**已存在的 `scripts/default/main.js`。本次群白名单逻辑在脚本内，升级后必须二选一：

- **方式 A**：手动替换 `plugins/VoxelLink/scripts/default/main.js` 为新版本
- **方式 B**：删除 `plugins/VoxelLink/scripts/default/manifest.json` 后重启，插件自动重装脚本（改过 main.js 的请先备份）

## ✅ 测试

121 项单元测试全部通过，覆盖消息路由、绑定服务、频道策略、重连退避、脚本超时与群白名单过滤等核心链路。

## 📋 依赖

- Java 21+
- VoxBotAPI 1.3.0（OneBot v11 客户端库，已并入发行 jar）
- VoxStorageAPI 1.1.0（存储抽象库，已并入发行 jar）
