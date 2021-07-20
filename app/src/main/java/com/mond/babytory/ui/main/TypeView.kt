package com.mond.babytory.ui.main

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mond.babytory.data.EventType

data class TypeView(
    val type: EventType,
    val rv: RecyclerView,
    val lastTimeView: TextView,
    val nextTimeView: TextView,
    val addBtn: View,
    val term: Long
)