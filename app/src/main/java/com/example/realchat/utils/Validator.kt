package com.example.realchat.utils

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.realchat.R
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Validator {

    companion object {
        private val PHONE_NUMBER = Pattern.compile("^(?:\\+?88)?01[15-9]\\d{8}$")

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


    fun inputBabyAgeValidation(
        textInputEditText: TextInputEditText,
        errorMessage: String?
    ): Boolean {
        return if (Objects.requireNonNull(textInputEditText.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textInputEditText.error = errorMessage
            textInputEditText.requestFocus()
            false
        } else if (textInputEditText.text.toString()
                .trim { it <= ' ' }.length < 1 || textInputEditText.text.toString()
                .trim { it <= ' ' }.length >= 2
        ) {
            if (textInputEditText.text.toString().trim { it <= ' ' }.toInt() > 23) {
                textInputEditText.error = errorMessage
                textInputEditText.requestFocus()
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun inputBabyAgeValidationTest(
        textInputEditText: TextInputEditText,
        errorMessage: String?
    ): Int {
        if (Objects.requireNonNull(textInputEditText.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textInputEditText.error = errorMessage
            textInputEditText.requestFocus()
            return -1
        } else if (textInputEditText.text.toString()
                .trim { it <= ' ' }.isEmpty() || textInputEditText.text.toString()
                .trim { it <= ' ' }.length >= 2
        ) {
            if (textInputEditText.text.toString().trim { it <= ' ' }.toInt() > 23) {
                textInputEditText.error = errorMessage
                textInputEditText.requestFocus()
                return textInputEditText.text.toString().trim { it <= ' ' }.toInt()
            }
        }
        return textInputEditText.text.toString().trim { it <= ' ' }.toInt()
    }

    @SuppressLint("ResourceAsColor")
    fun datePicker(textView: TextView, context: Context?) {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            context!!,
            { view: DatePicker?, numberOfYear: Int, monthOfYear: Int, dayOfMonth: Int ->
                val pickMonth = monthOfYear + 1
                val pickDate = "$dayOfMonth/$pickMonth/$numberOfYear"
                textView.text = pickDate
            }, year, month, day
        )
        datePickerDialog.show()
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(
                context, R.color.red
            )
        )
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(
                context, R.color.black
            )
        )
    }

    @SuppressLint("ResourceAsColor")
    fun datePicker(textView: TextInputEditText, context: Context?): String? {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            context!!,
            { view: DatePicker?, numberOfYear: Int, monthOfYear: Int, dayOfMonth: Int ->
                val pickMonth = monthOfYear + 1
                val pickDate = "$dayOfMonth/$pickMonth/$numberOfYear"
                textView.setText(pickDate)
            }, year, month, day
        )
        datePickerDialog.show()
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(
                context, R.color.colorAccent
            )
        )
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(
                context, R.color.black
            )
        )
        return Objects.requireNonNull(textView.text).toString()
    }


    fun mohilaAgeValidation(textInputEditText: TextInputEditText, errorMessage: String?): Boolean {
        return if (Objects.requireNonNull(textInputEditText.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textInputEditText.error = errorMessage
            textInputEditText.requestFocus()
            false
        } else if (textInputEditText.text.toString().trim { it <= ' ' }
                .toInt() < 15 || textInputEditText.text.toString().trim { it <= ' ' }
                .toInt() > 49) {
            textInputEditText.error = "Age Should be between 15 to 49 Years"
            textInputEditText.requestFocus()
            false
        } else {
            true
        }
    }

    fun inputNameValidation(textInputEditText: TextInputEditText, errorMessage: String?): Boolean {
        return if (Objects.requireNonNull(textInputEditText.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textInputEditText.error = errorMessage
            textInputEditText.requestFocus()
            false
        } else if (textInputEditText.text.toString()
                .trim { it <= ' ' }.length < 3 || textInputEditText.text.toString()
                .trim { it <= ' ' }.length >= 30
        ) {
            textInputEditText.error = "Should be 3 to 30 characters"
            textInputEditText.requestFocus()
            false
        } else {
            true
        }
    }

    fun inputDateOfBirthValidation(
        textInputEditText: TextInputEditText,
        errorMessage: String?
    ): Boolean {
        return if (Objects.requireNonNull(textInputEditText.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textInputEditText.error = errorMessage
            textInputEditText.requestFocus()
            false
        } else {
            textInputEditText.error = null
            textInputEditText.isFocusable = false
            true
        }
    }

    fun dateValidationTest(textView: TextInputEditText, errorMessage: String?): Int {
        return if (Objects.requireNonNull(textView.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textView.error = errorMessage
            textView.requestFocus()
            -1
        } else {
            var dateOfBirthFormat = Date()
            val currentDate = Date()
            @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
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
                diffMonth
            } else {
                textView.error = errorMessage
                textView.requestFocus()
                -1
            }
        }
    }

    fun dateValidation2(textView: TextInputEditText, errorMessage: String?): Boolean {
        return if (Objects.requireNonNull(textView.text).toString().trim { it <= ' ' }
                .isEmpty()) {
            textView.error = errorMessage
            textView.requestFocus()
            false
        } else {
            var dateOfBirthFormat = Date()
            val currentDate = Date()
            @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            try {
                dateOfBirthFormat = simpleDateFormat.parse(textView.text.toString())
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val diff = currentDate.time - dateOfBirthFormat.time
            val totalDay = (diff / (1000 * 60 * 60 * 24)).toInt()
            val totalMonth = totalDay / 30
            if (totalMonth <= 23) {
                true
            } else {
                textView.error = errorMessage
                textView.requestFocus()
                false
            }
        }
    }


    fun questionCheckValidation(textView: TextView) {
        if (textView.text.toString().trim { it <= ' ' }.endsWith("*")) {
            textView.text =
                textView.text.toString().substring(0, textView.text.toString().length - 2)
        }
    }

    fun resetDateVaccineError(textView: TextInputEditText) {
        textView.error = null
    }

    fun resetEditTextError(textInputEditText: TextInputEditText) {
        textInputEditText.error = null
        textInputEditText.setText("")
    }

    fun resetDateTextError(textInputEditText: TextInputEditText) {
        textInputEditText.error = null
        textInputEditText.setText("")
    }


    fun textStringValidation(ancVisitTimeSpinner: TextInputEditText, errorMsg: String?): Boolean {
        return if (TextUtils.isEmpty(
                Objects.requireNonNull(ancVisitTimeSpinner.text).toString().trim { it <= ' ' })
        ) {
            ancVisitTimeSpinner.error = errorMsg
            ancVisitTimeSpinner.requestFocus()
            false
        } else {
            true
        }
    }

    fun stringValidation(textView: TextInputEditText, error: String?): Boolean {
        return if (TextUtils.isEmpty(
                Objects.requireNonNull(textView.text).toString().trim { it <= ' ' })
        ) {
            textView.error = error
            textView.requestFocus()
            false
        } else {
            true
        }
    }


    fun radioGroupSelectedValidation(radioGroup: RadioGroup, textView: TextView): Boolean {
        val msg: String
        val radioGroupSelectedButtonId = radioGroup.checkedRadioButtonId
        if (radioGroupSelectedButtonId < 0) {
            if (!textView.text.toString().trim { it <= ' ' }.endsWith("*")) {
                msg =
                    "<font color=#262628>" + textView.text + "</font> " + "<font color=#FF0000><strong>" + "*" + "</strong></font>  "
                textView.text = Html.fromHtml(msg)
            }
            return false
        }
        return true
    }

}