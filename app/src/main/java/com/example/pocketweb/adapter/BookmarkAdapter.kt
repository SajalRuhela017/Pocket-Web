package com.example.pocketweb.adapter

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketweb.R
import com.example.pocketweb.activity.MainActivity
import com.example.pocketweb.activity.changeTab
import com.example.pocketweb.activity.checkForInternet
import com.example.pocketweb.databinding.BookmarkViewBinding
import com.example.pocketweb.databinding.LongBookmarkViewBinding
import com.example.pocketweb.fragment.BrowseFragment

class BookmarkAdapter(private val context: Context, private val isActivity: Boolean = false): RecyclerView.Adapter<BookmarkAdapter.MyHolder>() {

    private val colors = context.resources.getIntArray(R.array.myColors)

    class MyHolder(binding: BookmarkViewBinding? = null, bindingLong: LongBookmarkViewBinding? = null)
        :RecyclerView.ViewHolder((binding?.root ?: bindingLong?.root)!!) {
        val image = (binding?.bookmarkIcon ?: bindingLong?.bookmarkIcon)!!
        val name = (binding?.bookmarkName ?: bindingLong?.bookmarkName)!!
        val root = (binding?.root ?: bindingLong?.root)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        if(isActivity)
            return MyHolder(bindingLong = LongBookmarkViewBinding.inflate(LayoutInflater.from(context) , parent , false))
        return MyHolder(binding = BookmarkViewBinding.inflate(LayoutInflater.from(context) , parent , false))
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
            when {
                checkForInternet(context) -> {
                    changeTab(
                        MainActivity.bookmarkList[position].name,
                        BrowseFragment(MainActivity.bookmarkList[position].url))
                    if(isActivity) (context as Activity).finish()
                }
                else -> Toast.makeText(context, "Internet not connected" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return MainActivity.bookmarkList.size
    }
}