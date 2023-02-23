package com.example.realchat.view.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.ActivitySearchUserBinding
import com.example.realchat.model.profile.ActiveStatus
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference
import com.example.realchat.utils.Utils
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.UserSearchAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class SearchUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var adapter: UserSearchAdapter
    private lateinit var list: ArrayList<User>
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        getDisplayUsers()
        clickEvent()
    }

    private fun initValue() {
        list = ArrayList<User>()
        auth = Firebase.auth
    }

    private fun getDisplayUsers() {
        adapter = UserSearchAdapter(list, this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.userRV.layoutManager = linearLayoutManager
        binding.userRV.adapter = adapter
    }

    private fun clickEvent() {

        binding.CloseImg.setOnClickListener {
            if (binding.searchView.text.isNotEmpty()) binding.searchView.setText("")
        }

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.CloseImg.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim().isNotEmpty()) {
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                searchUsers(s.toString().trim())
                            }
                        }
                    }, 1000)
                } else {
                    binding.CloseImg.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun searchUsers(s: String) {
        //   Toast.makeText(this, "searchUsers", Toast.LENGTH_SHORT).show()
        list.clear()
        DBReference.userRef.orderByChild("name").equalTo(s)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("searchUsers", "searchUsers " + snapshot.value.toString())
                    val profile = snapshot.getValue(User::class.java)
                    val name = snapshot.child("name").value.toString().trim()
                    Log.d("searchUsers", "name $name")
                    profile?.let { list.add(it) }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
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
                .child(auth?.uid.toString())
                .child("UserState")
                .setValue(activeStatus)
        }
    }


/*    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.search_menu_item, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //query
                return false
            }
        })
        return true
    }*/
}