package com.pangaea.idothecooking

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.pangaea.idothecooking.databinding.ActivityMainBinding
import com.pangaea.idothecooking.ui.settings.SettingsActivity
import com.pangaea.idothecooking.ui.shared.AboutDialog

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_recipes, R.id.nav_shopping_lists
//            ), drawerLayout
//        )
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Navigate to fragment
        val bundle = intent?.extras
        if (bundle != null) {
            when (bundle.getString("start", null)) {
                "recipes" -> {
                    navController.navigate(R.id.nav_recipes)
                }
                "shoppingLists" -> {
                    navController.navigate(R.id.nav_shopping_lists)
                }
                "categories" -> {
                    navController.navigate(R.id.nav_categories)
                }
            }
        }

        // Display about modal at startup
        val app = (application as IDoTheCookingApp)
        if (app.startupMode) {
            app.startupMode = false
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
            if (sharedPreferences.getBoolean("auto_launch_about", true)) {
                AboutDialog().show(supportFragmentManager, null)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                AboutDialog().show(supportFragmentManager, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}