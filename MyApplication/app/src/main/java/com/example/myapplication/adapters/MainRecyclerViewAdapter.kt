package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemMainRecyclerViewBinding
import com.example.myapplication.util.webrtc.UserStatus

class MainRecyclerViewAdapter(private val listener:Listener) : RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder>() {

    private var usersList:List<Pair<String,String>>?=null
    fun updateList(list:List<Pair<String,String>>){
        this.usersList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        val binding = ItemMainRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )
        return MainRecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return usersList?.size?:0
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
        usersList?.let { list->
            val user = list[position]
            holder.bind(user,{
                listener.onVideoCallClicked(it)
            })
        }
    }

    interface  Listener {
        fun onVideoCallClicked(username:String)
        fun onAudioCallClicked(username:String)
    }



    class MainRecyclerViewHolder(private val binding: ItemMainRecyclerViewBinding):
        RecyclerView.ViewHolder(binding.root){
        private val context = binding.root.context

        fun bind(
            user:Pair<String,String>,
            videoCallClicked:(String) -> Unit,
        ){
            binding.apply {
                when (user.second) {
                    "ONLINE" -> {
                        videoCallBtn.isVisible = true
                        videoCallBtn.setOnClickListener {
                            videoCallClicked.invoke(user.first)
                        }
                        statusTv.setTextColor(context.resources.getColor(R.color.light_green, null))
                        statusTv.text = "접속 상태"
                    }
                    "OFFLINE" -> {
                        videoCallBtn.isVisible = false
                        statusTv.setTextColor(context.resources.getColor(R.color.red, null))
                        statusTv.text = "미접속 상태"
                    }
                    "IN_CALL" -> {
                        videoCallBtn.isVisible = false
                        statusTv.setTextColor(context.resources.getColor(R.color.yellow, null))
                        statusTv.text = "게임중"
                    }
                }

                usernameTv.text = user.first
            }



        }



    }
}