package com.example.realchat.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;


public class NetworkTracker extends BroadcastReceiver {
    private static final String TAG = NetworkTracker.class.getSimpleName();
    private final Context context;
    private NetworkListener networkListener;

    public static NetworkTracker with(Context context) {
        return new NetworkTracker(context);
    }

    private NetworkTracker(Context context) {
        this.context = context;
    }


    public NetworkTracker setNetworkListener(NetworkListener networkListener) {
        this.networkListener = networkListener;
        return this;
    }

    public static interface NetworkListener {
        public void onNetworkChange(boolean isConnected, Type networkType, NetworkInfo activeNetwork);
    }

    public static enum Type {
        unknown("unknown"), offline("offline"), twoG("2g"), threeG("3g"), fourG("4g"), fiveG("5g"), wifi("wifi");
        private final String value;

        private Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public void startReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver((BroadcastReceiver) this, intentFilter);
    }

    public void stopReceiver() {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
            if (networkListener != null) {
                final Type netType = getNetworkTypeBy(context, activeNetwork);
                networkListener.onNetworkChange(activeNetwork != null, netType, activeNetwork);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "ACCESS_NETWORK_STATE permission is missing");
        }


    }


    public static boolean hasNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            return activeNetwork != null;
        }
        return false;
    }

    private static Type getNetworkTypeBy(Context context, NetworkInfo activeNetwork) {
        try {
            if (activeNetwork == null)
                return Type.offline;//net work not connected

            //network connected
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return Type.wifi;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission") int networkType = mTelephonyManager.getNetworkType();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return Type.twoG;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return Type.threeG;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return Type.fourG;
                    default:
                        return Type.unknown;
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_NR:
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Type.unknown;
    }

    public static Type getNetworkType(Context context) {
        try {
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
            getNetworkTypeBy(context, activeNetwork);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Type.unknown;
    }
}