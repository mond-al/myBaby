package com.mond.mealdiapersleep.ui.main

import android.os.Bundle
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
import io.reactivex.schedulers.Schedulers
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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

        binding.meals.also {
            it.adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged(this, it))
            }
            it.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }
        binding.sleeps.also {
            it.adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged(this, it))
            }
            it.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }
        binding.diapers.also {
            it.adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createNotiChanged(this, it))
            }
            it.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
            }
        }

        viewModel.hour24.observe(this) { events ->
            (binding.meals.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Meal }
                .sortedByDescending { it.start }) {
            }
            (binding.sleeps.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Sleep }
                .sortedByDescending { it.start }) {
            }
            (binding.diapers.adapter as EventListAdapter).submitList(events.filter { it.type == EventType.Diaper }
                .sortedByDescending { it.start }) {
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

                val nextMeal =Duration.between(LocalDateTime.now(),viewModel.getLastEvent(EventType.Meal)?.plusHours(3)).toMinutes()
                val nextSleep =Duration.between(LocalDateTime.now(),viewModel.getLastEvent(EventType.Sleep)?.plusHours(3)).toMinutes()
                val nextDiaper =Duration.between(LocalDateTime.now(),viewModel.getLastEvent(EventType.Diaper)?.plusHours(3)).toMinutes()

                binding.nextMeal.text ="$nextMeal 분 남았음"
                binding.nextSleep.text ="$nextSleep 분 남았음"
                binding.nextDiaper.text ="$nextDiaper 분 남았음"
            }

    }

    private fun createNotiChanged(adapter: EventListAdapter, rv: RecyclerView) =
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                if (positionStart > 0)
                    adapter.notifyItemChanged(positionStart - 1)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                rv.smoothScrollToPosition(0)
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