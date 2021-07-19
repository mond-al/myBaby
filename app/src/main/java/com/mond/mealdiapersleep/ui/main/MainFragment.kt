package com.mond.mealdiapersleep.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mond.mealdiapersleep.MainApplication
import com.mond.mealdiapersleep.R
import com.mond.mealdiapersleep.data.EventListAdapter
import com.mond.mealdiapersleep.data.EventRepository
import com.mond.mealdiapersleep.data.EventType
import com.mond.mealdiapersleep.databinding.MainFragmentBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory((view.context?.applicationContext as MainApplication).repository)
        }
        val binding = MainFragmentBinding.bind(view)

        binding.meals.apply {
            adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged())
            }
            layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }
        binding.sleeps.apply {
            adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged())
            }
            layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }
        binding.diapers.apply {
            adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged())
            }
            layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }

        viewModel.hour24.observe(this) { events ->
            (binding.meals.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Meal }
                .sortedByDescending { it.start }) {
                binding.meals.post { binding.meals.smoothScrollToPosition(0) }
            }
            (binding.sleeps.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Sleep }
                .sortedByDescending { it.start }) {
                binding.sleeps.post { binding.sleeps.smoothScrollToPosition(0) }
            }
            (binding.diapers.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Diaper }
                .sortedByDescending { it.start }) {
                binding.diapers.post { binding.diapers.smoothScrollToPosition(0) }
            }

            val lastMealStart =
                events.filter { it.type == EventType.Meal }.maxByOrNull { it.start }?.start
            val lastSleepStart =
                events.filter { it.type == EventType.Sleep }.maxByOrNull { it.start }?.start
            val lastDiaperStart =
                events.filter { it.type == EventType.Diaper }.maxByOrNull { it.start }?.start

            binding.lastMeal.text =
                lastMealStart?.format(DateTimeFormatter.ofPattern("a hh시 mm분", Locale.KOREAN))
            binding.lastSleep.text =
                lastSleepStart?.format(DateTimeFormatter.ofPattern("a hh시 mm분", Locale.KOREAN))
            binding.lastDiaper.text =
                lastDiaperStart?.format(DateTimeFormatter.ofPattern("a hh시 mm분", Locale.KOREAN))

        }

        binding.btnAddMeal.setOnClickListener {
            viewModel.add(
                EventType.Meal,
                LocalDateTime.now(),
            )
        }
        binding.btnAddSleep.setOnClickListener {
            viewModel.add(
                EventType.Sleep,
                LocalDateTime.now(),
            )
        }
        binding.btnAddDiaper.setOnClickListener {
            viewModel.add(
                EventType.Diaper,
                LocalDateTime.now(),
            )
        }
        Observable.interval(10, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.meals.adapter?.notifyDataSetChanged()
                binding.sleeps.adapter?.notifyDataSetChanged()
                binding.diapers.adapter?.notifyDataSetChanged()
                viewModel.dirty.set(false)
            }


    }

    private fun EventListAdapter.createNotiChanged() =
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                if (positionStart > 0)
                    notifyItemChanged(positionStart - 1)
            }
        }
}

class MainViewModelFactory(private val repository: EventRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}