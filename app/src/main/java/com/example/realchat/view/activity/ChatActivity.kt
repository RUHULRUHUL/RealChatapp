package com.example.realchat.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var messageRecieverId: String
    private var getMessageRecievername: String = ""
    private var messagereceiverimage: String? = null
    private var messageSenderId: String? = null

    private var mauth: FirebaseAuth? = null
    private lateinit var RootRef: DatabaseReference
    private val messagesList = ArrayList<Messages>()
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>


    private var savecurrentTime: String? = null
    private var savecurrentDate: String? = null
    private var checker = ""
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
        clickEvent()
        getMessage()
        Displaylastseen()

    }

    private fun getMessage() {

        messageAdapter = MessageAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessageListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessageListOfUsers.adapter = messageAdapter

        RootRef.child("Messages")
            .child(messageSenderId!!)
            .child(messageRecieverId)
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
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd/MM/yyyy")
        savecurrentDate = currentDate.format(calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        savecurrentTime = currentTime.format(calendar.time)



        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        type = KeyBordType()

        loadingBar = ProgressDialog(this)
        mauth = FirebaseAuth.getInstance()
        messageSenderId = mauth!!.currentUser!!.uid
        RootRef = FirebaseDatabase.getInstance().reference


        messageRecieverId = intent.getStringExtra("visit_user_id").toString()
        getMessageRecievername = intent.getStringExtra("visit_user_name").toString()
        messagereceiverimage = intent.getStringExtra("visit_image").toString()

        binding.profileNameTxt.text = getMessageRecievername
    }

    private fun clickEvent() {

        binding.sendMessageBtn.setOnClickListener {
            sendMessage()
        }

        binding.sendFilesBtn.setOnClickListener(View.OnClickListener {
            val options = arrayOf<CharSequence>(
                "Images", "PDF Files", "Ms Word Files"
            )
            val builder = AlertDialog.Builder(this@ChatActivity)
            builder.setTitle("Select File")
            builder.setItems(options) { dialog, which ->
                if (which == 0) {
                    checker = "image"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), 555)
                } else if (which == 1) {
                    checker = "pdf"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/pdf"
                    startActivityForResult(Intent.createChooser(intent, "Select PDF File"), 555)
                } else if (which == 2) {
                    checker = "docx"
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "application/msword"
                    startActivityForResult(Intent.createChooser(intent, "Select Ms Word File"), 555)
                }
            }
            builder.show()
        })

        binding.inputMessages.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

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
                if (checker != "image") {
                    val storageReference =
                        FirebaseStorage.getInstance().reference.child("Document Files")
                    val messageSenderRef: String = "Messages/$messageSenderId/$messageRecieverId"
                    val messageReceiverRef =
                        "Messages/$messageRecieverId/$messageSenderId"
                    val Usermessagekeyref =
                        RootRef!!.child("Messages").child(messageSenderId!!).child(
                            messageRecieverId!!
                        ).push()
                    val messagePushID = Usermessagekeyref.key
                    val filepath = storageReference.child("$messagePushID.$checker")
                    filepath.putFile(fileuri!!).addOnSuccessListener {
                        filepath.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()

                            val messageDocsBody = HashMap<Any?, Any?>()
                            messageDocsBody["message"] = downloadUrl
                            messageDocsBody["name"] = fileuri!!.lastPathSegment
                            messageDocsBody["type"] = checker
                            messageDocsBody["from"] = messageSenderId
                            messageDocsBody["to"] = messageRecieverId
                            messageDocsBody["messageID"] = messagePushID
                            messageDocsBody["time"] = savecurrentTime
                            messageDocsBody["date"] = savecurrentDate


                            val messageBodyDDetail = HashMap<String, Any>()
                            messageBodyDDetail["$messageSenderRef/$messagePushID"] = messageDocsBody
                            messageBodyDDetail["$messageReceiverRef/$messagePushID"] =
                                messageDocsBody

                            RootRef.updateChildren(messageBodyDDetail)
                            loadingBar!!.dismiss()

                        }.addOnFailureListener { e ->
                            loadingBar!!.dismiss()
                            Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }.addOnProgressListener { taskSnapshot ->
                        val p = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        loadingBar!!.setMessage(p.toInt().toString() + " % Uploading...")
                    }
                } else if (checker == "image") {
                    val storageReference =
                        FirebaseStorage.getInstance().reference.child("Image Files")
                    val messageSenderRef = "Messages/$messageSenderId/$messageRecieverId"
                    val messageReceiverRef =
                        "Messages/$messageRecieverId/$messageSenderId"
                    val Usermessagekeyref =
                        RootRef!!.child("Messages").child(messageSenderId!!).child(
                            messageRecieverId!!
                        ).push()
                    val messagePushID = Usermessagekeyref.key
                    val filepath = storageReference.child("$messagePushID.jpg")
                    uploadTask = filepath.putFile(fileuri!!)

                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        filepath.downloadUrl
                    }.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            myUrl = downloadUrl.toString()
                            val messageTextBody = HashMap<Any?, Any?>()
                            messageTextBody["message"] = myUrl
                            messageTextBody["name"] = fileuri!!.lastPathSegment
                            messageTextBody["type"] = checker
                            messageTextBody["from"] = messageSenderId
                            messageTextBody["to"] = messageRecieverId
                            messageTextBody["messageID"] = messagePushID
                            messageTextBody["time"] = savecurrentTime
                            messageTextBody["date"] = savecurrentDate
                            val messageBodyDetails: MutableMap<String, Any> = HashMap()
                            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                            messageBodyDetails["$messageReceiverRef/$messagePushID"] =
                                messageTextBody
                            RootRef.updateChildren(messageBodyDetails)
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
                    })
                } else {
                    loadingBar!!.dismiss()
                    Toast.makeText(this, "please select file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Displaylastseen() {
        RootRef.child("Users").child(messageRecieverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("userState").hasChild("state")) {
                        val state = dataSnapshot.child("userState").child("state").value.toString()
                        val date = dataSnapshot.child("userState").child("date").value.toString()
                        val time = dataSnapshot.child("userState").child("time").value.toString()
                        if (state == "online") {
                            binding.customUserLastSeen!!.text = "online"
                        } else if (state == "offline") {
                            binding.customUserLastSeen!!.text = "Last seen: \n$date $time"
                        }
                    } else {
                        binding.customUserLastSeen!!.text = "offline"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    private fun sendMessage() {
        val messagetext = binding.inputMessages.text.toString()
        if (TextUtils.isEmpty(messagetext)) {
            Toast.makeText(this, "Please enter message first..", Toast.LENGTH_SHORT).show()
        } else {
            val messageSenderRef = "Messages/$messageSenderId/$messageRecieverId"
            val messageReceiverRef = "Messages/$messageRecieverId/$messageSenderId"
            val userMessageKeyRef =
                RootRef.child("Messages").child(messageSenderId!!).child(messageRecieverId).push()
            val messagePushID = userMessageKeyRef.key
            val messageTextBody = HashMap<String, Any?>()
            messageTextBody["message"] = messagetext
            messageTextBody["type"] = "text"
            messageTextBody["from"] = messageSenderId
            messageTextBody["to"] = messageRecieverId
            messageTextBody["messageID"] = messagePushID
            messageTextBody["time"] = savecurrentTime
            messageTextBody["date"] = savecurrentDate
            val messageBodyDetails = HashMap<String, Any?>()
            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody
            RootRef.updateChildren(messageBodyDetails)
                .addOnCompleteListener {
                    binding.inputMessages.setText("")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")

    }

    override fun onPause() {
        super.onPause()
        userStatusUpdate("offline")
    }

    override fun onResume() {
        super.onResume()
        userStatusUpdate("online")
    }

    override fun onRestart() {
        super.onRestart()
        userStatusUpdate("online")

    }

    override fun onStop() {
        super.onStop()
        userStatusUpdate("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        userStatusUpdate("offline")
    }

    private fun userStatusUpdate(state: String) {
        if (Utils.isNetworkAvailable(this)) {
            val activeStatus = ActiveStatus(
                state,
                Validator.getCurrentDate(),
                Validator.getCurrentTime()
            )
            DBReference.userRef
                .child(mauth?.uid.toString())
                .child("UserState")
                .setValue(activeStatus)
        }
    }
}