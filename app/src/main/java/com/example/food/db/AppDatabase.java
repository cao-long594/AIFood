package com.example.food.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.food.db.dao.FoodDao;
import com.example.food.db.dao.MealRecordDao;
import com.example.food.db.dao.UserDao;
import com.example.food.db.entity.Food;
import com.example.food.db.entity.MealRecord;
import com.example.food.db.entity.User;

@Database(entities = {Food.class, MealRecord.class, User.class}, version = 11, exportSchema = false)
@TypeConverters(DateTypeConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "food_app_database";
    private static volatile AppDatabase instance;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Schema stayed compatible; food unit normalization is handled by FoodSeedImporter.
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS water_records");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `users` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`username` TEXT NOT NULL, " +
                "`password` TEXT NOT NULL, " +
                "`role` TEXT NOT NULL DEFAULT 'USER', " +
                "`enabled` INTEGER NOT NULL DEFAULT 1)"
            );
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)");
            database.execSQL("ALTER TABLE `foods` ADD COLUMN `userId` INTEGER DEFAULT NULL");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `users` ADD COLUMN `userNumber` TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `phone` TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `displayName` TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `height` REAL DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `weight` REAL DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `age` INTEGER DEFAULT NULL");
            database.execSQL("ALTER TABLE `users` ADD COLUMN `gender` TEXT DEFAULT NULL");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_phone` ON `users` (`phone`)");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 先检查列是否存在，避免重复添加导致崩溃
            boolean hasSource = false;
            boolean hasSourceUser = false;
            android.database.Cursor cursor = database.query("PRAGMA table_info('foods')");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String colName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if ("source".equals(colName)) hasSource = true;
                    if ("sourceUserName".equals(colName)) hasSourceUser = true;
                }
                cursor.close();
            }
            if (!hasSource) {
                database.execSQL("ALTER TABLE `foods` ADD COLUMN `source` TEXT DEFAULT 'SYSTEM'");
            }
            if (!hasSourceUser) {
                database.execSQL("ALTER TABLE `foods` ADD COLUMN `sourceUserName` TEXT DEFAULT NULL");
            }
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `foods` ADD COLUMN `visibilityStatus` INTEGER DEFAULT 1");
            database.execSQL("ALTER TABLE `foods` ADD COLUMN `existStatus` INTEGER DEFAULT 1");
            // 旧数据迁移：以前 userId != null 的食物是私有的
            database.execSQL("UPDATE `foods` SET `visibilityStatus` = 2 WHERE `userId` IS NOT NULL");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            boolean hasExistStatus = false;
            android.database.Cursor cursor = database.query("PRAGMA table_info('users')");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if ("existStatus".equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                        hasExistStatus = true;
                        break;
                    }
                }
                cursor.close();
            }
            if (!hasExistStatus) {
                database.execSQL("ALTER TABLE `users` ADD COLUMN `existStatus` INTEGER DEFAULT 1");
            }
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            boolean hasGoal = false;
            android.database.Cursor cursor = database.query("PRAGMA table_info('users')");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if ("goal".equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                        hasGoal = true;
                        break;
                    }
                }
                cursor.close();
            }
            if (!hasGoal) {
                database.execSQL("ALTER TABLE `users` ADD COLUMN `goal` INTEGER DEFAULT NULL");
            }
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `meal_records` ADD COLUMN `userId` INTEGER DEFAULT NULL");
        }
    };

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `water_records` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`userId` INTEGER NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`date` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_water_records_userId_date` ON `water_records` (`userId`, `date`)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract FoodDao foodDao();

    public abstract MealRecordDao mealRecordDao();

    public abstract UserDao userDao();
}
