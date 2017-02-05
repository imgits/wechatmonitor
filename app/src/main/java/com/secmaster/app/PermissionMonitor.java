package com.secmaster.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class PermissionMonitor {

    public PermissionMonitor(Context context) {
        this.context = context;
    }

    private boolean isOpenSettings = false;
    private Context context;

    public void onResume() {
        if (isOpenSettings) {
            isOpenSettings = false;
        }
    }

    public boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Accessibility Setting Not Found:", e);
        }
        if (accessibilityEnabled == 0) {
            return false;
        }

        String settingValue = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(settingValue)) {
            return false;
        }

        final String service = context.getPackageName() + "/" + MyAccessibilityService.class.getName();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(settingValue);
        while (splitter.hasNext()) {
            String accessibilityService = splitter.next();
            if (service.equalsIgnoreCase(accessibilityService)) {
                return true;
            }
        }

        return false;
    }

    public void requirePermission() {
        requirePermission(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        handler.sendEmptyMessageDelayed(MSG_GOTO_GUIDE, 400);
    }

    private void requirePermission(String action) {
        try {
            Intent intent = new Intent(action);
            context.startActivity(intent);
            isOpenSettings = true;
        } catch (SecurityException | ActivityNotFoundException e) {
            Log.e("PermissionMonitor", "requireGeneralPermission", e);
        }
    }

    private static final int MSG_GOTO_GUIDE = 1;
    private static final int MSG_CHECK = 2;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GOTO_GUIDE:
                    handler.sendEmptyMessage(MSG_CHECK);
                    break;
                case MSG_CHECK:
                    if (isOpenSettings) {
                        if (isAccessibilitySettingsOn()) {
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        } else {
                            handler.sendEmptyMessageDelayed(MSG_CHECK, 100);
                        }
                    }
                    break;
            }
        }
    };

}
