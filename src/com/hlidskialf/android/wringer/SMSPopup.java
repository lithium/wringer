package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Contacts.Phones;
import android.telephony.gsm.SmsMessage;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import android.telephony.PhoneNumberUtils;
import java.text.DateFormat;
import android.text.format.DateUtils;
import android.text.SpannableStringBuilder;

public class SMSPopup extends Activity
{
  private class KillHandler extends Handler implements Runnable
  {
    public KillHandler(long delayMills) {
      super();
      postDelayed(this, delayMills);
    }
    public void run() { 
      SMSPopup.this.finish();
    }
    public void die() {
      removeCallbacks(this);
    }
  }
  private KillHandler mKillHandler;
  private String mFromAddress;
  private long mFromTimestamp;

  @Override 
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    Window win = getWindow();
    WindowManager.LayoutParams params = win.getAttributes();
    params.gravity = Gravity.BOTTOM;
    win.setAttributes(params);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.sms_popup);

    View layout = findViewById(android.R.id.content);
    layout.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    Button b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        mark_as_read();
        finish();
      }
    });
    b = (Button)findViewById(android.R.id.button2);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        start_messaging();
      }
    });


  }
  @Override
  public void onDestroy()
  {
    super.onDestroy();
    if (mKillHandler != null) {
      mKillHandler.die();
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if (!isFinishing())
      finish();
  }
  @Override
  public void onResume()
  {
    super.onResume();

    Intent intent = getIntent();
    Bundle sms_extras = intent.getBundleExtra(SMSReceiver.EXTRA_SMS_EXTRAS);
    Object[] messages = (Object[])sms_extras.get("pdus");
    int i=0;
    android.util.Log.v("WringerSms", "messages.length: "+messages.length);
    //for (i=0; i < messages.length; i++) {
      SmsMessage msg = SmsMessage.createFromPdu((byte[])messages[i]);
      mFromAddress = msg.getDisplayOriginatingAddress();
      String number_key = new StringBuffer(mFromAddress).reverse().toString().replaceAll("/[^0-9]/","");
      SpannableStringBuilder f = new SpannableStringBuilder(mFromAddress);
      PhoneNumberUtils.formatNanpNumber(f);
      String from = f.toString();

      String body = msg.getDisplayMessageBody();
      mFromTimestamp = msg.getTimestampMillis();

      int contact_id = -1;
      Cursor cursor = getContentResolver().query(Phones.CONTENT_URI,
        new String[]{Phones.PERSON_ID}, 
        Phones.NUMBER_KEY+"=?",new String[]{number_key},null);
      if (cursor.moveToFirst()) {
        contact_id = cursor.getInt(0); 
      }
      cursor.close();

      ImageView txt_icon = (ImageView)findViewById(android.R.id.icon);
      TextView txt_name = (TextView)findViewById(android.R.id.text1);
      TextView txt_number = (TextView)findViewById(android.R.id.text2);
      TextView txt_when = (TextView)findViewById(R.id.text3);
      TextView txt_body = (TextView)findViewById(R.id.sms_body);

      if (contact_id == -1) { // no contact found
        txt_name.setText(android.R.string.unknownName);
      } else {
        Bitmap icon = Wringer.getContactPhoto(this, contact_id); 
        if (icon != null)
          txt_icon.setImageBitmap( Bitmap.createScaledBitmap(icon, 64,64, true) );
        String name = Wringer.getContactName(this, contact_id);
        txt_name.setText(name);
      }
      txt_number.setText(from);
      txt_when.setText( DateUtils.formatSameDayTime(mFromTimestamp, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.MEDIUM) );
      txt_body.setText(body);


    //}

    
    SharedPreferences prefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    int timeout = prefs.getInt(Wringer.PREF_SMS_POPUP_AUTOHIDE, Wringer.DEF_SMS_POPUP_AUTOHIDE);
    mKillHandler = new KillHandler(timeout*1000);
  }

  private void start_messaging()
  {
    long thread_id = Wringer.getThreadIdFromAddress(this, mFromAddress);
    Intent i = new Intent();
    if (thread_id == -1) { // no thread found
      i.setAction(Intent.ACTION_MAIN);
      i.setType("vnd.android-dir/mms-sms");
    }
    else {
      i.setAction(Intent.ACTION_VIEW);
      i.setData( Uri.parse("content://mms-sms/threadID/"+thread_id) );
    }
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(i);
    finish();
  }
  private void mark_as_read()
  {
    long thread_id = Wringer.getThreadIdFromAddress(this, mFromAddress);
    Wringer.setLastMessageRead(this, thread_id);
  }
}
