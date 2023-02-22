package com.example.realchat.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.realchat.databinding.FragmentGroupsBinding
import com.example.realchat.utils.DBReference
import com.example.realchat.view.activity.GroupCreateActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private var arrayAdapter: ArrayAdapter<String>? = null
    private val list_of_groups = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(layoutInflater)
        binding.mainAppBar.title = "Groups List"
        clickEvent()
        IntializeFields()
        RetrieveAndDisplayGroups()
        return binding.root
    }

    private fun clickEvent() {

        binding.createGroup.setOnClickListener {
            RequestNewGroup()
        }

        binding.listView.onItemClickListener =
            OnItemClickListener { adapterView, _, position, _ ->
                val currentGroupName = adapterView.getItemAtPosition(position).toString()
                val intent = Intent(context, GroupCreateActivity::class.java)
                intent.putExtra("groupName", currentGroupName)
                startActivity(intent)
            }

    }

    private fun IntializeFields() {
        arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            list_of_groups
        )
        binding.listView.adapter = arrayAdapter
    }

    private fun RetrieveAndDisplayGroups() {
        DBReference.groupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val set = ArrayList<String>()
                val iterator = dataSnapshot.children.iterator()
                while (iterator.hasNext()) {
                    (iterator.next() as DataSnapshot).key?.let { set.add(it) }
                }
                list_of_groups.clear()
                list_of_groups.addAll(set)
                arrayAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun RequestNewGroup() {
        val builder = AlertDialog.Builder(requireContext())
        val groupNameField = EditText(requireContext())
        groupNameField.hint = "group name"
        builder.setView(groupNameField)
        builder.setPositiveButton(
            "Create"
        ) { _, _ ->
            val groupName = groupNameField.text.toString().trim()
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(requireContext(), "Please write Group Name...", Toast.LENGTH_SHORT)
                    .show()
            } else {
                CreateNewGroup(groupName)
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, i -> dialogInterface.cancel() }
        builder.show()
    }

    private fun CreateNewGroup(groupName: String) {
        DBReference.groupRef.child(groupName).setValue("")
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "$groupName group is Created Successfully...", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


}