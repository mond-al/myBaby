package com.mond.mealdiapersleep.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mond.mealdiapersleep.MainApplication
import com.mond.mealdiapersleep.R
import com.mond.mealdiapersleep.data.EventListAdapter
import com.mond.mealdiapersleep.data.EventRepository
import com.mond.mealdiapersleep.data.EventType
import com.mond.mealdiapersleep.databinding.MainFragmentBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.time.LocalDateTime
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
        val types = getTypes(binding)

        for (item in types) {
            item.rv.adapter = EventListAdapter(viewModel).apply {
                registerAdapterDataObserver(createAdapterDataObserver(viewModel, item))
            }
            item.addBtn.setOnClickListener {
                when (item.type) {
                    EventType.Meal -> inputMealVolume(viewModel, item)
                    EventType.Sleep -> {
                        val lastVol = viewModel.getLastEvent(EventType.Sleep)?.volume?:0
                        viewModel.add(
                            item.type,
                            LocalDateTime.now(),
                            null,
                            if (lastVol > 0) -1 else 1
                        )
                    }
                    EventType.Diaper -> {
                        MaterialAlertDialogBuilder(view.context)
                            .setTitle("Choice")
                            .setNegativeButton("소변") { dialogInterface, _ ->
                                viewModel.add(
                                    item.type,
                                    LocalDateTime.now(),
                                    null,
                                    -1
                                )
                                dialogInterface.dismiss()
                            }
                            .setPositiveButton("대변") { dialogInterface, _ ->
                                viewModel.add(
                                    item.type,
                                    LocalDateTime.now(),
                                    null,
                                    1
                                )
                                dialogInterface.dismiss()
                            }.create().show()
                    }
                }
            }
        }

        viewModel.in48Hours.observe(this) { events ->
            types.forEach { typeView ->
                (typeView.rv.adapter as? EventListAdapter)?.submitList(events.filter { it.type == typeView.type }
                    .sortedByDescending { it.start })
                typeView.lastTimeView.text = (events.filter {
                    it.type == typeView.type && if (typeView.type == EventType.Sleep) it.volume < 0 else true
                }
                    .maxByOrNull { it.start }?.start)?.format(
                        DateTimeFormatter.ofPattern("a hh시 mm분", Locale.KOREAN)
                    )
                typeView.nextTimeView.text =
                    "${viewModel.getNextGap(typeView.type, typeView.term).toMinutes()} 분 남았음"
            }
        }

        updater = Observable.interval(1, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                types.forEach { item ->
                    item.rv.adapter?.notifyDataSetChanged()
                }
            }
    }

    private fun inputMealVolume(
        viewModel: MainViewModel,
        item: TypeView
    ) {
        Dialog(context!!).apply {
            setContentView(R.layout.seek)
            var progressVal = 0
            val vol = findViewById<TextView>(R.id.seek_vol)
            findViewById<View>(R.id.confirm_button).setOnClickListener {
                viewModel.add(item.type, LocalDateTime.now(), null, progressVal)
                dismiss()
            }
            findViewById<SeekBar>(R.id.size_seekbar).run {
                setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        updateProgress(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }


                    private fun updateProgress(progress: Int) {
                        progressVal = progress * 2
                        vol.text = "$progressVal ml"
                    }
                })
                setProgress(50, true)
            }
        }.also { it.show() }
    }

    private fun getTypes(binding: MainFragmentBinding) =
        listOf(
            TypeView(
                EventType.Meal,
                binding.meals,
                binding.lastMeal,
                binding.nextMeal,
                binding.btnAddMeal,
                180
            ),
            TypeView(
                EventType.Sleep,
                binding.sleeps,
                binding.lastSleep,
                binding.nextSleep,
                binding.btnAddSleep,
                180
            ),
            TypeView(
                EventType.Diaper,
                binding.diapers,
                binding.lastDiaper,
                binding.nextDiaper,
                binding.btnAddDiaper,
                120
            )
        )

    override fun onDestroyView() {
        super.onDestroyView()
        updater?.dispose()
    }


    private fun createAdapterDataObserver(viewModel: MainViewModel, typeView: TypeView) =
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                if (positionStart > 0)
                    typeView.rv.adapter?.notifyItemChanged(positionStart - 1)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                typeView.rv.smoothScrollToPosition(0)
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