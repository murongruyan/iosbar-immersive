# 更新日志

## 0.6.0 (2026-10-05)

- **设置界面完全重做**，对齐 KernelSU Manager / LSPosed / InstallerX-Revived 的 Miuix 版式：
  - 32 sp 加粗左对齐大标题直接滚动在页面上，不再使用玻璃顶栏。
  - 纯白（暗色 #242424）扁平分组卡片，16 dp 圆角，无阴影、无渐变背景。
  - 单色 24 dp 前置图标，移除了彩色圆角图标底板。
  - 移除彩色 section 标题，改用 Miuix 原生次级标题。
  - 顶部状态卡改为 KernelSU StatusCard 样式：浅绿/浅灰容器 + 右侧出血水印。
  - 横条颜色、背景模式改用 Miuix 分段控件；高度与底部边距滑块显示原生关键点。
- 液态玻璃收敛到「悬浮底部导航胶囊」这一层，实时折射并模糊下方滚动内容，符合
  AndroidLiquidGlass 的用法（玻璃只用于浮动 chrome，不用于滚动内容）。新增开关与
  模糊/折射/高光/通透度四项参数。
- 新增 6 组界面强调色，联动开关、滑块、图标与底栏配色。
- 底栏按 **KernelSU 的 FloatingBottomBar 源码移植**（Apache-2.0，文件头保留出处）：
  - 移植 `DragGestureInspector` / `DampedDragAnimation` / `InteractiveHighlight` 三个文件到
    `ui/liquid/`。
  - 药丸为**一个 tab 宽**，由 `DampedDragAnimation.value` 连续驱动平移，因此按下即选中、
    按住会膨胀（pressedScale）、左右拖动可连续切换。
  - 速度驱动的挤压拉伸：`scaleX /= 1 - v*0.75`、`scaleY *= 1 - v*0.25`。
  - `InteractiveHighlight` 用 AGSL 在手指位置画镜面光斑。
  - 药丸采样 `rememberCombinedBackdrop(backdrop, tabsBackdrop)`：那份不可见的重复 Row 把
    图标层录进 `tabsBackdrop`，选中图标才能透过玻璃显示并被染成强调色。
  - 底栏玻璃为 `vibrancy + blur(4dp) + lens(24dp,24dp)`，药丸玻璃按 pressProgress 渐显
    `lens(10dp,14dp, depthEffect)`。
  - 本机适配：KernelSU 的 `pressedScale = 78/56`（1.39×）是给 4 tab / 304 dp 调的，
    我们 3 tab / 252 dp 会胀出边界，收到 1.18×；item 最小宽 84 dp。
  - 去掉了按住时把图标和文字放大到 1.2× 的行为：字号被放大后发虚，现在字号恒定，
    只有药丸本体膨胀/拉伸。
- 新增**预设（多套参数）**：**「调节」这个 tab 本身就是预设页**，参数编辑下沉为
  「参数调节」二级页（滑块不再和重命名/删除按钮同屏，避免误触）：
  - 「调节」页 = 预设列表 + 管理（新建 / 复制）。行内只有「圆圈单选 + 名称」，不放编辑/删除按钮。
  - 点圆圈只切换使用哪一套；点名称会选中该套并进入「参数调节」二级页。
  - 「参数调节」二级页 = 预设名称（可改名）+ 横条几何预览 + 尺寸滑块 + 颜色 +
    底部「删除此预设」。删除放在二级页底部，不再出现在列表行里。
  - 每行右侧带 chevron，明确提示点文字可进入。
  - 「调节」页右上角另有一个编辑按钮，直接进入当前预设的参数页。
- 二级页补上**进入/退出动画**（之前是直接闪现）：
  - 二级页改为独立的覆盖层，从右侧滑入（260 ms, FastOutSlowInEasing），
    下层 tab 页同步左移 28% 做视差。
  - 返回箭头也有退出动画（220 ms），与手势返回一致；预测性返回仍是手指 1:1 跟手。
  - tab 页在下层始终组合，所以返回时露出的是真实页面而不是空白背景。
  - 5 次进/出中位帧 7 ms、90 分位 21 ms、掉帧 14%。
  - 每套预设保存宽度（竖/横）、高度、底部边距、圆角、不透明度、颜色模式与自定义色。
  - 点一行即切换，参数立即写入 hook 读取的实时配置；下方滑块改的是**当前激活的那套**。
  - 支持新建（把当前参数另存）、复制、重命名、删除；至少保留一套。
  - 首次运行会把用户已有参数自动存成「当前参数」预设，不会静默覆盖成内置值；
    若与内置的 iOS 预设 / ColorOS 默认完全一致，则直接选中对应内置预设。
  - 存储为 settings 里的 presets / active_preset 两个键（JSON），随 remote preferences 持久化。
