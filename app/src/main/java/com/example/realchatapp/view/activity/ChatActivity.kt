@file:Suppress("DEPRECATION")

package com.example.realchatapp.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realchatapp.databinding.ActivityChatBinding
import com.example.realchatapp.helper.room.MessageDB
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.model.profile.ActiveStatus
import com.example.realchatapp.model.profile.KeyBordType
import com.example.realchatapp.utils.Constant
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Utils
import com.example.realchatapp.utils.Validator
import com.example.realchatapp.view.adapter.MessageAdapter
import com.example.realchatapp.view.adapter.MessagePagingAdapter
import com.example.realchatapp.viewModel.ChatViewModel
import com.firebase.ui.database.paging.DatabasePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
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
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var adapter: MessagePagingAdapter
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>
    private var fileType = ""
    private var fileuri: Uri? = null
    private lateinit var type: KeyBordType
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val messagesList = ArrayList<Messages>()
    private val allMessageList = ArrayList<Messages>()
    private var chatMsgFromRoom = ArrayList<Messages>()
    private lateinit var job: Job
    private var prevKey = ""
    private var lastKey = ""
    private var itemPosition = 0
    private var topMessageKey = ""

    private var loadMorePageStatus = false
    private var isLoading: Boolean = false

    private var lastMessageId = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        clickEvent()
        getTopKey()
        //paginationData()
        // getAllMessageFetch()
        //getMessageFromRoomDB()
        loadMessage()
        //loadMessage1()
        getKeyBordTypeStatus()
        getOnlineStatus()
        displayLastSeen()
    }

    private fun paginationData() {
        val config = PagingConfig(
            20,
            10,
            false
        )

        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)

        val options = DatabasePagingOptions.Builder<Messages>()
            .setLifecycleOwner(this)
            .setQuery(query, config, Messages::class.java)
            .build()

        adapter = MessagePagingAdapter(options)
        binding.messageRV.layoutManager = LinearLayoutManager(this)
        binding.messageRV.adapter = adapter

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


    private fun getTopKey() {
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .limitToFirst(1)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val message = item.getValue(Messages::class.java)
                    topMessageKey = message?.messageID.toString()
                    Log.d("TopKey", "Top key $topMessageKey")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun getAllMessageFetch() {
        job = GlobalScope.launch {
            val query = DBReference.messageRef
                .child(messageSenderId!!)
                .child(messageReceiverId)
                .orderByChild("date")
                .equalTo("11/03/2023")

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allMessageList.clear()
                    if (snapshot.exists()) {

                        Validator.showToast(this@ChatActivity, "getAllMessageFetch")

                        for (item in snapshot.children) {
                            val message = item.getValue(Messages::class.java)
                            Log.d(
                                "getAllMessageFetch",
                                "message: ${message?.message} date: ${message?.date}"
                            )
                            message?.let { allMessageList.add(it) }
                        }
                        allMessageList.reverse()
                        Log.d(
                            "MessageDBug",
                            "getAllMessageFetch list size:-${allMessageList.size}  thread Name  " + Thread.currentThread().name
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            // Log.d("MessageDBug", "getAllMessageFetch- thread Name " + Thread.currentThread().name)
                            messageDB.messageDao().deleteAllMessages()
                            if (messageDB.messageDao().getMessageList()
                                    .isEmpty() && allMessageList.isNotEmpty()
                            ) {
                                Log.d(
                                    "MessageDBug",
                                    "getAllMessageFetch insert list - thread Name " + Thread.currentThread().name
                                )
                                messageDB.messageDao().insertAllMessage(allMessageList)
                            }
                        }

                        for (item in allMessageList) {
                            Log.d(
                                "getAllMessageFetch",
                                "reverse List: ${item.message} date: ${item.date}"
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun getDateWiseChats(date: String) {
        binding.loader.visibility = View.VISIBLE
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByChild("date")
            .equalTo(date)
            .limitToLast(20)

        val chatList = ArrayList<Messages>()
        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()) {
                    binding.loader.visibility = View.GONE
                    // chatList.clear()
                    val message = snapshot.getValue(Messages::class.java)
                    if (message != null) {
                        messagesList.add(itemPosition++, message)
                        Log.d("MessageDateBug", "item -- ${message.message}")
                    }
                    messageAdapter.notifyDataSetChanged()
/*                    for (item in snapshot.children) {
                        val message = item.getValue(Messages::class.java)
                        message?.let { chatList.add(it) }
                        if (message != null) {
                            Log.d("DateWiseItem", "item -- ${message.message}")
                        }
                    }
                    for (i in 0 until chatList.size) {
                        messagesList.add(0, chatList[i])
                    }*/

                    // Validator.showToast(this@ChatActivity, "notifyDataSetChanged")

                } else {
                    Validator.showToast(this@ChatActivity, "No Data Found")
                    binding.loader.visibility = View.GONE
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        linearLayoutManager.scrollToPositionWithOffset(10, 0)


/*        query.addChildEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.loader.visibility = View.GONE
                    chatList.clear()
                    runBlocking {
                        for (item in snapshot.children) {
                            val message = item.getValue(Messages::class.java)
                            message?.let { chatList.add(it) }
                            if (message != null) {
                                Log.d("DateWiseItem", "item -- ${message.message}")
                            }
                        }
                        for (i in 0 until chatList.size) {
                            messagesList.add(0, chatList[i])
                        }

                        Validator.showToast(this@ChatActivity, "notifyDataSetChanged")
                        messageAdapter.notifyDataSetChanged()
                        linearLayoutManager.scrollToPositionWithOffset(messagesList.size - 1, 0)
                    }

                } else {
                    Validator.showToast(this@ChatActivity, "No Data Found")
                    binding.loader.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.loader.visibility = View.GONE
                Validator.showToast(this@ChatActivity, "Error: $error")
            }

        })*/
    }

    private fun getMessageFromRoomDB() {
        lifecycleScope.launch {
            chatMsgFromRoom.clear()
            chatMsgFromRoom = messageDB.messageDao().getAllChatMsgList() as ArrayList<Messages>
        }
    }


    private fun loadMessage() {
        binding.loader.visibility = View.VISIBLE
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                binding.loader.visibility = View.GONE
                val message = dataSnapshot.getValue(Messages::class.java)
                message?.let {
                    //itemPosition++
/*                    if (itemPosition == 1) {
                        val messageKey = dataSnapshot.key
                        lastKey = messageKey.toString()
                        prevKey = messageKey.toString()
                    }
                    messagesList.add(message)
                    messageAdapter.notifyDataSetChanged()
                    binding.messageRV.scrollToPosition(messagesList.size - 1)*/
                    if (!loadMorePageStatus) {
                        Validator.showToast(this@ChatActivity, "last KEy")
                        itemPosition++
                        if (itemPosition == 1) {
                            lastKey = dataSnapshot.key.toString()
                            prevKey = dataSnapshot.key.toString()
                        }
                        messagesList.add(message)
                        messageAdapter.notifyDataSetChanged()
                        binding.messageRV.scrollToPosition(messagesList.size - 1)
                    } else {
                        Validator.showToast(this@ChatActivity, "prev last KEy")
                        messagesList.add(message)
                        messageAdapter.notifyDataSetChanged()
                        binding.messageRV.scrollToPosition(messagesList.size - 1)
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    private fun loadMoreMessage() {
        prevKey = lastKey
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByKey()
            .endAt(lastKey)
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val messages = dataSnapshot.getValue(Messages::class.java)
                messages?.let {
                    binding.loader.visibility = View.GONE
                    val messageKey = dataSnapshot.key
                    if (prevKey != messageKey) messagesList.add(itemPosition++, messages)
                    if (itemPosition == 1) lastKey = dataSnapshot.key.toString()

                    messageAdapter.notifyDataSetChanged()
                    linearLayoutManager.scrollToPositionWithOffset(8, 0)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadMessage1() {
        binding.loader.visibility = View.VISIBLE
        val query = DBReference.messageRef
            .child(messageSenderId!!)
            .child(messageReceiverId)
            .orderByChild("date")
            .equalTo(Validator.getCurrentDate())

        Log.d("MessageDateBug", "item -- ${Validator.getCurrentDate()}")

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                binding.loader.visibility = View.GONE
                val totalItem = dataSnapshot.childrenCount
                Log.d("childrenCount", "onChildAdded $totalItem")
                val messages = dataSnapshot.getValue(Messages::class.java)
                messages?.let {
                    messagesList.add(messages)
                    Log.d("onChildAdded", "onChildAdded " + messages.message)
                    Log.d(
                        "FilterData",
                        "item -:  ${messages.message} -: ${messages.date} "
                    )
                    Log.d("MessageDateBug", "item -- ${messages.message}")
                    messageAdapter.notifyDataSetChanged()
                    Log.d("addChildEventListener", "notify " + messages.message)
                    binding.messageRV.scrollToPosition(messagesList.size - 1)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreMessage1() {
        binding.loader.visibility = View.VISIBLE
        val starSize = messagesList.size
        val limitSize = messagesList.size + 10
        for (i in starSize..limitSize) {
            if (i == chatMsgFromRoom.size) {
                Validator.showToast(this, "No More Data Found")
                break
            } else {
                messagesList.add(0, chatMsgFromRoom[i])
                Log.d(
                    "FilterData",
                    "item -:  ${chatMsgFromRoom[i].message} -: ${chatMsgFromRoom[i].date} "
                )
            }
        }
        messageAdapter.notifyDataSetChanged()
        linearLayoutManager.scrollToPositionWithOffset(8, 15)
        binding.loader.visibility = View.GONE
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

        messageAdapter = MessageAdapter(messagesList)
        binding.messageRV.layoutManager = linearLayoutManager
        binding.messageRV.setHasFixedSize(true)
        binding.messageRV.adapter = messageAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickEvent() {
        var day = 1
/*        binding.messageRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
*//*                        if (chatMsgFromRoom.size > 0 && messagesList.size != chatMsgFromRoom.size) {
                            Log.d("MessageDateBug", "Date  " + Validator.getDateWiseFilter(day++))
                            loadMoreMessage1()
                        } else {
                            Validator.showToast(this@ChatActivity, "No More Data")
                        }*//*
                    Log.d("MessageDateBug", "Date  " + Validator.getDateWiseFilter(day))
                    itemPosition = 0
                    getDateWiseChats(Validator.getDateWiseFilter(day).toString())
                    day++
                }
            }
        })*/

        binding.messageRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastKey == topMessageKey) {
                        Validator.showToast(this@ChatActivity, "No More Data")
                    } else {
                        binding.loader.visibility = View.VISIBLE
                        itemPosition = 0
                        loadMoreMessage()
                    }

                    if (!isLoading) {
                        if (lastKey == topMessageKey) {
                            Log.d(
                                "topLastMessageKey",
                                "rich to top no more data lastKEy $lastKey topKey: $topMessageKey are the same"
                            )
                            Validator.showToast(this@ChatActivity, "No More Data")
                        } else {
                            binding.loader.visibility = View.VISIBLE
                            isLoading = true
                            object : CountDownTimer(2000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                }

                                override fun onFinish() {
                                    loadMoreMessage()
                                }
                            }.start()
                        }
                    } else {
                        Validator.showToast(this@ChatActivity, "Please wait for loading...")
                    }

                }
            }
        })

        binding.sendMessageBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MessageDBug", "insert- thread Name " + Thread.currentThread().name)
                sendTextMessage()
            }
            binding.inputMessages.setText("")

        }
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
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                type.typing = true
                chatViewModel.updateTypingStatus(type)
            }

            override fun afterTextChanged(s: Editable?) {
                type.typing = false
                chatViewModel.updateTypingStatus(type)
            }
        })

        binding.inputMessages.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_DOWN == event.action) {
                if (messagesList.size > 0) {
                    binding.messageRV.scrollToPosition(messagesList.size - 1)
                }
            }
            false
        }

    }

    private fun addHistoryMessage(message: Messages) {
        Firebase.firestore.collection("history")
            .add(message)
            .addOnSuccessListener {
                lastMessageId = it.id
                Log.d("sendTxtMessage", "Message save FireStore")
            }
    }

    private fun updateHistoryMessage(message: Messages) {
        Firebase.firestore.collection("history")
            .document(lastMessageId)
            .update(
                "message", message.message,
                "time", message.time,
                "messageID", message.messageID

            )
            .addOnSuccessListener {
                Log.d("sendTxtMessage", "Message save FireStore")
            }
    }

    private fun  checkConversion(senderId:String,receiverId:String){
        Firebase.firestore.collection("history")
            .whereEqualTo("from",senderId)
            .whereEqualTo("to",receiverId)
            .get()
            .addOnCompleteListener {

            }
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
                        val state =
                            dataSnapshot.child("UserState").child("state").value.toString()
                        val date =
                            dataSnapshot.child("UserState").child("date").value.toString()
                        val time =
                            dataSnapshot.child("UserState").child("time").value.toString()
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
        if (lastMessageId.equals("", false)) {
            addHistoryMessage(message = messages)
        } else {
            updateHistoryMessage(messages)
        }

    }

/*    private fun sendTextMessage() {
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
    }*/

    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")
    }

    override fun onStop() {
        super.onStop()
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