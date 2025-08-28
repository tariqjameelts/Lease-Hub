package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mindblowers.leasehub.data.entities.Tenant
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Insert
    suspend fun insert(tenant: Tenant): Long

    @Update
    suspend fun update(tenant: Tenant)

    @Delete
    suspend fun delete(tenant: Tenant)

    @Query("SELECT * FROM tenants")
    suspend fun getAllForBackup(): List<Tenant>

    @Query("SELECT * FROM tenants WHERE id = :id AND userId = :userId")
    suspend fun getTenantByIdSync(userId: Long, id: Long): Tenant?

    // ✅ tenant by id + user
    @Query("SELECT * FROM tenants WHERE id = :id AND userId = :userId")
    fun getTenantById(userId: Long, id: Long): Flow<Tenant?>

    // ✅ all tenants of this user
    @Query("SELECT * FROM tenants WHERE isActive = 1 AND userId = :userId ORDER BY fullName")
    fun getAllTenants(userId: Long): Flow<List<Tenant>>

    // ✅ deactivate tenant, restricted by userId
    @Query("UPDATE tenants SET isActive = 0 WHERE id = :tenantId AND userId = :userId")
    suspend fun deactivateTenant(userId: Long, tenantId: Long)

    // ✅ active tenant count for this user
    @Query("SELECT COUNT(*) FROM tenants WHERE isActive = 1 AND userId = :userId")
    suspend fun getActiveTenantCount(userId: Long): Int
}