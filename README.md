<p align="center"><img src="docs/icon.svg" width="112" alt="S Tool 图标"></p>
<h1 align="center">S Tool</h1>
<p align="center">一个离线运行的原生 Android 小工具箱。</p>
<p align="center"><a href="https://github.com/LinnBenson/S-Tool/releases/latest">下载正式版</a> · <a href="https://github.com/LinnBenson/S-Tool/issues">反馈问题</a></p>

## 下载与安装

当前版本 **0.1.1**，支持 **Android 9（API 28）及以上**。

从 [GitHub Releases](https://github.com/LinnBenson/S-Tool/releases/tag/v0.1.1) 下载 `S-Tool-0.1.1.apk`，使用 Android 安装器安装。Release 同时提供 `SHA256SUMS.txt` 以校验下载文件。正式包名为 `io.softwarebyandroid.tool`。

## 功能

### SVG 查看器

- 使用系统文件选择器打开 SVG，查看矢量预览和源码。
- 支持双指缩放、双指平移、缩放按钮、适应屏幕及全屏查看。
- 复制源码，将渲染结果按原始尺寸导出为透明 PNG。

### 取色器

- 选择图片，单指点击或拖动取色，双指缩放和平移图片。
- 显示像素坐标、HEX、RGB 和透明度，HEX 与 RGB 均可单独复制。
- 提供像素放大镜和方向按钮，便于逐像素微调位置。
- 根据原始图片像素取色，预览缩放不改变结果。

### 水印工具

- 为图片添加覆盖全图的平铺文字水印，导出为原尺寸 PNG。
- 设置文字、字号、横向间隔、纵向间隔、颜色、不透明度和倾斜角度。
- 支持 HEX 输入、预设颜色以及双指缩放和平移预览。
- 在本机记住最后一组有效参数，下次导入或重新打开应用时复用。
- 导出以点击导出时的水印预览为准，不修改原图。

三个工具的选择与导出/复制操作均位于右上角，以图标显示；取色结果中的 HEX / RGB 仍可分别复制。

### 界面与适配

使用 Kotlin、Jetpack Compose 和 Material 3。支持跟随系统、浅色和深色主题；宽屏采用预览与操作分栏，折叠屏按铰链位置分区。旋转、折叠和正常切换后台时保留当前会话。

## 隐私与后台行为

- 不申请网络权限，不上传图片、文件或处理结果，不收集使用分析。
- 不持久保存输入图片、SVG、文件路径、文件名、取色记录和操作历史。
- 仅保存主题偏好及最后一组有效水印参数（包含水印文字），不记录历史版本；应用备份已禁用。
- 文件访问使用系统选择器的临时授权，不申请整库存储权限或长期 URI 授权。
- 仅在主动导出时保存到指定位置，主动点击复制时写入剪贴板。
- 没有自启动、后台服务、保活、定时任务或后台轮询。正常切后台和打开系统选择器不会主动清空会话；系统终止进程后不会自动恢复之前的图片。
- 退出工具释放图片引用。媒体处理受内存预算限制，大内存操作串行执行。

## 使用限制

- SVG 渲染基于 AndroidSVG 1.4，主要支持静态 SVG；动画、滤镜、`foreignObject`、外部资源及部分文字/CSS 效果不支持。
- SVG 在进入渲染器前通过 XML 预检；含 `<image>` 的文件（包括内嵌 Base64、外部图片）及 DTD 均会被拒绝，请改用纯矢量 SVG。
- 开发版和正式版均在构建时移除 AndroidSVG 日志调用，不将 SVG 内容写入系统日志；原始源码仍可由用户主动查看和复制。
- SVG 文件最大 4 MB；PNG 输出边长最大 8192 像素，总像素最多 1600 万。源码整体复制上限为 128,000 字符。
- 取色和水印支持 Android 解码器可读取的位图格式，如 PNG、JPEG、WebP；动态图片使用首帧，水印工具不直接读取 SVG。
- 图片最多 1600 万像素，同时受设备内存预算限制；取色会处理 EXIF 方向并转换到 sRGB。半透明像素可能受到解码器预乘精度影响。
- 水印文字最多 64 个 Unicode 字符；字号和间隔按原图像素计，角度为 −90°～90°。过密配置（超过 8000 个文字块）会提示调整。
- 缩放范围为适应视图的 25%～1600%，只影响预览。

## 本地构建

需要 JDK 17、Android SDK Platform 35、Build Tools 34.0.0。工程使用 Gradle 8.9、AGP 8.7.3，依赖版本集中在 `gradle/libs.versions.toml`。

```sh
git clone https://github.com/LinnBenson/S-Tool.git
cd S-Tool
# 在 local.properties 中填写本机 sdk.dir，或配置 ANDROID_HOME
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug
```

首次构建需要下载依赖。调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`，包名为 `io.softwarebyandroid.tool.debug`，显示名称为 **S Tool Dev**，可与正式版并存。

### 签名构建

正式版使用 R8 和资源压缩，签名材料不在仓库中。构建脚本从工程同级的 `../key/signing.properties` 读取以下字段：

```properties
storeFile=../key/your-release-key.p12
storePassword=YOUR_LOCAL_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_LOCAL_KEY_PASSWORD
```

```sh
./gradlew assembleRelease
```

输出为 `app/build/outputs/apk/release/app-release.apk`。密钥缺失时构建会失败。自行构建请使用自己的签名，不提交密钥或密码；使用不同签名的 APK 无法覆盖安装官方签名版本。

### 设备测试

```sh
./gradlew assembleDebug assembleDebugAndroidTest
adb -s DEVICE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_SERIAL install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s DEVICE_SERIAL shell am instrument -w io.softwarebyandroid.tool.debug.test/androidx.test.runner.AndroidJUnitRunner
```

测试覆盖坐标映射、SVG 解析和导出、取色复制、双指手势、水印渲染及参数记忆、后台往返、字体缩放和折叠姿态布局。折叠布局通过姿态注入验证，尚未完成真实折叠屏铰链硬件测试。

## 代码结构

```text
app/src/main/java/io/softwarebyandroid/tool/
├── MainActivity.kt       # Android 入口
├── app/                  # 导航、页面宿主、全局设置
├── core/data/            # 文件、图片、坐标及窗格算法
├── core/ui/              # 主题、公共组件、手势与自适应布局
└── feature/
    ├── home/             # 首页
    ├── settings/         # 设置
    ├── svg/              # SVG 查看器
    ├── color/            # 取色器
    └── watermark/        # 水印工具
```

工具页面、状态控制器和数据处理按功能分离。单元测试位于 `app/src/test`，设备测试位于 `app/src/androidTest`。

## 反馈

欢迎通过 [Issues](https://github.com/LinnBenson/S-Tool/issues) 提交问题或建议。请附上应用版本、Android 版本、设备型号和复现步骤；示例图片请先移除私人信息。

`buildSrc` 中的 AndroidSVG 字节码处理仅移除日志调用，适用于全部构建类型。升级渲染依赖时必须重新运行 SVG 安全测试，并检查最终 APK 中没有残留该依赖的日志调用。

可用 Android SDK 的 `dexdump` 检查 APK 中的 AndroidSVG 日志调用：

```sh
python3 tools/verify_svg_privacy.py app/build/outputs/apk/release/app-release.apk "$ANDROID_HOME/build-tools/34.0.0/dexdump"
```

0.1.1 在 BlueStacks Android 13 上通过 24 项设备测试（含 3 项 SVG 安全回归），并通过单元测试和 lint。人工构造的约 101 KB / 内嵌 4096×4096 位图 SVG 现会在渲染前被拒绝。
