package com.mond.babytory.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mond.mealdiapersleep.R
import com.mond.babytory.data.EventListAdapter.EventViewHolder
import com.mond.babytory.ui.main.MainViewModel
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventListAdapter(val viewModel: MainViewModel) :
    ListAdapter<Event, EventViewHolder>(EventsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(viewModel, current)
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gap: View = itemView.findViewById(R.id.gap)
        private val gapTime: TextView = itemView.findViewById(R.id.gap_time)

        private val wrapper: View = itemView.findViewById(R.id.wrapper)
        private val beforeTime: TextView = itemView.findViewById(R.id.before_now)
        private val eventTime: TextView = itemView.findViewById(R.id.event_time)
        private val volume: TextView = itemView.findViewById(R.id.seek_vol)

        @SuppressLint("SetTextI18n")
        fun bind(viewModel: MainViewModel, event: Event) {
            val dateTime = event.start
            val context = itemView.context

            val previousEventGap = getPreviousEventGap(viewModel, event)
            gap.visibility = if (previousEventGap == Duration.ZERO) View.GONE else View.VISIBLE

            val prevEventGapMinute = previousEventGap.toMinutes()
            gap.minimumHeight = (if (prevEventGapMinute > 100) 100 else prevEventGapMinute).dp2px()
            gapTime.text = when {
                prevEventGapMinute >= 60 -> String.format("%d?????? %d???", prevEventGapMinute / 60, (prevEventGapMinute % 60))
                else -> String.format("%d???", (prevEventGapMinute))
            }

            val fromNow = Duration.between(event.start, LocalDateTime.now()).toMinutes()
            beforeTime.text = when {
                fromNow >= 60 -> String.format("%d?????? %d??????", fromNow / 60, (fromNow % 60))
                fromNow < 5 -> "?????????"
                else -> String.format("%d??????", (fromNow))
            }
            eventTime.text =
                dateTime.format(DateTimeFormatter.ofPattern("a hh??? mm???", Locale.KOREAN))

           when(event.type){
               EventType.Meal -> volume.text = "${event.volume} ml"
               EventType.Sleep -> volume.text = if(event.volume>0) "??????" else "?????????"
               EventType.Diaper -> volume.text = if(event.volume>0) "??????" else "??????"
               else -> {}
           }

            wrapper.requestLayout()
            wrapper.setOnLongClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete This item?")
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .setPositiveButton("Delete") { dialogInterface, _ ->
                        viewModel.delete(event.mId)
                        dialogInterface.dismiss()
                    }.create().show()
                true
            }
        }

        private fun getPreviousEventGap(viewModel: MainViewModel, event: Event): Duration {
            var prevEvent: Event? = null
            runBlocking {
                async {
                    prevEvent = viewModel.getPrevEvent(event.type, event.start)
                }
            }
            if (prevEvent != null)
                return Duration.between(prevEvent!!.start, event.start)
            return Duration.ZERO

        }

        private fun getUnit(event: Event) = when (event.type) {
            EventType.Meal -> "ml"
            EventType.Sleep -> "???"
            else -> ""
        }

        companion object {
            fun create(parent: ViewGroup): EventViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.events, parent, false)
                return EventViewHolder(view)
            }
        }
    }

    class EventsComparator : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.mId == newItem.mId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}

