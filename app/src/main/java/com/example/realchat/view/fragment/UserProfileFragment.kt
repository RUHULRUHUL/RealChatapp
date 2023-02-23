package com.example.realchat.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.realchat.databinding.FragmentUserProfileBinding
import com.example.realchat.model.profile.User
import com.example.realchat.utils.Validator
import com.example.realchat.view.activity.FriendRequestActivity
import com.example.realchat.view.authUi.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var ref: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        binding.toolbar.title = "Profile Setting"
        initValue()
        clickEvent()
        getProfileData()
        return binding.root
    }

    private fun getProfileData() {
        auth.currentUser?.let {
            ref.child("Users").child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            val name = snapshot.child("name").value.toString().trim()
                            val phone = snapshot.child("phone").value.toString().trim()
                            val status = snapshot.child("status").value.toString().trim()
                            binding.userNameET.setText(name)
                            binding.statusET.setText(status)
                            binding.mobileET.setText(phone)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Validator.showToast(requireContext(), "onCancelled")
                    }

                })
        }

    }

    private fun initValue() {
        auth = Firebase.auth
        ref = Firebase.database.reference
    }

    private fun clickEvent() {
        binding.logOut.setOnClickListener {
            if (auth.currentUser != null) {
                Firebase.auth.signOut()
                val i = Intent(requireContext(), SignInActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
            }
        }

        binding.requestShowBtn.setOnClickListener {
            startActivity(Intent(requireContext(), FriendRequestActivity::class.java))
        }

        binding.updateButton.setOnClickListener {
            if (Validator.inputFieldValidation(binding.userNameET, "Provide Name")
                && Validator.inputFieldValidation(binding.statusET, "Provide Name")
                && Validator.validatePhone(binding.mobileET, "provide correct mobile number")
            ) {
                val user = User(
                    Validator.getValeFromEdiText(binding.userNameET),
                    auth.uid.toString(),
                    Validator.getValeFromEdiText(binding.statusET),
                    Validator.getValeFromEdiText(binding.mobileET),
                )
                ref.child("Users").child(auth.uid.toString()).setValue(user)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Validator.showToast(requireContext(), "update successfully")
                        }
                    }
            }
        }
    }
}