package com.example.smartcarrental.view

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartcarrental.database.AppDatabase
import com.example.smartcarrental.databinding.FragmentProfileBinding
import com.example.smartcarrental.repository.RepositoryFactory
import com.example.smartcarrental.service.MigrationService
import com.example.smartcarrental.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = UserSession.getUser()
        if (currentUser != null) {
            binding.tvUserName.text = currentUser.fullName
            binding.tvUserEmail.text = currentUser.email
            binding.tvUserPhone.text = currentUser.phoneNumber
        }

        binding.btnLogout.setOnClickListener {
            UserSession.setUser(null)
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.btnMigrateToFirebase.setOnClickListener {
            startMigration()
        }

        binding.btnUploadToFirebase.setOnClickListener {
            uploadAllDataToFirebase()
        }
    }

    private fun startMigration() {
        AlertDialog.Builder(requireContext())
            .setTitle("Migrate to Firebase")
            .setMessage("This will migrate all your local data to Firebase. The process cannot be undone. Continue?")
            .setPositiveButton("Migrate") { _, _ ->

                MigrationService.startMigration(requireContext())

                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage("Migrating data to Firebase...")
                    setCancelable(false)
                    show()
                }


                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Migration completed. Restart the app to use Firebase.",
                        Toast.LENGTH_LONG
                    ).show()
                }, 5000)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadAllDataToFirebase() {
        lifecycleScope.launch {
            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("Uploading data to Firebase...")
                setCancelable(false)
                show()
            }

            try {
                uploadDataInBackground(progressDialog)
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FIREBASE_UPLOAD", "Error uploading data", e)
            }
        }
    }

    private suspend fun uploadDataInBackground(progressDialog: ProgressDialog) {
        // 1. Get database references
        val db = FirebaseFirestore.getInstance()
        val roomDb = AppDatabase.getDatabase(requireContext())

        val userDao = roomDb.userDao()
        val users = userDao.getAllUsers().value ?: listOf()

        for (user in users) {
            db.collection("users").document(user.id.toString())
                .set(user)
                .await()
            Log.d("FIREBASE_UPLOAD", "Uploaded user: ${user.username}")
        }

        val carDao = roomDb.carDao()
        val cars = carDao.getAllCars().value ?: listOf()

        for (car in cars) {
            db.collection("cars").document(car.id.toString())
                .set(car)
                .await()
            Log.d("FIREBASE_UPLOAD", "Uploaded car: ${car.make} ${car.model}")
        }

        val bookingDao = roomDb.bookingDao()
        val bookings = bookingDao.getAllBookings().value ?: listOf()

        for (booking in bookings) {
            db.collection("bookings").document(booking.id.toString())
                .set(booking)
                .await()
            Log.d("FIREBASE_UPLOAD", "Uploaded booking: ${booking.id}")
        }

        withContext(Dispatchers.Main) {
            progressDialog.dismiss()
            val repositoryFactory = RepositoryFactory(requireContext())

            Toast.makeText(requireContext(),
                "Data uploaded! Restart app to use Firebase.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}