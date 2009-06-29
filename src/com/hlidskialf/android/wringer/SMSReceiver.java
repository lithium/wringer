package com.hlidskialf.android.wringer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.telephony.gsm.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {
  public static final String EXTRA_SMS_EXTRAS="sms_extras";

  @Override
  public void onReceive(Context context, Intent intent) {

    SharedPreferences prefs = context.getSharedPreferences(Wringer.PREFERENCES, 0);

    if (prefs.getBoolean(Wringer.PREF_SMS_NOTIFICATION, Wringer.DEF_SMS_NOTIFICATION)) {
      Intent service_intent = new Intent(context, WringerService.class);
      service_intent.putExtra(EXTRA_SMS_EXTRAS, intent.getExtras());
      context.startService(service_intent);
    }

    if (prefs.getBoolean(Wringer.PREF_SMS_POPUP, Wringer.DEF_SMS_POPUP)) {
      ComponentName messaging = new ComponentName(
        "com.android.mms","com.android.mms.ui.ConversationList");
      if (!Wringer.is_app_active(context, messaging)) {
        //TODO: wakeup
        Intent popup_intent = new Intent(context, SMSPopup.class);
        popup_intent.putExtra(SMSReceiver.EXTRA_SMS_EXTRAS, intent.getExtras());
        popup_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(popup_intent);
      }
    }
  }

}
