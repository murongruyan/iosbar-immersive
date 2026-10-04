# iOS-style immersive gesture bar

面向 Android 16/17（ColorOS 16/17）的 **单 APK LSPosed 模块**。它只向
`com.android.systemui` 注入 Hook，不替换 framework、SystemUI 或导航模式 RRO，
因此不会像旧版 overlay 方案一样破坏显示模块的资源映射。

## 功能

- 保留系统手势导航和手势触摸区域，只移除应用布局收到的 `navigationBars` inset。
- 关闭 SystemUI 瞬时导航栏的半透明 scrim，避免上划后整屏蒙层。
- ColorOS 17 适配：不再反射修改 `OplusNavigationHandle` 的 final 字段，而是接管
  `onDraw`，按 OEM 几何算法绘制横条。
- 可自定义：
  - 竖屏/横屏宽度
  - 高度
  - 底部边距
  - 圆角（胶囊或数值）
  - 不透明度
  - 颜色（跟随系统/白色/黑色/自定义颜色）
  - 沉浸式 inset 开关
  - 去除上划蒙层开关
- 设置页完全按 **Miuix 0.9.4 / KernelSU Manager** 的版式重做：超大扁平标题、16 dp 圆角
  分组卡片、单色 24 dp 前置图标、Miuix 分段控件与滑块关键点，不再使用彩色 section
  标题或图标底板。
- 卡片是**真实玻璃**而非纯白色块：每张卡片对专属的壁纸录制层做采样，叠一次饱和度的
  vibrancy、一次边缘透镜折射（12/24 dp，取自 AndroidLiquidGlass），再按「卡片颜色 +
  卡片通透度」铺一层蒙版，最后描一圈镜面高光。壁纸模糊只烘焙一次到共享录制层上，
  卡片不再各自开模糊通道，滚动帧率不受影响。
  - **卡片颜色**（自动/白色/黑色/自定义）决定蒙版色，**卡片通透度**决定蒙版透明度
    （越高壁纸越明显）。
  - 卡片内的**文字颜色会自动跟随蒙版**：选黑色蒙版时字变亮、选白色蒙版时字变暗，
    因此不会出现「黑底黑字」。页面、底栏、弹窗仍按「文字颜色」设置走，不受影响。
  - 右上角悬浮按钮、分段控件与滑块同样透出壁纸；分段控件的轨道与选中胶囊按当前
    卡片配色推导，并裁到卡片圆角内，不再出现白色方块越出卡片圆角的情况。
- 未选择背景图片时，页面保持纯色底：状态栏图标与顶部蒙层都按「是否真的有壁纸」判断，
  因此「自定义图片」但尚未选图时状态栏不会变成黑条。
- 底部悬浮导航胶囊接入 **io.github.kyant0:backdrop 2.0.1**，实时折射并模糊下方滚动内容，
  可在「设置 → 外观与玻璃 → 液态玻璃」中开关并调整模糊/折射/高光/通透度。
- 默认启用 **预测性返回手势**（动画模型参考慕容调度）：整页水平跟手位移，上一级页面同步滑入，
  中途取消按弹簧曲线回弹；提交时长按剩余距离缩放。二级页进入/退出也有对应的滑入滑出动画
  （260 ms 推入，下层页面左移 28% 做视差）。
- 一级页可直接左右滑动切换；底栏是居中的窄胶囊，选中指示器在切换时会平移并拉伸
  （KernelSU / M3 Expressive 观感）。
- 三个一级页：**主页 / 调节 / 设置**。
  - **调节**本身就是预设页：多套命名参数（宽度、高度、边距、圆角、不透明度、颜色），
    圆圈单选切换使用哪一套，点预设名进入「参数调节」二级页改参数，并可在那里改名或删除。
    右上角另有编辑按钮直达当前预设的参数页。
  - **设置**是二级入口，包含「外观与玻璃」「保存与备份」「关于」三个子页；关于页参考慕容调度
    的版式（居中 Hero 卡 + 强调色标题信息卡 + 全宽描边按钮），提供版本信息、运行环境、
    诊断信息复制，以及**检查更新**（读取仓库根目录的 `update.json`，KernelSU 模块清单格式）。
    更新检查失败时会把原始原因（HTTP 状态码、超时、连接失败）直接显示在卡片里。
