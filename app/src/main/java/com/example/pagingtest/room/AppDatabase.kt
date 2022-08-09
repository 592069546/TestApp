package com.example.pagingtest.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pagingtest.room.DBConst.FuckerTableName

@Database(
    entities = [User::class, Fucker::class], version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun fuckerDao(): FuckerDao

    companion object {
        private val DATABASE_NAME = "RoomTest"

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        @Volatile
        private var userRepository: UserRepository? = null

        @Volatile
        private var fuckerRepository: FuckerRepository? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        fun getUserRepository(context: Context) = userRepository ?: synchronized(this) {
            userRepository ?: UserRepository(getInstance(context).userDao()).also {
                userRepository = it
            }
        }

        fun getFuckerRepository(context: Context) = fuckerRepository ?: synchronized(this) {
            fuckerRepository ?: FuckerRepository(getInstance(context).fuckerDao()).also {
                fuckerRepository = it
            }
        }

        /*TODO 数据库配置*/
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
//                            val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
//                                .setInputData(workDataOf(KEY_FILENAME to PLANT_DATA_FILENAME))
//                                .build()
//                            WorkManager.getInstance(context).enqueue(request)
                        }
                    }
                )
                .addMigrations(Migiration_2_3())
                .build()
        }
    }
}


class Migiration_2_3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS $FuckerTableName (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fuckName` TEXT NOT NULL, `fuckNum` INTEGER NOT NULL)")
    }
}