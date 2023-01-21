package com.example.pocketweb.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketweb.R
import com.example.pocketweb.activity.MainActivity
import com.example.pocketweb.databinding.BookmarkViewBinding
import com.example.pocketweb.fragment.BrowseFragment

class BookmarkAdapter(private val context: Context): RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {

    private val colors = context.resources.getIntArray(R.array.myColors)

    class MyHolder(binding: BookmarkViewBinding):RecyclerView.ViewHolder(binding.root) {
        val image = binding.bookmarkIcon
        val name = binding.bookmarkName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(BookmarkViewBinding.inflate(LayoutInflater.from(context) , parent , false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        try {
            val icon = BitmapFactory.decodeByteArray(MainActivity.bookmarkList[position].image , 0 , MainActivity.bookmarkList[position].image!!.size)
            holder.image.background = icon.toDrawable(context.resources)
        }catch(e: Exception) {
            holder.image.setBackgroundColor(colors[(colors.indices).random()])
            holder.image.text = MainActivity.bookmarkList[position].name[0].toString()
        }
        holder.name.text = MainActivity.bookmarkList[position].name
        holder.root.setOnClickListener {
            context as MainActivity
            when {
                context.checkForInternet(context) -> context.changeTab(MainActivity.bookmarkList[position].name,
                BrowseFragment(urlNew = MainActivity.bookmarkList[position].url))
                else -> Toast.makeText(context, "Internet not connected" , Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun getItemCount(): Int {
        return MainActivity.bookmarkList.size
    }
}