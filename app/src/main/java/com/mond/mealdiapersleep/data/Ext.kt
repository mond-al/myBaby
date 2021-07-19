package com.mond.mealdiapersleep.data

import android.content.res.Resources
import android.util.TypedValue

fun Number.dp2px(): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
