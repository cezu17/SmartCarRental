package com.example.smartcarrental.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcarrental.databinding.ItemBotBubbleBinding
import com.example.smartcarrental.databinding.ItemUserBubbleBinding
import com.example.smartcarrental.network.Message

class ChatAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT  = 1

        private val DIFF = object: DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = false
            override fun areContentsTheSame(a: Message, b: Message) = a==b
        }
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).role == "user") TYPE_USER else TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_USER) {
            UserVH(ItemUserBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            BotVH(ItemBotBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val msg = getItem(pos)
        when (holder) {
            is UserVH -> holder.bind(msg)
            is BotVH  -> holder.bind(msg)
        }
    }

    fun addUserMessage(text: String) {
        val list = currentList.toMutableList()
        list.add(Message("user", text))
        submitList(list)
    }
    fun addBotMessage(text: String) {
        val list = currentList.toMutableList()
        list.add(Message("assistant", text))
        submitList(list)
    }

    class UserVH(val b: ItemUserBubbleBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(m: Message) { b.tvUserMessage.text = m.content }
    }
    class BotVH(val b: ItemBotBubbleBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(m: Message) { b.tvBotMessage.text = m.content }
    }
}
