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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private var arrayAdapter: ArrayAdapter<String>? = null
    private val groupList = ArrayList<String>()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(layoutInflater)
        binding.mainAppBar.title = "Groups List"

        auth = FirebaseAuth.getInstance()

        clickEvent()
        IntializeFields()
        displayMyGroups()
        return binding.root
    }

    private fun clickEvent() {

        binding.createGroup.setOnClickListener {
            RequestNewGroup()
        }

        binding.listView.onItemClickListener =
            OnItemClickListener { adapterView, _, position, _ ->
                val currentGroupName = groupList[position].toString().trim()
                val intent = Intent(context, GroupCreateActivity::class.java)
                intent.putExtra("groupName",currentGroupName)
                startActivity(intent)
            }

    }

    private fun IntializeFields() {
        arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            groupList
        )
        binding.listView.adapter = arrayAdapter
    }

    private fun displayMyGroups() {
/*        DBReference.groupRef.addValueEventListener(object : ValueEventListener {
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
        })*/

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

    private fun RequestNewGroup() {
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
                Toast.makeText(requireContext(), "Please write Group Name...", Toast.LENGTH_SHORT)
                    .show()
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
        DBReference.groupRef.child(groupName).setValue("")
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let {
                        DBReference.myGroupRef.child(it.uid).child(groupName)
                            .setValue("").addOnCompleteListener {
                                if (it.isSuccessful) Toast.makeText(
                                    requireContext(),
                                    "group create success",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            })
    }


}