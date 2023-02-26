package com.example.realchat.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.realchat.R;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

public class Utils {

    public static int dpToPx(int dp) {
        float density = Resources.getSystem()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public static String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            return new String(bytes);
        } catch (IOException e) {
            return null;
        }
    }

    //Full Screen with Status bar
    public static void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void hideStatusBar(boolean darkText, Activity activity) {
        hideStatusBar(activity.getWindow(), darkText);
    }

    public static int getStatusBarHeight(Activity activity) {
        @SuppressLint({"InternalInsetResource", "DiscouragedApi"}) int id = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? activity.getResources().getDimensionPixelSize(id) : id;
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String UTCDatePicker(String dateStr) {
        if (dateStr.isEmpty()) {
            return "";
        } else {
            @SuppressLint("SimpleDateFormat")
            Date date = new Date();
            try {
                date = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date d = new Date();
            String stringDate = DateFormat.getTimeInstance().format(d);
            Date time = new Date();
            try {
                time = new SimpleDateFormat("HH:mm:ss").parse(stringDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date combinedDate = combine(date, time);

            SimpleDateFormat ISOFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            return ISOFormat.format(combinedDate);
        }
    }

    private static Date combine(Date date, Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        return cal.getTime();
    }

    @SuppressLint("ResourceAsColor")
    public static int monthToDay(String textView) {

        return (Integer.parseInt(textView) * 30);

    }

    public static String CurrentDatePicker() {

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        return df.format(Calendar.getInstance().getTime());
    }


    public static int dayWiseActivity(int dayValue) {
        if (dayValue >= 0 && dayValue <= 41) {
            return 1;
        } else if (dayValue >= 42 && dayValue <= 69) {
            return 2;
        } else if (dayValue >= 70 && dayValue <= 97) {
            return 3;
        } else if (dayValue >= 98 && dayValue <= 269) {
            return 4;
        } else if (dayValue >= 270 && dayValue <= 449) {
            return 5;
        } else if (dayValue == 450) {
            return 6;
        }
        return dayValue;
    }

    public static boolean checkGpsLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean locationPermissionCheck(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            return null != info && info.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
