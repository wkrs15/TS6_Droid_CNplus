# TS6 Droid 简中版

基于原作者 [flamme-demon/TS6_Droid](https://github.com/flamme-demon/TS6_Droid) 的开源项目进行汉化与功能增强的 Android 客户端。

这是一个自由、轻量级的 TeamSpeak 3/6 安卓客户端，使用 Jetpack Compose 构建，底层由 Rust 编写的 `tslib` 驱动。

---

## 更新日志

### v2.2.1-Han（2026-07-12）— 全面修复与体验重构

**Bug 修复**
- 🔇 **VA 模式按钮不再永远显示「静音中」**：根据真实麦克风状态切换图标和颜色（开麦 Mic+绿、静音 MicOff+红）
- 🗣️ **悬浮窗说话状态改由 ID 匹配**：用户列表不再靠昵称文字匹配说话状态，改用 userId
- 📏 **底部栏间距修复**：消除手势导航设备上底部栏重复的 `navigationBarsPadding`
- 🔑 **聊天列表 key 稳定化**：消息 key 从 `hashCode` 改为 `timestamp+senderId+sender`，消除 duplicate key 警告
- 🆔 **频道消息补全 senderId**：频道消息的 `ChatMessage` 现在正确设置 senderId
- ⚡ **`disconnect()` 不再阻塞 UI 线程**：`runBlocking` 替换为 `disconnectScope.launch`，连接中按断开不再导致 ANR
- 🧵 **`audioRecord` 添加 `@Volatile`**：消除 IO 线程读到 stale null 导致的偶现捕获失败
- 💣 **`downloadCache` 使用 `ConcurrentHashMap`**：消除多线程并发 `mutableMapOf` 导致的 `ConcurrentModificationException`
- 🔊 **修复混音残余数据**：`bytesToShorts` 零填充剩余解码缓冲区，不再混入旧帧噪音
- 🔇 **移除 AudioBridge 重复 release 调用**：清理 `startCapture` 和 `release()` 中各 2 处重复的 audio effect release
- 🚫 **`commandErrors` 不再重放**：`replay = 1 → 0`，新连接不再弹旧连接的错误 Toast
- 📋 **文件选择器 cursor 泄漏修复**：两处均改用 `ContentResolver.query().use {}`，异常时自动关闭
- 🪟 **浮窗权限循环修复**：`onStop` 同一生命周期只弹一次权限申请，防止死循环
- 🔧 **浮窗开关统一 DataStore**：`MainActivity` 改用 `SettingsStore` 读取，与设置页保持一致
- 🗑️ **`BookmarkStore` 手写 JSON 重写**：改用 `org.json.JSONArray/JSONObject`，支持特殊字符不再乱码
- 🎙️ **VA 模式快速切换不再锁死**：快速切 PTT↔VA 导致捕获挂掉后自动重启，无需重进服务器
- 📱 **主界面与悬浮窗静音状态同步**：`isMicMuted` 改为直接引用 `audioBridge.isMuted`，两边共用同一个 StateFlow，不再有缓存不一致

**界面改动**
- 🔘 **PTT/VA 切换改为两段式开关**：底部栏显示 `[ PTT | VA ]` 分段按钮，选中高亮，点击切换，语义一目了然
- VA 模式下按钮文字显示「已开麦」/「静音」，不再显示「PTT」

**功能优化**
- **VA 模式逻辑彻底简化**：切到 VA 自动开麦、切到 PTT 自动静音，去掉了 `savedVaMuteState`，没有中间态
- **连接时正确同步麦克风状态**：`bindToService` 直接读 DataStore 判断 PTT/VA，VA 模式连接后自动 `setMuted(false)`，其他客户端不再看不到麦克风指示
- **麦克风异常时自动恢复**：`toggleMicMute` 和 `toggleVoiceMode` 检测到捕获停止时自动调用 `startCapture` 重启
- **通知栏实时更新**：users / channels / serverInfo 变化时同步刷新
- **PTT 模式持久化**：PTT/VA 模式保存到 DataStore，进程重启不丢失
- **👂 音频路由改为通话音道**：`USAGE_GAME → USAGE_VOICE_COMMUNICATION`，音量键单独控制通话音量
- **头像定时刷新并发防护**：添加 `isAvatarRefreshing` 防止重叠下载
- **通知国际化**：「人在线」改为 `R.string.notif_online_suffix`，支持三语言
- **连接页权限 Toast**：拒绝麦克风权限时提示而非静默跳过
- **文件超限 Toast 国际化**：改用 `R.string.file_too_large`

**代码重构**
- **`BookmarkStore` 替换手写 JSON**：使用 `org.json` 原生序列化，保留旧格式兼容回退
- **`_connectionState` 初始化为 `DISCONNECTED`**：避免 5s 绑定期内误显「已连接」
- **移除 `isLocalVoiceActive` 无用 getter**：该 getter 每次访问创建新 `MutableStateFlow(false)`，已删除

---

### v2.2.0-Han（2026-07-05）— 代码优化与用户体验改进

**新功能**
- **🎨 Material You 莫奈取色**：Android 12+ 自动跟随系统壁纸主题色，低于 12 的机型回退默认紫色
- **📅 聊天日期分组**：消息列表按天显示「今天」「昨天」等日期分隔标签
- **📳 PTT 振动反馈**：按住说话按键时伴随振动，松手再次振动
- **⌨️ Enter 发送**：聊天输入框按键盘 Enter 直接发送消息
- **➕ 空状态引导**：无服务器书签时显示「点右下角 + 添加服务器」提示
- **📋 私信排序**：私信列表按最后消息时间降序排列，最新对话置顶
- **🔊 输入/输出增益分离**：新增「麦克风增益」滑块，与原「听筒增益」独立调节
- **📳 屏幕旋转保持**：旋转屏幕不再重建 Activity，连接和聊天状态保持
- **🔄 仓库信息更新**：更新检测、关于页链接、贡献者 API 均指向 wkrs15 仓库

**体验优化**
- **长按复制消息**：聊天气泡支持长按复制文本到剪贴板，附带 Toast 反馈提示
- **断连确认弹窗**：点击断开连接按钮弹出二次确认对话框，防止误触
- **聊天输入框自动聚焦**：打开聊天面板自动弹出键盘并聚焦输入框，免去手动点击
- **断连自动返回**：服务端断开连接后自动回到连接页面，不再卡在空白页
- **启动加速**：去除二次元壁纸功能，SplashScreen 简化为 600ms 快速启动，不再等待网络加载
- **设置页精简**：移除「我是二刺螈」开关与壁纸缓存管理，设置页更清爽
- **PTT 模式切换修复**：VA 模式的大按钮仅控制麦克风静音，不再强制切回 PTT 模式；模式切换只由小按钮控制

**Bug 修复**
- 修复签名密码硬编码在版本控制中的安全问题，改由环境变量读取
- 修复 `MainActivity.kt` 中 2 处 `runBlocking` 阻塞主线程导致的 ANR 风险
- 修复 `TsClient.launchNativeCommand` 中并发 `closeAfterNativeFailure` 导致的空指针异常
- 修复 `ConnectionViewModel` 中 `StateFlow` 被 UI 层直接修改 `.value` 导致的不可预测状态
- 修复 PTT/VA 模式反复切换后误触发静音的问题
- 修复退出服务器卡顿问题：去掉 800ms 多余延迟，缩短至 200ms

**性能优化**
- **HSL 色板缓存**：`generateColorScheme` 首次计算后缓存 `(seedColor, darkTheme)` → `ColorScheme`，避免每次重组重新计算
- **ProGuard/R8 开启**：Release 编译启用代码混淆 + 资源压缩，APK 体积显著减小
- **CI 缓存**：GitHub Actions 工作流新增 Gradle 构建缓存，显著加速 CI 编译速度
- **NDK 路径自动检测**：`buildRustLibs` 任务自动查找已安装的最新 NDK 版本，不再硬编码版本号，换电脑无需修改
- **书签列表 LazyColumn**：服务端列表改用 `LazyColumn` 虚拟滚动，书签数量多时不再卡顿
- **Thread.sleep → delay**：`ConnectionViewModel.browseChannels()` 和 `disconnectAndClose()` 中阻塞线程的 `Thread.sleep` 替换为协程 `delay`，避免占用 IO 线程
- **离线缓存**：频道列表和用户列表自动缓存到本地文件，重连后立即显示缓存数据，不再空白等待
- **文件管理器多选下载**：新增多选模式，可勾选多个文件批量下载，无需逐个点击
- **通知栏增强**：通知标题改为服务器名，内容显示当前频道和在线人数（如「〈默认频道〉| 12 人在线」）
- **消息时间相对显示**：1分钟内显示「刚刚」，1小时内「N分钟前」，更久显示「MM-dd HH:mm」
- **返回键优化**：聊天打开时按返回优先关闭聊天，不再直接退出服务器
- **心跳检测**：每 500ms 检查底层连接状态，断连后自动返回连接页面
- **自动重连增强**：断线后按 1s → 3s → 10s → 30s 递增重试

**代码重构**
- **手写 JSON 替换为 kotlinx.serialization**：`MessageStore` 移除 263 行手写 JSON 解析/拼接代码，改用 kotlinx.serialization，自动兼容旧格式文件
- **StateFlow 封装**：`ConnectionViewModel` 中 4 个公开 `MutableStateFlow` 改为私有 `_xxx` + 公有 `StateFlow` + setter 方法，禁止 UI 直接赋值
- **下载方法统一**：`ServerViewModel` 中 3 处重复的下载逻辑（`downloadAttachment` / `downloadFileFromManager` / `previewImageFile`）抽取为 `downloadFileInternal` 共用方法
- **新增 Messages.kt 数据模型**：将 `ChatMessage`、`FileAttachment` 从 ViewModel 移至 `data` 包，统一序列化注解
- **移除大量未使用 import**：多处文件清理无用引用

**依赖升级**
- AGP `8.7.3` → `8.8.0`
- Compose BOM `2024.12.01` → `2025.06.00`
- DataStore `1.1.1` → `1.1.4`
- 新增 `kotlinx-serialization-json:1.7.3`

**编译优化**
- Rust NDK 编译新增 `armeabi-v7a` 架构支持，覆盖更多老旧设备
- Gradle wrapper 镜像改为国内腾讯云源，解决 `services.gradle.org` 连接超时问题
- `release.keystore` 密码优先读取 CI 环境变量 `KEYSTORE_PASSWORD`，本地开发回退默认值

### v2.1.0-Han（2026-06-27）

**新功能**
- **应用内更新**：点击更新检测后可直接在应用内下载并安装 APK，无需跳转浏览器
- 下载进度条实时显示百分比，下载完成自动弹出系统安装界面
- 新增「正在下载」状态，下载中不可关闭弹窗，失败显示错误信息

**Bug 修复**
- 修复版本检测无法识别新版问题：比较版本号前先清除 `-Han` 后缀
- 修复 API 请求失败时误判为「已是最新版本」，现在显示具体错误信息
- 修复 Release 未上传 APK 时直接崩溃，现回退至 Releases 页面

**编译签名**
- 新增统一签名文件 `release.keystore`，debug 和 release 均使用同一签名
- 多电脑协作只需复制 `release.keystore` 到项目根目录即可

### v2.0.5-Han（2026-06-27）

**功能修复**
- 修复音量增益滑块调节后不生效的问题，新增 Flow 观察者实时同步到音频桥
- 麦克风降噪实现验证：使用 Android 原生 NoiseSuppressor API，补全日志帮助排查设备兼容性
- Logo 背景色从蓝色更换为粉色（#2962FF → #FF69B4）
- 修复应用内版本检测无法检测到新版的问题：版本号比较前清除 `-Han` 后缀，API 失败时显示错误信息而非误判为「已是最新」
- 修复 GitHub API 在国内网络环境下不可用时长显「已是最新」的问题

**新功能**
- 应用内版本检测：设置页点击版本号可查询 GitHub 最新 Release 并弹窗更新
- 网络错误时显示具体错误信息，无更新时提示「已是最新版本」
- Release 未上传 APK 时自动回退跳转至 GitHub Releases 页面

### v2.0.1-Han（2026-06-26）

**Compose 性能优化**
- 全项目 54 处 Flow 采集从 `collectAsState` 迁移至 `collectAsStateWithLifecycle`，应用切后台时自动暂停 UI 采集，降低 CPU 占用和电量消耗
- 背景图片淡入动画从 `Modifier.alpha()` 迁移至 `Modifier.graphicsLayer {}`，动画帧跳过 Composition 阶段重组，减少掉帧
- 缓存壁纸网格添加稳定 `key`，避免增删壁纸时滚动位置跳回

**Bug 修复**
- 修复查看壁纸缓存无反应，点击后弹出缩略图网格弹窗
- 清空壁纸缓存添加二次确认弹窗
- 设置页音量增益滑块可正常调节
- 设置页开关切换页面时不再跳动闪烁
- 文件管理器点击图片文件支持应用内全屏预览

### v2.0.0-Han（2026-06-26）

**Material3 UI 全面重构**
- 采用 Google Material Design 3 规范，完全重构配色、排版与组件样式
- Dynamic Color 动态取色（Android 12+），主题色从壁纸图片自动提取并生成完整配色方案
- 15 级排版体系，Shape 圆角 token 对齐 M3 标准
- 所有组件（按钮、输入框、卡片、弹窗、底部栏）统一 M3 风格

**启动页与主题自适应**
- 新增 SplashScreen 启动界面，加载期间显示品牌标识
- 壁纸图片下载后自动提取主色调，主题配色实时适配
- 3 秒超时保护：网络异常时从缓存随机抽取壁纸作为背景

**底部导航栏 + 设置页**
- 首页新增底部导航栏（主页 + 设置），支持页面切换
- 语言切换、自动重连、音量增益、悬浮窗、动漫背景、麦克风降噪、关于软件全部整合到设置页
- 服务端不再显示设置弹窗，界面更简洁

**壁纸缓存系统**
- 壁纸图片自动缓存到本地，启动时优先使用缓存
- 可设置缓存最大容量（10MB - 500MB 滑块调节）
- 查看缓存壁纸缩略图网格，支持单张删除
- 清空缓存带二次确认弹窗
- 以上设置仅在「我是二刺螈」开启时可用

**动画背景优化**
- 壁纸切换不再闪烁：缓存机制 + 600ms 淡入动画
- 切页不再触发重新获取，全局共享同一张壁纸
- 首页空列表居中显示「暂无连接」

**文件管理器图片预览**
- 点击图片文件直接在应用内全屏预览，不再弹出外部打开方式

**Bug 修复**
- 修复 Config#HARDWARE bitmap 无法 getPixel 导致闪退
- 修复设置页开关在页面切换时跳动闪烁
- 修复 SettingsDialog 残留代码导致编译错误
- 修复窗口背景色导致的灰色底色问题
- 统一所有组件使用 M3 颜色 token

---

## 汉化及增强特性

1. **简体中文本地化**：100% 补齐全文本简体中文翻译（`zh-rCN`）。
2. **语言切换**：支持中文、English、Français 一键切换，无需更改手机系统语言。
3. **内置核心语音驱动**：直接内置全架构核心二进制库（jniLibs），开箱即用。
4. **CI/CD 深度优化**：适配 AndroidX/Jetifier 兼容环境，优化 Gradle JVM 内存上限。

---

## 多电脑编译签名说明

本项目使用统一的 `release.keystore` 签名文件，确保所有电脑编译的 APK 签名一致，覆盖安装时不报签名冲突。

- 签名文件位于项目根目录 `release.keystore`
- 密码/别名：`ts6droid`
- 该文件已被 `.gitignore` 排除，不会提交到 GitHub
- 多电脑协作时，将 `release.keystore` 复制到其他电脑的项目根目录即可

### 生成新的签名文件

如需替换签名（例如用于正式发布），在项目根目录执行：

```bash
keytool -genkey -v -keystore release.keystore -alias ts6droid -keyalg RSA -keysize 2048 -validity 10000
```

---

## 如何进行云编译 (GitHub Actions)

1. **Fork 本仓库** 到你自己的 GitHub 账号下。
2. 进入仓库页面，点击顶部的 **Actions** 标签，点击绿色按钮激活 Actions。
3. 每次代码推送或手动触发工作流，GitHub 自动打包。
4. 编译完成后，在 **Assets** 区域下载 `app-debug.apk`。

---

## 技术架构与配置

关于底层 Rust 架构、本地编译环境搭建等技术细节，请参考原作者仓库：

[flamme-demon/TS6_Droid](https://github.com/flamme-demon/TS6_Droid)

## 开源许可

本项目遵循 GNU GPLv3 开源许可证。详见 [LICENSE](LICENSE) 文件。

---

## 贡献者

感谢所有为本项目做出贡献的开发者！

<a href="https://github.com/wkrs15/TS6_Droid_CNplus/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=wkrs15/TS6_Droid_CNplus" />
</a>
