package com.example.pocketweb.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pocketweb.fragment.BrowseFragment
import com.example.pocketweb.fragment.HomeFragment
import com.example.pocketweb.model.Bookmark
import com.example.pocketweb.R
import com.example.pocketweb.databinding.ActivityMainBinding
import com.example.pocketweb.databinding.FeaturesMoreBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var printJob: PrintJob? = null
    companion object {
        var tabsList: ArrayList<Fragment> = ArrayList()
        private var isFullScreen: Boolean = true
        var isDesktopSite: Boolean = false
        var bookmarkList: ArrayList<Bookmark> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tabsList.add(HomeFragment())
        binding.viewPager.adapter = TabsAdapter(supportFragmentManager , lifecycle)
        binding.viewPager.isUserInputEnabled = false
        initializeView()
        changeFullScreen(false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        var frag: BrowseFragment?= null
        try {
            frag = tabsList[binding.viewPager.currentItem] as BrowseFragment
        }catch (_: Exception){}
        when {
            frag?.binding?.webView?.canGoBack() == true -> frag.binding.webView.goBack()
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

    @SuppressLint("NotifyDataSetChanged")
    fun changeTab(url: String, fragment: Fragment) {
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

    private fun initializeView() {
        binding.settingsBtn.setOnClickListener {
            var frag: BrowseFragment?= null
            try {
                frag = tabsList[binding.viewPager.currentItem] as BrowseFragment
            } catch (_: Exception){}
            val view = layoutInflater.inflate(R.layout.features_more, binding.root, false)
            val dialogBinding = FeaturesMoreBinding.bind(view)

            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
                setBackgroundDrawable(ColorDrawable(0xFFFFFFD5.toInt()))
            }
            dialog.show()

            if(isFullScreen) {
                dialogBinding.fullscreen.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity , R.color.cool_blue))
                }
            }

            if(isDesktopSite) {
                dialogBinding.desktop.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity , R.color.cool_blue))
                }
            }

            dialogBinding.back.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forward.setOnClickListener {
                frag?.apply {
                    if(binding.webView.canGoForward())
                        binding.webView.goForward()
                }
            }

            dialogBinding.save.setOnClickListener {
                dialog.dismiss()
                if(frag != null)
                    saveAsPdf(web = frag.binding.webView)
                else
                    Toast.makeText(this , "No webpage found" , Toast.LENGTH_SHORT).show()
            }

            dialogBinding.fullscreen.setOnClickListener {
                it as MaterialButton
                isFullScreen = if(isFullScreen) {
                    changeFullScreen(false)
                    it.setIconTintResource(R.color.black)
                    it.setTextColor(ContextCompat.getColor(this , R.color.black))
                    false
                }
                else {
                    changeFullScreen(true)
                    it.setIconTintResource(R.color.cool_blue)
                    it.setTextColor(ContextCompat.getColor(this , R.color.cool_blue))
                    true
                }
            }

            dialogBinding.desktop.setOnClickListener {
                it as MaterialButton
                frag?.binding?.webView?.apply {
                    isDesktopSite = if(isDesktopSite) {
                        settings.userAgentString = null
                        it.setIconTintResource(R.color.black)
                        it.setTextColor(ContextCompat.getColor(this@MainActivity , R.color.black))
                        false
                    }
                    else {
                        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0"
                        settings.useWideViewPort = true
                        evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content',"
                            + " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));" , null)
                        it.setIconTintResource(R.color.cool_blue)
                        it.setTextColor(ContextCompat.getColor(this@MainActivity ,
                            R.color.cool_blue
                        ))
                        true
                    }
                    reload()
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        printJob?.let {
            when{
                it.isCompleted -> Toast.makeText(this , "Webpage saved successfully" ,  Toast.LENGTH_SHORT).show()
                it.isFailed -> Toast.makeText(this , "Some error occurred" ,  Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun saveAsPdf(web: WebView) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${URL(web.url).host}_${SimpleDateFormat("HH:MM , dd_MMM_yy" , Locale.ENGLISH)
            .format(Calendar.getInstance().time)}"
        val printAdapter = web.createPrintDocumentAdapter(jobName)
        val printAttributes = PrintAttributes.Builder()

        printJob = printManager.print(jobName , printAdapter , printAttributes.build())
    }

    private fun changeFullScreen(enable: Boolean) {
        if(enable) {
            WindowCompat.setDecorFitsSystemWindows(window , false)
            WindowInsetsControllerCompat(window , binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }else{
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }