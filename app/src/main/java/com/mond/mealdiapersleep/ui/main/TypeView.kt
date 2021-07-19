package com.mond.mealdiapersleep.ui.main

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mond.mealdiapersleep.data.EventListAdapter
import com.mond.mealdiapersleep.data.EventType
import java.time.Duration
import java.time.LocalDateTime

data class TypeView(
    val type: EventType,
    val rv: RecyclerView,
    val lastTimeView: TextView,
    val nextTimeView: TextView,
    val addBtn: View,
    val term: Long = 3 * 60,
    val viewModel: MainViewModel
) {
    val adapter: EventListAdapter?
        get() {
            return rv.adapter as? EventListAdapter
        }

    fun updateTimeLine() {
        (adapter)?.notifyDataSetChanged()
        updateNextTime()
    }

    private fun updateNextTime() {
        val lastEvent = viewModel.getLastEvent(type)

    }

    fun clearNextTime() {
        (nextTimeView).text = ""
    }

    private val dataObserver = createAdapterDataObserver()

    private fun createAdapterDataObserver() = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            if (positionStart > 0)
                adapter?.notifyItemChanged(positionStart - 1)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            rv.smoothScrollToPosition(0)

            val lastEvent = viewModel.in48Hours.value?.filter { it.type == type }?.maxByOrNull { it.start }
            val toMinutes = Duration.between(LocalDateTime.now(), lastEvent?.start?.plusMinutes(term)).toMinutes()
            (nextTimeView).text = "$toMinutes 분 남았음"
        }
    }

    fun setAdapter() {
        unRegisterAdapterDataObserver()
        rv.adapter = EventListAdapter(viewModel).apply {
            registerAdapterDataObserver(dataObserver)
        }
    }

    fun unRegisterAdapterDataObserver() {
        adapter?.unregisterAdapterDataObserver(dataObserver)
    }

    fun setAddBtn() {
        addBtn.setOnClickListener {
            viewModel.add(type, LocalDateTime.now())
        }
    }

}