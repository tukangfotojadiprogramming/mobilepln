package com.example.sijaga.data.local.dao

import androidx.room.*
import com.example.sijaga.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getByRole(role: String): Flow<List<User>>

    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE nama LIKE '%' || :q || '%' OR email LIKE '%' || :q || '%'")
    fun search(q: String): Flow<List<User>>

    @Delete
    suspend fun delete(user: User)

    @Update
    suspend fun update(user: User)
}

@Dao
interface GangguanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(g: Gangguan): Long

    @Query("SELECT * FROM gangguan WHERE userId = :uid ORDER BY createdAt DESC")
    fun getByUser(uid: Int): Flow<List<Gangguan>>

    @Query("SELECT * FROM gangguan ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Gangguan>>

    @Query("SELECT * FROM gangguan WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): Flow<List<Gangguan>>

    @Query("SELECT COUNT(*) FROM gangguan WHERE status = 'baru'")
    fun countBaru(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gangguan")
    fun countAll(): Flow<Int>

    @Update
    suspend fun update(g: Gangguan)

    @Query("DELETE FROM gangguan")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(g: Gangguan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Gangguan>)
}

@Dao
interface PasangBaruDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: PasangBaru): Long

    @Query("DELETE FROM pasang_baru")
    suspend fun deleteAll()

    @Query("SELECT * FROM pasang_baru WHERE userId = :uid ORDER BY createdAt DESC")
    fun getByUser(uid: Int): Flow<List<PasangBaru>>

    @Query("SELECT * FROM pasang_baru ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PasangBaru>>

    @Query("SELECT * FROM pasang_baru WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): Flow<List<PasangBaru>>

    @Query("SELECT COUNT(*) FROM pasang_baru WHERE status = 'baru'")
    fun countBaru(): Flow<Int>

    @Update
    suspend fun update(p: PasangBaru)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: PasangBaru)
}


