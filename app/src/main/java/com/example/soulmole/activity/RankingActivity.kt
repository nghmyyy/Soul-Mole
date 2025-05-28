package com.example.soulmole.activity

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.soulmole.R
import com.example.soulmole.db.DatabaseHelper
import com.example.soulmole.model.Player

class RankingActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listViewRanking: ListView
    private lateinit var spinnerFilter: Spinner
    private lateinit var btn_delte_all: Button
    private lateinit var btn_return: Button
    private lateinit var btn_search: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        dbHelper = DatabaseHelper(this)
        listViewRanking = findViewById(R.id.listViewRanking)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btn_delte_all = findViewById(R.id.btn_delete_all)
        btn_return = findViewById(R.id.btn_return)
        btn_search = findViewById(R.id.btn_search)

        setupSpinner()
        setupButtons()
    }

    private fun setupSpinner() {
        val options = resources.getStringArray(R.array.top_options)
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        // Handle spinner selection
        spinnerFilter.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val limit = when (position) {
                    0 -> 5  // Top 5
                    1 -> 10 // Top 10
                    2 -> -1 // Show all
                    else -> -1
                }
                loadLeaderboard(limit)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if nothing is selected
            }
        })
    }


    private fun loadLeaderboard(limit: Int) {
        val dbHelper = DatabaseHelper(this)
        val sessions = if (limit > 0) {
            dbHelper.getTopPlayers(limit)
        } else {
            dbHelper.getAllPlayers()
        }

        val adapter = RankingAdapter(this, sessions)
        listViewRanking.adapter = adapter
    }

    private fun setupButtons() {
        btn_return.setOnClickListener {
            finish() // Return to the previous activity
        }

        btn_delte_all.setOnClickListener {
            showDeleteAllConfirmationDialog()
            loadLeaderboard(-1) // Reload all players after clearing
        }

        btn_search.setOnClickListener{
            showSearchDialog()
        }
    }

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Player")

        // Tạo EditText để nhập số thứ tự
        val input = android.widget.EditText(this)
        input.hint = "Enter Rank"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Search") { _, _ ->
            val rank = input.text.toString().toIntOrNull()
            if (rank == null || rank <= 0) {
                Toast.makeText(this, "Please enter a valid rank", Toast.LENGTH_SHORT).show()
            } else {
                showPlayerInfoDialog(rank)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showPlayerInfoDialog(rank: Int) {
        val dbHelper = DatabaseHelper(this)
        val sessions = dbHelper.getAllPlayers()

        if (rank > sessions.size) {
            Toast.makeText(this, "No player found for this rank", Toast.LENGTH_SHORT).show()
            return
        }

        val gameSession = sessions[rank - 1] // Vì rank là 1-based, còn danh sách là 0-based

        val player = gameSession.player
        val playTime = formatPlayTime(gameSession.timeSession)
        val playDate = formatDate(gameSession.dateSession)

        val info = """
        Rank: $rank
        Name: ${player.username}
        Score: ${player.score}
        Play Time: $playTime
        Play Date: $playDate
    """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Player Info")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }



    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All Players")
            .setMessage("Are you sure you want to delete all players?")
            .setPositiveButton("Yes") { _, _ ->
                val rowsDeleted = dbHelper.deleteAllPlayers()
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "All players deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No players to delete", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Format thời gian chơi (giây -> phút:giây)
    private fun formatPlayTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // Format ngày chơi (Date -> Chuỗi định dạng)
    private fun formatDate(date: java.util.Date): String {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
