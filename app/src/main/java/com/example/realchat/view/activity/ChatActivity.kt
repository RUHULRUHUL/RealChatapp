package com.example.realchat.view.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.ActivityChatBinding
import com.example.realchat.model.message.Messages
import com.example.realchat.view.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadingBar = ProgressDialog(this)
        mauth = FirebaseAuth.getInstance()
        messageSenderId = mauth!!.getCurrentUser()!!.uid
        RootRef = FirebaseDatabase.getInstance().reference

        messageRecieverId = intent.extras!!["visit_user_id"].toString()
        getMessageRecievername = intent.extras!!["visit_user_name"].toString()
        messagereceiverimage = intent.extras!!["visit_image"].toString()

        messageAdapter = MessageAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessageListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessageListOfUsers.adapter = messageAdapter

        val calendar = Calendar.getInstance()

        val currentDate = SimpleDateFormat("dd/MM/yyyy")
        savecurrentDate = currentDate.format(calendar.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        savecurrentTime = currentTime.format(calendar.time)

        binding.customProfileName.text = getMessageRecievername
        Displaylastseen()
        binding.sendMessageBtn.setOnClickListener(View.OnClickListener { SendMessage() })

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

        RootRef.child("Messages")
            .child(messageSenderId!!)
            .child(messageRecieverId)
            .addChildEventListener(object : ChildEventListener {
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
                    val Usermessagekeyref = RootRef!!.child("Messages").child(messageSenderId!!).child(
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
                } /*else if (checker == "image") {
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

                    uploadTask.continueWithTask(object : Continuation<Any?, Any?> {
                        @Throws(Exception::class)
                        fun then(task: Task<*>): Any? {
                            if (!task.isSuccessful) {
                                throw task.exception!!
                            }
                            return filepath.downloadUrl
                        }
                    }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
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
                            val messageBodyDetails: MutableMap<String,Any> = HashMap()
                            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
                            messageBodyDetails["$messageReceiverRef/$messagePushID"] =
                                messageTextBody
                            RootRef!!.updateChildren(messageBodyDetails)
                                .addOnCompleteListener {
                                    if (task.isSuccessful) {
                                        loadingBar!!.dismiss()
                                        //Toast.makeText(ChatActivity.this,"Message sent Successfully...",Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar!!.dismiss()
                                        Toast.makeText(
                                            this@ChatActivity,
                                            "Error:",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    binding.inputMessages!!.setText("")
                                }
  *//*                              .addOnCompleteListener(object : OnCompleteListener<Any?> {
                                    fun onComplete(task: Task<*>) {
                                        if (task.isSuccessful) {
                                            loadingBar!!.dismiss()
                                            //Toast.makeText(ChatActivity.this,"Message sent Successfully...",Toast.LENGTH_SHORT).show();
                                        } else {
                                            loadingBar!!.dismiss()
                                            Toast.makeText(
                                                this@ChatActivity,
                                                "Error:",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        binding.inputMessages!!.setText("")
                                    }
                                })*//*
                        }
                    })
                }*/ else {
                    loadingBar!!.dismiss()
                    Toast.makeText(this, "please select file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun Displaylastseen() {
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


    private fun SendMessage() {
        val messagetext = binding.inputMessages.text.toString()
        if (TextUtils.isEmpty(messagetext)) {
            Toast.makeText(this, "Please enter message first..", Toast.LENGTH_SHORT).show()
        } else {
            val messageSenderRef = "Messages/$messageSenderId/$messageRecieverId"
            val messageReceiverRef = "Messages/$messageRecieverId/$messageSenderId"
            val Usermessagekeyref = RootRef.child("Messages").child(messageSenderId!!).child(
                messageRecieverId
            ).push()
            val messagePushID = Usermessagekeyref.key
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
                    if (it.isSuccessful) {
                    } else {
                        Toast.makeText(this@ChatActivity, "Error:", Toast.LENGTH_SHORT).show()
                    }
                    binding.inputMessages.setText("")
                }
/*                .addOnCompleteListener(object : OnCompleteListener<Any?> {
                    fun onComplete(task: Task<*>) {
                        if (task.isSuccessful) {
                        } else {
                            Toast.makeText(this@ChatActivity, "Error:", Toast.LENGTH_SHORT).show()
                        }
                        binding.inputMessages.setText("")
                    }
                })*/
        }
    }
}