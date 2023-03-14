package com.example.realchat.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Validator {
    companion object {
        private val PHONE_NUMBER = Pattern.compile("^(?:\\+?88)?01[15-9]\\d{8}$")
        private val calendar: Calendar = Calendar.getInstance()

        fun inputFieldValidation(
            textInputEditText: TextInputEditText,
            errorMessage: String?
        ): Boolean {
            return if (Objects.requireNonNull(textInputEditText.text).toString().trim().isEmpty()) {
                textInputEditText.error = errorMessage
                textInputEditText.requestFocus()
                false
            } else {
                true
            }
        }

        fun timeStampToDateTime(timeStamp: Long): String? {
            var date: String? = null
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                date = dateFormat.format(timeStamp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return date
        }

        fun getSingleChatMsgPushKey(receiverId: String): String {
            return DBReference.messageRef.child(DBReference.uid.toString()).child(
                receiverId
            ).push().key.toString()
        }

        @SuppressLint("SimpleDateFormat")
        fun getCurrentDate(): String {
            val currentDate = SimpleDateFormat("dd/MM/yyyy")
            return currentDate.format(calendar.time)
        }

        @SuppressLint("SimpleDateFormat")
        fun getCurrentTime(): String {
            val currentTime = SimpleDateFormat("hh:mm:ss a")
            return currentTime.format(calendar.time)
        }

        @SuppressLint("ResourceAsColor")
        fun getDateWiseFilter(totalDay: Int): String? {
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("dd/MM/yyyy")
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -totalDay)
            val d = calendar.time
            return format.format(d)
        }

        fun notificationPermissionCheck(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                return false
            }

    }


        fun getValeFromEdiText(editText: TextInputEditText): String {
            return editText.text.toString().trim()
        }

        fun dateValidation(textView: TextInputEditText, errorMessage: String?): Boolean {
            return if (Objects.requireNonNull(textView.text).toString().trim { it <= ' ' }
                    .isEmpty()) {
                textView.error = errorMessage
                textView.requestFocus()
                false
            } else {
                var dateOfBirthFormat = Date()
                val currentDate = Date()
                @SuppressLint("SimpleDateFormat") val simpleDateFormat =
                    SimpleDateFormat("dd/MM/yyyy")
                try {
                    dateOfBirthFormat = simpleDateFormat.parse(textView.text.toString())
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                val current = Date()
                val cal1: Calendar = GregorianCalendar()
                val cal2: Calendar = GregorianCalendar()
                cal1.time = dateOfBirthFormat
                cal2.time = current
                val diffYear = cal2[Calendar.YEAR] - cal1[Calendar.YEAR]
                val diffMonth = diffYear * 12 + cal2[Calendar.MONTH] - cal1[Calendar.MONTH]
                Log.d("DIFFERENCEOFMONTH", "" + diffMonth)
                val diff = currentDate.time - dateOfBirthFormat.time
                val totalDay = (diff / (1000 * 60 * 60 * 24)).toInt()
                val totalMonth = totalDay / 30
                if (diffMonth <= 23) {
                    true
                } else {
                    textView.error = errorMessage
                    textView.requestFocus()
                    false
                }
            }
        }

        fun validatePhone(textInputEditText: TextInputEditText, errorMessage: String?): Boolean {
            return if (Objects.requireNonNull(textInputEditText.text).toString().trim().isEmpty()) {
                false
            } else if (!PHONE_NUMBER.matcher(textInputEditText.text.toString().trim()).matches()) {
                textInputEditText.error = errorMessage
                textInputEditText.requestFocus()
                false
            } else {
                true
            }
        }

        fun showToast(context: Context?, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

    }


}