package com.example.mila.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mila.databinding.ItemContainerLeaderboardBinding
import com.example.mila.model.ChatMessage
import com.example.mila.model.LeaderBoard

class LeaderBoardAdapter ( private val listLeaderBoard: MutableList<LeaderBoard>) : RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder>(
)  {

    class LeaderBoardViewHolder(val binding: ItemContainerLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardViewHolder {
        val binding = ItemContainerLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderBoardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listLeaderBoard.size
    }

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {
        val leaderBoardItem = listLeaderBoard[position]
        holder.binding.tvTag.text = leaderBoardItem.tag
        holder.binding.tvScore.text = leaderBoardItem.total.toString() + "x"
        holder.binding.tvRank.text = (position + 1).toString()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(leaderBoards: List<LeaderBoard>) {
        listLeaderBoard.clear()
        listLeaderBoard.addAll(leaderBoards)
        notifyDataSetChanged()
    }

}