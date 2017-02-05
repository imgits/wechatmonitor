package com.secmaster.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail extends javax.mail.Authenticator {

    String from;
    String password;
    String to;

    String sport;

    String smtp;
    String port;

    private boolean auth = true;        // smtp authentication - default on
    private boolean debuggable = false; // debug mode on or off - default off

    private Multipart multipart;

    public Mail(String from, String password, String smtp, String port, String to) {
        this.from = from;
        this.password = password;
        this.smtp = smtp;
        this.to = to;
        this.port = port;
        this.sport = port;


        multipart = new MimeMultipart();

        // There is something wrong with MailCap, javamail can not find a
        // handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public void send(String subject, String body) throws MessagingException {

        if (TextUtils.isEmpty(from)) {
            throw new MessagingException("From email address is empty");
        }
        if (TextUtils.isEmpty(password)) {
            throw new MessagingException("Password is empty");
        }
        if (TextUtils.isEmpty(to)) {
            throw new MessagingException("To email address is empty");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtp);
        if (debuggable) {
            props.put("mail.debug", "true");
        }
        if (auth) {
            props.put("mail.smtp.auth", "true");
        }
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", sport);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        Session session = Session.getInstance(props, this);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));

        InternetAddress[] addressTo = {new InternetAddress(to)};
        msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);

        msg.setHeader("X-Priority", "1");
        msg.setContent(multipart);

        Transport.send(msg);
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(from, password);
    }

    public void saveConfig(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(MainActivity.DB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("from", from);
        editor.putString("password", password);
        editor.putString("smtp", smtp);
        editor.putString("port", port);
        editor.putString("to", to);
        editor.apply();
    }

    public static Mail restoreConfig(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(MainActivity.DB, Context.MODE_PRIVATE);
        String from = sp.getString("from", "");
        String password = sp.getString("password", "");
        String smtp = sp.getString("smtp", "");
        String port = sp.getString("port", "");
        String to = sp.getString("to", "");

        return new Mail(from, password, smtp, port, to);
    }
}