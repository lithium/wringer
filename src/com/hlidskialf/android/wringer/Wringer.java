package com.hlidskialf.android.wringer;



import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
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
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts.People;
import android.provider.Settings;
import android.text.TextUtils;
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
import java.util.HashMap;
import java.util.Iterator;

public class Wringer
{
  public static final String PACKAGE_NAME="com.hlidskialf.android.wringer";

  public static final String EXTRA_PROFILE_ID="profile_id";

  public static final String PREFERENCES="wringer";
  public static final String PREF_CUR_PROFILE="cur_profile";

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
          if (mDialog == null)
            mDialog = ProgressDialog.show(mContext, "Wringer", "Applying profile...", true, false);
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
    public ProfileApplier(Context context, int profile_id, Handler handler)
    {
      mContext = context;
      mProfileId = profile_id;
      mHandler = handler;
      mResolver = mContext.getContentResolver();
    }

    public void run() {
      Message msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_START_PROGRESS);
      msg.sendToTarget();

      ProfileModel.getProfile(mResolver, this, mProfileId);

      // save profile_id in prefs
      SharedPreferences prefs = mContext.getSharedPreferences(Wringer.PREFERENCES, 0);
      prefs.edit().putInt(Wringer.PREF_CUR_PROFILE, mProfileId).commit();

      // update any widgets
      AppWidgetManager awm = AppWidgetManager.getInstance(mContext);
      int[] widget_ids = awm.getAppWidgetIds(new ComponentName(mContext, WringerWidgetProvider.class));
      Intent intent = new Intent(mContext, WringerWidgetProvider.class);
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widget_ids);
      mContext.sendBroadcast(intent);

      msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_DONE, mProfileId, 0);
      msg.sendToTarget();
    }

    public void reportProfile(int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      String ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on,
      boolean autosync_on, int brightness, int screen_timeout)
    {
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
      Message msg = mHandler.obtainMessage(ProfileApplierHandler.WHAT_BRIGHTNESS, brightness, 0);
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
}
