package com.hlidskialf.android.wringer;



import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsMessage;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.reflect.Method;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Iterator;

public class Wringer
{
  public static final String PACKAGE_NAME="com.hlidskialf.android.wringer";

  public static final String EXTRA_PROFILE_ID="profile_id";

  public static final String PREFERENCES=PACKAGE_NAME+"_preferences";
  public static final String PREF_CUR_PROFILE="cur_profile";
  public static final String PREF_WIDGET_SHOW_ICON_APP="widget_show_icon_app";
  public static final boolean DEF_WIDGET_SHOW_ICON_APP=true;
  public static final String PREF_WIDGET_SHOW_ICON_GPS="widget_show_icon_gps";
  public static final boolean DEF_WIDGET_SHOW_ICON_GPS=true;
  public static final String PREF_WIDGET_SHOW_ICON_3G="widget_show_icon_3g";
  public static final boolean DEF_WIDGET_SHOW_ICON_3G=true;
  public static final String PREF_SMS_POPUP="sms_popup";
  public static final boolean DEF_SMS_POPUP=true;
  public static final String PREF_SMS_POPUP_AUTOHIDE="sms_popup_autohide";
  public static final int DEF_SMS_POPUP_AUTOHIDE=15;
  public static final String PREF_SMS_POPUP_WAKEUP="sms_popup_wakeup";
  public static final boolean DEF_SMS_POPUP_WAKEUP=true;
  public static final String PREF_SMS_NOTIFICATION="sms_notification";
  public static final boolean DEF_SMS_NOTIFICATION=true;
  public static final String PREF_SMS_NOTIFICATION_TONE="sms_notification_tone";
  public static final boolean DEF_SMS_NOTIFICATION_TONE=true;
  public static final String PREF_SMS_NOTIFICATION_VIBRATE="sms_notification_vibrate";
  public static final boolean DEF_SMS_NOTIFICATION_VIBRATE=false;
  public static final String PREF_SMS_NOTIFICATION_COLOR="sms_notification_color";
  public static final String DEF_SMS_NOTIFICATION_COLOR="magenta";

  public static String getCurProfileName(Context context)
  {
    SharedPreferences prefs = context.getSharedPreferences(Wringer.PREFERENCES, 0);
    int profile_id = prefs.getInt(Wringer.PREF_CUR_PROFILE, 1);
    Cursor cursor = context.getContentResolver().query(ProfileModel.ProfileColumns.CONTENT_URI, 
      new String[] { ProfileModel.ProfileColumns.NAME }, 
      ProfileModel.ProfileColumns._ID+"="+profile_id, null, null);
    String ret = null;
    if (cursor.moveToFirst()) {
      ret = cursor.getString(0);
    }
    cursor.close();
    return ret;
  }

