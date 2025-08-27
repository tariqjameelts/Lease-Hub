package com.mindblowers.leasehub.di

import android.content.Context
import com.mindblowers.leasehub.data.AppDatabase
import com.mindblowers.leasehub.data.dao.*
import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.AuthRepository
import com.mindblowers.leasehub.utils.BackupManager
import com.mindblowers.leasehub.utils.SecurityUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {



    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext context: Context): AppPrefs{
        return AppPrefs(context)
    }
    @Provides
    fun provideActivityLogDao(database: AppDatabase): ActivityLogDao = database.activityLogDao()

    // DAO Providers
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideShopDao(database: AppDatabase): ShopDao = database.shopDao()

    @Provides
    fun provideTenantDao(database: AppDatabase): TenantDao = database.tenantDao()

    @Provides
    @Singleton
    fun provideAuthRepository(userDao: UserDao, appPrefs: AppPrefs):AuthRepository{
        return AuthRepository(userDao, appPrefs = appPrefs)
    }
    @Provides
    fun provideLeaseAgreementDao(database: AppDatabase): LeaseAgreementDao = database.leaseAgreementDao()

    @Provides
    fun provideRentPaymentDao(database: AppDatabase): RentPaymentDao = database.rentPaymentDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    // Repository Provider
    @Provides
    @Singleton
    fun provideAppRepository(
        userDao: UserDao,
        shopDao: ShopDao,
        tenantDao: TenantDao,
        leaseAgreementDao: LeaseAgreementDao,
        rentPaymentDao: RentPaymentDao,
        expenseDao: ExpenseDao,
        activityLogDao: ActivityLogDao // inject here
    ): AppRepository {
        return AppRepository(userDao, shopDao, tenantDao, leaseAgreementDao, rentPaymentDao, expenseDao, activityLogDao)
    }

    // Utility Providers
    @Provides
    @Singleton
    fun provideSecurityUtils(): SecurityUtils {
        return SecurityUtils()
    }

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): BackupManager {
        return BackupManager(context, database)
    }
}