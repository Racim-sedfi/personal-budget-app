package com.application.personal_budget_app.ui.components

import androidx.annotation.DrawableRes
import com.application.personal_budget_app.R

@DrawableRes
fun categoryIcon(iconKey: String): Int = when (iconKey) {
    "cart" -> R.drawable.ic_cat_cart
    "restaurant" -> R.drawable.ic_cat_restaurant
    "bus" -> R.drawable.ic_cat_bus
    "ticket" -> R.drawable.ic_cat_ticket
    "bag" -> R.drawable.ic_cat_bag
    "health" -> R.drawable.ic_cat_health
    "umbrella" -> R.drawable.ic_cat_umbrella
    else -> R.drawable.ic_cat_other
}