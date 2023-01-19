package com.example.pocketweb

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pocketweb.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        var tabsList: ArrayList<Fragment> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tabsList.add(HomeFragment())
//        tabsList.add(BrowseFragment())
        binding.viewPager.adapter = TabsAdapter(supportFragmentManager , lifecycle)
        binding.viewPager.isUserInputEnabled = false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        when {
            binding.viewPager.currentItem != 0 -> {
                tabsList.removeAt(binding.viewPager.currentItem)
                binding.viewPager.adapter?.notifyDataSetChanged()
                binding.viewPager.currentItem = tabsList.size - 1
            }
            else -> super.onBackPressed()
        }
    }

    private inner class TabsAdapter(fa: FragmentManager , lc: Lifecycle) : FragmentStateAdapter(fa , lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position]
    }

    fun changeTab(url: String , fragment: Fragment) {
        tabsList.add(fragment)
        binding.viewPager.adapter?.notifyDataSetChanged()
        binding.viewPager.currentItem = tabsList.size - 1
    }

    fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
}