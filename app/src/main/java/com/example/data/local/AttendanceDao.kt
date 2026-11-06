package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY id DESC")
    fun getAllAttendances(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendances WHERE meetingTitle = :title ORDER BY id DESC")
    fun getAttendancesByMeeting(title: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<Attendance>)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("SELECT COUNT(*) FROM attendances")
    suspend fun count(): Int
}
