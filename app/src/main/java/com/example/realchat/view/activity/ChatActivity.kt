@file:Suppress("DEPRECATION")

package com.example.realchat.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.databinding.ActivityChatBinding
import com.example.realchat.helper.room.MessageDB
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.ActiveStatus
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.utils.Constant
import com.example.realchat.utils.DBReference
import com.example.realchat.utils.Utils
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.MessageAdapter
import com.example.realchat.view.adapter.OneToOneMessageAdapter
import com.example.realchat.viewModel.ChatViewModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageDB: MessageDB

    private lateinit var messageReceiverId: String
    private var getMessageReceiverName: String = ""
    private var messageReceiverImage: String? = null
    private var messageSenderId: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference

    private val messagesList = ArrayList<Messages>()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var adapter: OneToOneMessageAdapter
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>

    private var fileType = ""
    private var fileuri: Uri? = null
    private lateinit var type: KeyBordType

    var totalITem = 0
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var itemLoadCount = 21
    private var isLoading: Boolean = false
    private var isMaxData: Boolean = false
    private var lastNode = ""
    private var lastKey = ""
    private var targetKeyNode = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        clickEvent()
        getTotalITem()
        //queryMessageTest()
        //getMessage()
        //getMessageByFilter()
        getFilterMessage()
        getKeyBordTypeStatus()
        getOnlineStatus()
        displayLastSeen()
    }

    private fun queryMessageTest() {
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .endAt(targetKeyNode)
            .limitToLast(10)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.loader.visibility = View.GONE
                    val newMessageList = kotlin.collections.ArrayList<Messages>()
                    for (message in snapshot.children) {
                        val messageItem = message.getValue(Messages::class.java)
                        Log.d("MessageDBug", "" + messageItem?.message.toString())
                        message.getValue(Messages::class.java)?.let { newMessageList.add(it) }
                    }
                    val targetKey = newMessageList[0].messageID
                    Log.d("MessageDBug", "targetKey : " + targetKey)
                    newMessageList.removeAt(newMessageList.size - 1)
                    Log.d("MessageDBug", "" + newMessageList.toString())
                    newMessageList.reverse()
                    messageAdapter.addNewMessageList(newMessageList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getFilterMessage() {
        messageAdapter = MessageAdapter(messagesList)
        binding.messageRV.layoutManager = linearLayoutManager
        binding.messageRV.adapter = messageAdapter

        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val messages = dataSnapshot.getValue(Messages::class.java)
                if (messages != null) {
                    messagesList.add(messages)
                    Log.d("getMessageByFilter", "Message Item Size: " + messagesList.size)
                }
                lastKey = messageAdapter.getLastItemId()
                targetKeyNode = messagesList[0].messageID
                Log.d("getMessageByFilter", "MessageID Item key : $lastKey")
                messageAdapter.notifyDataSetChanged()
                binding.messageRV.smoothScrollToPosition(binding.messageRV.adapter!!.itemCount)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getTotalITem() {
        runBlocking {
            DBReference.messageRef
                .child(messageSenderId!!)
                .child(messageReceiverId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        totalITem = snapshot.childrenCount.toInt()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }

    private fun getMessageByFilter() {

/*        getFirstKey()
        getMessageList()*/

        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val messages = dataSnapshot.getValue(Messages::class.java)
                if (messages != null) {
                    messagesList.add(messages)
                    Log.d("getMessageByFilter", "Message Item Size: " + messagesList.size)
                }

                lastKey = messageAdapter.getLastItemId().toString()

                messageAdapter.notifyDataSetChanged()
                binding.messageRV.smoothScrollToPosition(binding.messageRV.adapter!!.itemCount)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        messageAdapter = MessageAdapter(messagesList)
        binding.messageRV.layoutManager = linearLayoutManager
        binding.messageRV.adapter = messageAdapter

    }

    private fun getMessageList() {
        var query: Query
        if (!isMaxData) {
            if (TextUtils.isEmpty(lastNode)) {
                query = DBReference.messageRef
                    .child(messageSenderId!!)
                    .child(messageReceiverId)
                    .orderByKey()
                    .limitToFirst(itemLoadCount)

            } else {
                query = DBReference.messageRef
                    .child(messageSenderId!!)
                    .child(messageReceiverId)
                    .orderByKey()
                    .startAt(lastNode)
                    .limitToFirst(itemLoadCount)
            }

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        val newMessageList = ArrayList<Messages>()
                        for (items in snapshot.children) {
                            snapshot.getValue(Messages::class.java)?.let { newMessageList.add(it) }
                        }
                        lastNode = newMessageList[newMessageList.size - 1].messageID
                        messageAdapter.addNewMessageList(newMessageList)
                        if (lastNode != lastKey) {
                            newMessageList.removeAt(newMessageList.size - 1)
                        } else {
                            lastNode = "end"
                        }

                        messageAdapter.addNewMessageList(newMessageList)
                        isLoading = false
                    } else {
                        isLoading = false
                        isMaxData = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

    }

    private fun getFirstKey() {
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .limitToLast(1)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (items in snapshot.children) {
                    lastKey = items.key.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getKeyBordTypeStatus() {
        DBReference.userRef
            .child(messageReceiverId)
            .child("TypeState")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        val status = snapshot.child("typing").value
                        if (status as Boolean) {
                            binding.onlineOfflineTxt.text = "typing.."
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun getOnlineStatus() {
        chatViewModel.getOnlineStatus(receiverId = messageReceiverId)
            .observe(this) {
                if (it.equals("online")) {
                    binding.onlineOfflineTxt.text = it
                } else {
                    binding.onlineOfflineTxt.text = it
                }
            }
    }

    private fun getMessage() {
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .limitToLast(10)

        val options = FirebaseRecyclerOptions.Builder<Messages>()
            .setQuery(query, Messages::class.java)
            .build()

        adapter = OneToOneMessageAdapter(options)
        binding.messageRV.layoutManager = LinearLayoutManager(this)
        binding.messageRV.adapter = adapter


/*        messageAdapter = MessageAdapter(messagesList)
        binding.messageRV.layoutManager = LinearLayoutManager(this)
        binding.messageRV.adapter = messageAdapter*/

/*
        DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .addChildEventListener(object : ChildEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val messages = dataSnapshot.getValue(Messages::class.java)
                    if (messages != null) {
                        messagesList.add(messages)
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.messageRV.smoothScrollToPosition(binding.messageRV.adapter!!.itemCount)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })*/
    }

    private fun initValue() {
        type = KeyBordType()
        auth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        messageDB = MessageDB.getInstance(this)!!
        messageSenderId = auth.currentUser?.uid
        messageReceiverId = intent.getStringExtra("visit_user_id").toString()
        getMessageReceiverName = intent.getStringExtra("visit_user_name").toString()
        messageReceiverImage = intent.getStringExtra("visit_image").toString()
        binding.profileNameTxt.text = getMessageReceiverName

        linearLayoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickEvent() {
/*        binding.customProfileImage.setOnClickListener {
            isMaxData = false
            lastNode = messageAdapter.getLastItemId().toString()
            messageAdapter.removedLastItem()
            messageAdapter.notifyDataSetChanged()
            getFirstKey()
            getMessageList()
        }*/

        binding.sendMessageBtn.setOnClickListener { sendTextMessage() }
        binding.sendFilesBtn.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Images", "PDF Files", "Ms Word Files"
            )
            val builder = AlertDialog.Builder(this@ChatActivity)
            builder.setTitle("Select File")
            builder.setItems(options) { _, which ->
                if (which == 0) {
                    fileType = "image"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), 555)
                } else if (which == 1) {
                    fileType = "pdf"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/pdf"
                    startActivityForResult(Intent.createChooser(intent, "Select PDF File"), 555)
                } else if (which == 2) {
                    fileType = "docx"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/msword"
                    startActivityForResult(Intent.createChooser(intent, "Select Ms Word File"), 555)
                }
            }
            builder.show()
        }
        binding.inputMessages.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                type.typing = true
                chatViewModel.updateTypingStatus(type)
            }

            override fun afterTextChanged(s: Editable?) {
                type.typing = false
                chatViewModel.updateTypingStatus(type)
            }
        })

        binding.messageRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.loader.visibility = View.VISIBLE

                    queryMessageTest()

                }
/*                totalItem = linearLayoutManager.itemCount
                lastVisibleITem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItem <= (lastVisibleITem + itemLoadCount)) {
                    getMessageList()
                    isLoading = true
                }*/
            }
        })

/*        binding.inputMessages.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_DOWN == event.action) {
                messagesList.clear()
                messageAdapter.notifyDataSetChanged()
                getMessageByFilter()
            }
            false
        }*/
    }

    private fun getFetchMessage() {

        //messagesList.clear()
        // litmitITemLast += 10
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .endAt(lastKey)
            .limitToLast(10)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (message in snapshot.children) {
                        message.getValue(Messages::class.java)?.let { messagesList.add(it) }
                    }

                    messageAdapter.addNewMessageList(messagesList)

/*                    messageAdapter = MessageAdapter(messagesList)
                    binding.messageRV.layoutManager = LinearLayoutManager(this@ChatActivity)
                    binding.messageRV.adapter = messageAdapter*/
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                fileuri = data.data
                if (fileType != "image") {
                    sendDocMessage()
                } else if (fileType.equals("image", false)) {
                    sendImageSend()
                } else {
                    Toast.makeText(this, "please select file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendImageSend() {
        val messagePushID = Validator.getSingleChatMsgPushKey(messageReceiverId)
        val filepath = DBReference.storageRef.child("$messagePushID.jpg")
        uploadTask = filepath.putFile(fileuri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            filepath.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val messages = Messages(
                    messageSenderId.toString(),
                    task.result.toString(),
                    Constant.MESSAGE_TYPE_IMAGE,
                    messageReceiverId,
                    "",
                    Validator.getCurrentTime(),
                    Validator.getCurrentDate(),
                )
                chatViewModel.sendMessage(messageDB, messages, messageReceiverId)
                binding.inputMessages.setText("")
            }
        }
    }

    private fun sendDocMessage() {
        val messagePushID = Validator.getSingleChatMsgPushKey(messageReceiverId)
        val filepath = DBReference.storageDocRef.child("$messagePushID.$fileType")
        filepath.putFile(fileuri!!)
            .addOnSuccessListener {
                filepath.downloadUrl.addOnSuccessListener { uri ->
                    val messages = Messages(
                        messageSenderId.toString(),
                        uri.toString(),
                        fileType,
                        messageReceiverId,
                        "",
                        Validator.getCurrentTime(),
                        Validator.getCurrentDate(),
                    )
                    chatViewModel.sendMessage(messageDB, messages, messageReceiverId)
                    binding.inputMessages.setText("")
                }
            }
    }

    private fun displayLastSeen() {
        DBReference.userRef.child(messageReceiverId)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("UserState").hasChild("state")) {
                        val state = dataSnapshot.child("UserState").child("state").value.toString()
                        val date = dataSnapshot.child("UserState").child("date").value.toString()
                        val time = dataSnapshot.child("UserState").child("time").value.toString()
                        if (state == "online") {
                            binding.onlineOfflineTxt.text = "online"
                        } else if (state == "offline") {
                            binding.onlineOfflineTxt.text = "last seen: $date - $time"
                        }
                    } else {
                        binding.onlineOfflineTxt.text = "offline"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    private fun sendTextMessage() {
        val messages = Messages(
            messageSenderId.toString(),
            binding.inputMessages.text.toString(),
            "text",
            messageReceiverId,
            "",
            Validator.getCurrentTime(),
            Validator.getCurrentDate(),
        )
        chatViewModel.sendMessage(messageDB, messages, messageReceiverId)
        binding.inputMessages.setText("")
    }

    override fun onStart() {
        super.onStart()
        //adapter.startListening()

        userStatusUpdate("online")
    }

    override fun onStop() {
        super.onStop()
        //adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        userStatusUpdate("online")
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