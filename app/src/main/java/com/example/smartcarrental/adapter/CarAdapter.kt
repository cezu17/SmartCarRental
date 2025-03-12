package com.example.smartcarrental.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.ItemCarBinding
import com.example.smartcarrental.model.Car

class CarAdapter(private val onCarClick: (Car) -> Unit) :
    ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CarViewHolder(private val binding: ItemCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCarClick(getItem(position))
                }
            }
        }

        fun bind(car: Car) {
            binding.tvCarName.text = "${car.make} ${car.model}"
            binding.tvCarCategory.text = car.category
            binding.tvCarPrice.text = "€${car.price}/day"


            val context = binding.root.context
            val resourceId = context.resources.getIdentifier(
                car.imageUrl, "drawable", context.packageName
            )

            if (resourceId != 0) {
                binding.ivCar.setImageResource(resourceId)
            } else {
                binding.ivCar.setImageResource(R.drawable.ic_cars) // Default image
            }
        }
    }

    class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }
}