- 设置通过 LSPosed remote preferences 持久化；修改后点击右上角“重启作用域”即可生效。
- 支持保存当前参数、备份参数、恢复备份，跨重启保留配置。
- 支持自定义强调色（6 组）、纯色或自定义背景图片、壁纸模糊，设置项全部走 Miuix 原生组件。
- 目标类/方法不存在时 fail-open，不安装任何替代资源。
- 发布新版本时同步更新仓库根目录的 `update.json`：

  ```json
  {
    "version": "0.6.0",
    "versionCode": 10,
    "zipUrl": "https://github.com/murongruyan/iosbar-immersive/releases/download/v0.6.0/iosbar-navhook-v0.6.2.apk",
    "changelog": "https://raw.githubusercontent.com/murongruyan/iosbar-immersive/master/UPDATE.md",
    "homepage": "https://github.com/murongruyan/iosbar-immersive"
  }
  ```

  并把同名 APK 作为 Release 资源上传（`zipUrl` 指向它）。客户端把 `versionCode` 与
  `ModuleInfo.VERSION_CODE` 比较，只有更大时才显示「下载」按钮。

## 设备验证基线

当前已验证：realme GT8 Pro（RMX5200）、ColorOS 17 / Android 17 / SDK 37、
`RMX5200_17.0.0.105(CN01)`、LSPosed API 102。

ColorOS 17 SystemUI 原生参数：

- 横条高度：`4 dp`
- 底部边距：`7 dp`
- 圆角：`2 dp`
- 竖屏宽度：`120 dp`
- 横屏宽度：`200 dp`

模块默认使用 iOS 预设：

- 高度：`6.4 dp`
- 底部边距：`14 dp`
- 圆角：胶囊
- 竖屏宽度：`180 dp`
- 横屏宽度：`200 dp`

## 安装

1. 设备已安装 LSPosed API 102（当前验证版本：LSPosed 2.1.1-it）。
2. 直接安装 `iosbar-navhook-v0.6.2.apk`：
   ```powershell
   adb install -r .\dist\iosbar-navhook-v0.6.2.apk
   ```
   也可以从文件管理器点击 APK 安装。
3. 打开应用 `iOS 沉浸式小横条`，按需要调整参数；可点击“保存当前参数”或“备份当前参数”。
4. 在 LSPosed 管理器中启用模块，并只给 `com.android.systemui` 作用域。
5. 重启一次设备，或重启 SystemUI，让 LSPosed 重新加载作用域。
6. 检查 LSPosed 日志中出现：
   ```text
   installed NavigationBar inset hook methods=1
   installed NavigationBarTransitions background hook methods=1
   installed OplusNavigationHandle hooks
   installed SystemUI hook set for ColorOS 16/17
   ```

## 回滚

在 LSPosed 管理器中禁用模块并卸载 APK，然后重启。模块不会替换系统资源，
因此卸载后系统手势导航会恢复原生绘制。

如果从 0.3.x 旧版升级，建议先用 0.4.1 的 KernelSU 包清理旧 RRO/overlay，
再安装本版 APK；本仓库仍保留 `build_module.ps1` 作为兼容旧版安装方式的
legacy 打包脚本，但 v0.6.0 的主发布物是 APK。

## 构建

本地需要 JDK 21、Android SDK 37、Build Tools 37.0.0 和 Gradle 9.6+。

```powershell
.\build_apk.ps1 -OutputDirectory .\dist
```

输出：

```text
dist/iosbar-navhook-v0.6.2.apk
```

旧版 KernelSU/Magisk ZIP 可继续使用：

```powershell
.\build_module.ps1 -OutputDirectory .\dist
```

## 依赖与协议

- Miuix `top.yukonga.miuix.kmp:miuix-ui:0.9.4`，Apache-2.0
- AndroidLiquidGlass / Backdrop `io.github.kyant0:backdrop:2.0.1`，Apache-2.0
- 本项目以 GPL-3.0-only 发布，完整条款见 [LICENSE](LICENSE)。提交到 `main` 或
  推送 `v*` 标签会触发 `.github/workflows/build.yml`；标签构建会运行静态检查并将
  当前版本 APK 与更新日志发布到 GitHub Release。

## 打赏

如果好用，请支持我！

![收款码](222.png)
