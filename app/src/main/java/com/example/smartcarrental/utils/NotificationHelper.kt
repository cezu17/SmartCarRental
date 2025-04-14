package com.example.smartcarrental.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartcarrental.R
import com.example.smartcarrental.model.Booking
import com.example.smartcarrental.model.Car
import com.example.smartcarrental.view.MainActivity
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID_BOOKINGS = "bookings_channel"
        const val NOTIFICATION_ID_BOOKING_CONFIRMATION = 1001
        const val NOTIFICATION_ID_BOOKING_REMINDER = 1002
        const val NOTIFICATION_ID_BOOKING_STATUS_CHANGE = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bookingChannel = NotificationChannel(
                CHANNEL_ID_BOOKINGS,
                "Booking Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications related to your car bookings"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(bookingChannel)
        }
    }

    fun showBookingConfirmationNotification(booking: Booking, car: Car) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_tab", "bookings")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BOOKINGS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Booking Confirmed!")
            .setContentText("Your ${car.make} ${car.model} is reserved from ${dateFormat.format(booking.startDate)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your ${car.make} ${car.model} is reserved from ${dateFormat.format(booking.startDate)} to ${dateFormat.format(booking.endDate)}. Total: €${booking.totalPrice}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_BOOKING_CONFIRMATION, notification)
    }
}