package com.example.smartcarrental.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcarrental.databinding.ItemDateBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateAdapter(private val onDateSelected: (Date) -> Unit) :
    RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<Date>()
    private var selectedPosition = 0

    init {
        val calendar = Calendar.getInstance()
        repeat(14) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position], position == selectedPosition)
    }

    override fun getItemCount() = dates.size

    inner class DateViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val oldSelected = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldSelected)
                notifyItemChanged(selectedPosition)
                onDateSelected(dates[selectedPosition])
            }
        }

        fun bind(date: Date, isSelected: Boolean) {
            val calendar = Calendar.getInstance().apply { time = date }

            binding.tvDay.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            binding.tvDayOfWeek.text = dayFormat.format(date).uppercase()

            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            binding.tvMonth.text = monthFormat.format(date).uppercase()

            binding.tvDay.isSelected = isSelected
            val textColor = if (isSelected) Color.WHITE else Color.BLACK
            binding.tvDay.setTextColor(textColor)
        }
    }
}