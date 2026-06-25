package com.example.sijaga.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sijaga.data.local.dao.*
import com.example.sijaga.data.local.entity.*

@Database(
    entities = [User::class, Gangguan::class, PasangBaru::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gangguanDao(): GangguanDao
    abstract fun pasangBaruDao(): PasangBaruDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                AppDatabase::class.java,
                "sijaga.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }

        suspend fun seedIfEmpty(db: AppDatabase) {
            val count = db.userDao().countAll()
            if (count > 0) return

            db.userDao().insert(User(
                nama = "Vendor Demo",
                email = "vendor@sijaga.com",
                password = "sijaga123",
                role = "masyarakat",
                telepon = "08111111111"
            ))
            db.userDao().insert(User(
                nama = "Staff PLN",
                email = "staff@sijaga.com",
                password = "sijaga123",
                role = "staff_pln",
                telepon = "08333333333"
            ))
        }
    }
}