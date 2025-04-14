package com.example.smartcarrental.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcarrental.R
import com.example.smartcarrental.databinding.ItemBookingBinding
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.model.Car
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class BookingAdapter : ListAdapter<BookingWithCar, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("EUR")
        }

        fun bind(bookingWithCar: BookingWithCar) {
            val booking = bookingWithCar.booking
            val car = bookingWithCar.car

            binding.tvCarName.text = "${car.make} ${car.model}"
            binding.tvBookingDates.text = "${dateFormat.format(booking.startDate)} - ${dateFormat.format(booking.endDate)}"
            binding.tvBookingPrice.text = currencyFormat.format(booking.totalPrice)
            binding.tvBookingStatus.text = booking.status

            val statusColor = when (booking.status) {
                "PENDING" -> R.color.colorPending
                "ACTIVE" -> R.color.colorActive
                "COMPLETED" -> R.color.colorCompleted
                "CANCELLED" -> R.color.colorCancelled
                else -> R.color.colorPending
            }
            binding.tvBookingStatus.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )

            val context = binding.root.context
            val resourceId = context.resources.getIdentifier(
                car.imageUrl, "drawable", context.packageName
            )

            if (resourceId != 0) {
                binding.ivCarBooking.setImageResource(resourceId)
            } else {
                binding.ivCarBooking.setImageResource(R.drawable.ic_cars) // Default image
            }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<BookingWithCar>() {
        override fun areItemsTheSame(oldItem: BookingWithCar, newItem: BookingWithCar): Boolean {
            return oldItem.booking.id == newItem.booking.id
        }

        override fun areContentsTheSame(oldItem: BookingWithCar, newItem: BookingWithCar): Boolean {
            return oldItem == newItem
        }
    }
}

data class BookingWithCar(
    val booking: Booking,
    val car: Car
)