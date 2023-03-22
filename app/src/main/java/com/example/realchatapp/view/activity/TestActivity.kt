package com.example.realchatapp.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchatapp.databinding.ActivityTestBinding
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.view.adapter.MessagePagingAdapter
import com.firebase.ui.database.paging.DatabasePagingOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var adapter: MessagePagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.bind(binding.root)
        setContentView(binding.root)

        val config = PagingConfig(
            20,
            10,
            false
        )

        val options = DatabasePagingOptions.Builder<Messages>()
            .setLifecycleOwner(this)
            .setQuery(DBReference.userRef, config, Messages::class.java)
            .build()

        adapter = MessagePagingAdapter(options)
        binding.MRV.layoutManager = LinearLayoutManager(this)
        binding.MRV.adapter = adapter


        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Error -> {
                    }
                    is LoadState.Loading -> {
                    }
                    else -> {}
                }

                when (loadStates.append) {
                    is LoadState.Error -> {
                        // The additional load failed. Call the retry() method
                        // in order to retry the load operation.
                        // ...
                    }
                    is LoadState.Loading -> {
                        // The adapter has started to load an additional page
                        // ...
                    }
                    is LoadState.NotLoading -> {
                        if (loadStates.append.endOfPaginationReached) {
                            // The adapter has finished loading all of the data set
                            // ...
                        }
                        if (loadStates.refresh is LoadState.NotLoading) {
                            // The previous load (either initial or additional) completed
                            // ...
                        }
                    }
                }
            }
        }


    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
}