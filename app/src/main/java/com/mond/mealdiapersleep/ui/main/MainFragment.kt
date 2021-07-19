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
import com.mond.mealdiapersleep.MainApplication
import com.mond.mealdiapersleep.R
import com.mond.mealdiapersleep.data.EventRepository
import com.mond.mealdiapersleep.data.EventType
import com.mond.mealdiapersleep.databinding.MainFragmentBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class MainFragment : Fragment() {

    private var updater: Disposable? = null

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

        val typeMap = hashMapOf<EventType, TypeView>()
        typeMap[EventType.Meal] = TypeView(
            EventType.Meal,
            binding.meals,
            binding.lastMeal,
            binding.nextMeal,
            binding.btnAddMeal,
            viewModel = viewModel
        )
        typeMap[EventType.Sleep] = TypeView(
            EventType.Sleep,
            binding.sleeps,
            binding.lastSleep,
            binding.nextSleep,
            binding.btnAddSleep,
            viewModel = viewModel
        )
        typeMap[EventType.Diaper] = TypeView(
            EventType.Diaper,
            binding.diapers,
            binding.lastDiaper,
            binding.nextDiaper,
            binding.btnAddDiaper,
            viewModel = viewModel
        )

        typeMap.forEach { type ->
            type.value.also { tf ->
                tf.setAdapter()
                tf.rv.layoutManager = LinearLayoutManager(context).apply {
                    reverseLayout = true
                }
                tf.setAddBtn()
            }
        }

        viewModel.in48Hours.observe(this) { events ->
            typeMap.forEach { m ->
                (m.value.adapter)?.submitList(events.filter { it.type == m.key }
                    .sortedByDescending { it.start })
                (m.value.lastTimeView).text =
                    (events.filter { it.type == m.key }.maxByOrNull { it.start }?.start)?.format(
                        DateTimeFormatter.ofPattern("a hh시 mm분", Locale.KOREAN)
                    )
            }
        }

        updater = Observable.interval(1, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                typeMap.forEach { m ->
                    m.value.updateTimeLine()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updater?.dispose()
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