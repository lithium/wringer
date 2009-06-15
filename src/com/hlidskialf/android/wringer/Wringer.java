package com.hlidskialf.android.wringer;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.media.AudioManager;
import android.provider.Settings;
import android.net.wifi.WifiManager;
import java.lang.reflect.Method;

public class Wringer
{
  public static final String PACKAGE_NAME="com.hlidskialf.android.wringer";

  public static final String EXTRA_PROFILE_ID="profile_id";

  public static String getRingtoneTitle(Context context, Uri uri)
  {
    Ringtone r = RingtoneManager.getRingtone(context, uri);
    return r.getTitle(context);
  }

  public static void applyProfile(Context context, int profile_id)
  {
    ProfileModel.getProfile(context.getContentResolver(), new Wringer.ProfileApplier(context), profile_id);
  }
  private static class ProfileApplier implements ProfileModel.ProfileReporter 
  {
    private Context mContext;
    public ProfileApplier(Context context) {
      mContext = context;
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

      ContentResolver resolver = mContext.getContentResolver();
      Settings.System.putInt(resolver, Settings.System.SOUND_EFFECTS_ENABLED, play_soundfx ? 1 : 0);
      Settings.System.putString(resolver, Settings.System.RINGTONE, ringtone);
      Settings.System.putString(resolver, Settings.System.NOTIFICATION_SOUND, notifytone);
      Settings.System.putInt(resolver, Settings.System.AIRPLANE_MODE_ON, airplane_on ? 1 : 0);
      Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
      Settings.System.putInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, screen_timeout);

      WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
      wm.setWifiEnabled(wifi_on);

      //gps - secured
      //location - secured

      try {
        Object bm = mContext.getSystemService("bluetooth");
        if (bm == null) 
          android.util.Log.v("Wringer", "no bluetooth service");
        Class<?> cls = bm.getClass();
        Method method = cls.getMethod(bluetooth_on ? "enable" : "disable");
        if (method != null) {
          method.setAccessible(true);
          method.invoke(bm);
        }
        else
          android.util.Log.v("Wringer", "no bluetooth method");
      } catch (java.lang.Exception e) {
        android.util.Log.v("Wringer", "failed to reflect bluetooth service" + e); 
      }

      //autosync

    }
  }

}
