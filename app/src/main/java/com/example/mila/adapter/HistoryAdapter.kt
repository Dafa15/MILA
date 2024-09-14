package com.example.mila.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mila.databinding.ItemContainerHistoryBinding
import com.example.mila.model.ChatMessage

class HistoryAdapter(
    private var chatMessages: MutableList<ChatMessage>,
) : RecyclerView.Adapter<HistoryAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    class ListViewHolder(val binding: ItemContainerHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemContainerHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.binding.textMessage.text = chatMessage.message
        holder.binding.chatDate.text = chatMessage.dateTime
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(chatMessage)
        }
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ChatMessage)
    }

    // Fungsi untuk memperbarui data chatMessages dan memberi tahu adapter tentang perubahan data
    @SuppressLint("NotifyDataSetChanged")
    fun updateMessages(messages: List<ChatMessage>) {
        chatMessages.clear()
        chatMessages.addAll(messages)
        notifyDataSetChanged()
    }
}
