package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Contacts.Phones;
import android.telephony.gsm.SmsMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SMSPopup extends Activity
{
  @Override 
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    setContentView(R.layout.sms_popup);

    Intent intent = getIntent();
    Bundle sms_extras = intent.getBundleExtra(SMSReceiver.EXTRA_SMS_EXTRAS);
    Object[] messages = (Object[])sms_extras.get("pdus");
    int i=0;
    //for (i=0; i < messages.length; i++) {
      SmsMessage msg = SmsMessage.createFromPdu((byte[])messages[i]);
      String from = msg.getDisplayOriginatingAddress();
      String number_key = new StringBuffer(from).reverse().toString().replaceAll("/[^0-9]/","");
      String body = msg.getDisplayMessageBody();

      android.util.Log.v("Wringer","FROM: "+from);
      android.util.Log.v("Wringer","KEY: "+number_key);

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
      TextView txt_body = (TextView)findViewById(R.id.sms_body);

      if (contact_id == -1) { // no contact found
        txt_name.setText(from);
      } else {
        txt_name.setText("Contact_id: "+contact_id);
        txt_number.setText(from);
      }
      txt_body.setText(body);


    //}
  }
}
