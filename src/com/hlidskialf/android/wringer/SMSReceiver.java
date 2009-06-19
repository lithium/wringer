package com.hlidskialf.android.wringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;

public class SMSReceiver extends BroadcastReceiver {
  public static final String EXTRA_SMS_EXTRAS="sms_extras";

  @Override
  public void onReceive(Context context, Intent intent) {
  /*
    Intent service_intent = new Intent(context, SMSService.class);
    service_intent.putExtra(EXTRA_SMS_EXTRAS, intent.getExtras());
    context.startService(service_intent);
    */

    if (Wringer.is_app_active(context, new ComponentName("com.android.mms","com.android.mms.ui.ConversationList"))) {
      // dont popup if messaging is active app 
      
    }
    else {

      Intent popup_intent = new Intent(context, SMSPopup.class);
      popup_intent.putExtra(SMSReceiver.EXTRA_SMS_EXTRAS, intent.getExtras());
      popup_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(popup_intent);
    
    }
  }

}
