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
        WringerService.cancelAll(SMSPopup.this);
        finish();
      }
    });
    b = (Button)findViewById(android.R.id.button2);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        WringerService.cancelAll(SMSPopup.this);
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

    update_from_intent(getIntent());
  }
  @Override
  protected void onNewIntent(Intent intent)
  {
    update_from_intent(intent);
  }
  private void update_from_intent(Intent intent)
  {
    Bundle sms_extras = intent.getBundleExtra(SMSReceiver.EXTRA_SMS_EXTRAS);
    SmsMessage[] messages = Wringer.getSmsMessagesFromBundle(sms_extras);

    SmsMessage msg = messages[0];
    mFromAddress = msg.getDisplayOriginatingAddress();
    String body = msg.getDisplayMessageBody();
    mFromTimestamp = msg.getTimestampMillis();

    int contact_id = Wringer.getContactIdFromAddress(this, mFromAddress);
    String from = Wringer.formatPhoneNumber(mFromAddress);
    String when = Wringer.formatTimestamp(mFromTimestamp);

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
    txt_when.setText(when);

    int i;
    for (i=1; i < messages.length; i++) {
      msg = messages[i];
      if (mFromAddress.equals(msg.getDisplayOriginatingAddress()))
        body += msg.getDisplayMessageBody();
      else
        android.util.Log.v("SMSPopup","multiple smsmessages from different addresses");
    }
    txt_body.setText(body);
    
    SharedPreferences prefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    int timeout = prefs.getInt(Wringer.PREF_SMS_POPUP_AUTOHIDE, Wringer.DEF_SMS_POPUP_AUTOHIDE);
    if (mKillHandler != null)
      mKillHandler.die();
    mKillHandler = new KillHandler(timeout*1000);
  }

  private void start_messaging()
  {
    Intent i = Wringer.getMessagingIntent(this, mFromAddress);
    startActivity(i);
    finish();
  }
  private void mark_as_read()
  {
    long thread_id = Wringer.getThreadIdFromAddress(this, mFromAddress);
    Wringer.setLastMessageRead(this, thread_id);
  }
}
