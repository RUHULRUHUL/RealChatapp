package com.example.realchatapp.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.realchatapp.databinding.FragmentGroupsBinding
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Validator
import com.example.realchatapp.view.activity.GroupChatActivity
import com.example.realchatapp.viewModel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private lateinit var groupViewModel: GroupViewModel

    private var arrayAdapter: ArrayAdapter<String>? = null
    private val groupList = ArrayList<String>()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(layoutInflater)
        initFields()
        clickEvent()
        displayMyGroups()
        return binding.root
    }

    private fun clickEvent() {
        binding.createGroup.setOnClickListener { groupCreate() }
        binding.listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val currentGroupName = groupList[position].toString().trim()
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra("groupName", currentGroupName)
                startActivity(intent)
            }
    }

    private fun initFields() {
        binding.mainAppBar.title = "Groups List"
        auth = FirebaseAuth.getInstance()
        groupViewModel = ViewModelProvider(requireActivity())[GroupViewModel::class.java]

        arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            groupList
        )
        binding.listView.adapter = arrayAdapter
    }

    private fun displayMyGroups() {
        auth.currentUser?.let {
            DBReference.myGroupRef.child(it.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val set = ArrayList<String>()
                        val iterator = dataSnapshot.children.iterator()
                        while (iterator.hasNext()) {
                            (iterator.next() as DataSnapshot).key?.let { set.add(it) }
                        }
                        groupList.clear()
                        groupList.addAll(set)
                        arrayAdapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }

    private fun groupCreate() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Group Create")
        val groupNameField = EditText(requireContext())
        groupNameField.hint = "group name"
        builder.setView(groupNameField)
        builder.setPositiveButton(
            "Create"
        ) { _, _ ->
            val groupName = groupNameField.text.toString().trim()
            if (TextUtils.isEmpty(groupName)) {
                Validator.showToast(requireContext(), "Please write Group Name...")
            } else {
                createNewGroup(groupName)
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, i -> dialogInterface.cancel() }
        builder.show()
    }

    private fun createNewGroup(groupName: String) {
        groupViewModel.groupCreate(groupName)
            .observe(requireActivity()) {
                if (it) {
                    Validator.showToast(requireContext(), "group create success")
                }
            }
    }


}