- 「关于」页新增**检查更新**，接 GitHub 上的 `update.json`（KernelSU 模块更新清单格式）：
  - 新增 `update.json`（仓库根目录）：version / versionCode / zipUrl / changelog / homepage。
  - 新增 `ui/Update.kt`：ModuleInfo 统一版本号，HttpURLConnection 拉取并解析，失败信息直接
    显示在卡片里（HTTP 状态码、明文流量拦截、连接超时都会原样提示）。
  - 最新版本号高于当前时高亮显示，并出现「下载 x.y.z」按钮；另有「项目主页」入口。
  - 清单新增 INTERNET / ACCESS_NETWORK_STATE 权限。
- 页面切换按 KernelSU 的 MainActivity 参数调优：
  - `beyondViewportPageCount = 2`（三页全部预组合，滑动过程中不再发生组合）。
  - `overscrollEffect = null`（去掉边缘辉光）。
  - `flingBehavior(snapAnimationSpec = PagerNavigationSpringSpec)`，点 tab 用
    `PagerState.springAnimateToPage` —— 都来自 Miuix 的 PagerGestureUtils。
  - 效果：翻页/点按切换中位帧 14–25 ms → 8 ms，掉帧 43–59% → 18%。
- 底栏与页面切换重做：
  - 一级页改为 HorizontalPager，可直接左右滑动切换，也可点底栏或长按。
  - 选中指示器改为一个会平移的胶囊：两端使用不同刚度的弹簧（前缘 900/0.62、后缘 260/0.9），
    切换时先拉伸再收回，就是 KernelSU / M3 Expressive 的水滴观感。
  - 去掉了页面级的缩放/淡入 peek —— 每帧重采样两个全屏图层是滑动卡顿的主因；
    去掉后翻页中位帧从 17-25 ms 降到 8 ms。
  - 底栏改为**居中的窄胶囊**：每个 item 固定 78 dp，整条约 250 dp（约屏宽 55%），不再整屏铺满；
    高度保持原样（图标 24 dp、标签 11 sp、内边距 7 dp），离底边距 10→16 dp。
  - 移除了长按时撑开的圆形气泡（观感差），长按只保留图标轻微放大与触感反馈。
- 接入**预测性返回（Predictive Back）**：
  - 清单新增 android:enableOnBackInvokedCallback="true"。
  - 默认开启，无开关。
  - 动画模型直接照搬慕容调度 MurongComposeActivity / SecondaryPageHost：整页水平位移，
    当前页 translationX = progress * width，上一级 translationX = progress * width - width，
    两层都是纯平移、不缩放不变换透明度。
  - 取消回弹用慕容调度的弹簧参数（二级页 300/0.78，一级页 380/0.72）。
  - 提交动画时长按剩余距离缩放，同样来自慕容调度：
    clamp(180ms * (1 - progress), 72ms, 180ms)，LinearEasing。
  - 上一级页面常驻组合并屏蔽触摸，非手势期间停在 -width，手势起手没有组合卡顿。
  - 返回层级：二级页 → 所在 tab；非主页 tab → 主页；主页 → 退出应用。
  - 手势期间中位帧 5 ms / 90 分位 14 ms（优化前为 11 ms / 27 ms）。
- 导航结构改为 **主页 / 调节 / 设置** 三个一级页；原「外观」页下沉为「设置 → 外观与玻璃」二级页。
- 新增「关于」二级页，版式参考慕容调度：
  - 居中 Hero 卡：模块名、作者、76 dp 图标、模块说明、2x2 强调色描边按钮
    （重启作用域 / 复制版本信息 / 保存当前参数 / 备份当前参数）。
  - 「版本信息」卡：当前版本、接口版本、作用域、持久化方式 + 全宽「重启 SystemUI 作用域」。
  - 「运行环境」卡：设备型号、系统版本、系统 SDK、CPU 架构 + 全宽「复制诊断信息」。
  - 诊断信息通过系统剪贴板复制，方便提交 issue。
- 「保存与备份」从主页移到「设置」页，主页只保留状态、预览、开关与设备信息。
- 启动图标改为自适应图标（渐变底 + 手势条前景 + monochrome）。
- 性能：滚动中位帧从 25 ms 降到 7 ms（1440x3136 @60 Hz）。
- 新增设置项：glass_navbar、accent、glass_blur、glass_refraction、glass_highlight、glass_tint，
  均通过 LSPosed remote preferences 持久化。
