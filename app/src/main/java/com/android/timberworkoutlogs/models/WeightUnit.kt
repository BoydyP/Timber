package com.android.timberworkoutlogs.models

import com.android.timberworkoutlogs.R

enum class WeightUnit {
    KG,
    LB
}

fun WeightUnit.toStringResource(): Int {
    return when (this) {
        WeightUnit.KG -> R.string.unit_kg
        WeightUnit.LB -> R.string.unit_lb
    }
}