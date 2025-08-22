package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
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

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Long): Tenant?

    @Query("SELECT * FROM tenants WHERE isActive = 1 ORDER BY fullName")
    fun getAllTenants(): Flow<List<Tenant>>

    @Query("UPDATE tenants SET isActive = 0 WHERE id = :tenantId")
    suspend fun deactivateTenant(tenantId: Long)

    @Query("SELECT COUNT(*) FROM tenants WHERE isActive = 1")
    suspend fun getActiveTenantCount(): Int
}