package com.mindblowers.leasehub.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class LeaseAgreementWithDetails(
    @Embedded val agreement: LeaseAgreement,
    @Relation(
        parentColumn = "shopId",
        entityColumn = "id"
    )
    val shop: Shop,
    @Relation(
        parentColumn = "tenantId",
        entityColumn = "id"
    )
    val tenant: Tenant
)

data class RentPaymentWithDetails(
    @Embedded val payment: RentPayment,
    @Relation(
        parentColumn = "agreementId",
        entityColumn = "id"
    )
    val agreement: LeaseAgreement
) {
    // We'll get shop and tenant through the agreement in repository
}

data class ShopWithRelations(
    @Embedded val shop: Shop,
    @Relation(
        parentColumn = "id",
        entityColumn = "shopId"
    )
    val agreements: List<LeaseAgreement>
) {
    // We'll get tenant through agreement in repository
}