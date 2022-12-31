package com.codepath.stegcaption

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.codepath.stegcaption.ui.main.SectionsPagerAdapter
import com.codepath.stegcaption.databinding.ActivityTabbedMainBinding
import com.codepath.stegcaption.ui.main.DecodeFragment

class TabbedMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabbedMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabbedMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        Log.d("StegCap", "onCreate() called")

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d("StegCap", "onCreateOptionsMenu() called")
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_help -> Log.d("StegCap", "Help menu item clicked")
            R.id.menu_exit -> Log.d("StegCap", "Exit menu item clicked")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}