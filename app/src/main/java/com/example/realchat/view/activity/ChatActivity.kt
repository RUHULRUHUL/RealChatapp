@file:Suppress("DEPRECATION")

package com.example.realchat.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.ActivityChatBinding
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.ActiveStatus
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.utils.DBReference
import com.example.realchat.utils.Utils
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.MessageAdapter
import com.example.realchat.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var messageReceiverId: String
    private var getMessageReceiverName: String = ""
    private var messagereceiverimage: String? = null
    private var messageSenderId: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference
    private val messagesList = ArrayList<Messages>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>

    private var fileType = ""
    private var myUrl = ""
    private var fileuri: Uri? = null
    private var loadingBar: ProgressDialog? = null
    private lateinit var type: KeyBordType

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        userStatusUpdate("online")
        clickEvent()
        getMessage()
        getKeyBordTypeStatus()
        getOnlineStatus()
        displayLastSeen()
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
        messageAdapter = MessageAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessageListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessageListOfUsers.adapter = messageAdapter

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
                    binding.privateMessageListOfUsers.smoothScrollToPosition(binding.privateMessageListOfUsers.adapter!!.itemCount)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun initValue() {
        type = KeyBordType()
        auth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        loadingBar = ProgressDialog(this)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        loadingBar = ProgressDialog(this)
        messageSenderId = auth.currentUser?.uid

        messageReceiverId = intent.getStringExtra("visit_user_id").toString()
        getMessageReceiverName = intent.getStringExtra("visit_user_name").toString()
        messagereceiverimage = intent.getStringExtra("visit_image").toString()
        binding.profileNameTxt.text = getMessageReceiverName
    }

    private fun clickEvent() {
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
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                loadingBar!!.setTitle("Sending File")
                loadingBar!!.setMessage("please wait, we are sending that file...")
                loadingBar!!.setCanceledOnTouchOutside(false)
                loadingBar!!.show()
                fileuri = data.data
                if (fileType != "image") {
                    sendDocMessage()
                } else if (fileType.equals("image", false)) {
                    sendImageSend()
                } else {
                    loadingBar!!.dismiss()
                    Toast.makeText(this, "please select file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendImageSend() {
        val storageReference =
            FirebaseStorage.getInstance().reference.child("Image Files")
        val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
        val messageReceiverRef =
            "Messages/$messageReceiverId/$messageSenderId"
        val userMessageKey =
            rootRef.child("Messages").child(messageSenderId!!).child(
                messageReceiverId
            ).push()
        val messagePushID = userMessageKey.key
        val filepath = storageReference.child("$messagePushID.jpg")
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
                val downloadUrl = task.result
                myUrl = downloadUrl.toString()
                val messageTextBody = HashMap<Any?, Any?>()
                messageTextBody["message"] = myUrl
                messageTextBody["name"] = fileuri!!.lastPathSegment
                messageTextBody["type"] = fileType
                messageTextBody["from"] = messageSenderId
                messageTextBody["to"] = messageReceiverId
                messageTextBody["messageID"] = messagePushID
                messageTextBody["time"] = Validator.getCurrentTime()
                messageTextBody["date"] = Validator.getCurrentDate()
                val messageBodyDetails: MutableMap<String, Any> = HashMap()
                messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                messageBodyDetails["$messageReceiverRef/$messagePushID"] =
                    messageTextBody
                rootRef.updateChildren(messageBodyDetails)
                    .addOnCompleteListener {
                        if (task.isSuccessful) {
                            loadingBar!!.dismiss()
                        } else {
                            loadingBar!!.dismiss()
                            Toast.makeText(
                                this@ChatActivity,
                                "Error:",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.inputMessages.setText("")
                    }.addOnCompleteListener {
                        if (task.isSuccessful) {
                            loadingBar!!.dismiss()
                        } else {
                            loadingBar!!.dismiss()
                            Toast.makeText(
                                this@ChatActivity,
                                "Error:",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.inputMessages.setText("")
                    }
            }
        }
    }

    private fun sendDocMessage() {
        val storageReference =
            FirebaseStorage.getInstance().reference.child("Document Files")
        val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
        val messageReceiverRef =
            "Messages/$messageReceiverId/$messageSenderId"
        val userMessageKeyRef =
            rootRef.child("Messages").child(messageSenderId!!).child(
                messageReceiverId
            ).push()
        val messagePushID = userMessageKeyRef.key
        val filepath = storageReference.child("$messagePushID.$fileType")
        filepath.putFile(fileuri!!).addOnSuccessListener {
            filepath.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                val messageDocsBody = HashMap<Any?, Any?>()
                messageDocsBody["message"] = downloadUrl
                messageDocsBody["name"] = fileuri!!.lastPathSegment
                messageDocsBody["type"] = fileType
                messageDocsBody["from"] = messageSenderId
                messageDocsBody["to"] = messageReceiverId
                messageDocsBody["messageID"] = messagePushID
                messageDocsBody["time"] = Validator.getCurrentTime()
                messageDocsBody["date"] = Validator.getCurrentDate()


                val messageBodyDDetail = HashMap<String, Any>()
                messageBodyDDetail["$messageSenderRef/$messagePushID"] = messageDocsBody
                messageBodyDDetail["$messageReceiverRef/$messagePushID"] =
                    messageDocsBody

                rootRef.updateChildren(messageBodyDDetail)
                loadingBar!!.dismiss()

            }.addOnFailureListener { e ->
                loadingBar!!.dismiss()
                Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnProgressListener { taskSnapshot ->
            val p = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            loadingBar!!.setMessage(p.toInt().toString() + " % Uploading...")
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
        chatViewModel.sendMessage(messages, messageReceiverId)
        binding.inputMessages.setText("")
    }

    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")
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