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

    @Query("""
    SELECT * FROM lease_agreements 
    WHERE id = :agreementId 
      AND userId = :userId 
    LIMIT 1
""")
    suspend fun getAgreementById(
        agreementId: Long,
        userId: Long
    ): LeaseAgreement?

    @Update
    suspend fun update(agreement: LeaseAgreement)
    @Delete
    suspend fun deleteAgreement(agreement: LeaseAgreement)

    @Transaction
    @Query("""
    SELECT * FROM lease_agreements 
    WHERE id = :id 
      AND userId = :userId
""")
    suspend fun getAgreementWithDetails(
        userId: Long,
        id: Long
    ): LeaseAgreementWithDetails


    @Transaction
    @Query("""
    SELECT * FROM lease_agreements 
    WHERE status = 'ACTIVE' 
      AND userId = :userId
""")
    fun getActiveAgreementsWithDetails(
        userId: Long
    ): Flow<List<LeaseAgreementWithDetails>>

    @Query("UPDATE lease_agreements SET status = :status WHERE id = :agreementId")
    suspend fun updateAgreementStatus(agreementId: Long, status: AgreementStatus)

    // ✅ Existing (active agreement for shop, scoped by userId)
    @Query("""
    SELECT * FROM lease_agreements 
    WHERE shopId = :shopId 
      AND status = 'ACTIVE' 
      AND userId = :userId
    LIMIT 1
""")
    suspend fun getActiveAgreementForShop(
        shopId: Long,
        userId: Long
    ): LeaseAgreement?

    // ✅ New (active agreement for specific shop + tenant, scoped by userId)
    @Query("""
    SELECT * FROM lease_agreements 
    WHERE shopId = :shopId 
      AND tenantId = :tenantId 
      AND status = 'ACTIVE'
      AND userId = :userId
    LIMIT 1
""")
    suspend fun getActiveAgreementForShopAndTenant(
        shopId: Long,
        tenantId: Long,
        userId: Long
    ): LeaseAgreement?

    // ✅ Expired agreements (only belonging to user)
    @Query("""
    SELECT * FROM lease_agreements 
    WHERE endDate < :currentDate 
      AND status = 'ACTIVE' 
      AND userId = :userId
""")
    suspend fun getExpiredAgreements(
        currentDate: Date,
        userId: Long
    ): List<LeaseAgreement>

}
