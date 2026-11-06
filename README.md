# 楚河棋社

面向 PICO Spatial 的沉浸式象棋游戏，在立体棋社中使用水平棋盘下棋。

- 初级、中级、高级人机对弈。
- 闯关挑战与一步将死残局练习。
- 合法走法、将军和终局判断，走棋提示及悔棋。
- 双方落子声音、金色与蓝色最近棋步标记。
- 棋盘旁的模式、难度、关卡、回合和胜负提示。
- 本地对局与关卡进度保存。

先选己方棋子，再选目标位置落子。绿色标记表示可落点；金色和蓝色分别标记玩家与对手最近的棋步。

## 构建

使用 JDK 21、Android SDK 35 和项目 Gradle Wrapper。PICO Spatial SDK BOM 为 6.2.8。
配置本机 Android SDK 路径后运行：

```sh
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew testDebugUnitTest
```

Windows 使用 `gradlew.bat`。Debug 安装包位于 `app/build/outputs/apk/debug/`；Release 产物位于 `app/build/outputs/apk/release/`，分发前需要签名。VibeLab 根据关联的源码提交进行构建。

棋盘与棋子来自 Spatial Editor，预先打包的 `.bundle` 和 `.scenes.json` 位于 `app/src/main/assets/scenes/`。常规构建直接使用这些资源。汉字、棋社环境与交互逻辑由应用提供。

适用于支持该 Spatial SDK 的 PICO 系统。当前已在 PICO OS 6.3 真机运行；象棋规则按休闲对局处理重复局面和无进展判和。

本地发布图片和工具日志不属于构建源码。
