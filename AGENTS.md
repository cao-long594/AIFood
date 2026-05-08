# AGENTS.md

## 项目结构速览

这是一个原生 Android 项目，主模块是 `app`，主要代码使用 Java，页面使用 XML 布局。

- 应用入口：`app/src/main/java/com/example/food/ui/login/LoginActivity.java`
- 主页面容器：`app/src/main/java/com/example/food/MainActivity.java`
- 页面代码：`app/src/main/java/com/example/food/ui`
- 页面布局：`app/src/main/res/layout`
- 图标和 XML 背景：`app/src/main/res/drawable`
- 位图背景资源：`app/src/main/res/drawable-nodpi`
- 颜色、尺寸、字符串、主题：`app/src/main/res/values`
- 数据库和 DAO：`app/src/main/java/com/example/food/db`
- Repository：`app/src/main/java/com/example/food/data/repository`
- 偏好数据：`app/src/main/java/com/example/food/data/preferences`
- 工具类：`app/src/main/java/com/example/food/utils`

## 页面文件索引

### 登录页

- 页面：`app/src/main/java/com/example/food/ui/login/LoginActivity.java`
- 布局：`app/src/main/res/layout/activity_login.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/UserRepository.java`
- 用户会话：`app/src/main/java/com/example/food/data/preferences/UserSessionPreferences.java`

### 注册页

- 页面：`app/src/main/java/com/example/food/ui/register/RegisterActivity.java`
- 布局：`app/src/main/res/layout/activity_register.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/UserRepository.java`

### 主页面容器和底部导航

- 页面容器：`app/src/main/java/com/example/food/MainActivity.java`
- 布局：`app/src/main/res/layout/activity_main.xml`
- 底部菜单：`app/src/main/res/menu/bottom_nav_menu.xml`
- 导航图标：`app/src/main/res/drawable/ic_nav_home.xml`、`ic_nav_meal.xml`、`ic_nav_foodbank.xml`、`ic_nav_water.xml`、`ic_nav_profile.xml`
  - 图标需保持同一套矢量风格，并通过 `BottomNavigationView` 的 tint 状态适配选中/未选中颜色。

### 首页

- 页面：`app/src/main/java/com/example/food/ui/home/HomeFragment.java`
- 布局：`app/src/main/res/layout/fragment_home.xml`
- 卡路里卡片背景：`app/src/main/res/drawable-nodpi/bg_calorie_card.png`
- ViewModel：`app/src/main/java/com/example/food/ui/home/HomeViewModel.java`
- 自定义 View：
  - `app/src/main/java/com/example/food/ui/home/CalorieCircleView.java`
  - `app/src/main/java/com/example/food/ui/home/FatCircleView.java`
  - `app/src/main/java/com/example/food/ui/home/FatCompositionRingView.java`
  - `app/src/main/java/com/example/food/ui/home/NutrientDistributionView.java`
  - `app/src/main/java/com/example/food/ui/home/PeriodChartMarkerView.java`
- 相关数据：`app/src/main/java/com/example/food/data/repository/HistoryRepository.java`、`MealRepository.java`

### 饮食记录页

- 页面：`app/src/main/java/com/example/food/ui/meal/MealFragment.java`
- 布局：`app/src/main/res/layout/meal_fragment.xml`
- 详情/展示 Fragment：`app/src/main/java/com/example/food/ui/meal/MealShowFragment.java`
- 展示布局：`app/src/main/res/layout/meal_show.xml`
- Adapter：
  - `app/src/main/java/com/example/food/ui/meal/MealAdapter.java`
  - `app/src/main/java/com/example/food/ui/meal/CalendarAdapter.java`
- 列表项布局：
  - `app/src/main/res/layout/item_meal_summary_header.xml`
  - `app/src/main/res/layout/meal_calendar_day_item.xml`
  - `app/src/main/res/layout/meal_item_calendar.xml`
- 共享日期：`app/src/main/java/com/example/food/ui/common/SelectedDateViewModel.java`
- 相关数据：`app/src/main/java/com/example/food/data/repository/MealRepository.java`

### 添加饮食页

- 页面：`app/src/main/java/com/example/food/ui/meal/AddActivity.java`
- 布局：`app/src/main/res/layout/meal_add.xml`
- 食物选择 Adapter：`app/src/main/java/com/example/food/ui/meal/FoodAdapter.java`
- 食物项布局：`app/src/main/res/layout/item_meal_food.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/FoodRepository.java`、`MealRepository.java`

### 食物库页

- 页面：`app/src/main/java/com/example/food/ui/foodbank/FoodBankFragment.java`
- 布局：`app/src/main/res/layout/fragment_foodbank.xml`
- Adapter：`app/src/main/java/com/example/food/ui/foodbank/FoodAdapter.java`
- 分组模型：`app/src/main/java/com/example/food/ui/foodbank/FoodGroup.java`
- 列表项布局：
  - `app/src/main/res/layout/item_food.xml`
  - `app/src/main/res/layout/item_food_category_header.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/FoodRepository.java`

### 添加食物页

- 页面：`app/src/main/java/com/example/food/ui/foodbank/AddFoodActivity.java`
- 布局：`app/src/main/res/layout/activity_food_add.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/FoodRepository.java`

