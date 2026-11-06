package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Certificate
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates WHERE recipientNim = :nim ORDER BY id DESC")
    fun getCertificatesForUser(nim: String): Flow<List<Certificate>>

    @Query("SELECT * FROM certificates WHERE eventId = :eventId")
    fun getCertificatesByEvent(eventId: Long): Flow<List<Certificate>>

    @Query("SELECT * FROM certificates WHERE eventId = :eventId AND recipientNim = :nim LIMIT 1")
    fun getCertificateForUserAndEvent(eventId: Long, nim: String): Flow<Certificate?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: Certificate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(certificates: List<Certificate>)

    @Query("SELECT COUNT(*) FROM certificates")
    suspend fun count(): Int
}
