package com.iosbar.navhook.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosbar.navhook.BarConfig
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Reset
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.UploadCloud
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt

private const val ROW_DIVIDER_INDENT_DP = 50

private fun radiusLabel(radius: Float): String =
    if (radius < 0f) "胶囊" else String.format("%.1f dp", radius)

private fun Float.oneDecimal(): String = String.format("%.1f", this)

/* ---------------------------------------------------------------------------------------- */
/* Home                                                                                      */
/* ---------------------------------------------------------------------------------------- */

internal fun LazyListScope.homePage(
    state: SettingsState,
    dark: Boolean,
    accentColor: Color,
) {
    item { StatusCard(state, dark) }

    item {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "实时预览",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = resolvedBarColorLabel(state.colorMode),
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                BarPreview(
                    state = state,
                    dark = dark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(182.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetricPill("宽 " + state.widthPortrait.roundToInt() + " dp")
                    MetricPill("高 " + state.height.oneDecimal() + " dp")
                    MetricPill("边距 " + state.bottom.oneDecimal() + " dp")
                    MetricPill(radiusLabel(state.radius))
                }
            }
        }
    }

    item {
        SettingsCard {
            SwitchPreference(
                checked = state.enabled,
                onCheckedChange = { state.enabled = it; state.commit() },
                title = "启用模块",
                summary = "关闭后恢复系统原生横条绘制",
                startAction = { SettingIcon(MiuixIcons.Home, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SwitchPreference(
                checked = state.immersive,
                onCheckedChange = { state.immersive = it; state.commit() },
                title = "沉浸式布局",
                summary = "应用布局收到 navigationBars inset = 0",
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SwitchPreference(
                checked = state.removeScrim,
                onCheckedChange = { state.removeScrim = it; state.commit() },
                title = "去除上划蒙层",
                summary = "关闭瞬时导航栏的半透明 scrim",
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
            )
        }
    }

    item {
        SettingsCard {
            KeyValueRow("模块版本", moduleVersionText())
            InsetDivider(16.dp)
            KeyValueRow("LSPosed", "API 102 · 2.1.1-it")
            InsetDivider(16.dp)
            KeyValueRow("设备型号", Build.MODEL)
            InsetDivider(16.dp)
            KeyValueRow("系统版本", "Android " + Build.VERSION.RELEASE + " · ColorOS 17")
        }
    }
}

/** The KernelSU StatusCard: tinted container, headline, version lines and a bleeding watermark. */
@Composable
private fun StatusCard(state: SettingsState, dark: Boolean) {
    val ok = state.enabled
    val container = if (ok) {
        if (dark) Color(0xFF16301F) else Color(0xFFE6F7EC)
    } else {
        if (dark) Color(0xFF2A2A2E) else Color(0xFFEFEFF2)
    }
    val content = if (ok) {
        if (dark) Color(0xFF5BD98A) else Color(0xFF1B9E4B)
    } else {
        MiuixTheme.colorScheme.onSurface
    }
    SettingsCard(container = container) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCorner)),
        ) {
            BarMark(
                size = 124.dp,
                accent = content,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 38.dp)
                    .alpha(0.17f),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (ok) "工作中" else "已停用",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = content,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "版本：" + moduleVersionText(),
                    fontSize = 13.sp,
                    color = content.copy(alpha = 0.78f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "LSPosed API 102 · ColorOS 17",
                    fontSize = 13.sp,
                    color = content.copy(alpha = 0.78f),
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Tune                                                                                      */
/* ---------------------------------------------------------------------------------------- */

internal fun LazyListScope.paramsPage(
    state: SettingsState,
    dark: Boolean,
    accentColor: Color,
) {
    item { PresetNameCard(state) }

    item {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "横条几何",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "数值以 dp 为单位，在 SystemUI 绘制阶段实时生效",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(12.dp))
                BarPreview(
                    state = state,
                    dark = dark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
        }
    }

    item { SmallTitle("横条尺寸") }
    item {
        SettingsCard {
            SliderPreference(
                value = state.widthPortrait,
                onValueChange = { state.widthPortrait = it },
                onValueChangeFinished = { state.commit() },
                title = "竖屏宽度",
                summary = "实际绘制会强制使用该宽度并居中",
                valueText = state.widthPortrait.roundToInt().toString() + " dp",
                valueRange = 24f..320f,
                steps = 148,
                startAction = { SettingIcon(MiuixIcons.Tune, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.widthLandscape,
                onValueChange = { state.widthLandscape = it },
                onValueChangeFinished = { state.commit() },
                title = "横屏宽度",
                summary = "横屏时的横条宽度",
                valueText = state.widthLandscape.roundToInt().toString() + " dp",
                valueRange = 24f..360f,
                steps = 168,
                startAction = { SettingIcon(MiuixIcons.Tune, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.height,
                onValueChange = { state.height = it },
                onValueChangeFinished = { state.commit() },
                title = "高度",
                summary = "ColorOS 17 原生为 4 dp",
                valueText = state.height.oneDecimal() + " dp",
                valueRange = 2f..16f,
                steps = 27,
                showKeyPoints = true,
                keyPoints = listOf(4f, 6.4f),
                startAction = { SettingIcon(MiuixIcons.Tune, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.bottom,
                onValueChange = { state.bottom = it },
                onValueChangeFinished = { state.commit() },
                title = "底部边距",
                summary = "ColorOS 17 原生为 7 dp",
                valueText = state.bottom.oneDecimal() + " dp",
                valueRange = 0f..40f,
                steps = 39,
                showKeyPoints = true,
                keyPoints = listOf(7f, 14f),
                startAction = { SettingIcon(MiuixIcons.Tune, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.radius,
                onValueChange = { state.radius = it },
                onValueChangeFinished = { state.commit() },
                title = "圆角",
                summary = "-1 表示自动胶囊形",
                valueText = radiusLabel(state.radius),
                valueRange = -1f..12f,
                steps = 12,
                startAction = { SettingIcon(MiuixIcons.Tune, Modifier.padding(end = 10.dp)) },
            )
        }
    }

    item { SmallTitle("横条颜色") }
    item {
        SettingsCard {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                GlassTabRow(
                    tabs = listOf("跟随系统", "白色", "黑色", "自定义"),
                    selectedTabIndex = state.colorMode,
                    onTabSelected = {
                        state.colorMode = it
                        state.commit()
                    },
                )
            }
        }
    }
    item {
        SettingsCard {
            SliderPreference(
                value = state.alpha.toFloat(),
                onValueChange = { state.alpha = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "不透明度",
                summary = "0% 完全透明，100% 完全显示",
                valueText = state.alpha.toString() + "%",
                valueRange = 0f..100f,
                steps = 99,
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
            )
            if (state.colorMode == BarConfig.COLOR_CUSTOM) {
                InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    ColorPicker(
                        color = Color(state.customColor),
                        onColorChanged = {
                            state.customColor = it.copy(alpha = 1f).toArgb()
                            state.commit()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    item { PresetDeleteCard(state) }
}

/* ---------------------------------------------------------------------------------------- */
/* Settings hub                                                                              */

internal fun LazyListScope.settingsPage(
    state: SettingsState,
    accentColor: Color,
    onOpenAppearance: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    // One grouped card holding all three destinations, the way KernelSU groups its rows: a separate
    // card per row plus a matching section title made every entry look like a button with a label,
    // and the title repeated the large title above it.
    item {
        SettingsCard {
            ArrowPreference(
                title = "外观与玻璃",
                summary = "强调色、卡片与底栏玻璃、文字颜色",
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
                onClick = onOpenAppearance,
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "保存与备份",
                summary = "保存当前参数、备份或恢复预设",
                startAction = { SettingIcon(MiuixIcons.Backup, Modifier.padding(end = 10.dp)) },
                onClick = onOpenBackup,
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "关于",
                summary = "版本信息、运行环境与模块说明",
                startAction = { SettingIcon(MiuixIcons.Info, Modifier.padding(end = 10.dp)) },
                onClick = onOpenAbout,
            )
        }
    }
}

/* Appearance                                                                                */
/* ---------------------------------------------------------------------------------------- */

internal fun LazyListScope.appearancePage(
    state: SettingsState,
    dark: Boolean,
    accentColor: Color,
    hasImageAccess: Boolean,
    onPickBackground: () -> Unit,
    onGrantAccess: () -> Unit,
) {
    item { SmallTitle("界面配色") }
    item {
        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    AccentOptions.forEach { option ->
                        val swatch = if (dark) option.dark else option.light
                        val selected = option.index == state.accent
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(swatch)
                                .clickable {
                                    state.accent = option.index
                                    state.commit()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = MiuixIcons.Ok,
                                    contentDescription = option.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = accentOption(state.accent).label + " · 影响开关、滑块与底栏强调色",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }

    item { SmallTitle("液态玻璃") }
    item {
        SettingsCard {
            SwitchPreference(
                checked = state.glassNavbar,
                onCheckedChange = { state.glassNavbar = it; state.commit() },
                title = "液态玻璃底栏",
                summary = "底栏实时折射并模糊下方滚动的内容",
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.glassBlur.toFloat(),
                onValueChange = { state.glassBlur = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "玻璃模糊",
                summary = "底栏背后内容的模糊半径",
                valueText = state.glassBlur.toString() + " dp",
                valueRange = 0f..40f,
                steps = 39,
                enabled = state.glassNavbar,
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.glassRefraction.toFloat(),
                onValueChange = { state.glassRefraction = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "边缘折射",
                summary = "AndroidLiquidGlass 透镜折射，0 为纯模糊",
                valueText = state.glassRefraction.toString() + " dp",
                valueRange = 0f..24f,
                steps = 23,
                enabled = state.glassNavbar,
                startAction = { SettingIcon(MiuixIcons.Image, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.glassHighlight.toFloat(),
                onValueChange = { state.glassHighlight = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "高光强度",
                summary = "玻璃边缘的镜面高光",
                valueText = state.glassHighlight.toString() + "%",
                valueRange = 0f..100f,
                steps = 99,
                enabled = state.glassNavbar,
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.glassTint.toFloat(),
                onValueChange = { state.glassTint = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "玻璃通透度",
                summary = "越高越通透，越低文字越清晰",
                valueText = state.glassTint.toString() + "%",
                valueRange = 10f..95f,
                steps = 84,
                enabled = state.glassNavbar,
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
        }
    }

    item { SmallTitle("设置卡片玻璃") }
    item {
        SettingsCard {
            SwitchPreference(
                checked = state.glassCards,
                onCheckedChange = { state.glassCards = it; state.commit() },
                title = "卡片液态玻璃",
                summary = "卡片模糊并透出下方壁纸，不再是纯白色块",
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SliderPreference(
                value = state.cardGlassBlur.toFloat(),
                onValueChange = { state.cardGlassBlur = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "卡片模糊",
                summary = "卡片背后壁纸的模糊半径",
                valueText = state.cardGlassBlur.toString() + " dp",
                valueRange = 0f..40f,
                steps = 39,
                enabled = state.glassCards,
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp)) {
                Text(
                    text = "卡片颜色",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = "卡片蒙版颜色，自动跟随壁纸明暗",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                GlassTabRow(
                    tabs = listOf("自动", "白色", "黑色", "自定义"),
                    selectedTabIndex = state.cardGlassColorMode,
                    onTabSelected = {
                        state.cardGlassColorMode = it
                        state.commit()
                    },
                )
            }
            if (state.cardGlassColorMode == CARD_COLOR_CUSTOM) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ColorPicker(
                        color = Color(state.cardGlassCustom),
                        onColorChanged = {
                            state.cardGlassCustom = it.copy(alpha = 1f).toArgb()
                            state.commit()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            }
            SliderPreference(
                value = state.cardGlassTint.toFloat(),
                onValueChange = { state.cardGlassTint = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "卡片通透度",
                summary = "越高壁纸越明显，越低卡片越实",
                valueText = state.cardGlassTint.toString() + "%",
                valueRange = 10f..95f,
                steps = 84,
                enabled = state.glassCards,
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            SwitchPreference(
                checked = state.cardInnerShadow,
                onCheckedChange = { state.cardInnerShadow = it; state.commit() },
                title = "卡片内阴影",
                summary = "卡片内壁的暗边，更像一块有厚度的玻璃；每张卡多一次离屏渲染",
                enabled = state.glassCards,
                startAction = { SettingIcon(MiuixIcons.Layers, Modifier.padding(end = 10.dp)) },
            )
        }
    }

    item { SmallTitle("文字颜色") }
    item {
        SettingsCard {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                GlassTabRow(
                    tabs = listOf("跟随主题", "白色", "黑色", "自定义"),
                    selectedTabIndex = state.textColorMode,
                    onTabSelected = {
                        state.textColorMode = it
                        state.commit()
                    },
                )
            }
            if (state.textColorMode == TEXT_COLOR_CUSTOM) {
                InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    ColorPicker(
                        color = Color(state.textColorCustom),
                        onColorChanged = {
                            state.textColorCustom = it.copy(alpha = 1f).toArgb()
                            state.commit()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    item { SmallTitle("背景") }
    item {
        SettingsCard {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                GlassTabRow(
                    tabs = listOf("纯色背景", "自定义图片"),
                    selectedTabIndex = if (state.backgroundMode == "custom") 1 else 0,
                    onTabSelected = {
                        state.backgroundMode = if (it == 1) "custom" else "gradient"
                        state.commit()
                    },
                )
            }
        }
    }
    item {
        SettingsCard {
            SliderPreference(
                value = state.backgroundBlur.toFloat(),
                onValueChange = { state.backgroundBlur = it.roundToInt() },
                onValueChangeFinished = { state.commit() },
                title = "壁纸模糊",
                summary = "只作用于自定义背景图片",
                valueText = state.backgroundBlur.toString() + " dp",
                valueRange = 0f..60f,
                steps = 59,
                enabled = state.backgroundMode == "custom",
                startAction = { SettingIcon(MiuixIcons.Theme, Modifier.padding(end = 10.dp)) },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "选择背景图片",
                summary = "从相册挑选一张图片作为页面底图",
                startAction = { SettingIcon(MiuixIcons.UploadCloud, Modifier.padding(end = 10.dp)) },
                onClick = onPickBackground,
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "存储权限",
                summary = if (hasImageAccess) {
                    "已授权 · " + PermissionManager.permissionSummary()
                } else {
                    "未授权，无法读取自定义图片 · 点此授权"
                },
                startAction = { SettingIcon(MiuixIcons.Info, Modifier.padding(end = 10.dp)) },
                onClick = onGrantAccess,
            )
        }
    }
    item {
        SettingsCard {
            ArrowPreference(
                title = "恢复玻璃与背景默认值",
                summary = "重置强调色、玻璃参数与背景设置",
                startAction = { SettingIcon(MiuixIcons.Reset, Modifier.padding(end = 10.dp)) },
                onClick = { state.resetGlass() },
            )
        }
    }
}


/* ---------------------------------------------------------------------------------------- */
/* Presets                                                                                   */
/* ---------------------------------------------------------------------------------------- */

@Composable
internal fun PresetActionsCard(state: SettingsState) {
    SettingsCard {
        ArrowPreference(
            title = "新建预设",
            summary = "把当前参数另存为一套新预设",
            startAction = { SettingIcon(MiuixIcons.Add, Modifier.padding(end = 10.dp)) },
            onClick = { state.addPreset() },
        )
        InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
        ArrowPreference(
            title = "复制当前预设",
            summary = "在当前预设基础上再改一套，互不影响",
            startAction = { SettingIcon(MiuixIcons.Copy, Modifier.padding(end = 10.dp)) },
            onClick = { state.duplicatePreset(state.activePresetId) },
        )
    }
}

/**
 * The 调节 tab is the preset list itself. The radio picks which set is in use, tapping the name
 * opens that preset's parameter page, and rename / delete live on that page rather than here.
 */
internal fun LazyListScope.tunePage(
    state: SettingsState,
    accentColor: Color,
    onOpenParams: () -> Unit,
) {
    item { SmallTitle("预设") }
    item { PresetListCard(state, accentColor, onOpenParams) }
    item { PresetActionsCard(state) }
}

/** Radio with a blue centre dot - the selection affordance for the preset list. */
@Composable
private fun RadioDot(selected: Boolean, accentColor: Color) {
    val ring = if (selected) accentColor else MiuixTheme.colorScheme.onSurfaceVariantActions
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
        }
    }
}

@Composable
internal fun PresetListCard(
    state: SettingsState,
    accentColor: Color,
    onOpenParams: () -> Unit,
) {
    SettingsCard {
        state.presets.forEachIndexed { index, preset ->
            if (index > 0) InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { state.applyPreset(preset.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    RadioDot(
                        selected = preset.id == state.activePresetId,
                        accentColor = accentColor,
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            // Opening a preset also makes it the active one, so the editor always
                            // edits what the user just tapped.
                            state.applyPreset(preset.id)
                            onOpenParams()
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = preset.summary(),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

/** Rename entry shown at the top of the parameter page. */
@Composable
internal fun PresetNameCard(state: SettingsState) {
    val active = state.presets.firstOrNull { it.id == state.activePresetId }
    var renameText by remember { mutableStateOf(TextFieldValue("")) }
    var renaming by remember { mutableStateOf(false) }

    SettingsCard {
        ArrowPreference(
            title = "预设名称",
            summary = active?.name ?: "",
            startAction = { SettingIcon(MiuixIcons.Edit, Modifier.padding(end = 10.dp)) },
            onClick = {
                renameText = TextFieldValue(active?.name ?: "")
                renaming = true
            },
        )
    }

    if (renaming && active != null) {
        GlassWindowDialog(
            show = true,
            onDismissRequest = { renaming = false },
            title = "重命名预设",
            summary = "当前：" + active.name,
        ) {
            Column {
                TextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = "预设名称",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DialogButton("取消", Modifier.weight(1f)) { renaming = false }
                    DialogButton("保存", Modifier.weight(1f), primary = true) {
                        state.renamePreset(active.id, renameText.text)
                        renaming = false
                    }
                }
            }
        }
    }
}

/** Delete action for the current preset, kept at the very bottom of the parameter page. */
@Composable
internal fun PresetDeleteCard(state: SettingsState) {
    val active = state.presets.firstOrNull { it.id == state.activePresetId }
    var confirming by remember { mutableStateOf(false) }

    SettingsCard {
        ArrowPreference(
            title = "删除此预设",
            summary = if (state.presets.size > 1) {
                "删除「" + (active?.name ?: "") + "」，不可撤销"
            } else {
                "至少需要保留一套预设"
            },
            titleColor = BasicComponentDefaults.titleColor(
                color = if (state.presets.size > 1) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
            ),
            startAction = { SettingIcon(MiuixIcons.Delete, Modifier.padding(end = 10.dp), tint = MiuixTheme.colorScheme.error) },
            enabled = state.presets.size > 1,
            onClick = { confirming = true },
        )
    }

    if (confirming && active != null) {
        GlassWindowDialog(
            show = true,
            onDismissRequest = { confirming = false },
            title = "删除预设",
            summary = "将删除「" + active.name + "」，此操作不可撤销。",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton("取消", Modifier.weight(1f)) { confirming = false }
                DialogButton("删除", Modifier.weight(1f), primary = true) {
                    state.deletePreset(active.id)
                    confirming = false
                }
            }
        }
    }
}


/** Version plus the GitHub update.json check, mirroring KernelSU's update manifest shape. */
@Composable
internal fun UpdateCard(accentColor: Color) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val hasUpdate = info != null && info!!.versionCode.toLong() > ModuleInfo.versionCode(context)

    SettingsCard {
        Column(modifier = Modifier.padding(20.dp)) {
            CardTitle("版本与更新", accentColor)
            Spacer(Modifier.height(12.dp))
            KeyValueRow(
                "当前版本",
                ModuleInfo.versionName(context) + " (" + ModuleInfo.versionCode(context) + ")",
            )
            InsetDivider(16.dp)
            KeyValueRow(
                label = "最新版本",
                value = when {
                    checking -> "检查中…"
                    hasUpdate -> info!!.version
                    info != null -> info!!.version + "（最新）"
                    error != null -> "检查失败"
                    else -> "未检查"
                },
                valueColor = if (hasUpdate) accentColor else MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedActionButton(
                text = if (checking) "检查中…" else "检查更新",
                accent = accentColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (checking) return@OutlinedActionButton
                    checking = true
                    error = null
                    scope.launch {
                        val result = checkForUpdate()
                        checking = false
                        result
                            .onSuccess { latest ->
                                info = latest
                                if (latest.versionCode.toLong() <= ModuleInfo.versionCode(context)) {
                                    Toast.makeText(context, "当前已经是最新版", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .onFailure { throwable ->
                                error = throwable.message ?: "检查更新失败"
                                Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
            )
            if (hasUpdate) {
                Spacer(Modifier.height(10.dp))
                OutlinedActionButton(
                    text = "下载 " + info!!.version,
                    accent = accentColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openUrl(context, info!!.zipUrl) },
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedActionButton(
                text = "项目主页",
                accent = accentColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, ModuleInfo.HOMEPAGE) },
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "检查失败：" + error,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Backup                                                                                    */
/* ---------------------------------------------------------------------------------------- */

internal fun LazyListScope.backupPage(
    state: SettingsState,
    accentColor: Color,
) {
    item {
        SettingsCard {
            ArrowPreference(
                title = "保存当前参数",
                summary = "立即写入作用域配置",
                startAction = { SettingIcon(MiuixIcons.Backup, Modifier.padding(end = 10.dp)) },
                onClick = { state.commit() },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "备份当前参数",
                summary = "复制到独立的备份配置文件",
                startAction = { SettingIcon(MiuixIcons.UploadCloud, Modifier.padding(end = 10.dp)) },
                onClick = { state.backup() },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "恢复备份",
                summary = "用备份覆盖当前全部参数",
                startAction = { SettingIcon(MiuixIcons.Download, Modifier.padding(end = 10.dp)) },
                onClick = { state.restoreBackup() },
            )
        }
    }

    item { SmallTitle("恢复预设") }
    item {
        SettingsCard {
            ArrowPreference(
                title = "恢复 iOS 预设",
                summary = "180 / 200 dp 宽度，6.4 dp 高度，胶囊圆角",
                startAction = { SettingIcon(MiuixIcons.Reset, Modifier.padding(end = 10.dp)) },
                onClick = { state.resetToIosPreset() },
            )
            InsetDivider(ROW_DIVIDER_INDENT_DP.dp)
            ArrowPreference(
                title = "恢复 ColorOS 默认",
                summary = "120 / 200 dp 宽度，4 dp 高度，2 dp 圆角",
                startAction = { SettingIcon(MiuixIcons.Info, Modifier.padding(end = 10.dp)) },
                onClick = { state.resetToSystem() },
            )
        }
    }

    item {
        SettingsCard {
            KeyValueRow("持久化", "LSPosed remote preferences")
            InsetDivider(16.dp)
            KeyValueRow("备份文件", SettingsStoreKeys.BACKUP_NAME)
        }
    }
}

private object SettingsStoreKeys {
    const val BACKUP_NAME = "settings_backup"
}

/* ---------------------------------------------------------------------------------------- */
/* About                                                                                     */
/* ---------------------------------------------------------------------------------------- */

private const val MODULE_NAME = "iOS 沉浸式小横条"
private const val MODULE_AUTHOR = "作者：Codex"
private const val MODULE_DESC =
    "面向 Android 16/17（ColorOS 16/17）的单 APK LSPosed 模块。它只向 com.android.systemui " +
        "注入 Hook，不替换 framework、SystemUI 或导航模式 RRO，因此不会破坏显示模块的资源映射。"

/** 首页状态卡与「模块版本」行共用的版本文本，统一走 PackageManager，避免再有漏改的硬编码。 */
@Composable
private fun moduleVersionText(): String {
    val context = LocalContext.current
    return ModuleInfo.versionName(context) + " (" + ModuleInfo.versionCode(context) + ")"
}

private fun buildDiagnostics(context: Context): String = buildString {
    append("模块: ").append(MODULE_NAME).append('\n')
    append("版本: ").append(ModuleInfo.versionName(context)).append(" (")
        .append(ModuleInfo.versionCode(context)).append(")").append('\n')
    append("框架: ").append(ModuleInfo.XPosed_API).append(" (2.1.1-it)").append('\n')
    append("作用域: com.android.systemui").append('\n')
    append("设备: ").append(Build.MODEL).append('\n')
    append("系统: Android ").append(Build.VERSION.RELEASE).append(" / SDK ").append(Build.VERSION.SDK_INT).append('\n')
    append("ABI: ").append(Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    runCatching {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
    Toast.makeText(context, "已复制" + label, Toast.LENGTH_SHORT).show()
}

internal fun LazyListScope.aboutPage(
    state: SettingsState,
    accentColor: Color,
    onRestart: () -> Unit,
) {
    item {
        val context = LocalContext.current
        SettingsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = MODULE_NAME,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = MODULE_AUTHOR,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(18.dp))
                BarMark(size = 76.dp, accent = accentColor)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = MODULE_DESC,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedActionButton(
                        text = "重启作用域",
                        accent = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = onRestart,
                    )
                    OutlinedActionButton(
                        text = "复制版本信息",
                        accent = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { copyToClipboard(context, "版本信息", buildDiagnostics(context)) },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedActionButton(
                        text = "保存当前参数",
                        accent = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { state.commit() },
                    )
                    OutlinedActionButton(
                        text = "备份当前参数",
                        accent = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { state.backup() },
                    )
                }
            }
        }
    }

    item {
        SettingsCard {
            Column(modifier = Modifier.padding(20.dp)) {
                CardTitle("模块信息", accentColor)
                Spacer(Modifier.height(12.dp))
                KeyValueRow("接口版本", ModuleInfo.XPosed_API)
                InsetDivider(16.dp)
                KeyValueRow("作用域", "com.android.systemui")
                InsetDivider(16.dp)
                KeyValueRow("持久化", "remote preferences")
                Spacer(Modifier.height(12.dp))
                OutlinedActionButton(
                    text = "重启 SystemUI 作用域",
                    accent = accentColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestart,
                )
            }
        }
    }

    item { UpdateCard(accentColor) }

    item {
        val context = LocalContext.current
        SettingsCard {
            Column(modifier = Modifier.padding(20.dp)) {
                CardTitle("运行环境", accentColor)
                Spacer(Modifier.height(12.dp))
                KeyValueRow("设备型号", Build.MODEL)
                InsetDivider(16.dp)
                KeyValueRow("系统版本", "Android " + Build.VERSION.RELEASE + " · ColorOS 17")
                InsetDivider(16.dp)
                KeyValueRow("系统 SDK", Build.VERSION.SDK_INT.toString())
                InsetDivider(16.dp)
                KeyValueRow("CPU 架构", Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
                Spacer(Modifier.height(12.dp))
                OutlinedActionButton(
                    text = "复制诊断信息",
                    accent = accentColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { copyToClipboard(context, "诊断信息", buildDiagnostics(context)) },
                )
            }
        }
    }
}

