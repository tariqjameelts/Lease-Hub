package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("SELECT * FROM lease_agreements WHERE id = :agreementId LIMIT 1")
    suspend fun getAgreementById(agreementId: Long): LeaseAgreement?

    @Update
    suspend fun update(agreement: LeaseAgreement)
    @Delete
    suspend fun deleteAgreement(agreement: LeaseAgreement)

    @Transaction
    @Query("SELECT * FROM lease_agreements WHERE id = :id")
    suspend fun getAgreementWithDetails(id: Long): LeaseAgreementWithDetails

    @Transaction
    @Query("SELECT * FROM lease_agreements WHERE status = 'ACTIVE'")
    fun getActiveAgreementsWithDetails(): Flow<List<LeaseAgreementWithDetails>>

    @Query("UPDATE lease_agreements SET status = :status WHERE id = :agreementId")
    suspend fun updateAgreementStatus(agreementId: Long, status: AgreementStatus)

    // ✅ Existing
    @Query("SELECT * FROM lease_agreements WHERE shopId = :shopId AND status = 'ACTIVE'")
    suspend fun getActiveAgreementForShop(shopId: Long): LeaseAgreement?

    // ✅ NEW → active agreement for specific shop + tenant
    @Query("""
        SELECT * FROM lease_agreements 
        WHERE shopId = :shopId AND tenantId = :tenantId AND status = 'ACTIVE'
        LIMIT 1
    """)
    suspend fun getActiveAgreementForShopAndTenant(shopId: Long, tenantId: Long): LeaseAgreement?

    @Query("SELECT * FROM lease_agreements WHERE endDate < :currentDate AND status = 'ACTIVE'")
    suspend fun getExpiredAgreements(currentDate: Date): List<LeaseAgreement>
}
