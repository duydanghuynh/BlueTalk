package com.bignerdranch.android.bluetalk

import android.util.Log
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

private const val TAG="ChatViewModel"
class ChatViewModel : ViewModel() {
    init {
        Log.d(TAG,"Chat ViewModel instance is created")
    }

    var fromId = 0 //to note the owner info
    private var toId = 0//to show profile info
    private var currentChatId=0

    private var conversations = mutableListOf<Chat>()

    fun setCurrentChatId(id:Int){
        if(id!= currentChatId){
            currentChatId = id
        }
    }

    //later add from conversation
    fun getConversations(toId:Int): Chat? {
        for(chat in conversations){
            if(chat.toUserId == toId){
                return chat
            }
        }
        return null
    }

    fun setConversations(fromUserId:Int, toUserId:Int, chatId:Int, content: String) {
        val currentConversation = conversations[chatId]
        if (currentConversation!=null){
            currentConversation.messages.add(
                Message(
                    currentConversation.messages.lastIndex+1,
                    fromUserId,
                    toUserId,
                    content,
                    LocalDateTime.now()
                )
            )
        }
    }

    override fun onCleared(){
        super.onCleared()
        Log.d(TAG,"Profile ViewModel instance is about to be destroyed")
    }
}