- **底栏两套配方**，按背景模式自动切换，不再「一套配方打天下」：
  - 纯色背景 → 保留 KernelSU 配方（`vibrancy + blur + lens`，均匀 Ambient 高光）。
  - 自定义图片（壁纸）→ 换成 AndroidLiquidGlass 配方：`colorControls` 提亮
    （浅色 0.20 / 深色 0.06，饱和度 1.5）、`depthEffect` 透镜、45° 斜向高光，
    面色改为 `0xFFFAFAFA` / `0xFF121212` 的 40% 半透明 —— 壁纸模式下底栏不再显得「割裂」。
- **卡片拉真玻璃**：卡片配方补上 AndroidLiquidGlass 的三件套 —— `colorControls` 提亮
  （按通透度渐隐，避免在实心卡上洗掉用户选的蒙版色）、`depthEffect` 透镜、
  以及仅通透时绘制的镜面亮边；卡片文字色继续按壁纸亮度推导，保持可读。
- 新增设置项 **卡片内阴影**（外观与玻璃 → 设置卡片玻璃）：打开后卡片内壁多一道暗边，
  玻璃更有厚度。实测（1440×3136 @60 Hz，6 组上下滚动）：关闭 2.85–5.21%、
  开启 2.74–2.99%，两态在噪声内不可区分 —— 此前的 38% 掉帧来自「高光常开」
  （每卡一个离屏 `GraphicsLayer`），已改为条件式，因此内阴影默认关闭但可放心开启。
- **弹窗补上玻璃**：重启作用域 / 重命名预设 / 删除预设三处 Miuix `WindowDialog`
  换成玻璃版 —— 采样页面录制的壁纸层，套用与卡片相同的提亮 + 透镜 + 亮边 + 半透明胶片；
  拿不到背景层时回退到原生不透明面板，保证按钮与文字始终可读。

## 0.5.0 (2026-10-01)

- 适配 ColorOS 17 / Android 17 / SDK 37；保留 ColorOS 16 fail-open 兼容。
- 不再反射修改 `OplusNavigationHandle.mHeight / mHandleBottom / mRadius` 等 final 字段，改为接管 `onDraw` 按 OEM 几何算法绘制。
- 保留并适配 `NavigationBar.getBarLayoutParamsForRotation`、`NavigationBarTransitions.getBarBackground` 和 `getGestureWidthRes`。
- 重构为单 APK LSPosed 模块，主发布物不再依赖 KernelSU ZIP；KernelSU 包仅保留旧版清理/兼容用途。
- 新增 Miuix 0.9.4 设置页，接入 AndroidLiquidGlass / Backdrop 2.0.1 液态玻璃效果。
- 支持自定义竖屏/横屏宽度、高度、底部边距、圆角、透明度、颜色及沉浸/去蒙层开关。
- 设置改为 XposedService remote preferences 持久化，支持保存、备份和恢复备份。
- 移除 1 秒热同步，改为设置页右上角“重启作用域”按钮。
- 新增自定义背景图片、背景模糊和更接近 KSU/慕容调度的 Miuix Glass 页面结构。
- 增加实时预览、iOS 预设恢复和 ColorOS 默认恢复。

## 0.4.1 (2026-08-20)

- 修复旧版 overlay 包仅删除文件、未清理 PackageManager 注册，导致 SystemUI 读取错误导航栏资源并触发 LSPosed 安全模式的问题。
- 安装/卸载时仅清理 `/data` 下的旧 overlay 包，跳过系统分区原厂包，避免误伤系统手势资源。
- 安全模式恢复说明：切回手势导航后再刷入本版，避免三键导航状态被误认为模块功能。

## 0.4.0 (2026-08-20)

- 移除 framework/SystemUI RRO 和 metamodule 挂载，改为只 Hook `com.android.systemui`。
- 保留手势 provider，仅将应用布局的导航栏 inset 设为 0。
- 关闭瞬时导航栏 scrim，修复横屏右下角定位、透明框和整屏蒙层。
- 横条几何在 attach、layout、方向切换和绘制阶段重新应用。
- 升级安装时清理 0.3.x 旧 overlay、脚本及 `disable` 标记。

## 0.3.3

- 旧版 RRO/metamodule 方案，已废弃。不要在 Android 16 上继续使用。
