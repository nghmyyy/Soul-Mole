package com.example.soulmole.activity

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.soulmole.R
import com.example.soulmole.model.GameSession
import com.example.soulmole.model.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RankingAdapter(
    private val context: Context,
    private val leaderboard: List<GameSession>
) : BaseAdapter() {

    override fun getCount(): Int = leaderboard.size + 1 // +1 cho header

    override fun getItem(position: Int): Any =
        if (position == 0) HeaderItem else leaderboard[position - 1]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_ranking, parent, false)

        val rankIcon = view.findViewById<ImageView>(R.id.rankIcon)
        val rankTextView = view.findViewById<TextView>(R.id.rankTextView)
        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val scoreTextView = view.findViewById<TextView>(R.id.scoreTextView)
        val playTimeTextView = view.findViewById<TextView>(R.id.playTimeTextView)
        val playDateTextView = view.findViewById<TextView>(R.id.playDateTextView)

        when (position) {
            0 -> bindHeaderRow(rankTextView, usernameTextView, scoreTextView, rankIcon, playTimeTextView, playDateTextView)
            else -> bindDataRow(
                position,
                leaderboard[position - 1],
                rankIcon,
                rankTextView,
                usernameTextView,
                scoreTextView,
                playTimeTextView,
                playDateTextView
            )
        }

        // Animation cho mỗi item
        view.animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)

        return view
    }

    private fun bindHeaderRow(
        rankView: TextView,
        usernameView: TextView,
        scoreView: TextView,
        rankIcon: ImageView,
        playTimeView: TextView, // Thời gian chơi
        playDateView: TextView  // Ngày chơi
    ) {
        rankIcon.visibility = View.GONE // Không hiển thị icon ở hàng header

        rankView.apply {
            text = "RANKING"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.header_text))
            visibility = View.VISIBLE
        }

        usernameView.apply {
            text = "NAME"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.header_text))
        }

        scoreView.apply {
            text = "SCORE"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.header_text))
        }

        playTimeView.apply {
            text = "TIME"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.header_text))
        }

        playDateView.apply {
            text = "DATE"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.header_text))
        }
    }

    private fun bindDataRow(
        position: Int,
        gameSession: GameSession,
        rankIcon: ImageView,
        rankView: TextView,
        usernameView: TextView,
        scoreView: TextView,
        playTimeView: TextView, // Thời gian chơi
        playDateView: TextView  // Ngày chơi
    ) {
        // Icon và thứ tự xếp hạng
        when (position) {
            1 -> {
                rankIcon.setImageResource(R.drawable.ic_rank_gold)
                rankIcon.visibility = View.VISIBLE
                rankView.visibility = View.GONE
            }
            2 -> {
                rankIcon.setImageResource(R.drawable.ic_rank_silver)
                rankIcon.visibility = View.VISIBLE
                rankView.visibility = View.GONE
            }
            3 -> {
                rankIcon.setImageResource(R.drawable.ic_rank_bronze)
                rankIcon.visibility = View.VISIBLE
                rankView.visibility = View.GONE
            }
            else -> {
                rankIcon.visibility = View.GONE
                rankView.visibility = View.VISIBLE
                rankView.setTextColor(getRankColor(position)) // Thiết lập màu sắc cho thứ hạng
            }
        }
        rankView.text = position.toString()

        // Thông tin người chơi
        val player = gameSession.player
        usernameView.text = player.username

        val textColor = if (position > 3) {
            ContextCompat.getColor(context, R.color.white)
        } else {
            getRankColor(position)
        }
        usernameView.setTextColor(textColor)
        scoreView.setTextColor(textColor)
        playTimeView.setTextColor(textColor)
        playDateView.setTextColor(textColor)


        // Điểm số
        scoreView.text = gameSession.player.score.toString()

        // Thời gian chơi
        playTimeView.text = formatPlayTime(gameSession.timeSession)

        // Ngày chơi
        playDateView.text = formatDate(gameSession.dateSession)
    }

    // Format thời gian chơi (giây -> phút:giây)
    private fun formatPlayTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // Format ngày chơi (Date -> Chuỗi định dạng)
    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }



    private fun getRankColor(position: Int): Int {
        val colorResId = when (position) {
            1 -> R.color.gold
            2 -> R.color.silver
            3 -> R.color.bronze
            else -> R.color.white
        }
        return ContextCompat.getColor(context, colorResId)
    }

    private object HeaderItem
}
