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
            if (count > 0) return // sudah ada data, skip

            db.userDao().insert(User(
                nama = "Pelanggan Demo",
                email = "pelanggan@sijaga.com",
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
            db.gangguanDao().insert(Gangguan(
                userId = 1,
                namaPelapor = "Pelanggan Demo",
                telepon = "08111111111",
                jenis = "Mati Total",
                deskripsi = "Listrik padam sejak pukul 06.00",
                alamat = "Jl. Sudirman No. 10, Jakarta",
                status = "baru"
            ))
            db.pasangBaruDao().insert(PasangBaru(
                userId = 1,
                nama = "Pelanggan Demo",
                nik = "3174010101900001",
                telepon = "08111111111",
                alamat = "Jl. Kebon Jeruk No. 5",
                daya = 1300,
                status = "baru"
            ))
        }
    }
}
