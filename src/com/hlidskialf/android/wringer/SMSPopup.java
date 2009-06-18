package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
      String from = msg.getDisplayOriginatingAddress();
      String number_key = new StringBuffer(from).reverse().toString().replaceAll("/[^0-9]/","");
      SpannableStringBuilder f = new SpannableStringBuilder(from);
      PhoneNumberUtils.formatNanpNumber(f);
      from = f.toString();

      String body = msg.getDisplayMessageBody();
      long when = msg.getTimestampMillis();

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
      txt_when.setText( DateUtils.formatSameDayTime(when, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.MEDIUM) );
      txt_body.setText(body);


    //}

    mKillHandler = new KillHandler(15000);
  }

  private void start_messaging()
  {
    //Uri u = Uri.parse("content://mms-sms/threadID/"+mThreadId);
    Intent i = new Intent(Intent.ACTION_MAIN);
    i.setType("vnd.android-dir/mms-sms");
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(i);
    if (mKillHandler != null) {
      mKillHandler.die();
    }
    finish();
  }
}
