package com.example.realchatapp.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyBoardTracker {
    private static final String TAG = KeyBoardTracker.class.getSimpleName();
    private Activity activity;
    private View rootView;
    private KeyBoardListener onKeyboardListener = null;
    private static final int KEYBOARD_VISIBLE_THRESHOLD_DP = 150;

    private boolean keyboardVisible = false;
//    public static boolean debuggable=true;

    public static interface KeyBoardListener {
        public void onKeyBoardShown(Rect keyBoardRect, Rect resizedRootViewRect);

        public void onKeyBoardHide(Rect resizedRootViewRect);
    }

    public static KeyBoardTracker newInstance(Activity activity) {
        KeyBoardTracker tracker = new KeyBoardTracker();
        tracker.activity = activity;
        return tracker;
    }

    private KeyBoardTracker() {
    }

    public KeyBoardTracker setRootView(View rootView) {
        this.rootView = rootView;
        return this;
    }

    public void setOnKeyboardListener(KeyBoardListener onKeyboardListener) {
        this.onKeyboardListener = onKeyboardListener;
    }

    public void track() {
        if(activity != null && this.rootView != null && this.onKeyboardListener != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect resizedViewRect = new Rect();
                    View window = activity.getWindow().getDecorView();
                    window.getWindowVisibleDisplayFrame(resizedViewRect);

                    int screenHeight = rootView.getRootView().getHeight();
                    int heightDifference = screenHeight - resizedViewRect.height();
                    if(heightDifference > Utils.dpToPx(KEYBOARD_VISIBLE_THRESHOLD_DP) && !keyboardVisible) {
                        Rect keyBoardRect = new Rect(resizedViewRect.left,
                            resizedViewRect.bottom,
                            resizedViewRect.right,
                            screenHeight);
                        onKeyboardListener.onKeyBoardShown(keyBoardRect, resizedViewRect);
                        keyboardVisible = true;
                    }
                    else if(heightDifference <= Utils.dpToPx(KEYBOARD_VISIBLE_THRESHOLD_DP) && keyboardVisible) {
                        Rect actualViewRect = new Rect();
                        rootView.getGlobalVisibleRect(actualViewRect);
                        onKeyboardListener.onKeyBoardHide(actualViewRect);
                        keyboardVisible = false;
                    }
                }
            });
        }
    }

    public void track(View rootView, KeyBoardListener onKeyBoardListener) {
        setRootView(rootView);
        setOnKeyboardListener(onKeyBoardListener);
        track();
    }


}