  public static String getRingtoneTitle(Context context, Uri uri)
  {
    Ringtone r = RingtoneManager.getRingtone(context, uri);
    return r.getTitle(context);
  }
  public static Bitmap getContactPhoto(Context context, int contact_id)
  {
    Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, contact_id);
    return People.loadContactPhoto(context, uri, android.R.drawable.gallery_thumb, null);
  }
  public static String getContactName(Context context, int contact_id)
  {
    Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, contact_id);
    Cursor c = context.getContentResolver().query(uri,
      new String[] {People.NAME}, null,null,null);
    String ret = null;
    if (c.moveToFirst()) {
      ret = c.getString(0);
    }
    c.close();
    return ret;
  }

  public static long getThreadIdFromAddress(Context context, String address) 
  {
    Uri uri = Uri.parse("content://mms-sms/threadID?recipient="+address);
    Cursor c = context.getContentResolver().query(uri, 
        new String[] {"_id"}, null, null, null);    
    long ret = -1;
    if (c.moveToFirst()) {
      ret = c.getLong(0);
    }
    c.close();
    return ret;
  }
  public static boolean setLastMessageRead(Context context, long thread_id)
  {
    ContentResolver resolver = context.getContentResolver();
    long msg_id = -1;
    Cursor c = resolver.query(Uri.parse("content://sms/conversations/"+thread_id), 
      new String[] {"_id"}, null, null, "date desc");
    if (c.moveToFirst()) {
      msg_id = c.getLong(0);
    }
    c.close();
    if (msg_id == -1) {
      return false;
    }

    ContentValues values = new ContentValues(1);
    values.put("read", String.valueOf(1));
    try {
      resolver.update(Uri.parse("content://sms/"+msg_id), values, null, null);
    } catch (Exception e) {
      android.util.Log.v("Wringer","failed to mark as read: "+e);
      return false;
    }
    return true;
  }
  public static boolean is_app_active(Context context, ComponentName component)
  {
    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
    Iterator<ActivityManager.RunningTaskInfo> it = am.getRunningTasks(1).iterator();
    while (it.hasNext()) {
      ActivityManager.RunningTaskInfo task_info = it.next();
      ComponentName task_component = task_info.baseActivity;
      if (component.equals(task_component))
        return true;
    }
    return false;
  }






  public static void applyProfile(Context context, int profile_id, Window window, OnProfileAppliedListener listener)
  {
    ProfileApplierHandler handler = new ProfileApplierHandler(context, window, listener);
    ProfileApplier thread = new ProfileApplier(context, profile_id, handler);
    thread.start();
  }
  public interface OnProfileAppliedListener {
    public void onProfileApplied(int profile_id);
  }
  private static class ProfileApplierHandler extends Handler
  {
    public static final int WHAT_BRIGHTNESS=1;
    public static final int WHAT_START_PROGRESS=2;
    public static final int WHAT_DONE=4;

    private Window mWindow;
    private Context mContext;
    private ProgressDialog mDialog;
    private OnProfileAppliedListener mListener;
    public ProfileApplierHandler(Context context, Window window, Wringer.OnProfileAppliedListener listener)
    {
      mContext = context;
      mWindow = window;
      mListener = listener;
    }
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
        case WHAT_BRIGHTNESS:
          if (mWindow != null) {
            int brightness = msg.arg1;
            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.screenBrightness = Math.max(0.1f, (float)brightness / 255.0f);
            mWindow.setAttributes(params);
          }
          break;
        case WHAT_START_PROGRESS:
          if (mDialog == null) {
            String name = (String)msg.obj;
            mDialog = ProgressDialog.show(mContext, "Wringer", "Applying '"+name+"' profile...", true, false);
          }
          break;
        case WHAT_DONE:
          if (mDialog != null)
            mDialog.dismiss();
          if (mListener != null) {
            mListener.onProfileApplied(msg.arg1);
          }
          break;
      }
    }
  }
  private static class ProfileApplier extends Thread implements ProfileModel.ProfileReporter 
  {
    private Context mContext;
    private Handler mHandler;
    private int mProfileId;
    private ContentResolver mResolver;
    private String mName;

    public ProfileApplier(Context context, int profile_id, Handler handler)
    {
      mContext = context;
      mProfileId = profile_id;
      mHandler = handler;
      mResolver = mContext.getContentResolver();
    }

    public void run() {

      ProfileModel.getProfile(mResolver, this, mProfileId);

      // save profile_id in prefs
      SharedPreferences prefs = mContext.getSharedPreferences(Wringer.PREFERENCES, 0);
      prefs.edit().putInt(Wringer.PREF_CUR_PROFILE, mProfileId).commit();

      Wringer.updateWidgets(mContext);

      Message msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_DONE, mProfileId, 0);
      msg.sendToTarget();
    }

    public void reportProfile(int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      String ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on,
      boolean autosync_on, int brightness, int screen_timeout)
    {
      Message msg;
      msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_START_PROGRESS);
      msg.obj = name;
      msg.sendToTarget();

      AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
      am.setStreamVolume(AudioManager.STREAM_ALARM, alarm_vol, 0);
      am.setStreamVolume(AudioManager.STREAM_MUSIC, music_vol, 0);
      am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notify_vol, 0);
      am.setStreamVolume(AudioManager.STREAM_RING, ringer_vol, 0);
      am.setStreamVolume(AudioManager.STREAM_SYSTEM, system_vol, 0);
      am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, voice_vol, 0);
      if (ringer_mode == null || ringer_mode.equals("normal"))
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
      else if (ringer_mode.equals("vibrate"))
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
      else if (ringer_mode.equals("silent"))
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
      am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, 
        ringer_vibrate ? AudioManager.VIBRATE_SETTING_ON : AudioManager.VIBRATE_SETTING_OFF);
      am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, 
        notify_vibrate ? AudioManager.VIBRATE_SETTING_ON : AudioManager.VIBRATE_SETTING_OFF);

      Settings.System.putInt(mResolver, Settings.System.SOUND_EFFECTS_ENABLED, play_soundfx ? 1 : 0);
      Settings.System.putString(mResolver, Settings.System.RINGTONE, ringtone);
      Settings.System.putString(mResolver, Settings.System.NOTIFICATION_SOUND, notifytone);
      Settings.System.putInt(mResolver, Settings.System.SCREEN_OFF_TIMEOUT, screen_timeout*1000);
      Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
      
      //apply brightness immediately
      msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_BRIGHTNESS, brightness, 0);
      msg.sendToTarget();
  
      Settings.System.putInt(mResolver, Settings.System.AIRPLANE_MODE_ON, airplane_on ? 1 : 0);
      //broadcast airplane intent
      Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
      i.putExtra("state", airplane_on);
      mContext.sendBroadcast(i);

      //gps - secured
      //location - secured
      //autosync - secured

      //iterate over all contacts and update their ringtones
      HashMap<Integer,Uri[]> ringtones = ProfileModel.getAllContactRingtones(mResolver, mProfileId);
      Cursor people = mResolver.query(People.CONTENT_URI, 
        new String[] {People._ID}, 
        People.PRIMARY_PHONE_ID+" IS NOT NULL",null,null);
      if (people != null && people.moveToFirst()) {
        do {
          int contact_id = people.getInt(0);  
          Uri[] uris = ringtones.get(contact_id);
          ContentValues values = new ContentValues(1);
          String new_ringtone = "";
          if (uris != null && uris[0] != null)
            new_ringtone = uris[0].toString();

          values.put(People.CUSTOM_RINGTONE, new_ringtone);
          mResolver.update(
            Uri.withAppendedPath(People.CONTENT_URI, String.valueOf(contact_id)),
            values, null,null
          );
        } while (people.moveToNext());
      }
      people.close();

      //wifi
      WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
      wm.setWifiEnabled(wifi_on);

      //bluetooth
      Object obj = mContext.getSystemService("bluetooth");
      if (obj != null) {
        Class<?> cls = obj.getClass();
        try { 
          Method meth = cls.getMethod(bluetooth_on ? "enable" : "disable");
          if (meth != null) {
            meth.setAccessible(true);
            meth.invoke(obj,new Object[]{});
          } 
          else 
            android.util.Log.v("Wringer", "no bluetooth method");
        } catch (java.lang.reflect.InvocationTargetException e) {
            android.util.Log.v("Wringer", "invocation target error: "+e.getTargetException());
        } catch (java.lang.Exception e) {
            android.util.Log.v("Wringer", "reflection error: "+e);
        }
      }
      else
        android.util.Log.v("Wringer", "no bluetooth for you!");
    }
  }

  public static Cursor getProfileCursor(ContentResolver resolver)
  {
    Cursor c = resolver.query(ProfileModel.ProfileColumns.CONTENT_URI,
      new String[] {ProfileModel.ProfileColumns._ID, 
        ProfileModel.ProfileColumns.NAME,
        ProfileModel.ProfileColumns.RINGER_MODE,
        ProfileModel.ProfileColumns.AIRPLANE_ON,
        ProfileModel.ProfileColumns.WIFI_ON,
        ProfileModel.ProfileColumns.GPS_ON,
        ProfileModel.ProfileColumns.LOCATION_ON,
        ProfileModel.ProfileColumns.BLUETOOTH_ON,
        ProfileModel.ProfileColumns.AUTOSYNC_ON}, null,null,null);
    return c;
  }

  public static class ProfileAdapter extends CursorAdapter 
  {
    private LayoutInflater mFactory;
    private int mNameIdx, mModeIdx, mAirplaneIdx, mWifiIdx, mGpsIdx, mLocationIdx, mBluetoothIdx, mAutosyncIdx;
    private int mCurProfile;
    private OnChooseProfileListener mListener;

    public interface OnChooseProfileListener {
      public void onChooseProfile(int pos, long profile_id);
    }

    public ProfileAdapter(Context context, Cursor cursor) {
      super(context,cursor);
      mFactory = LayoutInflater.from(context);

      mNameIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.NAME);
      mModeIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.RINGER_MODE);
      mAirplaneIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.AIRPLANE_ON);
      mWifiIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.WIFI_ON);
      mGpsIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.GPS_ON);
      mLocationIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.LOCATION_ON);
      mBluetoothIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.BLUETOOTH_ON);
      mAutosyncIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.AUTOSYNC_ON);
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {
      TextView tv = (TextView)v.findViewById(android.R.id.text1);
      String name = cursor.getString(mNameIdx);
      if (TextUtils.isEmpty(name))
        tv.setText(android.R.string.unknownName);
      else
        tv.setText(name);

      final int pos = cursor.getPosition();
      final int id = (int)getItemId(pos);
      CheckBox cb = (CheckBox)v.findViewById(android.R.id.checkbox);
      cb.setChecked(mCurProfile == id);
      cb.setOnClickListener(new Button.OnClickListener() {
        public void onClick(View v) {
          mCurProfile = id;
          if (mListener != null) {
            mListener.onChooseProfile(pos, id);
          }
        }
      });

      ImageView iv; 
      ViewGroup bar = (ViewGroup)v.findViewById(R.id.icon_bar);
      bar.removeAllViews();

      String ringer_mode = cursor.getString(mModeIdx);
      if (ringer_mode == null || ringer_mode.equals("normal")) {
      }
      else if (ringer_mode.equals("vibrate")) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_ringer_vibrate);
        bar.addView(iv);
      }
      else if (ringer_mode.equals("silent")) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_ringer_silent);
        bar.addView(iv);
      }
      int airplane_on = cursor.getInt(mAirplaneIdx);
      if (airplane_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_signal_flightmode);
        bar.addView(iv);
      }
      int bluetooth_on = cursor.getInt(mBluetoothIdx);
      if (bluetooth_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_data_bluetooth);
        bar.addView(iv);
      }
      int wifi_on = cursor.getInt(mWifiIdx);
      if (wifi_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_wifi_signal_4);
        bar.addView(iv);
      }
      /*
      int gps_on = cursor.getInt(mGpsIdx);
      if (gps_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_gps_on);
        bar.addView(iv);
      }
      int autosync_on = cursor.getInt(mAutosyncIdx);
      if (autosync_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_notify_sync);
        bar.addView(iv);
      }
      */
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View v = mFactory.inflate(R.layout.profile_list_item, parent, false);
      bindView(v,context,cursor);
      return v;
    }

    public void setCurProfile(int profile_id) { 
      mCurProfile = profile_id;
    }
    public void setOnChooseProfileListener(OnChooseProfileListener listener) { 
      mListener = listener; 
    }
  }

  public static void updateWidgets(Context context)
  {
    AppWidgetManager awm = AppWidgetManager.getInstance(context);
    int[] widget_ids = awm.getAppWidgetIds(new ComponentName(context, WringerWidgetProvider.class));
    Intent intent = new Intent(context, WringerWidgetProvider.class);
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widget_ids);
    context.sendBroadcast(intent);
  }

  public static String formatTimestamp(long when)
  {
    return DateUtils.formatSameDayTime(when, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.MEDIUM).toString();
  }
  public static String formatPhoneNumber(String from)
  {
    SpannableStringBuilder ssb = new SpannableStringBuilder(from);
    PhoneNumberUtils.formatNanpNumber(ssb);
    return ssb.toString();
  }
  public static int getContactIdFromAddress(Context context, String from_address)
  {
    int contact_id = -1;
    String number_key = new StringBuffer(from_address).reverse().toString().replaceAll("/[^0-9]/","");
    Cursor cursor = context.getContentResolver().query(Phones.CONTENT_URI,
      new String[]{Phones.PERSON_ID}, 
      Phones.NUMBER_KEY+"=?",new String[]{number_key},null);
    if (cursor.moveToFirst()) {
      contact_id = cursor.getInt(0); 
    }
    cursor.close();
    return contact_id;
  }
  public static SmsMessage[] getSmsMessagesFromBundle(Bundle b) 
  {
    Object[] messages = (Object[])b.get("pdus");
    int i;
    SmsMessage[] ret = new SmsMessage[messages.length];
    for (i=0; i < messages.length; i++) {
      ret[i] = SmsMessage.createFromPdu((byte[])messages[i]);
    }
    return ret;
  }
  public static Intent getMessagingIntent(Context context, String from)
  {
    Intent ret = new Intent();
    long thread_id = -1;
    if (from != null)
      thread_id = Wringer.getThreadIdFromAddress(context, from);
    if (thread_id == -1) { // no thread found
      ret.setAction(Intent.ACTION_MAIN);
      ret.setType("vnd.android-dir/mms-sms");
    }
    else {
      ret.setAction(Intent.ACTION_VIEW);
      ret.setData( Uri.parse("content://mms-sms/threadID/"+thread_id) );
    }
    ret.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    return ret;
  }

  public static int getUnreadMessagesCount(Context context)
  {
    Cursor c = context.getContentResolver().query(Uri.parse("content://sms/inbox"),
      new String[] { "_id" }, "read=0", null, null);
    int ret =  c.getCount();
    c.close();
    return ret;
  }

  public static int getRgbFromColor(String color) 
  {
    if (color.equals("red")) return 0xffff0000;
    if (color.equals("green")) return 0xff00ff00;
    if (color.equals("blue")) return 0xff0000ff;
    if (color.equals("orange")) return 0xffff7f00;
    if (color.equals("magenta")) return 0xffec008c;
    if (color.equals("cyan")) return 0xffec008c;
    return 0xff00ff00;
  }
}
