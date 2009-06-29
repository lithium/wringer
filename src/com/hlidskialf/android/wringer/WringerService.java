package com.hlidskialf.android.wringer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.gsm.SmsMessage;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings.System;

public class WringerService extends Service 
                            implements ProfileModel.ProfileReporter
{
  public static final int NOTIFICATION_ID=1;

  private SmsMessage[] mMessages;
  private SharedPreferences mPrefs;
  private int mStartId;

  @Override
  public void onCreate() {
    super.onCreate();  
  }
  @Override
  public void onStart(Intent intent, int start_id) {
    mMessages = Wringer.getSmsMessagesFromBundle(intent.getBundleExtra(SMSReceiver.EXTRA_SMS_EXTRAS));
    mPrefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    mStartId = start_id;

    int cur_profile = mPrefs.getInt(Wringer.PREF_CUR_PROFILE, -1);
    if (cur_profile == -1) 
      stopSelfResult(mStartId);

    ProfileModel.getProfile(getContentResolver(), this, cur_profile);
  }
  public void reportProfile(
    int id, String name, 
    int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
    String ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
    String ringtone, String notifytone, 
    boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on,
    boolean autosync_on, int brightness, int screen_timeout 
  ) {

    NotificationManager nmgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    SmsMessage msg = mMessages[0];

    String from = msg.getDisplayOriginatingAddress();
    String body = msg.getDisplayMessageBody();
    int contact_id = Wringer.getContactIdFromAddress(this, from);
    String ticker;
    if (contact_id != -1) {
      ticker = Wringer.getContactName(this, contact_id)+": "+body;
    }
    else {
      ticker = from+": "+body;
    }
    int unread_count = Wringer.getUnreadMessagesCount(this) + 1;
    
    Notification n = new Notification(R.drawable.stat_notify_mms, ticker, msg.getTimestampMillis());
    n.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;

    if (mPrefs.getBoolean(Wringer.PREF_SMS_NOTIFICATION_TONE, Wringer.DEF_SMS_NOTIFICATION_TONE)) {
      String contact_notifytone = ProfileModel.getContactNotifytone(getContentResolver(), id, contact_id);
      Uri tone = System.DEFAULT_NOTIFICATION_URI;

      if (contact_notifytone != null) 
        tone = Uri.parse(contact_notifytone);
      else if (notifytone != null)
        tone = Uri.parse(notifytone);

      n.sound = tone;
    }
    if (mPrefs.getBoolean(Wringer.PREF_SMS_NOTIFICATION_VIBRATE, Wringer.DEF_SMS_NOTIFICATION_VIBRATE)) {
      n.flags |= Notification.DEFAULT_VIBRATE;
    }
    
    String color = mPrefs.getString(Wringer.PREF_SMS_NOTIFICATION_COLOR, Wringer.DEF_SMS_NOTIFICATION_COLOR);
    if (!color.equals("none")) {
      n.flags |= Notification.FLAG_SHOW_LIGHTS;
      n.ledOnMS = 500;
      n.ledOffMS = 2000;
      n.ledARGB = Wringer.getRgbFromColor(color);
    }


    Intent smsIntent = Wringer.getMessagingIntent(this, from);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, smsIntent, 0);
    n.setLatestEventInfo(this, "New messages", unread_count+" unread messages.", pendingIntent);

    nmgr.notify(NOTIFICATION_ID, n);

    stopSelfResult(mStartId);
  }

  @Override 
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static void cancelAll(Context context)
  {
    NotificationManager nmgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    nmgr.cancelAll();
  }

}
