package com.example.pocketweb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pocketweb.R
import com.example.pocketweb.activity.MainActivity
import com.example.pocketweb.adapter.BookmarkAdapter
import com.example.pocketweb.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container , false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }


    override fun onResume() {
        super.onResume()
        val mainActivityRef = requireActivity() as MainActivity
        mainActivityRef.binding.topSearchBar.setText("")
        binding.searchView.setQuery("" , false)
        mainActivityRef.binding.webIcon.setImageResource(R.drawable.ic_search)
        binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result: String?): Boolean {
//
                if(mainActivityRef.checkForInternet(requireContext()))
                    mainActivityRef.changeTab(result!! , BrowseFragment(result))
                else
                    Snackbar.make(binding.root , "Internet not Connected" , 2000).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        mainActivityRef.binding.goBtn.setOnClickListener{
            if(mainActivityRef.checkForInternet(requireContext()))
                mainActivityRef.changeTab(mainActivityRef.binding.topSearchBar.text.toString() ,
                    BrowseFragment(mainActivityRef.binding.topSearchBar.text.toString())
                )
            else
                Toast.makeText(context , "Internet not connected" , Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext() , 5)
        binding.recyclerView.adapter = BookmarkAdapter(requireContext())
    }
}