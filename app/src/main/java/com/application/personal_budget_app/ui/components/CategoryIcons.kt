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
    "pet" -> R.drawable.ic_cat_pet
    "home" -> R.drawable.ic_cat_home
    "car" -> R.drawable.ic_cat_car
    "gift" -> R.drawable.ic_cat_gift
    "book" -> R.drawable.ic_cat_book
    "phone" -> R.drawable.ic_cat_phone
    "sport" -> R.drawable.ic_cat_sport
    "child" -> R.drawable.ic_cat_child
    "coffee" -> R.drawable.ic_cat_coffee
    "travel" -> R.drawable.ic_cat_travel
    "shirt" -> R.drawable.ic_cat_shirt
    "beauty" -> R.drawable.ic_cat_beauty
    "music" -> R.drawable.ic_cat_music
    else -> R.drawable.ic_cat_other
}