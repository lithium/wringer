package com.hlidskialf.android.wringer;

import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import com.hlidskialf.android.preference.SeekBarPreference;

public class SetProfile extends PreferenceActivity implements ProfileModel.ProfileReporter
{
  private ContentResolver mResolver;
  private Uri mContentUri;
  private Intent mContactsIntent = null;

  private int mId;
  private String mName;
  private int mVolAlarm, mVolMusic, mVolNotify, mVolRinger, mVolSystem, mVolVoice;
  private String mRingerMode;
  private boolean mRingerVibrate, mNotifyVibrate, mPlaySoundfx;
  private String mRingtone, mNotifytone;
  private boolean mAirplaneOn, mWifiOn, mGpsOn, mLocationOn, mBluetoothOn, mAutoSyncOn;
  private int mBrightness, mTimeout;

  private EditTextPreference mPrefName;
  private SeekBarPreference mPrefBrightness, mPrefTimeout, mPrefVolAlarm, mPrefVolMusic, mPrefVolNotify, mPrefVolRinger, mPrefVolSystem, mPrefVolVoice;
  private CheckBoxPreference mPrefAutosync, mPrefRingerVibrate, mPrefNotifyVibrate, mPrefPlaySoundfx, mPrefAirplane, mPrefWifi, mPrefGps, mPrefLocation, mPrefBluetooth;
  private RingtonePreference mPrefRingtone, mPrefNotifytone;
  private ListPreference mPrefRingerMode;
  private Preference mPrefContacts; 

