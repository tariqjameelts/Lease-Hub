package com.mindblowers.leasehub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mindblowers.leasehub.data.converters.DateConverter
import com.mindblowers.leasehub.data.converters.EnumConverters
import com.mindblowers.leasehub.data.dao.ExpenseDao
import com.mindblowers.leasehub.data.dao.LeaseAgreementDao
import com.mindblowers.leasehub.data.dao.RentPaymentDao
import com.mindblowers.leasehub.data.dao.ShopDao
import com.mindblowers.leasehub.data.dao.TenantDao
import com.mindblowers.leasehub.data.dao.UserDao
import com.mindblowers.leasehub.data.entities.Expense
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.Tenant
import com.mindblowers.leasehub.data.entities.User

@Database(
    entities = [
        User::class,
        Shop::class,
        Tenant::class,
        LeaseAgreement::class,
        RentPayment::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shopDao(): ShopDao
    abstract fun tenantDao(): TenantDao
    abstract fun leaseAgreementDao(): LeaseAgreementDao
    abstract fun rentPaymentDao(): RentPaymentDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shop_leasing_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}