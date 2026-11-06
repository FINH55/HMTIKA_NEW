package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE nim = :nim OR email = :email LIMIT 1")
    suspend fun getUserByNimOrEmail(nim: String, email: String): User?

    @Query("SELECT * FROM users WHERE nim = :nim LIMIT 1")
    fun observeUser(nim: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
