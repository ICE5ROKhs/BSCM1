# Android 应用打包指南

本指南将帮助您将 BSCM 项目打包为 Android 安装包（APK/AAB）。

## 前置要求

在开始之前，请确保您已安装以下工具：

1. **Node.js** (16+)
2. **Java JDK** (11 或更高版本，推荐 JDK 17)
3. **Android Studio** (最新版本)
   - 包含 Android SDK
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
4. **Gradle** (通常随 Android Studio 安装)

## 环境变量配置

1. 设置 `JAVA_HOME` 环境变量指向 JDK 安装目录
2. 设置 `ANDROID_HOME` 环境变量指向 Android SDK 目录（通常在 `%LOCALAPPDATA%\Android\Sdk`）
3. 将 `%ANDROID_HOME%\platform-tools` 添加到 PATH 环境变量

### 验证安装

```powershell
# 检查 Java
java -version

# 检查 Android SDK
# 在 Android Studio 中：File -> Settings -> Appearance & Behavior -> System Settings -> Android SDK
```

## 配置后端 API 地址

在移动应用中，需要配置完整的后端 API URL，而不是相对路径。

### 方法一：使用环境变量（推荐）

1. 在 `frontend` 目录创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://your-backend-server.com
```

2. 或者在构建时设置：

```powershell
$env:VITE_API_BASE_URL="http://your-backend-server.com"; npm run build
```

### 方法二：在应用中配置（运行时）

应用启动后，用户可以在设置中配置 API 地址（如果实现了此功能）。

**注意**：将 `frontend/src/config/api.js` 中的 `'http://your-backend-server.com'` 替换为您的实际后端服务器地址。

## 构建步骤

### 1. 安装依赖

```powershell
cd frontend
npm install
```

### 2. 配置 API 地址

编辑 `frontend/src/config/api.js`，将默认的 API 地址替换为您的后端服务器地址。

### 3. 构建前端

```powershell
npm run build
```

这将生成优化的生产版本到 `dist` 目录。

### 4. 同步到 Android 项目

```powershell
npm run cap:sync
```

或手动执行：

```powershell
npx cap sync
```

此命令会：
- 将 `dist` 目录的内容复制到 Android 项目
- 更新 Android 插件
- 同步 Capacitor 配置

### 5. 打开 Android Studio

```powershell
npm run cap:open:android
```

或在 Android Studio 中：
1. File -> Open
2. 选择 `frontend/android` 目录

### 6. 配置签名（用于发布）

#### 生成签名密钥

```powershell
keytool -genkey -v -keystore bscm-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias bscm
```

#### 配置签名

在 `frontend/android/app/build.gradle` 中添加签名配置：

```gradle
android {
    ...
    signingConfigs {
        release {
            storeFile file('path/to/your/bscm-release-key.jks')
            storePassword 'your-store-password'
            keyAlias 'bscm'
            keyPassword 'your-key-password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            ...
        }
    }
}
```

**安全提示**：不要在代码中硬编码密码，使用 `local.properties` 或环境变量。

### 7. 构建 APK/AAB

#### 方式一：使用 Android Studio（推荐）

1. 在 Android Studio 中打开项目
2. Build -> Generate Signed Bundle / APK
3. 选择 APK 或 Android App Bundle (AAB)
4. 选择签名配置
5. 选择构建类型（Release）
6. 点击 Finish

#### 方式二：使用命令行

```powershell
cd frontend/android
.\gradlew assembleRelease  # 构建 APK
.\gradlew bundleRelease    # 构建 AAB (用于 Google Play)
```

构建产物位置：
- APK: `frontend/android/app/build/outputs/apk/release/app-release.apk`
- AAB: `frontend/android/app/build/outputs/bundle/release/app-release.aab`

## 开发调试

### 在 Android 设备/模拟器上运行

1. 启动 Android 模拟器或连接真实设备
2. 在 Android Studio 中点击 Run 按钮
3. 或使用命令行：

```powershell
cd frontend/android
.\gradlew installDebug
```

### 使用 Capacitor Live Reload（开发模式）

1. 在一个终端运行前端开发服务器：

```powershell
cd frontend
npm run dev
```

2. 在另一个终端同步并运行：

```powershell
cd frontend
npx cap sync
npx cap run android
```

## 应用配置

### 修改应用信息

编辑 `frontend/capacitor.config.json`：

```json
{
  "appId": "com.bscm.app",
  "appName": "BSCM",
  "webDir": "dist"
}
```

### 修改 Android 应用设置

主要配置文件：
- `frontend/android/app/build.gradle` - 构建配置、版本号、依赖
- `frontend/android/app/src/main/AndroidManifest.xml` - 应用权限、Activity 配置

### 应用权限

根据应用需求，在 `AndroidManifest.xml` 中添加必要权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- 如果需要相机功能 -->
<uses-permission android:name="android.permission.CAMERA" />
<!-- 如果需要访问存储 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 常见问题

### 1. 构建失败：找不到 Android SDK

确保设置了 `ANDROID_HOME` 环境变量，并在 Android Studio 中安装了所需的 SDK 组件。

### 2. API 请求失败（网络错误）

- 检查 API 地址配置是否正确
- 确保 Android 应用有 INTERNET 权限
- 如果使用 HTTP（非 HTTPS），需要在 `AndroidManifest.xml` 中配置网络安全：

```xml
<application
    ...
    android:usesCleartextTraffic="true">
    ...
</application>
```

### 3. 版本冲突

如果遇到 Gradle 版本或依赖冲突，可以：
- 更新 Android Studio 到最新版本
- 更新 Gradle wrapper：`cd frontend/android && .\gradlew wrapper --gradle-version=8.0`

### 4. 应用图标和启动画面

- 应用图标：替换 `frontend/android/app/src/main/res` 目录下的图标资源
- 启动画面：在 Capacitor 配置中配置，或使用插件

## 发布到 Google Play

1. 创建 Google Play 开发者账号
2. 在 Google Play Console 创建应用
3. 构建 Release AAB 文件
4. 上传 AAB 到 Google Play Console
5. 填写应用信息、截图等
6. 提交审核

## 更新应用

当需要更新应用时：

1. 修改前端代码
2. 运行 `npm run build`
3. 运行 `npm run cap:sync`
4. 在 Android Studio 中增加版本号（`build.gradle` 中的 `versionCode` 和 `versionName`）
5. 重新构建并发布

## 相关资源

- [Capacitor 官方文档](https://capacitorjs.com/docs)
- [Android 开发者文档](https://developer.android.com/)
- [Google Play 发布指南](https://developer.android.com/distribute/googleplay/start)