### 饮水页

- 页面：`app/src/main/java/com/example/food/ui/water/WaterFragment.java`
- 布局：`app/src/main/res/layout/fragment_water.xml`
- 今日记录项布局：`app/src/main/res/layout/item_water_record.xml`
- 自定义 View：`app/src/main/java/com/example/food/ui/water/WaterCircleView.java`
- 主要交互：
  - 快捷添加饮水记录：100ml、200ml、300ml、500ml、自定义。
  - 展示今日饮水总量、目标、完成率和圆环进度。
  - 展示今日饮水明细列表，并支持删除单条记录。
  - 点击圆环区域可调整今日总量和每日目标。
- 数据表：`water_records`，字段包含 `id`、`userId`、`amount`、`date`、`created_at`。

### 我的/个人中心

- 页面：`app/src/main/java/com/example/food/ui/profile/ProfileFragment.java`
- 布局：`app/src/main/res/layout/fragment_profile.xml`
- 资料编辑页：`app/src/main/java/com/example/food/ui/profile/ProfileEditActivity.java`
- 资料编辑布局：`app/src/main/res/layout/activity_profile_edit.xml`
- 旧资料编辑弹窗布局：`app/src/main/res/layout/dialog_edit_profile.xml`
  - 当前主入口已改为独立编辑页；旧布局如无调用，不应继续作为新功能入口。
- 字段项布局：`app/src/main/res/layout/item_profile_field.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/UserRepository.java`
- 用户目标：`app/src/main/java/com/example/food/data/preferences/UserGoalPreferences.java`
- 主要交互：
  - 个人中心只负责展示资料、推荐热量和营养素分配。
  - 点击“编辑资料”进入 `ProfileEditActivity`。
  - 编辑页保存后返回个人中心，`ProfileFragment.onResume()` 会重新加载用户资料。
  - 推荐热量和营养素目标仍在个人中心根据身高、体重、年龄、性别、目标同步到 `UserGoalPreferences`。

### 历史记录页

- 页面：`app/src/main/java/com/example/food/ui/history/HistoryActivity.java`
- 布局：`app/src/main/res/layout/activity_history.xml`
- ViewModel：`app/src/main/java/com/example/food/ui/history/HistoryViewModel.java`
- 自定义 View：`app/src/main/java/com/example/food/ui/history/WeekBandOverlayView.java`
- 列表/日期项布局：
  - `app/src/main/res/layout/item_history_day_cell.xml`
  - `app/src/main/res/layout/item_history_month_cell.xml`
  - `app/src/main/res/layout/item_history_month_section.xml`
  - `app/src/main/res/layout/item_history_year_section.xml`
  - `app/src/main/res/layout/item_calendar_date.xml`
- 相关数据：`app/src/main/java/com/example/food/data/repository/HistoryRepository.java`

### 食物识别页

- 页面：`app/src/main/java/com/example/food/ui/recognition/FoodRecognitionActivity.java`
- 布局：`app/src/main/res/layout/activity_food_recognition.xml`
- 识别逻辑：`app/src/main/java/com/example/food/domain/recognition`
- 相关服务：`app/src/main/java/com/example/food/domain/service/NutritionService.java`

### 管理后台

- 页面容器：`app/src/main/java/com/example/food/ui/admin/AdminActivity.java`
- 布局：`app/src/main/res/layout/activity_admin.xml`
- 食物管理：
  - 页面：`app/src/main/java/com/example/food/ui/admin/FoodManagementFragment.java`
  - 布局：`app/src/main/res/layout/fragment_admin_foods.xml`
  - Adapter：`app/src/main/java/com/example/food/ui/admin/FoodManagementAdapter.java`
  - 列表项：`app/src/main/res/layout/item_admin_food.xml`
- 用户管理：
  - 页面：`app/src/main/java/com/example/food/ui/admin/UserManagementFragment.java`
  - 布局：`app/src/main/res/layout/fragment_admin_users.xml`
  - 列表项：`app/src/main/res/layout/item_admin_user.xml`
- 管理设置：
  - 页面：`app/src/main/java/com/example/food/ui/admin/AdminSettingsFragment.java`
  - 布局：`app/src/main/res/layout/fragment_admin_settings.xml`
  - 日志项：`app/src/main/res/layout/item_admin_log.xml`

## 数据和模型位置

- Room 数据库：`app/src/main/java/com/example/food/db/AppDatabase.java`
- 实体：`app/src/main/java/com/example/food/db/entity`
- DAO：`app/src/main/java/com/example/food/db/dao`
- 食物默认数据：`app/src/main/res/raw/default_foods.json`
- 食物导入：`app/src/main/java/com/example/food/db/FoodSeedImporter.java`
- 用户导入：`app/src/main/java/com/example/food/db/UserSeedImporter.java`
- 营养计算：`app/src/main/java/com/example/food/model/NutritionCalculator.java`
- 用户目标：`app/src/main/java/com/example/food/model/UserGoal.java`
- 用户角色：`app/src/main/java/com/example/food/model/UserRole.java`

## 构建提示

本机运行 Gradle 建议使用 JDK 17：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```
