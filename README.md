# LegitKeepAlive - 安卓应用合法保活解决方案

## 🧐 项目背景

在复杂的安卓生态中，尤其是面对国内各大厂商（OEM）高度定制的操作系统，开发者常常会遇到一个棘手的问题：应用在后台运行时，容易被系统的省电策略“杀死”，导致推送、定位、定时任务等功能无法正常工作。

`LegitKeepAlive` 旨在解决这一痛点。本项目**不使用任何黑科技或非公开API**，而是提供了一套**合法的、以用户为中心**的解决方案。其核心思想是友好地引导用户**手动**前往系统设置页面，授予应用在后台持续运行所需的权限，从而实现“合法保活”。

## ✨ 方案特性

* **配置驱动**: 无需修改代码，只需在 `assets` 目录中添加或修改 JSON 配置文件，即可轻松适配新的手机厂商和操作系统版本。
* **智能适配**: 方案会自动检测设备的厂商和系统版本，加载最匹配的配置，向用户展示最有效的设置项。
* **精确跳转**: 能够精准地构建 `Intent`，将用户一键引导至系统深层的设置页面（如小米的“神隐模式”、荣耀的“应用启动管理”等），极大提升了用户体验。
* **状态检测**: 集成了对标准及非标准权限的状态检测，能够向用户清晰地展示哪些设置项已经完成，哪些尚待开启。
* **架构清晰**: 采用 MVVM 架构，将数据层（Repository）、逻辑层（ViewModel）和 UI 层（Activity）清晰地分离，便于开发者理解、集成或二次开发。
* **厂商兼容层**: 内置了针对特定厂商（如小米、荣耀）的帮助类 (`OemPermissionHelper`)，用于处理非标准的权限检查。

## 🚀 工作原理

本解决方案的整套流程如下：

1.  **识别设备**: 通过 `Build.MANUFACTURER` 获取设备厂商。
2.  **加载配置**: `SettingsRepository` 根据厂商名加载对应的 JSON 文件（如 `xiaomi.json`），若无专属配置则加载 `default.json`。
3.  **匹配版本**: 如果配置文件中定义了特定系统版本的设置 (`version_specifics`)，`MainViewModel` 会读取系统属性，匹配最合适的设置列表。
4.  **展示UI**: `MainActivity` 从 `ViewModel` 获取设置列表，并通过 `RecyclerView` 渲染出来。同时，通过 `StatusCheckerRegistry` 检查各项设置的当前状态，更新UI。
5.  **引导跳转**: 当用户点击某个设置项时，`MainActivity` 会根据该项在 JSON 中配置的 `intent` 信息（`action` 或 `component`）构建 `Intent`，并启动 `Activity`，从而将用户带到目标设置页。

## 🛠️ 如何集成到你的项目

开发者可以参考本项目的实现，将这套解决方案集成到自己的应用中。

1.  **拷贝核心模块**:
    * 将 `util`、`model`、`enumid`、`data` 和 `ui` 包下的所有代码拷贝到你的项目中。
    * 将 `permission` 包下的权限处理逻辑拷贝过去，或者根据你的项目需求进行调整。
    * 拷贝 `assets` 目录下的所有 JSON 配置文件作为基础。

2.  **添加依赖**: 参考 `app/build.gradle.kts` 和 `gradle/libs.versions.toml`，确保添加了 `lifecycle-viewmodel`、`kotlinx-serialization-json` 和 `XXPermissions` 等必要的依赖。

3.  **创建引导页面**:
    * 在你的应用中创建一个类似 `MainActivity` 的页面。
    * 在该页面中，实例化 `MainViewModel`，观察其 `LiveData`，并将设置列表展示给用户。
    * 实现列表项的点击逻辑，调用 `executeNavigation` 方法来跳转到系统设置。

4.  **自定义和扩展**:
    * 通过修改或添加 `assets` 目录下的 JSON 文件来适配更多设备。
    * 你可以轻松地在 `StatusCheckerRegistry` 中为新的 `SettingId` 添加状态检查逻辑。

## 📱 已支持的配置

* **小米 (xiaomi)**: 适配至 MIUI 12。
* **荣耀 (honor)**: 适配至 Magic OS 9.x。
* **通用安卓 (default)**: 为其他未特定适配的设备提供基础的保活设置项。
