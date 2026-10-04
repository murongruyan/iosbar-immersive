# LSPosed 通过 META-INF/xposed/java_init.list 里写死的类名反射加载模块入口，R8 不会读这个
# 文件，所以入口类的名字和成员都必须原样保留 —— 否则模块在 LSPosed 里会直接加载失败。
-keep class com.iosbar.navhook.IosBarHook { *; }

# libxposed 自己带了 consumer rules 保留 io.github.libxposed.api.**；这里只是压掉注解类缺失的告警。
-dontwarn io.github.libxposed.annotation.**

# Miuix / Compose 的正常告警，避免 R8 因为不可达分支报 missing class。
-dontwarn org.jetbrains.skia.**
-dontwarn org.jetbrains.skiko.**
