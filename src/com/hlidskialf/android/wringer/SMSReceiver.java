package com.hlidskialf.android.wringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.Phones;
import android.telephony.gsm.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle b = intent.getExtras();
    Object[] messages = (Object[])b.get("pdus");
    int i;
    for (i=0; i < messages.length; i++) {
      SmsMessage msg = SmsMessage.createFromPdu((byte[])messages[i]);
      String from = msg.getDisplayOriginatingAddress();
      android.util.Log.v("Wringer","FROM: "+from);

      String number_key = new StringBuffer(from).reverse().toString().replaceAll("/[^0-9]/","");

      android.util.Log.v("Wringer","KEY: "+number_key);

      Cursor cursor = context.getContentResolver().query(Phones.CONTENT_URI,
        new String[]{Phones.PERSON_ID}, 
        Phones.NUMBER_KEY+"=?",new String[]{number_key},null);
      if (cursor.moveToFirst()) {
        int contact_id = cursor.getInt(0); 
        android.util.Log.v("Wringer", "CONTACT: "+contact_id);
      }
      cursor.close();
    }
  }

}
