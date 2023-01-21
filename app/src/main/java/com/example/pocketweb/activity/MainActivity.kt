package com.example.pocketweb.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.pocketweb.R
import com.example.pocketweb.activity.MainActivity.Companion.myPager
import com.example.pocketweb.databinding.ActivityMainBinding
import com.example.pocketweb.databinding.BookmarkDialogBinding
import com.example.pocketweb.databinding.FeaturesMoreBinding
import com.example.pocketweb.databinding.TabsViewBinding
import com.example.pocketweb.fragment.BrowseFragment
import com.example.pocketweb.fragment.HomeFragment
import com.example.pocketweb.model.Bookmark
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
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
        var bookmarkIndex: Int = -1
        lateinit var myPager: ViewPager2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllBookmarks()
        tabsList.add(HomeFragment())
        binding.viewPager.adapter = TabsAdapter(supportFragmentManager , lifecycle)
        binding.viewPager.isUserInputEnabled = false
        myPager = binding.viewPager
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

    private fun initializeView() {

        binding.tabsBtn.setOnClickListener {
            val viewTabs = layoutInflater.inflate(R.layout.tabs_view, binding.root, false)
            val BindingTabs = TabsViewBinding.bind(viewTabs)

            val dialogTabs = MaterialAlertDialogBuilder(this, R.style.roundCornerDialog).setView(viewTabs)
                .setTitle("Select Tab")
                .setPositiveButton("Home"){self , _ ->
                    self.dismiss()}
                .setNeutralButton("Duck Duck Go"){self, _ ->
                    self.dismiss()
                }
                .create()
            dialogTabs.show()
            val pBtn = dialogTabs.getButton(AlertDialog.BUTTON_POSITIVE)
            val nBtn = dialogTabs.getButton(AlertDialog.BUTTON_NEUTRAL)
            pBtn.setTextColor(Color.BLACK)
            nBtn.setTextColor(Color.BLACK)
            pBtn.isAllCaps = false
            nBtn.isAllCaps = false
            pBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat.getDrawable(resources, R.drawable.ic_home, theme), null, null, null)
            nBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat.getDrawable(resources, R.drawable.ic_add, theme), null, null, null)
        }

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
                dialogBinding.fullscreenBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity , R.color.cool_blue))
                }
            }

            frag?.let {
                bookmarkIndex = isBookmarked(it.binding.webView.url!!)
                if(bookmarkIndex != -1) {
                dialogBinding.bookmarkBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity , R.color.cool_blue))
                }
            } }

            if(isDesktopSite) {
                dialogBinding.desktopBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor(this@MainActivity , R.color.cool_blue))
                }
            }

            dialogBinding.backBtn.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forwardBtn.setOnClickListener {
                frag?.apply {
                    if(binding.webView.canGoForward())
                        binding.webView.goForward()
                }
            }

            dialogBinding.saveBtn.setOnClickListener {
                dialog.dismiss()
                if(frag != null)
                    saveAsPdf(web = frag.binding.webView)
                else
                    Toast.makeText(this , "No webpage found" , Toast.LENGTH_SHORT).show()
            }

            dialogBinding.fullscreenBtn.setOnClickListener {
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

            dialogBinding.desktopBtn.setOnClickListener {
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

            dialogBinding.bookmarkBtn.setOnClickListener {
                frag?.let {
                    if(bookmarkIndex == -1) {
                        val viewBookmark = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false)
                        val bookmarkBinding = BookmarkDialogBinding.bind(viewBookmark)
                        val dialogBookmark = MaterialAlertDialogBuilder(this)
                            .setTitle("Add Bookmark")
                            .setMessage("URL: ${it.binding.webView.url}")
                            .setPositiveButton("Add"){self, _ ->
                                try {
                                    val array = ByteArrayOutputStream()
                                    it.favicon?.compress(Bitmap.CompressFormat.PNG, 100, array)
                                    bookmarkList.add(Bookmark(bookmarkBinding.bookmarkTitle.text.toString() , it.binding.webView.url!! , array.toByteArray()))
                                } catch (e: Exception) {
                                    val array = ByteArrayOutputStream()
                                    it.favicon?.compress(Bitmap.CompressFormat.PNG, 100, array)
                                    bookmarkList.add(Bookmark(bookmarkBinding.bookmarkTitle.text.toString() , it.binding.webView.url!!))
                                }
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self , _ -> self.dismiss()}
                            .setView(viewBookmark).create()
                        dialogBookmark.show()
                        bookmarkBinding.bookmarkTitle.setText(it.binding.webView.title)
                    } else {
                        val viewBookmark = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false)
                        val bookmarkBinding = BookmarkDialogBinding.bind(viewBookmark)
                        val dialogBookmark = MaterialAlertDialogBuilder(this)
                            .setTitle("Remove Bookmark")
                            .setMessage("URL: ${it.binding.webView.url}")
                            .setPositiveButton("Remove"){self, _ ->
                                bookmarkList.removeAt(bookmarkIndex)
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self , _ -> self.dismiss()}
                            .create()
                        dialogBookmark.show()
                    }
                }
                dialog.dismiss()
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

    fun isBookmarked(url: String): Int {
        bookmarkList.forEachIndexed { index, bookmark ->
            if (bookmark.url == url)    return index
        }
        return -1
    }

    fun savedBookmarks() {
        //storing bookmarks in shared preferences
        val editor = getSharedPreferences("BOOKMARKS" , MODE_PRIVATE).edit()
        val data = GsonBuilder().create().toJson(bookmarkList)
        editor.putString("bookmarkList" , data)
        editor.apply()
    }

    private fun getAllBookmarks() {
        //getting Bookmarks from shared preferences
        bookmarkList = ArrayList()
        val editor = getSharedPreferences("BOOKMARKS" , MODE_PRIVATE)
        val data = editor.getString("bookmarkList" , null)
        if(data != null) {
            val list: ArrayList<Bookmark> = GsonBuilder().create().fromJson(data , object:
                TypeToken<ArrayList<Bookmark>>(){}.type)
            bookmarkList.addAll(list)
        }
    }
}

@SuppressLint("NotifyDataSetChanged")
fun changeTab(url: String, fragment: Fragment) {
    MainActivity.tabsList.add(fragment)
    myPager.adapter?.notifyDataSetChanged()
    myPager.currentItem = MainActivity.tabsList.size - 1
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