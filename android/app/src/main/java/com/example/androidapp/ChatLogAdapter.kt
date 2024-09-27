package com.example.androidapp

import ai.perso.live.perso.Chat
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatLogAdapter : RecyclerView.Adapter<ChatLogViewHolder>() {

    private val chatLog = mutableListOf<Chat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatLogViewHolder {
        return ChatLogViewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun getItemCount(): Int {
        return chatLog.size
    }

    override fun onBindViewHolder(holder: ChatLogViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.message).text = chatLog[position].text
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatLog[position].isUser) 0 else 1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replaceChatLog(chatLog: List<Chat>) {
        clearChatLog()
        this.chatLog.addAll(chatLog)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearChatLog() {
        this.chatLog.clear()
        notifyDataSetChanged()
    }

}

sealed class ChatLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class UserMessage(itemView: View) : ChatLogViewHolder(itemView)
    class ChatbotMessage(itemView: View) : ChatLogViewHolder(itemView)
}

private object ChatLogViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int): ChatLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            ChatLogViewHolder.UserMessage(inflater.inflate(R.layout.chat_log_user_message, parent, false))
        } else {
            ChatLogViewHolder.ChatbotMessage(inflater.inflate(R.layout.chat_log_chatbot_message, parent, false))
        }
    }
}
