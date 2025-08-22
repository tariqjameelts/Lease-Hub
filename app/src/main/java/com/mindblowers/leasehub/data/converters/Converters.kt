package com.mindblowers.leasehub.data.converters

import androidx.room.TypeConverter
import com.mindblowers.leasehub.data.entities.AgreementStatus
import com.mindblowers.leasehub.data.entities.ExpenseCategory
import com.mindblowers.leasehub.data.entities.PaymentMethod
import com.mindblowers.leasehub.data.entities.PaymentStatus
import com.mindblowers.leasehub.data.entities.RecurringFrequency
import com.mindblowers.leasehub.data.entities.ShopStatus
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class EnumConverters {
    @TypeConverter
    fun fromShopStatus(value: String): ShopStatus = enumValueOf(value)

    @TypeConverter
    fun toShopStatus(status: ShopStatus): String = status.name

    @TypeConverter
    fun fromAgreementStatus(value: String): AgreementStatus = enumValueOf(value)

    @TypeConverter
    fun toAgreementStatus(status: AgreementStatus): String = status.name

    @TypeConverter
    fun fromPaymentMethod(value: String): PaymentMethod = enumValueOf(value)

    @TypeConverter
    fun toPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun fromPaymentStatus(value: String): PaymentStatus = enumValueOf(value)

    @TypeConverter
    fun toPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun fromExpenseCategory(value: String): ExpenseCategory = enumValueOf(value)

    @TypeConverter
    fun toExpenseCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun fromRecurringFrequency(value: String): RecurringFrequency = enumValueOf(value)

    @TypeConverter
    fun toRecurringFrequency(frequency: RecurringFrequency): String = frequency.name
}