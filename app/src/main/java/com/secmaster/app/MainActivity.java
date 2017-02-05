package com.secmaster.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.secmaster.R;

import javax.mail.MessagingException;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText from;
    private EditText password;
    private EditText port;
    private EditText to;
    private EditText smtp;
    private PermissionMonitor monitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);
//        PackageManager p = getPackageManager();
//        p.setComponentEnabledSetting(getComponentName(),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Mail mail = Mail.restoreConfig(this);
        from = (EditText) findViewById(R.id.from);
        if (!TextUtils.isEmpty(mail.from)) {
            from.setText(mail.from);
        } else {
            from.setText(from.getHint());
        }
        password = (EditText) findViewById(R.id.password);
        if (!TextUtils.isEmpty(mail.password)) {
            password.setText(mail.password);
        } else {
            password.setText(password.getHint());
        }
        smtp = (EditText) findViewById(R.id.smtp);
        if (!TextUtils.isEmpty(mail.smtp)) {
            smtp.setText(mail.smtp);
        } else {
            smtp.setText(smtp.getHint());
        }
        port = (EditText) findViewById(R.id.ssl_port);
        if (!TextUtils.isEmpty(mail.port)) {
            port.setText(mail.port);
        } else {
            port.setText(port.getHint());
        }
        to = (EditText) findViewById(R.id.to);
        if (!TextUtils.isEmpty(mail.to)) {
            to.setText(mail.to);
        } else {
            to.setText(to.getHint());
        }

        monitor = new PermissionMonitor(this);
        findViewById(R.id.send).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        monitor.onResume();

        TextView permission = (TextView) findViewById(R.id.permission);
        if (monitor.isAccessibilitySettingsOn()) {
            permission.setText(R.string.granted);
            permission.setTextColor(0xffffffff);
            permission.setOnClickListener(null);
        } else {
            permission.setText(R.string.click_granted);
            permission.setOnClickListener(this);
            permission.setTextColor(0xffff0000);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.permission:
                monitor.requirePermission();
                break;
            case R.id.send:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        final Mail mail = new Mail(from.getText().toString(), password.getText().toString(),
                smtp.getText().toString(), port.getText().toString(), to.getText().toString());
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mail.send("WeChat", "This is a test email!");
                    mail.saveConfig(MainActivity.this);
                } catch (MessagingException e) {
                    Log.e("Main", e.toString(), e);
                    return e.toString();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (TextUtils.isEmpty(result)) {
                    Toast.makeText(MainActivity.this, "Send Email Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Send Email Fail:" + result, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public static final String DB = "db";
}