  @Override
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.profile_prefs);

    setup_preferences();

    mResolver = getContentResolver();
    
    Intent i = getIntent();
    mId = i.getIntExtra(Wringer.EXTRA_PROFILE_ID, -1);
    ProfileModel.getProfile(mResolver, this, mId);
  }

  private void setup_preferences()
  {
    AudioManager audio_mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

    mPrefName = (EditTextPreference)findPreference("name");
    mPrefName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_name((String)newValue, true);
        return true;
      }
    });
    mPrefBrightness = (SeekBarPreference)findPreference("brightness");
    mPrefBrightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_brightness((Integer)newValue, true);
        return true;
      }
    });
    mPrefTimeout = (SeekBarPreference)findPreference("screen_timeout");
    mPrefTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_timeout((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolAlarm = (SeekBarPreference)findPreference("alarm_vol");
    mPrefVolAlarm.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_ALARM));
    mPrefVolAlarm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_alarm_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolMusic = (SeekBarPreference)findPreference("music_vol");
    mPrefVolMusic.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    mPrefVolMusic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_music_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolNotify = (SeekBarPreference)findPreference("notify_vol");
    mPrefVolNotify.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
    mPrefVolNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_notify_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolRinger = (SeekBarPreference)findPreference("ringer_vol");
    mPrefVolRinger.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_RING));
    mPrefVolRinger.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_ringer_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolSystem = (SeekBarPreference)findPreference("system_vol");
    mPrefVolSystem.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
    mPrefVolSystem.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_system_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefVolVoice = (SeekBarPreference)findPreference("voice_vol");
    mPrefVolVoice.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
    mPrefVolVoice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_voice_vol((Integer)newValue, true);
        return true;
      }
    });
    mPrefRingerMode = (ListPreference)findPreference("ringer_mode");
    mPrefRingerMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_ringer_mode((String)newValue, true);
        return true;
      }
    });
    mPrefAutosync = (CheckBoxPreference)findPreference("autosync_on");
    mPrefAutosync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_autosync((Boolean)newValue, true);
        return true;
      }
    });
    mPrefRingerVibrate = (CheckBoxPreference)findPreference("ringer_vibrate");
    mPrefRingerVibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_ringer_vibrate((Boolean)newValue, true);
        return true;
      }
    });
    mPrefNotifyVibrate = (CheckBoxPreference)findPreference("notify_vibrate");
    mPrefNotifyVibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_notify_vibrate((Boolean)newValue, true);
        return true;
      }
    });
    mPrefPlaySoundfx = (CheckBoxPreference)findPreference("play_soundfx");
    mPrefPlaySoundfx.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_play_soundfx((Boolean)newValue, true);
        return true;
      }
    });
    mPrefAirplane = (CheckBoxPreference)findPreference("airplane_on");
    mPrefAirplane.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_airplane((Boolean)newValue, true);
        return true;
      }
    });
    mPrefWifi = (CheckBoxPreference)findPreference("wifi_on");
    mPrefWifi.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_wifi((Boolean)newValue, true);
        return true;
      }
    });
    mPrefGps = (CheckBoxPreference)findPreference("gps_on");
    mPrefGps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_gps((Boolean)newValue, true);
        return true;
      }
    });
    mPrefLocation = (CheckBoxPreference)findPreference("location_on");
    mPrefLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_location((Boolean)newValue, true);
        return true;
      }
    });
    mPrefBluetooth = (CheckBoxPreference)findPreference("bluetooth_on");
    mPrefBluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_bluetooth((Boolean)newValue, true);
        return true;
      }
    });
    mPrefRingtone = (RingtonePreference)findPreference("ringtone");
    mPrefRingtone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_ringtone((String)newValue, true);
        return true;
      }
    });
    mPrefNotifytone = (RingtonePreference)findPreference("notifytone");
    mPrefNotifytone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        update_notifytone((String)newValue, true);
        return true;
      }
    });

    mPrefContacts = findPreference("contacts");

  }


  public void reportProfile(
      int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      String ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on, boolean autosync_on, int brightness, int screen_timeout) 
  {
    mId = id;
    mContentUri = Uri.withAppendedPath(ProfileModel.ProfileColumns.CONTENT_URI, String.valueOf(mId));
    update_name(name, false);
    update_alarm_vol(alarm_vol, false);
    update_music_vol(music_vol, false);
    update_notify_vol(notify_vol, false);
    update_ringer_vol(ringer_vol, false);
    update_system_vol(system_vol, false);
    update_voice_vol(voice_vol, false);
    update_ringer_mode(ringer_mode, false);
    update_ringer_vibrate(ringer_vibrate, false); mPrefRingerVibrate.setChecked(ringer_vibrate);
    update_notify_vibrate(notify_vibrate, false); mPrefNotifyVibrate.setChecked(notify_vibrate);
    update_play_soundfx(play_soundfx, false); mPrefPlaySoundfx.setChecked(play_soundfx);
    update_ringtone(ringtone, false);
    update_notifytone(notifytone, false);
    update_airplane(airplane_on, false); mPrefAirplane.setChecked(airplane_on);
    update_wifi(wifi_on, false); mPrefWifi.setChecked(wifi_on);
    update_gps(gps_on, false); mPrefGps.setChecked(gps_on);
    update_location(location_on, false); mPrefLocation.setChecked(location_on);
    update_bluetooth(bluetooth_on, false); mPrefBluetooth.setChecked(bluetooth_on);
    update_autosync(autosync_on, false); mPrefAutosync.setChecked(autosync_on);
    update_brightness(brightness, false);
    update_timeout(screen_timeout, false);

    mContactsIntent = new Intent(this, SetProfileContacts.class); 
    mContactsIntent.putExtra(Wringer.EXTRA_PROFILE_ID, mId);
    mPrefContacts.setIntent(mContactsIntent);
  }

  private void update_column(String key, String value)
  {
    ContentValues values = new ContentValues(1);
    values.put(key, value);
    mResolver.update(mContentUri, values, null, null);
  }
  private void update_column(String key, int value)
  {
    ContentValues values = new ContentValues(1);
    values.put(key, value);
    mResolver.update(mContentUri, values, null, null);
  }
  private void update_column(String key, boolean value)
  {
    ContentValues values = new ContentValues(1);
    values.put(key, (int)(value ? 1 : 0));
    mResolver.update(mContentUri, values, null, null);
  }

  private void update_name(String name, boolean save) {
    mPrefName.setSummary(mName = name);
    if (save)
      update_column(ProfileModel.ProfileColumns.NAME, mName);
  }
  private void update_alarm_vol(int vol, boolean save) {
    mVolAlarm = vol;
    mPrefVolAlarm.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolAlarm.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.ALARM_VOLUME, mVolAlarm);
  }
  private void update_music_vol(int vol, boolean save) {
    mVolMusic = vol;
    mPrefVolMusic.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolMusic.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.ALARM_VOLUME, mVolMusic);
  }
  private void update_notify_vol(int vol, boolean save) {
    mVolNotify = vol;
    mPrefVolNotify.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolNotify.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.NOTIFY_VOLUME, mVolNotify);
  }
  private void update_ringer_vol(int vol, boolean save) {
    mVolRinger = vol;
    mPrefVolRinger.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolRinger.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.RINGER_VOLUME, mVolRinger);
  }
  private void update_system_vol(int vol, boolean save) {
    mVolSystem = vol;
    mPrefVolSystem.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolSystem.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.SYSTEM_VOLUME, mVolSystem);
  }
  private void update_voice_vol(int vol, boolean save) {
    mVolVoice = vol;
    mPrefVolVoice.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolVoice.getMax()));
    if (save)
      update_column(ProfileModel.ProfileColumns.VOICE_VOLUME, mVolVoice);
  }
  private void update_ringer_mode(String mode, boolean save) {
    mRingerMode = mode;
    if (mode == null || mode.equals("normal")) { 
      mPrefRingerMode.setSummary(R.string.ringer_mode_normal);
    } 
    else if (mode.equals("vibrate")) { 
      mPrefRingerMode.setSummary(R.string.ringer_mode_vibrate);
    } 
    else if (mode.equals("silent")) { 
      mPrefRingerMode.setSummary(R.string.ringer_mode_silent);
    }
    if (save)
      update_column(ProfileModel.ProfileColumns.RINGER_MODE, mRingerMode);
  }
  private void update_brightness(int brightness, boolean save) {
    mPrefBrightness.setSummary(String.valueOf(mBrightness = brightness) +" / 255");
    if (save)
      update_column(ProfileModel.ProfileColumns.BRIGHTNESS, mBrightness);
  }
  private void update_timeout(int timeout, boolean save) {
    mPrefTimeout.setSummary(String.valueOf(mTimeout = timeout) +" seconds");
    if (save)
      update_column(ProfileModel.ProfileColumns.SCREEN_TIMEOUT, mTimeout);
  }
  private void update_ringtone(String uri, boolean save) {
    mRingtone = uri;
    if (uri == null) return;
    Ringtone tone = RingtoneManager.getRingtone(this, Uri.parse(uri));
    if (tone != null)
      mPrefRingtone.setSummary(tone.getTitle(this));
    if (save)
      update_column(ProfileModel.ProfileColumns.RINGTONE, mRingtone);
  }
  private void update_notifytone(String uri, boolean save) {
    mNotifytone = uri;
    if (uri == null) return;
    Ringtone tone = RingtoneManager.getRingtone(this, Uri.parse(uri));
    if (tone != null)
      mPrefNotifytone.setSummary(tone.getTitle(this));
    if (save)
      update_column(ProfileModel.ProfileColumns.NOTIFYTONE, mNotifytone);
  }

  private void update_ringer_vibrate(boolean is_on, boolean save)
  {
    mRingerVibrate = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.RINGER_VIBRATE, mRingerVibrate);
  }
  private void update_notify_vibrate(boolean is_on, boolean save)
  {
    mNotifyVibrate = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.NOTIFY_VIBRATE, mNotifyVibrate);
  }
  private void update_play_soundfx(boolean is_on, boolean save)
  {
    mPlaySoundfx = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.NOTIFY_VIBRATE, mNotifyVibrate);
  }
  private void update_airplane(boolean is_on, boolean save)
  {
    mAirplaneOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.AIRPLANE_ON, mAirplaneOn);
  }
  private void update_wifi(boolean is_on, boolean save)
  {
    mWifiOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.WIFI_ON, mWifiOn);
  }
  private void update_gps(boolean is_on, boolean save)
  {
    mGpsOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.GPS_ON, mGpsOn);
  }
  private void update_location(boolean is_on, boolean save)
  {
    mLocationOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.LOCATION_ON, mLocationOn);
  }
  private void update_bluetooth(boolean is_on, boolean save)
  {
    mBluetoothOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.BLUETOOTH_ON, mBluetoothOn);
  }
  private void update_autosync(boolean is_on, boolean save)
  {
    mAutoSyncOn = is_on;
    if (save)
      update_column(ProfileModel.ProfileColumns.AUTOSYNC_ON, mAutoSyncOn);
  }


}
