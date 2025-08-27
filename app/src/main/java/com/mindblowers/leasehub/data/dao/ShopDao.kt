package com.mindblowers.leasehub.data.dao

import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mindblowers.leasehub.data.entities.ShopWithRelations
import kotlinx.coroutines.flow.Flow


@Dao
interface ShopDao {
    @Insert
    suspend fun insert(shop: Shop): Long

    @Update
    suspend fun update(shop: Shop)

    @Delete
    suspend fun delete(shop: Shop)

    @Query("SELECT * FROM shops WHERE id = :id")
    fun getShopById(id: Long): Flow<Shop?>

    @Query("UPDATE shops SET status = :status WHERE id = :shopId")
    suspend fun updateShopStatus(shopId: Long, status: ShopStatus)

    @Transaction
    @Query("SELECT * FROM shops WHERE isActive = 1 AND userId = :userId")
    fun getShopsWithRelations(userId: Long): Flow<List<ShopWithRelations>>

    @Query("SELECT * FROM shops WHERE isActive = 1 AND userId = :userId ORDER BY buildingName, floor, shopNumber")
    fun getAllShops(userId: Long): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE status = :status AND isActive = 1 AND userId = :userId")
    fun getShopsByStatus(userId: Long, status: ShopStatus): Flow<List<Shop>>

    @Query("SELECT COUNT(*) FROM shops WHERE status = 'VACANT' AND isActive = 1 AND userId = :userId")
    suspend fun getVacantShopCount(userId: Long): Int

    @Query("SELECT COUNT(*) FROM shops WHERE status = 'OCCUPIED' AND isActive = 1 AND userId = :userId")
    suspend fun getOccupiedShopCount(userId: Long): Int
}