package com.example.pocketweb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pocketweb.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home , container , false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivityRef = requireActivity() as MainActivity
        binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(result: String?): Boolean {
//
                if(mainActivityRef.checkForInternet(requireContext()))
                    mainActivityRef.changeTab(result!! , BrowseFragment(result))
                else
                    Snackbar.make(binding.root , "Internet not Connected" , 3000).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false

        })
    }
}