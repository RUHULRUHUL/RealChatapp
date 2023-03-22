package com.example.realchatapp.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.realchatapp.R
import com.example.realchatapp.model.profile.ActiveStatus
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Utils
import com.example.realchatapp.utils.Validator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavController()
        initValue()
    }

    private fun initValue() {
        auth = Firebase.auth
    }

    private fun initNavController() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        val navController: NavController = Navigation.findNavController(this, R.id.Fragment)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")
    }

    override fun onPause() {
        super.onPause()
        userStatusUpdate("offline")
    }

    override fun onResume() {
        super.onResume()
        userStatusUpdate("online")
    }

    override fun onRestart() {
        super.onRestart()
        userStatusUpdate("online")
    }

    override fun onStop() {
        super.onStop()
        userStatusUpdate("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        userStatusUpdate("offline")
    }

    private fun userStatusUpdate(state: String) {
        if (Utils.isNetworkAvailable(this)) {
            val activeStatus = ActiveStatus(
                state,
                Validator.getCurrentDate(),
                Validator.getCurrentTime()
            )
            DBReference.userRef
                .child(auth.uid.toString())
                .child("UserState")
                .setValue(activeStatus)
        }
    }
}