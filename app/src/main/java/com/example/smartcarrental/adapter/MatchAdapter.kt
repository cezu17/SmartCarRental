package com.example.smartcarrental.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.ItemMatchBinding
import com.example.smartcarrental.model.Car

class MatchAdapter(private val onClick: (Car) -> Unit) :
    ListAdapter<Car, MatchAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Car>() {
            override fun areItemsTheSame(a: Car, b: Car) = a.id == b.id
            override fun areContentsTheSame(a: Car, b: Car) = a == b
        }
    }

    inner class VH(private val b: ItemMatchBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(car: Car) {
            b.tvName.text = "${car.make} ${car.model}"
            b.tvPrice.text = "€${car.price}/day"

            val resId = b.root.context.resources
                .getIdentifier(car.imageUrl, "drawable", b.root.context.packageName)
            b.ivThumb.setImageResource(if (resId != 0) resId else R.drawable.ic_cars)
            b.root.setOnClickListener { onClick(car) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))
}
