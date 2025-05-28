package com.example.soulmole.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.soulmole.R
import com.example.soulmole.db.DatabaseHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        val btnStart: Button = findViewById(R.id.btn_start)
        val btnRanking: Button = findViewById(R.id.btn_ranking)
        val btnExit: Button = findViewById(R.id.btn_exit)

        // Start GameActivity directly when Start button is pressed
        btnStart.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        // Navigate to RankingActivity when Ranking button is pressed
        btnRanking.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        // Exit the app when Exit button is pressed
        btnExit.setOnClickListener {
            showExitConfirmationDialog()
        }

    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
