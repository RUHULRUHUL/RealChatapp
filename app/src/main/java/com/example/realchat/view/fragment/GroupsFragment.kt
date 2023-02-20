package com.example.realchat.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.realchat.R
import com.example.realchat.databinding.FragmentGroupsBinding
import com.example.realchat.view.activity.GroupCreateActivity

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(layoutInflater)

        binding.createGroup.setOnClickListener {
            startActivity(Intent(requireContext(), GroupCreateActivity::class.java))
        }

        return binding.root
    }
}