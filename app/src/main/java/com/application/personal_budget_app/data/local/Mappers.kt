package com.application.personal_budget_app.data.local

import com.application.personal_budget_app.domain.model.*

fun TransactionEntity.toDomain() = Transaction(id, Money(amountCents), type, categoryId, date, note)
fun Transaction.toEntity() = TransactionEntity(id, amount.cents, type, categoryId, date, note)

fun CategoryEntity.toDomain() = Category(id, name, iconKey, capCents?.let(::Money), isFuse, position)
fun Category.toEntity() = CategoryEntity(id, name, iconKey, cap?.cents, isFuse, position)

fun IncomeEntity.toDomain() = Income(id, name, Money(amountCents), dayOfMonth)
fun Income.toEntity() = IncomeEntity(id, name, amount.cents, dayOfMonth)

fun PlannedSavingEntity.toDomain() = PlannedSaving(id, label, Money(amountCents), dayOfMonth)
fun PlannedSaving.toEntity() = PlannedSavingEntity(id, label, amount.cents, dayOfMonth)

fun FixedChargeEntity.toDomain() = FixedCharge(id, name, Money(amountCents), frequency, nextDueDate)
fun FixedCharge.toEntity() = FixedChargeEntity(id, name, amount.cents, frequency, nextDueDate)