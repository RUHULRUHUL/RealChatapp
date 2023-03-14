package com.example.realchat.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.realchat.R
import com.example.realchat.utils.Validator
import com.example.realchat.view.activity.GroupChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class SendNotification : FirebaseMessagingService() {
    var pendingIntent: PendingIntent? = null
    var notificationManagerCompat: NotificationManagerCompat? = null
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e(ContentValues.TAG, "ProfileData Payload: " + remoteMessage.getData().toString())
        val intent = Intent(this, GroupChatActivity::class.java)
        intent.putExtra("messege", remoteMessage.notification?.body)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        showNotification(remoteMessage)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(remoteMessage: RemoteMessage) {
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        try {
            if (remoteMessage.notification!!.imageUrl != null) {
                ImageDownload(remoteMessage).execute(remoteMessage.notification!!.imageUrl.toString())
            } else {
                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(this, "CHANNEL_1_ID")
                        .setSmallIcon(R.drawable.ic_online)
                        .setContentTitle(remoteMessage.notification!!.title)
                        .setContentText(remoteMessage.notification!!.body)
                        .setStyle(
                            NotificationCompat.BigTextStyle().bigText(
                                remoteMessage.notification!!.body
                            )
                        )
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                val random = Random()
                val notificationId = random.nextInt(1000)
                if (notificationId > 0) {
                    if (Validator.notificationPermissionCheck(context = this)) {
                        notificationManagerCompat.notify(notificationId, builder.build())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class ImageDownload(private val remoteMessage: RemoteMessage) :
        AsyncTask<String?, Void?, Bitmap?>() {
        protected override fun doInBackground(vararg params: String?): Bitmap? {
            val inputStream: InputStream
            try {
                val url = URL(strings[0])
                try {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    inputStream = connection.inputStream
                    return BitmapFactory.decodeStream(inputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(bitmap: Bitmap?) {
            /*       showImageNotification(bitmap,remoteMessage, context = this)*/
        }

        /*       private fun showImageNotification(bitmap: Bitmap?, remoteMessage: RemoteMessage,context:Context) {
                   if (remoteMessage != null) {
                       val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "CHANNEL_1_ID")
                           .setSmallIcon(R.drawable.ic_send)
                           .setContentTitle(remoteMessage.notification!!.title)
                           .setContentText(remoteMessage.notification!!.body)
                           .setStyle(NotificationCompat.BigPictureStyle().bigLargeIcon(bitmap))
                           .setContentIntent()
                           .setDefaults(Notification.DEFAULT_SOUND)
                           .setAutoCancel(false)
                           .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                           .setPriority(NotificationCompat.PRIORITY_HIGH)
                       val random = Random()
                       val notificationId = random.nextInt(1000)
                       if (notificationId > 0) {
                          if (Validator.notificationPermissionCheck(this))
                              notificationManagerCompat.notify(notificationId, builder.build())
                       }
                   }

               }*/

    }
}