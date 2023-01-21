package com.example.pocketweb.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketweb.adapter.BookmarkAdapter
import com.example.pocketweb.databinding.ActivityBookmarkBinding

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerViewBookmarks.setItemViewCacheSize(5)
        binding.recyclerViewBookmarks.hasFixedSize()
        binding.recyclerViewBookmarks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBookmarks.adapter = BookmarkAdapter(this , true)
    }
}