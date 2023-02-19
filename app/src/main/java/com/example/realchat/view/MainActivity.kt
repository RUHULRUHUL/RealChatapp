package com.example.realchat.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.realchat.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavController()
    }

    private fun initNavController() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        val navController: NavController = Navigation.findNavController(this, R.id.Fragment)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}