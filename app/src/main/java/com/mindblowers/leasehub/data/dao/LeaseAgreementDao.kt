package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mindblowers.leasehub.data.entities.AgreementStatus
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.LeaseAgreementWithDetails
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LeaseAgreementDao {
    @Insert
    suspend fun insert(agreement: LeaseAgreement): Long

    @Update
    suspend fun update(agreement: LeaseAgreement)

    @Query("SELECT * FROM lease_agreements WHERE id = :id")
    suspend fun getAgreementById(id: Long): LeaseAgreement?

    @Transaction
    @Query("SELECT * FROM lease_agreements WHERE id = :id")
    suspend fun getAgreementWithDetails(id: Long): LeaseAgreementWithDetails

    @Transaction
    @Query("SELECT * FROM lease_agreements WHERE status = 'ACTIVE'")
    fun getActiveAgreementsWithDetails(): Flow<List<LeaseAgreementWithDetails>>

    @Query("UPDATE lease_agreements SET status = :status WHERE id = :agreementId")
    suspend fun updateAgreementStatus(agreementId: Long, status: AgreementStatus)

    @Query("SELECT * FROM lease_agreements WHERE shopId = :shopId AND status = 'ACTIVE'")
    suspend fun getActiveAgreementForShop(shopId: Long): LeaseAgreement?

    @Query("SELECT * FROM lease_agreements WHERE endDate < :currentDate AND status = 'ACTIVE'")
    suspend fun getExpiredAgreements(currentDate: Date): List<LeaseAgreement>
}
