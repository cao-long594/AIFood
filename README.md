# AIFood

一款基于 **Android + Java** 开发的饮食记录与营养分析 App，用于帮助用户记录每日饮食、管理饮水目标、维护食物库，并通过日 / 周 / 月维度的数据分析更直观地了解摄入情况。

## 项目简介

AIFood 面向个人健康管理场景，围绕“吃了什么、喝了多少、营养是否达标”三个核心问题进行设计。项目采用本地数据持久化方案，重点实现了饮食记录、营养统计、日期联动、历史回溯和食物库管理等功能，适合作为 Android 本地应用、Jetpack 架构实践和数据可视化项目展示。

## 功能特性

- **首页营养分析**
  - 按天 / 周 / 月统计热量、碳水、蛋白质、脂肪等摄入数据
  - 支持目标值对比、剩余热量展示、脂肪构成分析
  - 基于 `MPAndroidChart` 展示趋势图和多维度摄入情况

- **餐次记录**
  - 支持按早餐、午餐、下午加餐、晚餐等不同餐次记录饮食
  - 可从食物库快速添加食物，并输入摄入重量自动换算营养值
  - 支持餐次内食物编辑、删除、热量占比分析

- **饮水打卡**
  - 支持快捷添加常用饮水量和自定义输入
  - 支持设置每日饮水目标，并实时展示完成进度
  - 可查看指定日期饮水记录并进行删除操作

- **食物库管理**
  - 内置默认食物数据，首次进入自动导入本地数据库
  - 支持搜索、分类展示、新增、编辑、删除食物
  - 支持维护每种食物的营养成分数据，提升记录效率

- **历史视图**
  - 提供日 / 周 / 月三种历史维度浏览方式
  - 支持日期选择与首页、餐次页联动
  - 对较大范围日期数据进行了后台线程计算优化，提升切换流畅度

## 技术亮点

- **Jetpack 架构实践**
  - 使用 `Room` 构建本地数据库，管理 `Food`、`MealRecord`、`WaterRecord` 等核心实体
  - 通过 `ViewModel + LiveData` 实现跨页面日期状态共享与界面联动
  - 使用 `Repository` 封装数据访问逻辑，降低 UI 层与数据库层耦合

- **线程与性能优化**
  - 通过 `AppExecutors` 将数据库读写切换到 IO 线程，避免阻塞主线程
  - 历史页的大范围月份 / 年份数据构建放到后台线程执行
  - 结合 `DiffUtil`、`RecyclerView` 和自定义视图优化页面刷新体验

- **数据分析能力**
  - 基于 `NutritionCalculator` 和 `NutritionService` 计算热量、三大营养素和脂肪构成
  - 在首页对不同时间粒度下的数据进行聚合和趋势可视化展示

- **良好的可扩展性**
  - 模块划分清晰，便于继续扩展登录、云同步、提醒通知、后端接口等功能
  - 当前架构适合作为 Android 本地应用升级到前后端联动项目的基础版本

## 技术栈

- **开发语言**: Java 17
- **Android SDK**: minSdk 24, targetSdk 35, compileSdk 35
- **架构模式**: 分层架构 + Repository 模式
- **核心组件**:
  - AndroidX AppCompat
  - Material Components
  - ViewModel
  - LiveData
  - Room
  - RecyclerView
  - ViewPager2
  - ConstraintLayout
  - MPAndroidChart

## 项目结构

```text
AIFood/
├─ app/
│  ├─ src/main/java/com/example/food/
│  │  ├─ data/repository/     # 数据仓库层
│  │  ├─ data/preferences/    # 本地偏好设置
│  │  ├─ db/                  # Room 数据库、DAO、实体、默认数据导入
│  │  ├─ domain/service/      # 业务服务层
│  │  ├─ model/               # 营养模型与计算逻辑
│  │  ├─ ui/home/             # 首页营养分析
│  │  ├─ ui/meal/             # 餐次记录
│  │  ├─ ui/water/            # 饮水记录
│  │  ├─ ui/foodbank/         # 食物库
│  │  ├─ ui/history/          # 历史视图
│  │  └─ utils/               # 通用工具类
│  └─ src/main/res/           # 布局、颜色、字符串、图标、默认食物数据
├─ gradle/
├─ build.gradle.kts
└─ settings.gradle.kts
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-name/AIFood.git
cd AIFood
```

### 2. 使用 Android Studio 打开项目

推荐使用较新的 Android Studio 版本，确保本地已安装：

- Android SDK 35
- JDK 17
- Gradle 环境

### 3. 运行项目

连接真机或启动模拟器后，直接运行 `app` 模块即可。

也可以使用命令行构建：

```bash
./gradlew assembleDebug
```

Windows:

```bash
gradlew.bat assembleDebug
```

## 数据说明

- 项目默认会从 `app/src/main/res/raw/default_foods.json` 导入基础食物数据
- 食物、饮食记录、饮水记录均保存在本地 Room 数据库中
- 用户目标值（如饮水目标、营养目标）通过本地偏好设置保存

## 适用场景

这个项目适合作为：

- Android 课程设计 / 期末项目展示
- 校招 / 实习阶段的移动端作品集项目
- Room、ViewModel、LiveData、RecyclerView、图表可视化的综合练习项目
- 后续扩展为云端健康管理应用的基础版本

## 后续可优化方向

- 接入用户登录与云同步能力
- 增加提醒通知、打卡统计和连续签到能力
- 增加拍照识别食物或 AI 营养建议功能
- 增加深色模式、多语言和更完整的单元测试
- 接入后端接口，实现多端数据同步

## License

本项目仅用于学习与交流，若需商用请根据你的实际需求补充 License。
