package com.example.realchat.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.realchat.databinding.FragmentUserProfileBinding
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.utils.Validator
import com.example.realchat.view.authUi.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        initValue()
        clickEvent()
        return binding.root
    }

    private fun initValue() {
        auth = Firebase.auth
        database = Firebase.database.reference
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

        binding.updateButton.setOnClickListener {
            if (Validator.inputFieldValidation(binding.userNameET, "Provide Name")
                && Validator.inputFieldValidation(binding.statusET, "Provide Name")
                && Validator.validatePhone(binding.mobileET, "provide correct mobile number")) {
                val userProfile = UserProfile(
                    Validator.getValeFromEdiText(binding.userNameET),
                    Validator.getValeFromEdiText(binding.statusET),
                    Validator.getValeFromEdiText(binding.mobileET),
                )
                database.child("Users").child(auth.uid.toString()).setValue(userProfile)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Validator.showToast(requireContext(), "update successfully")
                        }
                    }
            }
        }
    }
}