package com.example.realchat.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.realchat.R
import com.example.realchat.databinding.FragmentSearchContactBinding

class SearchContactFragment : Fragment() {
    private lateinit var binding: FragmentSearchContactBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchContactBinding.inflate(inflater)
        binding.toolbar.title = "Search User"
        return binding.root

    }

}